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

/**
 * {@link IConfigBackend} over the mod's {@link ForgeConfigSpec}s. Keys are the leaf names of each spec, which
 * are unique per file, so the screen can address a value without knowing its TOML section.
 */
public final class ForgeConfigBackend implements IConfigBackend {

    private final Map<ConfigFile, Map<String, ForgeConfigSpec.ConfigValue<?>>> index = new EnumMap<>(ConfigFile.class);

    static ForgeConfigSpec spec(ConfigFile file) {
        return switch (file) {
            case COMMON -> ForgeConfigCommon.SPEC;
            case ITEMS -> ForgeConfigItems.SPEC;
            case COMPAT -> ForgeConfigCompat.SPEC;
            case CLIENT -> ForgeClientConfig.SPEC;
        };
    }

    Map<String, ForgeConfigSpec.ConfigValue<?>> values(ConfigFile file) {
        return index.computeIfAbsent(file, f -> {
            Map<String, ForgeConfigSpec.ConfigValue<?>> map = new HashMap<>();
            collect(spec(f).getValues(), map);
            return map;
        });
    }

    static void collect(UnmodifiableConfig config, Map<String, ForgeConfigSpec.ConfigValue<?>> out) {
        for (UnmodifiableConfig.Entry entry : config.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ForgeConfigSpec.ConfigValue<?> configValue) {
                List<String> path = configValue.getPath();
                String key = path.get(path.size() - 1);
                ForgeConfigSpec.ConfigValue<?> previous = out.putIfAbsent(key, configValue);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate config leaf key '" + key + "': "
                            + previous.getPath() + " and " + path);
                }
            } else if (value instanceof UnmodifiableConfig inner) {
                collect(inner, out);
            }
        }
    }

    private ForgeConfigSpec.ConfigValue<?> value(ConfigFile file, String key) {
        ForgeConfigSpec.ConfigValue<?> value = values(file).get(key);
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
        ForgeConfigSpec.ConfigValue<Object> configValue = (ForgeConfigSpec.ConfigValue<Object>) value(file, key);
        configValue.set(convert(configValue.getDefault(), value));
    }

    @Override
    public void save(Set<ConfigFile> files) {
        for (ConfigFile file : files) {
            spec(file).save();
            switch (file) {
                case COMMON -> ForgeConfigCommon.sync();
                case ITEMS -> ForgeConfigItems.sync();
                case COMPAT -> ForgeConfigCompat.sync();
                case CLIENT -> ForgeClientConfig.sync();
            }
        }
    }

    @Override
    public void reloadItemValues() {
        ForgeConfigItems.reloadThirstValues();
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

