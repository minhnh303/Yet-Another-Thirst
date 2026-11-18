package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Shape of a list-valued option. A flat schema stores {@code List<String>} (one id column); otherwise each
 * entry is a row ({@code List<Object>}) with one cell per {@link Column}.
 *
 * @param <T> canonical element type ({@link String} for flat lists, {@code List<Object>} for multi-column rows)
 */
public final class ListSchema<T> {

    public enum Kind {
        /** Item id or {@code #tag}. */
        ITEM,
        /** Effect rule: {@code effect_id} or {@code effect_id <op> amplifier}. */
        EFFECT,
        INT,
        DECIMAL
    }

    public record Column(Kind kind, String label, double min, double max) {
        public boolean numeric() {
            return kind == Kind.INT || kind == Kind.DECIMAL;
        }
    }

    private final List<Column> columns;
    private final boolean flat;

    private ListSchema(boolean flat, Column... columns) {
        this.flat = flat;
        this.columns = List.of(columns);
    }

    public static ListSchema<String> items() {
        return new ListSchema<>(true, new Column(Kind.ITEM, "item", 0, 0));
    }

    public static ListSchema<String> effects() {
        return new ListSchema<>(true, new Column(Kind.EFFECT, "effect", 0, 0));
    }

    public static ListSchema<List<Object>> itemValues() {
        return new ListSchema<>(false,
                new Column(Kind.ITEM, "item", 0, 0),
                new Column(Kind.INT, "thirst", 0, 40),
                new Column(Kind.INT, "quenched", 0, 40));
    }

    public static ListSchema<List<Object>> temperatureTiers() {
        return new ListSchema<>(false,
                new Column(Kind.INT, "threshold", -10000, 10000),
                new Column(Kind.DECIMAL, "modifier", 0, 10));
    }

    public List<Column> columns() {
        return columns;
    }

    public boolean flat() {
        return flat;
    }

    /** Copies a raw backend list into canonical cells; malformed rows are dropped. */
    @SuppressWarnings("unchecked")
    public List<T> canonical(Object raw) {
        List<T> out = new ArrayList<>();
        if (!(raw instanceof List<?> list)) {
            return out;
        }
        for (Object element : list) {
            if (flat) {
                out.add((T) String.valueOf(element));
            } else if (element instanceof List<?> row && row.size() >= columns.size()) {
                List<Object> cells = new ArrayList<>(columns.size());
                for (int i = 0; i < columns.size(); i++) {
                    cells.add(cell(columns.get(i), row.get(i)));
                }
                out.add((T) List.copyOf(cells));
            }
        }
        return out;
    }

    static Object cell(Column column, Object value) {
        return switch (column.kind()) {
            case ITEM, EFFECT -> String.valueOf(value);
            case INT -> value instanceof Number n ? n.intValue() : (int) parse(value);
            case DECIMAL -> value instanceof Number n ? Math.round(n.doubleValue() * 1000.0) / 1000.0 : parse(value);
        };
    }

    private static double parse(Object value) {
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Text form of one cell for an edit box. */
    static String text(Column column, Object value) {
        if (column.kind() == Kind.DECIMAL && value instanceof Number n) {
            return ConfigOption.trim(n.doubleValue());
        }
        return String.valueOf(value);
    }

    /** Whether {@code text} is acceptable for the column. Unknown-but-well-formed ids are allowed. */
    static boolean valid(Column column, String text) {
        String value = text.trim();
        switch (column.kind()) {
            case ITEM -> {
                String id = value.startsWith("#") ? value.substring(1) : value;
                return !id.isEmpty() && ResourceLocation.tryParse(id) != null;
            }
            case EFFECT -> {
                return validEffect(value);
            }
            case INT -> {
                try {
                    long v = Long.parseLong(value);
                    return v >= column.min() && v <= column.max();
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case DECIMAL -> {
                try {
                    double v = Double.parseDouble(value);
                    return !Double.isNaN(v) && v >= column.min() && v <= column.max();
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean validEffect(String value) {
        if (value.isEmpty() || ThirstConfig.EffectCondition.parse(value) == null) {
            return false;
        }
        String[] operators = {">=", "<=", "==", ">", "<", "="};
        for (String operator : operators) {
            int index = value.indexOf(operator);
            if (index < 0) continue;
            String id = value.substring(0, index).trim();
            String amplifier = value.substring(index + operator.length()).trim();
            if (ResourceLocation.tryParse(id) == null) return false;
            try {
                Integer.parseInt(amplifier);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return ResourceLocation.tryParse(value) != null;
    }

    /** Cells for a freshly added row. */
    List<Object> blankRow() {
        List<Object> row = new ArrayList<>();
        for (Column column : columns) {
            row.add(switch (column.kind()) {
                case ITEM, EFFECT -> "";
                case INT -> (int) Math.max(column.min(), Math.min(column.max(), 1));
                case DECIMAL -> Math.max(column.min(), Math.min(column.max(), 1.0));
            });
        }
        return row;
    }
}
