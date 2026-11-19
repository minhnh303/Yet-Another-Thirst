package dev.minhnh.yetanotherthirst.client.config;

import java.util.List;

/** Type descriptor binding a compile-time value type {@code T} to a {@link ConfigOption.Type}. */
public final class ConfigValueType<T> {
    public static final ConfigValueType<Boolean> BOOL = new ConfigValueType<>(ConfigOption.Type.BOOL, Boolean.class, null);
    public static final ConfigValueType<Integer> INT = new ConfigValueType<>(ConfigOption.Type.INT, Integer.class, null);
    public static final ConfigValueType<Double> DECIMAL = new ConfigValueType<>(ConfigOption.Type.DECIMAL, Double.class, null);
    public static final ConfigValueType<String> ENUM = new ConfigValueType<>(ConfigOption.Type.ENUM, String.class, null);
    public static final ConfigValueType<String> STRING = new ConfigValueType<>(ConfigOption.Type.STRING, String.class, null);
    public static final ConfigValueType<Integer> COLOR = new ConfigValueType<>(ConfigOption.Type.COLOR, Integer.class, null);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <E> ConfigValueType<List<E>> list(ListSchema<E> schema) {
        return new ConfigValueType<>(ConfigOption.Type.LIST, (Class) List.class, schema);
    }

    private final ConfigOption.Type kind;
    private final Class<T> valueClass;
    private final ListSchema<?> schema;

    private ConfigValueType(ConfigOption.Type kind, Class<T> valueClass, ListSchema<?> schema) {
        this.kind = kind;
        this.valueClass = valueClass;
        this.schema = schema;
    }

    public ConfigOption.Type kind() {
        return kind;
    }

    public Class<T> valueClass() {
        return valueClass;
    }

    public ListSchema<?> schema() {
        return schema;
    }
}

