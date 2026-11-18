package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.platform.Services;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Edits made on the config screen, kept apart from the live config until {@link #save()}. Values are
 * canonical (see {@link ConfigOption#canonical(Object)}); an edit equal to the value on disk is dropped, so
 * {@link #pendingCount()} is exactly the number of settings Save would write.
 */
public final class ConfigSession {

    /** Where server-side config writes run. */
    @FunctionalInterface
    interface ServerThread {
        /**
         * Runs {@code task} on the integrated server thread and waits for it, returning true; with no
         * integrated server it runs the task in place and returns false. When it throws, the task has either
         * finished or will never run; it is never left running.
         */
        boolean runAndWait(Runnable task);
    }

    private static final long SERVER_TIMEOUT_SECONDS = 10;
    private static IConfigBackend serviceBackend;

    private final IConfigBackend backend;
    private final ServerThread serverThread;
    private final Map<ConfigOption<?>, Object> saved = new HashMap<>();
    private final Map<ConfigOption<?>, Object> defaults = new HashMap<>();
    private final Map<ConfigOption<?>, Object> pending = new LinkedHashMap<>();
    private final boolean remote;

    public ConfigSession() {
        this(serviceBackend(), isRemote(), ConfigSession::runOnIntegratedServer);
    }

    ConfigSession(IConfigBackend backend, boolean remote, ServerThread serverThread) {
        this.backend = Objects.requireNonNull(backend);
        this.remote = remote;
        this.serverThread = Objects.requireNonNull(serverThread);
    }

    private static IConfigBackend serviceBackend() {
        if (serviceBackend == null) {
            serviceBackend = Services.load(IConfigBackend.class);
        }
        return serviceBackend;
    }

    private static boolean isRemote() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null && !minecraft.isLocalServer();
    }

    /** The integrated server keeps draining tasks while paused, so waiting here cannot stall on the pause menu. */
    private static boolean runOnIntegratedServer(Runnable task) {
        MinecraftServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || server.isSameThread()) {
            task.run();
            return server != null;
        }
        runWithDeadline(server, task, SERVER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return true;
    }

    /**
     * Runs {@code task} on {@code executor} and waits. If it has not started by the deadline it is withdrawn and
     * never runs; if it has started, this waits for it to finish. Either way, once this returns or throws, the
     * task is not running and will not run, so the caller's view of what was written matches the disk.
     */
    static void runWithDeadline(Executor executor, Runnable task, long timeout, TimeUnit unit) {
        AtomicBoolean claimed = new AtomicBoolean();
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            if (claimed.compareAndSet(false, true)) task.run();
        }, executor);
        try {
            future.get(timeout, unit);
        } catch (TimeoutException e) {
            if (claimed.compareAndSet(false, true)) {
                throw new IllegalStateException("Server thread did not start the config save in time; nothing was written", e);
            }
            awaitStarted(future);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Config save failed on the server thread", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (claimed.compareAndSet(false, true)) {
                throw new IllegalStateException("Interrupted before the config save started; nothing was written", e);
            }
            awaitStarted(future);
        }
    }

    /** Waits for a task that already started; uninterruptible so its outcome is always known. */
    private static void awaitStarted(CompletableFuture<Void> future) {
        try {
            future.join();
        } catch (CompletionException e) {
            throw new IllegalStateException("Config save failed on the server thread", e.getCause());
        }
    }

    /** Whether the running loader defines this option. */
    public boolean available(ConfigOption<?> option) {
        return backend.has(option.file, option.key);
    }

    /**
     * Server-side files are read-only while connected to a remote server: the server's copy is the one in effect.
     * Client-only options are the exception, since the client reads them from its own local file.
     */
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

    public void set(ConfigOption<?> option, Object value) {
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

    public void reset(ConfigOption<?> option) {
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
            if (pending.containsKey(option)) count++;
        }
        return count;
    }

    public void discard() {
        pending.clear();
    }

    /** Outcome of {@link #save()}: edits written to disk, and edits that stay pending because writing failed. */
    public record SaveResult(int saved, int failed) {
        static final SaveResult NOTHING = new SaveResult(0, 0);
    }

    /**
     * Writes every pending edit file by file and applies it. Server-side files are written on the integrated
     * server thread (this call waits), since the server reads the live config while ticking; the client file
     * is written here. Item values are re-resolved on the server thread, where tags are bound; on the title
     * screen they are resolved when a world starts. Only edits whose file reached disk leave the pending
     * set; when a file fails, its in-memory values are rolled back so the live config matches the disk.
     */
    public SaveResult save() {
        if (pending.isEmpty()) {
            return SaveResult.NOTHING;
        }
        Map<ConfigFile, Map<ConfigOption<?>, Object>> byFile = new EnumMap<>(ConfigFile.class);
        for (Map.Entry<ConfigOption<?>, Object> entry : pending.entrySet()) {
            byFile.computeIfAbsent(entry.getKey().file, f -> new LinkedHashMap<>()).put(entry.getKey(), entry.getValue());
        }
        Map<ConfigOption<?>, Object> committed = new LinkedHashMap<>();
        Map<ConfigOption<?>, Object> clientEdits = byFile.remove(ConfigFile.CLIENT);

        if (!byFile.isEmpty()) {
            // Filled on the server thread. runAndWait never leaves the task running, so reading it afterwards is safe
            // and reflects exactly the files that reached disk, even when the call failed.
            Map<ConfigOption<?>, Object> serverCommitted = new LinkedHashMap<>();
            boolean onServer;
            try {
                onServer = serverThread.runAndWait(() -> byFile.forEach((file, edits) -> writeFile(file, edits, serverCommitted)));
            } catch (RuntimeException e) {
                Constants.LOG.error("[YAT] Config screen could not run the save on the server thread", e);
                onServer = !serverCommitted.isEmpty();
            }
            committed.putAll(serverCommitted);
            if (onServer && committed.keySet().stream().anyMatch(o -> o.file == ConfigFile.ITEMS)) {
                try {
                    serverThread.runAndWait(backend::reloadItemValues);
                } catch (RuntimeException e) {
                    Constants.LOG.error("[YAT] Config screen could not reload item values", e);
                }
            }
        }
        if (clientEdits != null) {
            writeFile(ConfigFile.CLIENT, clientEdits, committed);
        }

        saved.putAll(committed);
        pending.keySet().removeAll(committed.keySet());
        if (!committed.isEmpty()) {
            Constants.LOG.info("[YAT] Saved {} config change(s) from the in-game config screen", committed.size());
        }
        return new SaveResult(committed.size(), pending.size());
    }

    /** Sets {@code edits} and saves {@code file}; adds them to {@code committed} only once the file is on disk. */
    private void writeFile(ConfigFile file, Map<ConfigOption<?>, Object> edits, Map<ConfigOption<?>, Object> committed) {
        Map<ConfigOption<?>, Object> written = new LinkedHashMap<>();
        for (Map.Entry<ConfigOption<?>, Object> entry : edits.entrySet()) {
            ConfigOption<?> option = entry.getKey();
            try {
                backend.set(file, option.key, entry.getValue());
                written.put(option, entry.getValue());
            } catch (RuntimeException e) {
                Constants.LOG.error("[YAT] Config screen could not set {}/{} to {}", file, option.key, entry.getValue(), e);
            }
        }
        if (written.isEmpty()) {
            return;
        }
        try {
            backend.save(EnumSet.of(file));
            committed.putAll(written);
        } catch (RuntimeException e) {
            Constants.LOG.error("[YAT] Config screen could not save {}", file, e);
            for (ConfigOption<?> option : written.keySet()) {
                try {
                    backend.set(file, option.key, saved(option));
                } catch (RuntimeException rollback) {
                    Constants.LOG.error("[YAT] Could not roll back {}/{}", file, option.key, rollback);
                }
            }
        }
    }
}
