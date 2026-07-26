package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class NeoForgeConfigItems {

    static final ForgeConfigSpec SPEC;

    @SuppressWarnings("unused")
    private static final ForgeConfigSpec.IntValue CONFIG_VERSION;
    // [items]
    private static final ForgeConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DRINK_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEMS_BLACKLIST;
    // [filter]
    private static final ForgeConfigSpec.IntValue FILTER_BASE_SPEED;
    private static final ForgeConfigSpec.DoubleValue DIRTY_WATER_DECAY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER;
    private static final ForgeConfigSpec.DoubleValue ACCEPTABLE_WATER_DECAY_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue FABRIC_FILTER_DURABILITY;
    private static final ForgeConfigSpec.IntValue FABRIC_FILTER_MAX_PURITY;
    private static final ForgeConfigSpec.IntValue SAND_FILTER_DURABILITY;
    private static final ForgeConfigSpec.IntValue SAND_FILTER_MAX_PURITY;
    private static final ForgeConfigSpec.IntValue CARBON_FILTER_DURABILITY;
    private static final ForgeConfigSpec.IntValue CARBON_FILTER_MAX_PURITY;
    // [boiler]
    private static final ForgeConfigSpec.IntValue WATER_BOILER_CAPACITY;
    private static final ForgeConfigSpec.IntValue WATER_BOILER_BOIL_TIME;
    private static final ForgeConfigSpec.IntValue WATER_BOILER_ENERGY_CONSUMPTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

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
                .comment("Items excluded from thirst restoration even if present in drink or food values")
                .defineList("itemsBlacklist", List.of("yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2"), value -> value instanceof String);
        builder.pop();

        builder.push("filter");
        FILTER_BASE_SPEED = builder
                .comment("Base throughput speed of the Sand Filter Frame block in mB/tick (default: 1)")
                .defineInRange("filterBaseSpeed", 1, 1, 1000);
        DIRTY_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability consumption multiplier for dirty water (default: 4.0)")
                .defineInRange("dirtyWaterDecayMultiplier", 4.0, 0.0, 100.0);
        SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability consumption multiplier for slightly dirty water (default: 2.0)")
                .defineInRange("slightlyDirtyWaterDecayMultiplier", 2.0, 0.0, 100.0);
        ACCEPTABLE_WATER_DECAY_MULTIPLIER = builder
                .comment("Durability consumption multiplier for acceptable water (default: 1.0)")
                .defineInRange("acceptableWaterDecayMultiplier", 1.0, 0.0, 100.0);
        FABRIC_FILTER_DURABILITY = builder
                .comment("Durability of Fabric Filter Core in mB (default: 10000)")
                .defineInRange("fabricFilterDurability", 10000, 1, 10000000);
        FABRIC_FILTER_MAX_PURITY = builder
                .comment("Maximum water purity output allowed by Fabric Filter Core (0=dirty, 1=slightly dirty, 2=acceptable, 3=purified) (default: 1)")
                .defineInRange("fabricFilterMaxPurity", 1, 0, 3);
        SAND_FILTER_DURABILITY = builder
                .comment("Durability of Sand Filter Core in mB (default: 50000)")
                .defineInRange("sandFilterDurability", 50000, 1, 10000000);
        SAND_FILTER_MAX_PURITY = builder
                .comment("Maximum water purity output allowed by Sand Filter Core (0=dirty, 1=slightly dirty, 2=acceptable, 3=purified) (default: 2)")
                .defineInRange("sandFilterMaxPurity", 2, 0, 3);
        CARBON_FILTER_DURABILITY = builder
                .comment("Durability of Carbon Filter Core in mB (default: 250000)")
                .defineInRange("carbonFilterDurability", 250000, 1, 10000000);
        CARBON_FILTER_MAX_PURITY = builder
                .comment("Maximum water purity output allowed by Carbon Filter Core (0=dirty, 1=slightly dirty, 2=acceptable, 3=purified) (default: 3)")
                .defineInRange("carbonFilterMaxPurity", 3, 0, 3);
        builder.pop();

        builder.push("boiler");
        WATER_BOILER_CAPACITY = builder
                .comment("Maximum water capacity of the Water Boiler block in mB (default: 2000)")
                .defineInRange("waterBoilerCapacity", 2000, 1000, 1000000);
        WATER_BOILER_BOIL_TIME = builder
                .comment("Ticks required to boil water (default: 200)")
                .defineInRange("waterBoilerBoilTime", 200, 1, 72000);
        WATER_BOILER_ENERGY_CONSUMPTION = builder
                .comment("Energy consumption rate in FE/tick when powered by electricity (default: 32)")
                .defineInRange("waterBoilerEnergyConsumption", 32, 1, 100000);
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
        Map<Item, ThirstValue> drinks = parseItemValues(DRINK_VALUES.get());
        Map<Item, ThirstValue> foods = parseItemValues(FOOD_VALUES.get());
        Set<Item> blacklist = parseItems(ITEMS_BLACKLIST.get());

        if (dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL != null) {
            Item terracotta = dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL.get();
            if (terracotta != null && terracotta != Items.AIR) {
                drinks.putIfAbsent(terracotta, new ThirstValue(5, 7));
            }
        }
        if (dev.minhnh.yetanotherthirst.core.item.ModItems.WOODEN_WATER_BOWL != null) {
            Item wooden = dev.minhnh.yetanotherthirst.core.item.ModItems.WOODEN_WATER_BOWL.get();
            if (wooden != null && wooden != Items.AIR) {
                drinks.putIfAbsent(wooden, new ThirstValue(5, 7));
            }
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
                resolveTag(id.substring(1)).ifPresent(tag -> tag.stream().forEach(item -> values.put(item, value)));
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
                resolveTag(id.substring(1)).ifPresent(tag -> tag.stream().forEach(items::add));
            } else {
                resolveItem(id).ifPresent(items::add);
            }
        }
        return items;
    }

    private static Optional<Item> resolveItem(String id) {
        try {
            ResourceLocation location = new ResourceLocation(id);
            Item item = ForgeRegistries.ITEMS.getValue(location);
            if (item != null && (item != Items.AIR || "minecraft:air".equals(id))) {
                return Optional.of(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<ITag<Item>> resolveTag(String id) {
        try {
            return ForgeRegistries.ITEMS.tags().stream()
                    .filter(tag -> tag.getKey().location().toString().equals(id))
                    .findFirst();
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private NeoForgeConfigItems() {}
}
