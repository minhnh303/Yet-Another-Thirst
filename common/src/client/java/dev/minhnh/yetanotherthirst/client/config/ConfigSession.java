package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.platform.Services;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Transactional config editing session that keeps pending edits isolated from live gameplay state
 * until {@link #save()} is invoked.
 */
public final class ConfigSession {
    private static IConfigBackend serviceBackend;

    private final IConfigBackend backend;
    private final boolean remote;
    private final Consumer<Runnable> serverExecutor;
    private final Map<ConfigOption<?>, Object> saved = new HashMap<>();
    private final Map<ConfigOption<?>, Object> defaults = new HashMap<>();
    private final Map<ConfigOption<?>, Object> pending = new LinkedHashMap<>();

    public ConfigSession() {
        this(serviceBackend(), isRemote(), ConfigSession::executeOnIntegratedServer);
    }

    public ConfigSession(IConfigBackend backend, boolean remote, Consumer<Runnable> serverExecutor) {
        this.backend = Objects.requireNonNull(backend);
        this.remote = remote;
        this.serverExecutor = Objects.requireNonNull(serverExecutor);
    }

    private static IConfigBackend serviceBackend() {
        if (serviceBackend == null) {
            serviceBackend = Services.load(IConfigBackend.class);
        }
        return serviceBackend;
    }

    private static boolean isRemote() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft != null && minecraft.level != null && !minecraft.isLocalServer();
    }

    /** Runs on the integrated server thread when one exists (it reads the same static config), else inline. */
    private static void executeOnIntegratedServer(Runnable action) {
        Minecraft minecraft = Minecraft.getInstance();
        MinecraftServer server = minecraft == null ? null : minecraft.getSingleplayerServer();
        if (server != null) {
            server.execute(action);
        } else {
            action.run();
        }
    }

    public boolean available(ConfigOption<?> option) {
        return backend.has(option.file, option.key);
    }

    public boolean locked(ConfigOption<?> option) {
        return remote && option.file.serverSide() && !option.isClientOnly();
    }

    public boolean remote() {
        return remote;
    }

    @SuppressWarnings("unchecked")
    public <T> T saved(ConfigOption<T> option) {
        return (T) saved.computeIfAbsent(option, o -> o.canonical(backend.get(o.file, o.key)));
    }

    @SuppressWarnings("unchecked")
    public <T> T defaultValue(ConfigOption<T> option) {
        return (T) defaults.computeIfAbsent(option, o -> o.canonical(backend.getDefault(o.file, o.key)));
    }

    @SuppressWarnings("unchecked")
    public <T> T value(ConfigOption<T> option) {
        Object edit = pending.get(option);
        return edit != null ? (T) edit : saved(option);
    }

    public <T> void set(ConfigOption<T> option, T value) {
        setRaw(option, value);
    }

    public void setRaw(ConfigOption<?> option, Object value) {
        if (locked(option)) {
            return;
        }
        Object canonical = option.canonical(value);
        if (Objects.equals(canonical, saved(option))) {
            pending.remove(option);
        } else {
            pending.put(option, canonical);
        }
    }

    public <T> void reset(ConfigOption<T> option) {
        set(option, defaultValue(option));
    }

    public boolean modified(ConfigOption<?> option) {
        return pending.containsKey(option);
    }

    public boolean isDefault(ConfigOption<?> option) {
        return Objects.equals(value(option), defaultValue(option));
    }

    public int pendingCount() {
        return pending.size();
    }

    public int pendingCount(ConfigCategory category) {
        int count = 0;
        for (ConfigOption<?> option : category.options()) {
            if (pending.containsKey(option)) {
                count++;
            }
        }
        return count;
    }

    public void discard() {
        pending.clear();
    }

    /**
     * Writes pending edits to disk one file at a time, then applies them to runtime state on the server thread.
     * On failure the backend is rolled back to the last saved values, files already written are rewritten with
     * those values so disk never holds a partial save, and the edits stay pending.
     *
     * @return whether every changed file was written
     */
    public boolean save() {
        if (pending.isEmpty()) {
            return true;
        }
        Set<ConfigFile> files = EnumSet.noneOf(ConfigFile.class);
        Set<ConfigFile> written = EnumSet.noneOf(ConfigFile.class);
        Map<ConfigOption<?>, Object> committed = new LinkedHashMap<>();
        try {
            for (Map.Entry<ConfigOption<?>, Object> entry : pending.entrySet()) {
                ConfigOption<?> option = entry.getKey();
                Object backendValue = option.toBackend(entry.getValue());
                committed.put(option, option.canonical(backendValue));
                backend.set(option.file, option.key, backendValue);
                files.add(option.file);
            }
            for (ConfigFile file : files) {
                backend.save(EnumSet.of(file));
                written.add(file);
            }
        } catch (RuntimeException e) {
            Constants.LOG.error("[YAT] Failed to save config changes; keeping them unsaved", e);
            rollback(committed.keySet());
            restore(written);
            return false;
        }

        serverExecutor.accept(() -> backend.apply(files));
        Constants.LOG.info("[YAT] Saved {} config change(s) from the in-game config screen", pending.size());
        saved.putAll(committed);
        pending.clear();
        return true;
    }

    private void rollback(Iterable<ConfigOption<?>> options) {
        for (ConfigOption<?> option : options) {
            try {
                backend.set(option.file, option.key, option.toBackend(saved(option)));
            } catch (RuntimeException e) {
                Constants.LOG.error("[YAT] Failed to roll back {}/{}", option.file, option.key, e);
            }
        }
    }

    /** Rewrites files that were saved before a later file failed, so they hold the rolled-back values again. */
    private void restore(Set<ConfigFile> written) {
        for (ConfigFile file : written) {
            try {
                backend.save(EnumSet.of(file));
            } catch (RuntimeException e) {
                Constants.LOG.error("[YAT] Failed to restore config file {} after a failed save", file, e);
            }
        }
    }
}
