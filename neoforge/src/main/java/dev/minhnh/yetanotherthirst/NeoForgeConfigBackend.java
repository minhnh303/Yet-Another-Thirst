package dev.minhnh.yetanotherthirst;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@link IConfigBackend} over the mod's {@link ModConfigSpec}s. Keys are the leaf names of each spec, which
 * are unique per file, so the screen can address a value without knowing its TOML section.
 */
public final class NeoForgeConfigBackend implements IConfigBackend {

    private final Map<ConfigFile, Map<String, ModConfigSpec.ConfigValue<?>>> index = new EnumMap<>(ConfigFile.class);

    static ModConfigSpec spec(ConfigFile file) {
        return switch (file) {
            case COMMON -> NeoForgeConfigCommon.SPEC;
            case ITEMS -> NeoForgeConfigItems.SPEC;
            case COMPAT -> NeoForgeConfigCompat.SPEC;
            case CLIENT -> NeoForgeClientConfig.SPEC;
        };
    }

    Map<String, ModConfigSpec.ConfigValue<?>> values(ConfigFile file) {
        return index.computeIfAbsent(file, f -> {
            Map<String, ModConfigSpec.ConfigValue<?>> map = new HashMap<>();
            collect(spec(f).getValues(), map);
            return map;
        });
    }

    static void collect(UnmodifiableConfig config, Map<String, ModConfigSpec.ConfigValue<?>> out) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ModConfigSpec.ConfigValue<?> configValue) {
                List<String> path = configValue.getPath();
                String key = path.get(path.size() - 1);
                ModConfigSpec.ConfigValue<?> previous = out.putIfAbsent(key, configValue);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate config leaf key '" + key + "': "
                            + previous.getPath() + " and " + path);
                }
            } else if (value instanceof UnmodifiableConfig inner) {
                collect(inner, out);
            }
        }
    }

    private ModConfigSpec.ConfigValue<?> value(ConfigFile file, String key) {
        ModConfigSpec.ConfigValue<?> value = values(file).get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        }
        return value;
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
        ModConfigSpec.ConfigValue<Object> configValue = (ModConfigSpec.ConfigValue<Object>) value(file, key);
        configValue.set(convert(configValue.getDefault(), value));
    }

    @Override
    public void save(Set<ConfigFile> files) {
        for (ConfigFile file : files) {
            spec(file).save();
            switch (file) {
                case COMMON -> NeoForgeConfigCommon.sync();
                case ITEMS -> NeoForgeConfigItems.sync();
                case COMPAT -> NeoForgeConfigCompat.sync();
                case CLIENT -> NeoForgeClientConfig.sync();
            }
        }
    }

    @Override
    public void reloadItemValues() {
        NeoForgeConfigItems.reloadThirstValues();
    }

    /** Enums become their names and lists are copied so callers never mutate the live config. */
    private static Object normalize(Object value) {
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object element : list) {
                copy.add(normalize(element));
            }
            return copy;
        }
        return value;
    }

    /** Converts a normalized value back to the Java type the spec was defined with. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object convert(Object template, Object value) {
        if (template instanceof Enum<?> e && value instanceof String name) {
            return Enum.valueOf((Class) e.getDeclaringClass(), name);
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
