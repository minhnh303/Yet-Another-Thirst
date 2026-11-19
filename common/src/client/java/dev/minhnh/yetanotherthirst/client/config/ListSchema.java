package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Shape and domain conversion of a list-valued option.
 * Converts between raw backend lists ({@code List<String>} or {@code List<List<Object>>}) and domain values {@code T}.
 */
public final class ListSchema<T> {

    public enum Kind {
        ITEM,
        EFFECT,
        INT,
        DECIMAL
    }

    public record Column(Kind kind, String label, double min, double max) {
        public boolean numeric() {
            return kind == Kind.INT || kind == Kind.DECIMAL;
        }
    }

    private static final String LANG = Lang.CONFIG_PREFIX;

    private final List<Column> columns;
    private final boolean flat;
    private final Class<T> elementType;
    private final Function<List<Object>, T> fromCells;
    private final Function<T, List<Object>> toCells;
    private final ListPresentation<T> presentation;

    private ListSchema(
            boolean flat,
            Class<T> elementType,
            Function<List<Object>, T> fromCells,
            Function<T, List<Object>> toCells,
            ListPresentation<T> presentation,
            Column... columns) {
        this.flat = flat;
        this.elementType = elementType;
        this.fromCells = fromCells;
        this.toCells = toCells;
        this.presentation = presentation;
        this.columns = List.of(columns);
    }

    public static ListSchema<String> items() {
        Column itemCol = new Column(Kind.ITEM, "item", 0, 0);
        return new ListSchema<>(
                true,
                String.class,
                cells -> String.valueOf(cells.get(0)),
                List::of,
                itemPresentation(s -> s),
                itemCol
        );
    }

    public static ListSchema<String> effects() {
        Column effectCol = new Column(Kind.EFFECT, "effect", 0, 0);
        return new ListSchema<>(
                true,
                String.class,
                cells -> String.valueOf(cells.get(0)),
                List::of,
                null,
                effectCol
        );
    }

    public static ListSchema<ItemValue> itemValues() {
        Column itemCol = new Column(Kind.ITEM, "item", 0, 0);
        Column thirstCol = new Column(Kind.INT, "thirst", 0, 40);
        Column quenchedCol = new Column(Kind.INT, "quenched", 0, 40);
        return new ListSchema<>(
                false,
                ItemValue.class,
                cells -> new ItemValue(
                        String.valueOf(cells.get(0)),
                        ((Number) cell(thirstCol, cells.get(1))).intValue(),
                        ((Number) cell(quenchedCol, cells.get(2))).intValue()
                ),
                val -> List.of(val.itemId(), val.thirst(), val.quenched()),
                itemPresentation(ItemValue::itemId),
                itemCol, thirstCol, quenchedCol
        );
    }

    public static ListSchema<TemperatureTier> temperatureTiers() {
        Column thresholdCol = new Column(Kind.INT, "threshold", -10000, 10000);
        Column modifierCol = new Column(Kind.DECIMAL, "modifier", 0, 10);
        return new ListSchema<>(
                false,
                TemperatureTier.class,
                cells -> new TemperatureTier(
                        ((Number) cell(thresholdCol, cells.get(0))).intValue(),
                        ((Number) cell(modifierCol, cells.get(1))).doubleValue()
                ),
                val -> List.of(val.threshold(), ((Number) cell(modifierCol, val.modifier())).doubleValue()),
                null,
                thresholdCol, modifierCol
        );
    }

    public List<Column> columns() {
        return columns;
    }

    public boolean flat() {
        return flat;
    }

    public Class<T> elementType() {
        return elementType;
    }

    public ListPresentation<T> presentation() {
        return presentation;
    }

    /** Copies a raw backend or domain list into canonical domain entries; malformed rows are dropped. */
    public List<T> canonical(Object raw) {
        if (!(raw instanceof List<?> list)) {
            return List.of();
        }
        List<T> out = new ArrayList<>(list.size());
        for (Object element : list) {
            if (element == null) continue;
            if (!flat && elementType.isInstance(element)) {
                T typed = elementType.cast(element);
                out.add(fromCells.apply(toCells.apply(typed)));
            } else if (flat) {
                if (element instanceof List<?>) continue;
                out.add(fromCells.apply(List.of(cell(columns.get(0), element))));
            } else if (element instanceof List<?> row && row.size() >= columns.size()) {
                List<Object> cells = new ArrayList<>(columns.size());
                for (int i = 0; i < columns.size(); i++) {
                    cells.add(cell(columns.get(i), row.get(i)));
                }
                out.add(fromCells.apply(cells));
            }
        }
        return List.copyOf(out);
    }

    /** Converts canonical domain entries back to normalized backend list structure. */
    public List<Object> toBackend(List<T> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        List<Object> out = new ArrayList<>(entries.size());
        for (T entry : entries) {
            List<Object> cells = toCells.apply(entry);
            out.add(flat ? cells.get(0) : cells);
        }
        return out;
    }

    public List<Object> toCells(T entry) {
        return toCells.apply(entry);
    }

    public String[] toRowStrings(T entry) {
        List<Object> cells = toCells.apply(entry);
        String[] row = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            row[i] = text(columns.get(i), cells.get(i));
        }
        return row;
    }

    public T fromRowStrings(String[] row) {
        List<Object> cells = new ArrayList<>(columns.size());
        for (int i = 0; i < columns.size(); i++) {
            cells.add(cell(columns.get(i), row[i].trim()));
        }
        return fromCells.apply(cells);
    }

    public String headText(Object entry) {
        if (entry == null) return "";
        if (elementType.isInstance(entry)) {
            List<Object> cells = toCells.apply(elementType.cast(entry));
            return text(columns.get(0), cells.get(0));
        }
        Object head = entry instanceof List<?> row && !row.isEmpty() ? row.get(0) : entry;
        return text(columns.get(0), head);
    }

    static Object cell(Column column, Object value) {
        return switch (column.kind()) {
            case ITEM, EFFECT -> String.valueOf(value);
            case INT -> value instanceof Number n ? n.intValue() : (int) parse(value);
            case DECIMAL -> value instanceof Number n ? Math.round(n.doubleValue() * 1000.0) / 1000.0 : Math.round(parse(value) * 1000.0) / 1000.0;
        };
    }

    private static double parse(Object value) {
        try {
            return Double.parseDouble(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String text(Column column, Object value) {
        if (column.kind() == Kind.DECIMAL && value instanceof Number n) {
            return ConfigOption.trim(n.doubleValue());
        }
        return String.valueOf(value);
    }

    static boolean valid(Column column, String text) {
        if (text == null) return false;
        String value = text.trim();
        return switch (column.kind()) {
            case ITEM -> {
                String id = value.startsWith("#") ? value.substring(1) : value;
                yield !id.isEmpty() && ResourceLocation.tryParse(id) != null;
            }
            case EFFECT -> validEffect(value);
            case INT -> {
                try {
                    long v = Long.parseLong(value);
                    yield v >= column.min() && v <= column.max();
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
            case DECIMAL -> {
                try {
                    double v = Double.parseDouble(value);
                    yield !Double.isNaN(v) && Double.isFinite(v) && v >= column.min() && v <= column.max();
                } catch (NumberFormatException e) {
                    yield false;
                }
            }
        };
    }

    private static boolean validEffect(String value) {
        if (value.isEmpty() || ThirstConfig.EffectCondition.parse(value) == null) {
            return false;
        }
        String[] operators = {">=", "<=", "==", "!=", ">", "<", "="};
        for (String operator : operators) {
            int index = value.indexOf(operator);
            if (index < 0) continue;
            String id = value.substring(0, index).trim();
            String amplifier = value.substring(index + operator.length()).trim();
            if (id.isEmpty() || ResourceLocation.tryParse(id) == null) return false;
            try {
                int amp = Integer.parseInt(amplifier);
                return amp >= 0 && amp <= 255;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return ResourceLocation.tryParse(value) != null;
    }

    List<Object> blankRow() {
        List<Object> row = new ArrayList<>(columns.size());
        for (Column column : columns) {
            row.add(switch (column.kind()) {
                case ITEM, EFFECT -> "";
                case INT -> (int) Math.max(column.min(), Math.min(column.max(), 1));
                case DECIMAL -> Math.max(column.min(), Math.min(column.max(), 1.0));
            });
        }
        return row;
    }

    /**
     * Resolves an item id or {@code #tag} to a fresh icon stack. Not cached: tags are unbound until a world
     * loads and can change on reload, so callers cache per screen lifetime instead.
     */
    static ItemStack resolveItemIcon(String rawId) {
        String id = rawId == null ? "" : rawId.trim();
        if (id.isEmpty()) return ItemStack.EMPTY;
        if (id.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(id.substring(1));
            if (location == null) return ItemStack.EMPTY;
            Optional<HolderSet.Named<Item>> tag = BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, location));
            if (tag.isEmpty() || tag.get().size() == 0) return ItemStack.EMPTY;
            return new ItemStack(tag.get().get(0).value());
        }
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getOptional(location).orElse(Items.AIR);
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static <T> ListPresentation<T> itemPresentation(Function<T, String> idExtractor) {
        return new ListPresentation<>() {
            @Override
            public Component displayName(T value) {
                String id = idExtractor.apply(value).trim();
                ItemStack stack = icon(value);
                if (!stack.isEmpty()) {
                    return id.startsWith("#")
                            ? Component.translatable(LANG + "list.tag", id).append(" · ").append(stack.getHoverName())
                            : stack.getHoverName();
                }
                return Component.translatable(LANG + "list.unknown").withStyle(ChatFormatting.GOLD);
            }

            @Override
            public ItemStack icon(T value) {
                return resolveItemIcon(idExtractor.apply(value));
            }

            @Override
            public List<Component> tooltip(T value) {
                return List.of(displayName(value));
            }
        };
    }
}

