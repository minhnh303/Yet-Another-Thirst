package dev.minhnh.yetanotherthirst;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** {@link IConfigBackend} backed by NeoForge 1.20.1 config specs. */
public final class NeoForgeConfigBackend implements IConfigBackend {
    private final Map<ConfigFile, Map<String, ForgeConfigSpec.ConfigValue<?>>> index = new EnumMap<>(ConfigFile.class);

    static ForgeConfigSpec spec(ConfigFile file) {
        return switch (file) {
            case COMMON -> NeoForgeConfigCommon.SPEC;
            case ITEMS -> NeoForgeConfigItems.SPEC;
            case COMPAT -> NeoForgeConfigCompat.SPEC;
            case CLIENT -> NeoForgeClientConfig.SPEC;
        };
    }

    private Map<String, ForgeConfigSpec.ConfigValue<?>> values(ConfigFile file) {
        return index.computeIfAbsent(file, value -> {
            Map<String, ForgeConfigSpec.ConfigValue<?>> result = new HashMap<>();
            collect(spec(value).getValues(), result, value);
            return result;
        });
    }

    static void collect(UnmodifiableConfig config, Map<String, ForgeConfigSpec.ConfigValue<?>> out, ConfigFile file) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                List<String> path = configValue.getPath();
                String key = path.get(path.size() - 1);
                if (out.putIfAbsent(key, configValue) != null) {
                    throw new IllegalStateException("Duplicate config leaf key " + file + "/" + key);
                }
            } else if (value instanceof UnmodifiableConfig inner) {
                collect(inner, out, file);
            }
        }
    }

    private ForgeConfigSpec.ConfigValue<?> value(ConfigFile file, String key) {
        ForgeConfigSpec.ConfigValue<?> result = values(file).get(key);
        if (result == null) throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        return result;
    }

    @Override
    public boolean has(ConfigFile file, String key) {
        return spec(file).isLoaded() && values(file).containsKey(key);
    }

    @Override
    public Object get(ConfigFile file, String key) {
        return normalize(value(file, key).get());
    }

    @Override
    public Object getDefault(ConfigFile file, String key) {
        return normalize(value(file, key).getDefault());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(ConfigFile file, String key, Object value) {
        ForgeConfigSpec.ConfigValue<Object> configValue = (ForgeConfigSpec.ConfigValue<Object>) value(file, key);
        configValue.set(convert(configValue.getDefault(), value));
    }

    @Override
    public void save(Set<ConfigFile> files) {
        for (ConfigFile file : files) {
            spec(file).save();
        }
    }

    @Override
    public void apply(Set<ConfigFile> files) {
        for (ConfigFile file : files) {
            switch (file) {
                case COMMON -> NeoForgeConfigCommon.sync();
                case ITEMS -> {
                    NeoForgeConfigItems.sync();
                    NeoForgeConfigItems.reloadThirstValues();
                }
                case COMPAT -> NeoForgeConfigCompat.sync();
                case CLIENT -> NeoForgeClientConfig.sync();
            }
        }
    }

    private static Object normalize(Object value) {
        if (value instanceof Enum<?> entry) return entry.name();
        if (value instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            list.forEach(entry -> result.add(normalize(entry)));
            return result;
        }
        return value;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convert(Object template, Object value) {
        if (template instanceof Enum<?> entry && value instanceof String name) {
            return Enum.valueOf((Class) entry.getDeclaringClass(), name);
        }
        if (value instanceof Number number) {
            if (template instanceof Integer) return number.intValue();
            if (template instanceof Long) return number.longValue();
            if (template instanceof Double) return number.doubleValue();
            if (template instanceof Float) return number.floatValue();
        }
        return value;
    }
}
