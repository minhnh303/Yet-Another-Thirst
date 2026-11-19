package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TestConfigBackend implements IConfigBackend {
    private final Map<ConfigKey, Object> values = new HashMap<>();
    private final Map<ConfigKey, Object> defaults = new HashMap<>();
    public final Set<ConfigFile> savedFiles = EnumSet.noneOf(ConfigFile.class);
    public int saveCalls;
    public final Set<ConfigFile> appliedFiles = EnumSet.noneOf(ConfigFile.class);
    public int applyCalls;
    public RuntimeException failOnSave;
    /** When set, only saves that include this file throw {@link #failOnSave}. */
    public ConfigFile failOnFile;
    /** Snapshot of each file's values at its last successful save. */
    public final Map<ConfigFile, Map<ConfigKey, Object>> persisted = new HashMap<>();

    public TestConfigBackend() {
        for (ConfigOption<?> option : ConfigOptions.all()) {
            Object value = switch (option.type) {
                case BOOL -> false;
                case INT -> (int) option.min();
                case DECIMAL -> option.min();
                case ENUM -> option.enumValues().isEmpty() ? "" : option.enumValues().get(0);
                case STRING -> "";
                case COLOR -> 0;
                case LIST -> List.of();
            };
            seed(option, value);
        }
    }

    public void seed(ConfigOption<?> option, Object value) {
        Object backendVal = option.toBackend(option.canonical(value));
        values.put(option.configKey(), copy(backendVal));
        defaults.put(option.configKey(), copy(backendVal));
    }

    @Override
    public boolean has(ConfigFile file, String key) {
        return defaults.containsKey(new ConfigKey(file, key));
    }

    @Override
    public Object get(ConfigFile file, String key) {
        return copy(values.get(new ConfigKey(file, key)));
    }

    @Override
    public Object getDefault(ConfigFile file, String key) {
        return copy(defaults.get(new ConfigKey(file, key)));
    }

    @Override
    public void set(ConfigFile file, String key, Object value) {
        values.put(new ConfigKey(file, key), copy(value));
    }

    @Override
    public void save(Set<ConfigFile> files) {
        saveCalls++;
        if (failOnSave != null && (failOnFile == null || files.contains(failOnFile))) throw failOnSave;
        savedFiles.addAll(files);
        for (ConfigFile file : files) {
            Map<ConfigKey, Object> snapshot = new HashMap<>();
            values.forEach((key, value) -> {
                if (key.file() == file) snapshot.put(key, copy(value));
            });
            persisted.put(file, snapshot);
        }
    }

    @Override
    public void apply(Set<ConfigFile> files) {
        applyCalls++;
        appliedFiles.addAll(files);
    }

    private static Object copy(Object value) {
        if (!(value instanceof List<?> list)) return value;
        List<Object> result = new ArrayList<>(list.size());
        for (Object entry : list) {
            result.add(copy(entry));
        }
        return result;
    }
}
