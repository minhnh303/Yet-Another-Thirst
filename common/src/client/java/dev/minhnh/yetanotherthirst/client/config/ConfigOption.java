package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Screen-side description of one config value: which file and key it lives under, how it is edited and
 * how it is shown. Values handled by the screen are always in the canonical form produced by
 * {@link #canonical(Object)}, so equality checks (modified / default) are plain {@link Objects#equals}.
 *
 * @param <T> canonical value type of this option
 */
public final class ConfigOption<T> {

    public enum Type { BOOL, INT, DECIMAL, ENUM, STRING, COLOR, LIST }

    /** Preferred control for numeric options. */
    public enum Control { SLIDER, FIELD, STEPPER }

    /** How a numeric value is rendered next to its control. */
    public enum Format {
        PLAIN, PERCENT, MULTIPLIER, TICKS, HEARTS, DROPLETS, PURITY, BLOCK_Y, MILLIBUCKETS
    }

    private static final String LANG = Lang.CONFIG_PREFIX;

    public final ConfigFile file;
    public final String key;
    public final Type type;
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

    private Language cachedLanguage;
    private String cachedSearchText;

    ConfigOption(ConfigFile file, String key, Type type) {
        this.file = file;
        this.key = key;
        this.type = type;
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

    /**
     * Marks a value stored in a server-side file that only the local client reads (rendering, tooltips).
     * Server-side files are not synced to clients, so it stays editable while connected to a remote server.
     */
    ConfigOption<T> clientOnly() {
        this.clientOnly = true;
        return this;
    }

    public boolean isClientOnly() {
        return clientOnly;
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

    /** Coerces a backend or widget value into this option's canonical type; null falls back to the minimum/empty. */
    @SuppressWarnings("unchecked")
    public T canonical(Object raw) {
        Object result = switch (type) {
            case BOOL -> raw instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(raw));
            case INT, COLOR -> raw instanceof Number n ? clampInt(n.longValue()) : clampInt(parseLong(raw));
            case DECIMAL -> raw instanceof Number n ? snap(n.doubleValue()) : snap(parseDouble(raw));
            case ENUM -> raw == null ? enumValues.get(0) : String.valueOf(raw).toUpperCase(Locale.ROOT);
            case STRING -> raw == null ? "" : String.valueOf(raw);
            case LIST -> schema.canonical(raw);
        };
        return (T) result;
    }

    private int clampInt(long value) {
        if (type == Type.COLOR) {
            return (int) (value & 0xFFFFFF);
        }
        return (int) Mth.clamp(value, (long) min, (long) max);
    }

    /** Clamps into range and rounds to the step's precision so float noise never reads as a modification. */
    double snap(double value) {
        double clamped = Mth.clamp(value, min, max);
        double scale = Math.pow(10, decimals());
        return Math.round(clamped * scale) / scale;
    }

    /** Decimal places implied by {@link #step}, at least 2 for decimals. */
    int decimals() {
        if (type != Type.DECIMAL) {
            return 0;
        }
        String text = BigDecimalHolder.plain(step);
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

    /** The value as the short label shown on a control or in a tooltip. */
    public Component formatValue(Object value) {
        return switch (type) {
            case BOOL -> Boolean.TRUE.equals(value)
                    ? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                    : Component.translatable("options.off").withStyle(ChatFormatting.RED);
            case ENUM -> Component.translatable(LANG + "enum." + key + "." + String.valueOf(value).toLowerCase(Locale.ROOT));
            case STRING -> Component.literal(String.valueOf(value).isEmpty() ? "\"\"" : String.valueOf(value));
            case COLOR -> Component.literal(String.format(Locale.ROOT, "#%06X", (Integer) value))
                    .withStyle(s -> s.withColor((Integer) value));
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
        String text = BigDecimalHolder.plain(Math.round(v * 1000.0) / 1000.0);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
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

    /** Tooltip lines for the option label: description, default, range and constraints. */
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

    /** Case-insensitive match of a search query against the cached translated title, description and key. */
    public boolean matches(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        return searchIndex().contains(normalized);
    }

    private String searchIndex() {
        Language current = Language.getInstance();
        if (cachedSearchText == null || cachedLanguage != current) {
            cachedLanguage = current;
            cachedSearchText = (title().getString() + "\n" + description().getString() + "\n" + key)
                    .toLowerCase(Locale.ROOT);
        }
        return cachedSearchText;
    }

    /** Plain decimal formatting without scientific notation. */
    private static final class BigDecimalHolder {
        static String plain(double v) {
            String text = java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
            return text.contains(".") ? text : text + ".0";
        }
    }
}
