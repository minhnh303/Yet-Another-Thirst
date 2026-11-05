package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class NeoForgeConfigItems {

    static final ModConfigSpec SPEC;

    @SuppressWarnings("unused")
    private static final ModConfigSpec.IntValue CONFIG_VERSION;
    // [items]
    private static final ModConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;
    private static final ModConfigSpec.ConfigValue<List<? extends List<?>>> DRINK_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEMS_BLACKLIST;
    // [filter]
    private static final ModConfigSpec.IntValue FILTER_BASE_SPEED;
    private static final ModConfigSpec.DoubleValue DIRTY_WATER_DECAY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER;
    private static final ModConfigSpec.DoubleValue ACCEPTABLE_WATER_DECAY_MULTIPLIER;
    private static final ModConfigSpec.IntValue FABRIC_FILTER_DURABILITY;
    private static final ModConfigSpec.IntValue FABRIC_FILTER_MAX_PURITY;
    private static final ModConfigSpec.IntValue SAND_FILTER_DURABILITY;
    private static final ModConfigSpec.IntValue SAND_FILTER_MAX_PURITY;
    private static final ModConfigSpec.IntValue CARBON_FILTER_DURABILITY;
    private static final ModConfigSpec.IntValue CARBON_FILTER_MAX_PURITY;
    // [boiler]
    private static final ModConfigSpec.IntValue WATER_BOILER_CAPACITY;
    private static final ModConfigSpec.IntValue WATER_BOILER_BOIL_TIME;
    private static final ModConfigSpec.IntValue WATER_BOILER_ENERGY_CONSUMPTION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        CONFIG_VERSION = builder
                .comment("Config file version — do not modify")
                .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);

        builder.push("items");
        WATER_BOTTLE_STACKSIZE = builder
                .comment("Maximum stack size for water bottles (1-64)")
                .defineInRange("waterBottleStackSize", 64, 1, 64);
        DRINK_VALUES = builder
                .comment("Items that restore thirst when drunk. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("drinks", DefaultItemValues.defaultDrinkValues(), NeoForgeConfigItems::validItemValueEntry);
        FOOD_VALUES = builder
                .comment("Items that restore thirst when eaten. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("foods", DefaultItemValues.defaultFoodValues(), NeoForgeConfigItems::validItemValueEntry);
        ITEMS_BLACKLIST = builder
                .comment("Items and tags excluded from thirst restoration entirely. Format: [\"item-id\", \"#item-tag\"]")
                .defineList("itemsBlacklist", List.of(), e -> e instanceof String);
        builder.pop();

        builder.push("filter");
        FILTER_BASE_SPEED = builder
                .comment("Ticks required to filter one unit of fluid (lower is faster)")
                .defineInRange("filterBaseSpeed", 1, 1, Integer.MAX_VALUE);
        DIRTY_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability decay multiplier when filtering dirty water")
                .defineInRange("dirtyWaterDecayMultiplier", 4.0, 0.0, 1000.0);
        SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability decay multiplier when filtering slightly dirty water")
                .defineInRange("slightlyDirtyWaterDecayMultiplier", 2.0, 0.0, 1000.0);
        ACCEPTABLE_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability decay multiplier when filtering acceptable water")
                .defineInRange("acceptableWaterDecayMultiplier", 1.0, 0.0, 1000.0);
        FABRIC_FILTER_DURABILITY = builder
                .comment("Max damage/mB the fabric filter can process before breaking")
                .defineInRange("fabricFilterDurability", 10000, 1, Integer.MAX_VALUE);
        FABRIC_FILTER_MAX_PURITY = builder
                .comment("Highest purity level the fabric filter can achieve (1=Slightly Dirty, 2=Acceptable, 3=Purified)")
                .defineInRange("fabricFilterMaxPurity", 1, 1, 3);
        SAND_FILTER_DURABILITY = builder
                .comment("Max damage/mB the sand filter can process before breaking")
                .defineInRange("sandFilterDurability", 50000, 1, Integer.MAX_VALUE);
        SAND_FILTER_MAX_PURITY = builder
                .comment("Highest purity level the sand filter can achieve (1=Slightly Dirty, 2=Acceptable, 3=Purified)")
                .defineInRange("sandFilterMaxPurity", 2, 1, 3);
        CARBON_FILTER_DURABILITY = builder
                .comment("Max damage/mB the carbon filter can process before breaking")
                .defineInRange("carbonFilterDurability", 250000, 1, Integer.MAX_VALUE);
        CARBON_FILTER_MAX_PURITY = builder
                .comment("Highest purity level the carbon filter can achieve (1=Slightly Dirty, 2=Acceptable, 3=Purified)")
                .defineInRange("carbonFilterMaxPurity", 3, 1, 3);
        builder.pop();

        builder.push("boiler");
        WATER_BOILER_CAPACITY = builder
                .comment("Internal fluid tank capacity of the Water Boiler in mB (default: 16000 = 16 buckets)")
                .defineInRange("waterBoilerCapacity", 16000, 1000, Integer.MAX_VALUE);
        WATER_BOILER_BOIL_TIME = builder
                .comment("Ticks required to boil 1000 mB (1 bucket) of water (default: 200 = 10 seconds)")
                .defineInRange("waterBoilerBoilTime", 200, 1, Integer.MAX_VALUE);
        WATER_BOILER_ENERGY_CONSUMPTION = builder
                .comment("FE/Forge Energy consumed per tick while boiling (default: 32)")
                .defineInRange("waterBoilerEnergyConsumption", 32, 0, Integer.MAX_VALUE);
        builder.pop();

        SPEC = builder.build();
    }

    static void sync() {
        ThirstConfig.WATER_BOTTLE_STACKSIZE = WATER_BOTTLE_STACKSIZE.get();

        ThirstConfig.FILTER_BASE_SPEED = FILTER_BASE_SPEED.get();
        ThirstConfig.DIRTY_WATER_DECAY_MULTIPLIER = DIRTY_WATER_DECAY_MULTIPLIER.get().floatValue();
        ThirstConfig.SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER.get().floatValue();
        ThirstConfig.ACCEPTABLE_WATER_DECAY_MULTIPLIER = ACCEPTABLE_WATER_DECAY_MULTIPLIER.get().floatValue();
        ThirstConfig.FABRIC_FILTER_DURABILITY = FABRIC_FILTER_DURABILITY.get();
        ThirstConfig.FABRIC_FILTER_MAX_PURITY = FABRIC_FILTER_MAX_PURITY.get();
        ThirstConfig.SAND_FILTER_DURABILITY = SAND_FILTER_DURABILITY.get();
        ThirstConfig.SAND_FILTER_MAX_PURITY = SAND_FILTER_MAX_PURITY.get();
        ThirstConfig.CARBON_FILTER_DURABILITY = CARBON_FILTER_DURABILITY.get();
        ThirstConfig.CARBON_FILTER_MAX_PURITY = CARBON_FILTER_MAX_PURITY.get();

        ThirstConfig.WATER_BOILER_CAPACITY = WATER_BOILER_CAPACITY.get();
        ThirstConfig.WATER_BOILER_BOIL_TIME = WATER_BOILER_BOIL_TIME.get();
        ThirstConfig.WATER_BOILER_ENERGY_CONSUMPTION = WATER_BOILER_ENERGY_CONSUMPTION.get();
    }

    static void reloadThirstValues() {
        if (!SPEC.isLoaded()) {
            Constants.LOG.warn("Tried to load thirst values before config was loaded");
            return;
        }

        Map<Item, ThirstValue> drinks = parseItemValues(DRINK_VALUES.get());
        Map<Item, ThirstValue> foods = parseItemValues(FOOD_VALUES.get());
        Set<Item> blacklist = parseItems(ITEMS_BLACKLIST.get());

        if (dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL != null) {
            Item t = dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL.get();
            if (t != null && t != Items.AIR) drinks.putIfAbsent(t, new ThirstValue(5, 7));
        }
        if (dev.minhnh.yetanotherthirst.core.item.ModItems.WOODEN_WATER_BOWL != null) {
            Item w = dev.minhnh.yetanotherthirst.core.item.ModItems.WOODEN_WATER_BOWL.get();
            if (w != null && w != Items.AIR) drinks.putIfAbsent(w, new ThirstValue(5, 7));
        }

        ThirstValues.replaceConfiguredValues(drinks, foods, blacklist);
    }

    private static boolean validItemValueEntry(Object value) {
        if (!(value instanceof List<?> entry) || entry.size() < 3) {
            return false;
        }
        return entry.get(0) instanceof String && entry.get(1) instanceof Number && entry.get(2) instanceof Number;
    }

    private static Map<Item, ThirstValue> parseItemValues(List<? extends List<?>> entries) {
        Map<Item, ThirstValue> values = new LinkedHashMap<>();
        for (List<?> entry : entries) {
            if (!validItemValueEntry(entry)) continue;
            String id = (String) entry.get(0);
            ThirstValue value = new ThirstValue(((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).intValue());
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag ->
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(h -> values.put(h.value(), value)));
            } else {
                resolveItem(id).ifPresent(item -> values.put(item, value));
            }
        }
        return values;
    }

    private static Set<Item> parseItems(List<? extends String> entries) {
        Set<Item> items = new HashSet<>();
        for (String id : entries) {
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag ->
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(h -> items.add(h.value())));
            } else {
                resolveItem(id).ifPresent(items::add);
            }
        }
        return items;
    }

    private static Optional<Item> resolveItem(String id) {
        try {
            Identifier location = Identifier.parse(id);
            return BuiltInRegistries.ITEM.get(location)
                    .map(net.minecraft.core.Holder.Reference::value)
                    .filter(item -> item != Items.AIR || "minecraft:air".equals(id));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<TagKey<Item>> resolveTag(String id) {
        try {
            return Optional.of(TagKey.create(Registries.ITEM, Identifier.parse(id)));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private NeoForgeConfigItems() {}
}
