package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Typed metadata descriptor for a single config setting, including range constraints, formatting,
 * canonical value conversion, and cached search indexing.
 */
public final class ConfigOption<T> {

    public enum Type { BOOL, INT, DECIMAL, ENUM, STRING, COLOR, LIST }

    public enum Control { SLIDER, FIELD, STEPPER }

    public enum Format {
        PLAIN, PERCENT, MULTIPLIER, TICKS, HEARTS, DROPLETS, PURITY, BLOCK_Y, MILLIBUCKETS
    }

    static final String LANG = Lang.CONFIG_PREFIX;

    public final ConfigFile file;
    public final String key;
    public final Type type;
    public final ConfigValueType<T> valueType;

    Control control = Control.SLIDER;
    Format format = Format.PLAIN;
    double min;
    double max;
    double step = 1.0;
    List<String> enumValues = List.of();
    ListSchema<?> schema;
    String requiredMod;
    boolean rejoin;
    boolean clientOnly;
    ConfigControlFactory<T> controlFactory;

    private String categoryId;
    private String sectionId;
    private Language cachedLanguage;
    private String cachedSearchText;

    ConfigOption(ConfigFile file, String key, ConfigValueType<T> valueType) {
        this.file = file;
        this.key = key;
        this.type = valueType.kind();
        this.valueType = valueType;
        this.schema = valueType.schema();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    ConfigOption(ConfigFile file, String key, Type type) {
        this(file, key, (ConfigValueType<T>) defaultValueType(type));
    }

    private static ConfigValueType<?> defaultValueType(Type type) {
        return switch (type) {
            case BOOL -> ConfigValueType.BOOL;
            case INT -> ConfigValueType.INT;
            case DECIMAL -> ConfigValueType.DECIMAL;
            case ENUM -> ConfigValueType.ENUM;
            case STRING -> ConfigValueType.STRING;
            case COLOR -> ConfigValueType.COLOR;
            case LIST -> ConfigValueType.list(ListSchema.items());
        };
    }

    static ConfigOption<Boolean> bool(ConfigFile file, String key) {
        return new ConfigOption<>(file, key, ConfigValueType.BOOL);
    }

    static ConfigOption<String> string(ConfigFile file, String key) {
        return new ConfigOption<>(file, key, ConfigValueType.STRING);
    }

    static ConfigOption<Integer> color(ConfigFile file, String key) {
        ConfigOption<Integer> option = new ConfigOption<>(file, key, ConfigValueType.COLOR);
        option.max = 0xFFFFFF;
        return option;
    }

    static ConfigOption<Integer> integer(ConfigFile file, String key, int min, int max) {
        ConfigOption<Integer> option = new ConfigOption<>(file, key, ConfigValueType.INT);
        option.min = min;
        option.max = max;
        return option;
    }

    static ConfigOption<Double> decimal(ConfigFile file, String key, double min, double max, double step) {
        ConfigOption<Double> option = new ConfigOption<>(file, key, ConfigValueType.DECIMAL);
        option.min = min;
        option.max = max;
        option.step = step;
        return option;
    }

    static ConfigOption<Double> chance(String key) {
        return decimal(ConfigFile.COMMON, key, 0, 1, 0.01).format(Format.PERCENT);
    }

    static ConfigOption<String> enumeration(ConfigFile file, String key, Enum<?>... values) {
        return enumeration(file, key, Arrays.stream(values).map(Enum::name).toArray(String[]::new));
    }

    static ConfigOption<String> enumeration(ConfigFile file, String key, String... values) {
        ConfigOption<String> option = new ConfigOption<>(file, key, ConfigValueType.ENUM);
        option.enumValues = List.of(values);
        return option;
    }

    static <E> ConfigOption<List<E>> list(ConfigFile file, String key, ListSchema<E> schema) {
        ConfigOption<List<E>> option = new ConfigOption<>(file, key, ConfigValueType.list(schema));
        option.schema = schema;
        return option;
    }

    ConfigOption<T> format(Format format) {
        this.format = format;
        return this;
    }

    ConfigOption<T> field() {
        this.control = Control.FIELD;
        return this;
    }

    ConfigOption<T> stepper() {
        this.control = Control.STEPPER;
        return this;
    }

    ConfigOption<T> rejoin() {
        this.rejoin = true;
        return this;
    }

    /** Marks a server-side-file option that only the local client reads, so it stays editable on remote servers. */
    ConfigOption<T> clientOnly() {
        this.clientOnly = true;
        return this;
    }

    public boolean isClientOnly() {
        return clientOnly;
    }

    ConfigOption<T> requiredMod(String modId) {
        this.requiredMod = modId;
        return this;
    }

    public ConfigOption<T> controlFactory(ConfigControlFactory<T> controlFactory) {
        this.controlFactory = controlFactory;
        return this;
    }

    public ConfigControlFactory<T> controlFactory() {
        return controlFactory;
    }

    void bindOwner(String categoryId, String sectionId) {
        if (this.categoryId != null || this.sectionId != null) {
            throw new IllegalStateException("Option " + file + "/" + key + " is already bound to "
                    + this.categoryId + "/" + this.sectionId);
        }
        this.categoryId = categoryId;
        this.sectionId = sectionId;
        this.cachedSearchText = null;
    }

    public ConfigKey configKey() {
        return new ConfigKey(file, key);
    }

    public String categoryId() {
        return categoryId;
    }

    public String sectionId() {
        return sectionId;
    }

    public Component title() {
        return Component.translatable(LANG + "option." + key);
    }

    public Component description() {
        return Component.translatable(LANG + "option." + key + ".desc");
    }

    public String requiredMod() {
        return requiredMod;
    }

    public boolean requiresRejoin() {
        return rejoin;
    }

    public ListSchema<?> schema() {
        return schema;
    }

    public List<String> enumValues() {
        return enumValues;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public double step() {
        return step;
    }

    public Control control() {
        return control;
    }

    public Format format() {
        return format;
    }

    /** Coerces a backend or widget value into this option's canonical domain type {@code T}. */
    @SuppressWarnings("unchecked")
    public T canonical(Object raw) {
        Object result = switch (type) {
            case BOOL -> raw instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(raw));
            case INT, COLOR -> raw instanceof Number n ? clampInt(n.longValue()) : clampInt(parseLong(raw));
            case DECIMAL -> raw instanceof Number n ? snap(n.doubleValue()) : snap(parseDouble(raw));
            case ENUM -> raw == null
                    ? (enumValues.isEmpty() ? "" : enumValues.get(0))
                    : String.valueOf(raw).toUpperCase(Locale.ROOT);
            case STRING -> raw == null ? "" : String.valueOf(raw);
            case LIST -> schema.canonical(raw);
        };
        return (T) result;
    }

    /** Converts a canonical value into the normalized structure expected by {@link dev.minhnh.yetanotherthirst.platform.services.IConfigBackend}. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object toBackend(Object canonicalValue) {
        if (type != Type.LIST || !(canonicalValue instanceof List<?> list)) {
            return canonicalValue;
        }
        List<?> toConvert = list;
        if ("environmentzTemperatureTiers".equals(key)) {
            List<Object> sorted = new ArrayList<>(list);
            sorted.sort(Comparator.comparingInt(entry -> {
                if (entry instanceof TemperatureTier tier) return tier.threshold();
                if (entry instanceof List<?> row && !row.isEmpty() && row.get(0) instanceof Number n) return n.intValue();
                return 0;
            }));
            toConvert = sorted;
        }
        ListSchema rawSchema = schema;
        return rawSchema.toBackend(rawSchema.canonical(toConvert));
    }

    private int clampInt(long value) {
        if (type == Type.COLOR) {
            return (int) (value & 0xFFFFFF);
        }
        return (int) Mth.clamp(value, (long) min, (long) max);
    }

    double snap(double value) {
        double clamped = Mth.clamp(value, min, max);
        double scale = Math.pow(10, decimals());
        return Math.round(clamped * scale) / scale;
    }

    /** Rounds a typed number to the nearest multiple of {@link #step()} from {@link #min()}, like the slider does. */
    double snapToStep(double value) {
        if (step <= 0) return snap(value);
        double clamped = Mth.clamp(value, min, max);
        return snap(min + Math.round((clamped - min) / step) * step);
    }

    int decimals() {
        if (type != Type.DECIMAL) {
            return 0;
        }
        String text = plainDecimal(step);
        int dot = text.indexOf('.');
        return Math.max(2, dot < 0 ? 0 : text.length() - dot - 1);
    }

    private static long parseLong(Object raw) {
        try {
            return Long.parseLong(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDouble(Object raw) {
        try {
            return Double.parseDouble(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Component formatValue(Object value) {
        return switch (type) {
            case BOOL -> Boolean.TRUE.equals(value)
                    ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            case ENUM -> Component.translatable(LANG + "enum." + key + "." + String.valueOf(value).toLowerCase(Locale.ROOT));
            case STRING -> Component.literal(String.valueOf(value).isEmpty() ? "\"\"" : String.valueOf(value));
            case COLOR -> Component.literal(String.format(Locale.ROOT, "#%06X", ((Number) value).intValue() & 0xFFFFFF))
                    .withStyle(s -> s.withColor(((Number) value).intValue() & 0xFFFFFF));
            case LIST -> Component.translatable(LANG + "list.count", ((List<?>) value).size());
            case INT, DECIMAL -> formatNumber(((Number) value).doubleValue());
        };
    }

    public Component formatNumber(double v) {
        String number = numberText(v);
        return switch (format) {
            case PLAIN, BLOCK_Y -> Component.literal(number);
            case PERCENT -> Component.literal(Math.round(v * 100) + "%");
            case MULTIPLIER -> Component.literal(number + "×");
            case MILLIBUCKETS -> Component.literal(number + " mB");
            case TICKS -> Component.translatable(LANG + "unit.ticks", number, trim(v / 20.0));
            case HEARTS -> Component.translatable(LANG + "unit.hearts", number, trim(v / 2.0));
            case DROPLETS -> Component.translatable(LANG + "unit.droplets", number, trim(v / 2.0));
            case PURITY -> purityName((int) Math.round(v));
        };
    }

    String numberText(double v) {
        return type == Type.DECIMAL ? trim(v) : Long.toString(Math.round(v));
    }

    static String trim(double v) {
        String text = plainDecimal(Math.round(v * 1000.0) / 1000.0);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    private static String plainDecimal(double v) {
        String text = BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
        return text.contains(".") ? text : text + ".0";
    }

    public static MutableComponent purityName(int purity) {
        ChatFormatting color = switch (purity) {
            case 0 -> ChatFormatting.DARK_RED;
            case 1 -> ChatFormatting.GOLD;
            case 2 -> ChatFormatting.AQUA;
            default -> ChatFormatting.GREEN;
        };
        return Component.translatable(Lang.purityKey(Mth.clamp(purity, 0, 3))).withStyle(color);
    }

    public List<Component> tooltip(Object defaultValue, boolean locked, boolean modMissing) {
        List<Component> lines = new ArrayList<>();
        lines.add(title().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        lines.add(description().copy().withStyle(ChatFormatting.GRAY));
        lines.add(Component.empty());
        if (defaultValue != null) {
            lines.add(Component.translatable(LANG + "tooltip.default", formatValue(defaultValue)).withStyle(ChatFormatting.DARK_GRAY));
        }
        if ((type == Type.INT || type == Type.DECIMAL) && format != Format.PURITY) {
            lines.add(Component.translatable(LANG + "tooltip.range", numberText(min), numberText(max)).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (rejoin) {
            lines.add(Component.translatable(LANG + "tooltip.rejoin").withStyle(ChatFormatting.GOLD));
        }
        if (modMissing) {
            lines.add(Component.translatable(LANG + "tooltip.mod_missing", requiredMod).withStyle(ChatFormatting.GOLD));
        }
        if (locked) {
            lines.add(Component.translatable(LANG + "tooltip.locked").withStyle(ChatFormatting.RED));
        }
        lines.add(Component.literal(file.name().toLowerCase(Locale.ROOT) + " › " + key).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        return lines;
    }

    /** Case-insensitive match against cached normalized key, title, description, category, and section text. */
    public boolean matches(String query) {
        if (query == null) return true;
        String normalized = query.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return true;
        return searchIndex().contains(normalized);
    }

    String searchIndex() {
        Language currentLanguage = Language.getInstance();
        if (cachedSearchText == null || cachedLanguage != currentLanguage) {
            String catText = categoryId != null
                    ? Component.translatable(LANG + "category." + categoryId).getString()
                    : "";
            String secText = sectionId != null
                    ? Component.translatable(LANG + "section." + sectionId).getString()
                    : "";
            cachedSearchText = (key + "\n"
                    + title().getString() + "\n"
                    + description().getString() + "\n"
                    + catText + "\n"
                    + secText).toLowerCase(Locale.ROOT);
            cachedLanguage = currentLanguage;
        }
        return cachedSearchText;
    }
}
