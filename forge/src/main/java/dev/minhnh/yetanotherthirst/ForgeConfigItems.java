package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ForgeConfigItems {

    static final ForgeConfigSpec SPEC;

    // Items
    private static final ForgeConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DRINK_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEMS_BLACKLIST;

    // Filter
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

    // Boiler
    private static final ForgeConfigSpec.IntValue WATER_BOILER_CAPACITY;
    private static final ForgeConfigSpec.IntValue WATER_BOILER_BOIL_TIME;
    private static final ForgeConfigSpec.IntValue WATER_BOILER_ENERGY_CONSUMPTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("items");
        WATER_BOTTLE_STACKSIZE = builder
                .comment("Maximum stack size for water bottles (1-64)")
                .defineInRange("waterBottleStackSize", 64, 1, 64);
        DRINK_VALUES = builder
                .comment("Items that restore thirst when drunk. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("drinks", defaultDrinkValues(), ForgeConfigItems::validItemValueEntry);
        FOOD_VALUES = builder
                .comment("Items that restore thirst when eaten. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("foods", defaultFoodValues(), ForgeConfigItems::validItemValueEntry);
        ITEMS_BLACKLIST = builder
                .comment("Items excluded from thirst restoration even if present in drink or food values")
                .defineList("itemsBlacklist", List.of("yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2"), value -> value instanceof String);
        builder.pop();

        builder.push("filter");
        FILTER_BASE_SPEED = builder
                .comment("Base throughput speed of the Filter Frame block in mB/tick (default: 1)")
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
            if (!validItemValueEntry(entry)) {
                continue;
            }
            String id = (String) entry.get(0);
            ThirstValue value = new ThirstValue(((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).intValue());
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag -> tag.forEach(item -> values.put(item, value)));
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
                resolveTag(id.substring(1)).ifPresent(tag -> tag.forEach(items::add));
            } else {
                resolveItem(id).ifPresent(items::add);
            }
        }
        return items;
    }

    private static Optional<Item> resolveItem(String id) {
        try {
            ResourceLocation location = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(location);
            if (item != null && (item != Items.AIR || "minecraft:air".equals(id))) {
                return Optional.of(item);
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    private static Optional<List<Item>> resolveTag(String id) {
        try {
            TagKey<Item> key = TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
            return BuiltInRegistries.ITEM.getTag(key)
                    .map(tag -> tag.stream().map(Holder::value).toList());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static List<List<?>> defaultDrinkValues() {
        List<List<?>> values = new ArrayList<>();
        addValue(values, "minecraft:potion", 6, 8);
        addValue(values, "minecraft:honey_bottle", 3, 4);
        addValue(values, "minecraft:milk_bucket", 2, 4);
        addValue(values, Constants.MOD_ID + ":terracotta_water_bowl", 5, 7);
        addValue(values, Constants.MOD_ID + ":wooden_water_bowl", 5, 7);
        addValue(values, "farmersrespite:green_tea", 10, 14);
        addValue(values, "farmersrespite:long_green_tea", 10, 14);
        addValue(values, "farmersrespite:strong_green_tea", 10, 14);
        addValue(values, "farmersrespite:yellow_tea", 10, 14);
        addValue(values, "farmersrespite:long_yellow_tea", 10, 14);
        addValue(values, "farmersrespite:strong_yellow_tea", 10, 14);
        addValue(values, "farmersrespite:black_tea", 10, 14);
        addValue(values, "farmersrespite:long_black_tea", 10, 14);
        addValue(values, "farmersrespite:strong_black_tea", 10, 14);
        addValue(values, "farmersrespite:rose_hip_tea", 12, 22);
        addValue(values, "farmersrespite:long_rose_hip_tea", 12, 22);
        addValue(values, "farmersrespite:strong_rose_hip_tea", 12, 22);
        addValue(values, "farmersrespite:dandelion_tea", 12, 22);
        addValue(values, "farmersrespite:long_dandelion_tea", 12, 22);
        addValue(values, "farmersrespite:strong_dandelion_tea", 12, 22);
        addValue(values, "farmersrespite:gamblers_tea", 6, 11);
        addValue(values, "farmersrespite:long_gamblers_tea", 6, 11);
        addValue(values, "farmersrespite:strong_gamblers_tea", 6, 11);
        addValue(values, "farmersrespite:coffee", 6, 11);
        addValue(values, "farmersrespite:long_coffee", 6, 11);
        addValue(values, "farmersrespite:strong_coffee", 6, 11);
        addValue(values, "farmersrespite:strong_melon_juice", 8, 13);
        addValue(values, "farmersrespite:strong_apple_cider", 8, 13);
        addValue(values, "create:builders_tea", 12, 22);
        addValue(values, "farmersdelight:apple_cider", 8, 13);
        addValue(values, "farmersdelight:melon_juice", 8, 13);
        addValue(values, "brewinandchewin:beer", 10, 14);
        addValue(values, "brewinandchewin:vodka", 10, 14);
        addValue(values, "brewinandchewin:rice_wine", 10, 14);
        addValue(values, "brewinandchewin:mead", 10, 14);
        addValue(values, "brewinandchewin:egg_grog", 10, 14);
        addValue(values, "brewinandchewin:glittering_grenadine", 10, 14);
        addValue(values, "brewinandchewin:bloody_mary", 12, 22);
        addValue(values, "brewinandchewin:salty_folly", 12, 22);
        addValue(values, "brewinandchewin:pale_jane", 12, 22);
        addValue(values, "brewinandchewin:saccharine_rum", 12, 22);
        addValue(values, "brewinandchewin:strongroot_ale", 12, 22);
        addValue(values, "brewinandchewin:dread_nog", 12, 22);
        addValue(values, "brewinandchewin:kombucha", 14, 22);
        addValue(values, "brewinandchewin:red_rum", 14, 22);
        addValue(values, "brewinandchewin:steel_toe_stout", 14, 22);
        addValue(values, "collectorsreap:pink_limeade", 8, 13);
        addValue(values, "collectorsreap:berry_limeade", 8, 13);
        addValue(values, "collectorsreap:limeade", 8, 13);
        addValue(values, "collectorsreap:pomegranate_black_tea", 10, 14);
        addValue(values, "collectorsreap:lime_green_tea", 10, 14);
        addValue(values, "toughasnails:dirty_water_bottle", 6, 8);
        addValue(values, "toughasnails:purified_water_bottle", 8, 10);
        addValue(values, "toughasnails:leather_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:leather_water_canteen", 9, 11);
        addValue(values, "toughasnails:leather_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:copper_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:copper_water_canteen", 9, 11);
        addValue(values, "toughasnails:copper_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:iron_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:iron_water_canteen", 9, 11);
        addValue(values, "toughasnails:iron_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:gold_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:gold_water_canteen", 9, 11);
        addValue(values, "toughasnails:gold_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:diamond_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:diamond_water_canteen", 9, 11);
        addValue(values, "toughasnails:diamond_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:netherite_dirty_water_canteen", 8, 10);
        addValue(values, "toughasnails:netherite_water_canteen", 9, 11);
        addValue(values, "toughasnails:netherite_purified_water_canteen", 10, 12);
        addValue(values, "toughasnails:melon_juice", 8, 13);
        addValue(values, "toughasnails:apple_juice", 8, 13);
        addValue(values, "toughasnails:cactus_juice", 8, 13);
        addValue(values, "toughasnails:carrot_juice", 8, 13);
        addValue(values, "toughasnails:glow_berry_juice", 8, 13);
        addValue(values, "toughasnails:chorus_fruit_juice", 8, 13);
        addValue(values, "toughasnails:suspicious_water_cup", 8, 13);
        addValue(values, "toughasnails:pumpkin_juice", 8, 13);
        addValue(values, "toughasnails:sweet_berry_juice", 8, 13);
        addValue(values, "toughasnails:ice_cream", 6, 12);
        addValue(values, "supernatural:blood_bottle", 6, 8);
        addValue(values, "farmersdelight:milk_bottle", 2, 4);
        addValue(values, "brewery:beer_barley", 10, 14);
        addValue(values, "brewery:beer_haley", 10, 14);
        addValue(values, "brewery:beer_hops", 10, 14);
        addValue(values, "brewery:beer_nettle", 10, 14);
        addValue(values, "brewery:beer_oat", 10, 14);
        addValue(values, "brewery:beer_wheat", 10, 14);
        addValue(values, "brewery:dark_brew", 10, 14);
        addValue(values, "brewery:whiskey_ak", 10, 14);
        addValue(values, "brewery:whiskey_carrasconlabel", 10, 14);
        addValue(values, "brewery:whiskey_cristelwalker", 10, 14);
        addValue(values, "brewery:whiskey_highland_hearth", 10, 14);
        addValue(values, "brewery:whiskey_jamesons_malt", 10, 14);
        addValue(values, "brewery:whiskey_jojannik", 10, 14);
        addValue(values, "brewery:whiskey_lilitusinglemalt", 10, 14);
        addValue(values, "brewery:whiskey_maggoallan", 10, 14);
        addValue(values, "brewery:whiskey_smokey_reverie", 10, 14);
        addValue(values, "farm_and_charm:nettle_tea", 10, 14);
        addValue(values, "farm_and_charm:nettle_tea_cup", 10, 14);
        addValue(values, "farm_and_charm:ribwort_tea", 10, 14);
        addValue(values, "farm_and_charm:ribwort_tea_cup", 10, 14);
        addValue(values, "farm_and_charm:strawberry_tea", 10, 14);
        addValue(values, "farm_and_charm:strawberry_tea_cup", 10, 14);
        addValue(values, "cold_sweat:filled_waterskin", 6, 8);
        return values;
    }

    private static List<List<?>> defaultFoodValues() {
        List<List<?>> values = new ArrayList<>();
        addValue(values, "minecraft:apple", 2, 3);
        addValue(values, "minecraft:golden_apple", 2, 3);
        addValue(values, "minecraft:enchanted_golden_apple", 2, 3);
        addValue(values, "minecraft:melon_slice", 4, 5);
        addValue(values, "minecraft:carrot", 1, 2);
        addValue(values, "minecraft:beetroot", 1, 2);
        addValue(values, "minecraft:sweet_berries", 1, 2);
        addValue(values, "minecraft:glow_berries", 1, 2);
        addValue(values, "minecraft:golden_carrot", 1, 2);
        addValue(values, "minecraft:chorus_fruit", 1, 2);
        addValue(values, "minecraft:mushroom_stew", 2, 3);
        addValue(values, "minecraft:rabbit_stew", 2, 3);
        addValue(values, "minecraft:beetroot_soup", 5, 7);
        addValue(values, "minecraft:suspicious_stew", 2, 3);
        addValue(values, "minecraft:cod", 1, 1);
        addValue(values, "minecraft:salmon", 1, 1);
        addValue(values, "minecraft:tropical_fish", 1, 2);
        addValue(values, "minecraft:cooked_cod", 1, 2);
        addValue(values, "minecraft:cooked_salmon", 1, 2);
        addValue(values, "minecraft:dried_kelp", 1, 1);
        addValue(values, "minecraft:bread", 1, 1);
        addValue(values, "minecraft:pumpkin_pie", 1, 2);
        addValue(values, "minecraft:cookie", 1, 1);
        addValue(values, "farmersdelight:pumpkin_slice", 2, 1);
        addValue(values, "farmersdelight:cabbage_leaf", 1, 2);
        addValue(values, "farmersdelight:melon_popsicle", 7, 9);
        addValue(values, "farmersdelight:fruit_salad", 6, 8);
        addValue(values, "farmersdelight:tomato_sauce", 4, 5);
        addValue(values, "farmersdelight:mixed_salad", 4, 5);
        addValue(values, "farmersdelight:beef_stew", 4, 5);
        addValue(values, "farmersdelight:chicken_soup", 4, 5);
        addValue(values, "farmersdelight:vegetable_soup", 4, 5);
        addValue(values, "farmersdelight:fish_stew", 4, 5);
        addValue(values, "farmersdelight:pumpkin_soup", 4, 5);
        addValue(values, "farmersdelight:baked_cod_stew", 4, 5);
        addValue(values, "farmersdelight:noodle_soup", 4, 5);
        addValue(values, "farmersdelight:bone_broth", 4, 5);
        addValue(values, "farmersdelight:onion_soup", 4, 5);
        addValue(values, "farmersdelight:gleaming_salad", 4, 5);
        addValue(values, "farmersdelight:nether_salad", 2, 3);
        addValue(values, "farmersdelight:glow_berry_custard", 3, 4);
        addValue(values, "farmersdelight:apple_pie_slice", 1, 2);
        addValue(values, "farmersdelight:sweet_berry_cheesecake_slice", 1, 2);
        addValue(values, "farmersdelight:chocolate_pie_slice", 1, 1);
        addValue(values, "brewery:potato_salad", 2, 3);
        addValue(values, "bakery:strawberry_cake_slice", 1, 2);
        addValue(values, "bakery:sweetberry_cake_slice", 1, 2);
        addValue(values, "bakery:apple_pie_slice", 1, 2);
        addValue(values, "bakery:glowberry_pie_slice", 1, 2);
        addValue(values, "bakery:chocolate_tart_slice", 1, 1);
        addValue(values, "bakery:linzer_tart_slice", 1, 2);
        addValue(values, "bakery:pudding_slice", 2, 3);
        addValue(values, "bakery:bread_with_jam", 1, 2);
        addValue(values, "bakery:apple_cupcake", 1, 2);
        addValue(values, "bakery:strawberry_cupcake", 1, 2);
        addValue(values, "bakery:sweetberry_cupcake", 1, 2);
        addValue(values, "farm_and_charm:barley_soup", 4, 5);
        addValue(values, "farm_and_charm:onion_soup", 4, 5);
        addValue(values, "farm_and_charm:potato_soup", 4, 5);
        addValue(values, "farm_and_charm:simple_tomato_soup", 4, 5);
        addValue(values, "farm_and_charm:goulash", 4, 5);
        addValue(values, "farm_and_charm:oatmeal_with_strawberries", 3, 4);
        addValue(values, "farm_and_charm:farmer_salad", 4, 5);
        addValue(values, "farm_and_charm:strawberry", 1, 2);
        addValue(values, "farm_and_charm:tomato", 2, 3);
        addValue(values, "collectorsreap:lime_slice", 1, 2);
        addValue(values, "collectorsreap:lime", 2, 3);
        addValue(values, "collectorsreap:portobello_rice_soup", 6, 8);
        addValue(values, "collectorsreap:lime_popsicle", 7, 9);
        return values;
    }

    private static void addValue(List<List<?>> values, String id, int thirst, int quenched) {
        values.add(List.of(id, thirst, quenched));
    }

    private ForgeConfigItems() {}
}
