package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregated metadata registry for all config categories and options,
 * composed from domain modules ({@link GeneralOptions}, {@link DepletionOptions},
 * {@link PurityOptions}, {@link MachineOptions}, {@link ItemOptions},
 * {@link CompatOptions}, {@link ClientOptions}).
 */
public final class ConfigOptions {
    public static final List<ConfigCategory> CATEGORIES;
    public static final Map<ConfigKey, ConfigOption<?>> BY_KEY;
    private static final List<ConfigOption<?>> ALL_OPTIONS;

    static {
        List<ConfigCategory> categories = new ArrayList<>();
        categories.addAll(GeneralOptions.categories());
        categories.addAll(DepletionOptions.categories());
        categories.addAll(PurityOptions.categories());
        categories.addAll(MachineOptions.categories());
        categories.addAll(ItemOptions.categories());
        categories.addAll(CompatOptions.categories());
        categories.addAll(ClientOptions.categories());

        Map<ConfigKey, ConfigOption<?>> byKey = new LinkedHashMap<>();
        List<ConfigOption<?>> all = new ArrayList<>();
        for (ConfigCategory category : categories) {
            for (ConfigCategory.Section section : category.sections()) {
                for (ConfigOption<?> option : section.options()) {
                    option.bindOwner(category.id(), section.id());
                    ConfigKey configKey = option.configKey();
                    if (byKey.putIfAbsent(configKey, option) != null) {
                        throw new IllegalStateException("Duplicate config option key: " + configKey);
                    }
                    all.add(option);
                }
            }
        }

        CATEGORIES = List.copyOf(categories);
        BY_KEY = Collections.unmodifiableMap(byKey);
        ALL_OPTIONS = List.copyOf(all);
    }

    private ConfigOptions() {
    }

    public static List<ConfigOption<?>> all() {
        return ALL_OPTIONS;
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigOption<T> get(ConfigFile file, String key) {
        return get(new ConfigKey(file, key));
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigOption<T> get(ConfigKey key) {
        ConfigOption<?> option = BY_KEY.get(key);
        if (option == null) {
            throw new IllegalArgumentException("Unknown config option: " + key);
        }
        return (ConfigOption<T>) option;
    }
}
