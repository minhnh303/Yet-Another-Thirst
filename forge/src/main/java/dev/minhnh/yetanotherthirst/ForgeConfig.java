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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ForgeConfig {

    static final ForgeConfigSpec SPEC;

    // General
    private static final ForgeConfigSpec.BooleanValue EXTRA_HYDRATION_CONVERTS_TO_QUENCHED;
    private static final ForgeConfigSpec.BooleanValue THIRST_DEPLETES_IN_PEACEFUL;
    private static final ForgeConfigSpec.BooleanValue DRINK_RAIN_WATER;
    private static final ForgeConfigSpec.BooleanValue DEPLETES_WHEN_NAUSEA;
    private static final ForgeConfigSpec.BooleanValue SPRINT_PREVENTION;
    private static final ForgeConfigSpec.IntValue SPRINT_THRESHOLD;
    private static final ForgeConfigSpec.BooleanValue DEHYDRATION_HALTS_HEALTH_REGEN;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> SUSPEND_THIRST_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> PAUSE_DEPLETION_EFFECTS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> REGEN_THIRST_EFFECTS;
    private static final ForgeConfigSpec.IntValue REGEN_THIRST_INTERVAL;
    private static final ForgeConfigSpec.IntValue REGEN_THIRST_AMOUNT;

    // Dehydration
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE;
    private static final ForgeConfigSpec.IntValue DAMAGE_INTERVAL_TICKS;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_EASY_LIMIT;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_NORMAL_LIMIT;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_HARD_LIMIT;

    // Depletion
    private static final ForgeConfigSpec.DoubleValue THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue NETHER_THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue FIRE_RESISTANCE_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue BIOME_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue FIRE_PROTECTION_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue ENVIRONMENT_MODIFIER_HARSHNESS;

    // Purity effects
    private static final ForgeConfigSpec.IntValue DEFAULT_PURITY;
    private static final ForgeConfigSpec.BooleanValue QUENCH_WHEN_DEBUFFED;
    private static final ForgeConfigSpec.DoubleValue DIRTY_NAUSEA_CHANCE;
    private static final ForgeConfigSpec.DoubleValue DIRTY_POISON_CHANCE;
    private static final ForgeConfigSpec.DoubleValue SLIGHTLY_DIRTY_NAUSEA_CHANCE;
    private static final ForgeConfigSpec.DoubleValue SLIGHTLY_DIRTY_POISON_CHANCE;
    private static final ForgeConfigSpec.DoubleValue ACCEPTABLE_NAUSEA_CHANCE;
    private static final ForgeConfigSpec.DoubleValue ACCEPTABLE_POISON_CHANCE;
    private static final ForgeConfigSpec.DoubleValue PURIFIED_NAUSEA_CHANCE;
    private static final ForgeConfigSpec.DoubleValue PURIFIED_POISON_CHANCE;

    // World purity
    private static final ForgeConfigSpec.IntValue MOUNTAINS_Y;
    private static final ForgeConfigSpec.IntValue CAVES_Y;
    private static final ForgeConfigSpec.IntValue RUNNING_WATER_PURIFICATION_AMOUNT;

    // Items
    private static final ForgeConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> DRINK_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_VALUES;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEMS_BLACKLIST;

    // Hand drinking
    private static final ForgeConfigSpec.BooleanValue CAN_DRINK_BY_HAND;
    private static final ForgeConfigSpec.IntValue HAND_DRINKING_THIRST;
    private static final ForgeConfigSpec.IntValue HAND_DRINKING_QUENCHED;
    private static final ForgeConfigSpec.BooleanValue DRINK_BOTH_HANDS_NEEDED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = builder
                .comment("Convert overflow hydration into quenched bonus")
                .define("extraHydrationConvertsToQuenched", true);
        THIRST_DEPLETES_IN_PEACEFUL = builder
                .comment("Allow thirst to deplete in Peaceful difficulty")
                .define("thirstDepletesInPeaceful", false);
        DRINK_RAIN_WATER = builder
                .comment("Allow players to drink rain water by looking up while it rains")
                .define("drinkRainWater", true);
        DEPLETES_WHEN_NAUSEA = builder
                .comment("Deplete thirst faster while the player has the Nausea effect")
                .define("depletesWhenNausea", true);
        SPRINT_PREVENTION = builder
                .comment("Prevent sprinting when thirst is at or below the sprint threshold")
                .define("sprintPrevention", true);
        SPRINT_THRESHOLD = builder
                .comment("Thirst value (0-20) at or below which sprinting is blocked (6 = 3 droplets)")
                .defineInRange("sprintThreshold", 6, 0, 20);
        DEHYDRATION_HALTS_HEALTH_REGEN = builder
                .comment("Prevent natural health regeneration while the thirst bar is not full")
                .define("dehydrationHaltsHealthRegen", true);
        SUSPEND_THIRST_EFFECTS = builder
                .comment("List of effects that completely suspend all thirst mechanics.",
                        "While active, the thirst level remains frozen, the player is immune to dehydration damage,",
                        "and sprinting is not blocked by low thirst.",
                        "Format: 'effect_id' or 'effect_id operator amplifier'",
                        "  - 'effect_id': Matches the effect at any level (e.g. 'minecraft:invisibility')",
                        "  - 'effect_id operator amplifier': Matches based on effect level (0-indexed, i.e., 0 = level I, 1 = level II, etc.)",
                        "    Supported operators: >=, <=, ==, >, <, =",
                        "    Example: 'minecraft:speed >= 1' (matches Speed II or higher)")
                .defineList("suspendThirstEffects", List.of("tombstone:ghostly_shape"), value -> value instanceof String);
        PAUSE_DEPLETION_EFFECTS = builder
                .comment("List of effects that only pause thirst depletion (exhaustion from movement, etc.).",
                        "Unlike suspend, the thirst system remains active: player can still drink, receive regeneration,",
                        "sprint can still be blocked, and dehydration damage is still dealt if thirst is already at 0.",
                        "Format: 'effect_id' or 'effect_id operator amplifier'",
                        "  - 'effect_id': Matches the effect at any level (e.g. 'farmersdelight:nourishment')",
                        "  - 'effect_id operator amplifier': Matches based on effect level (0-indexed, i.e., 0 = level I, 1 = level II, etc.)",
                        "    Supported operators: >=, <=, ==, >, <, =",
                        "    Example: 'farmersdelight:nourishment >= 0'")
                .defineList("pauseDepletionEffects", List.of(
                        "farmersdelight:nourishment",
                        "bakery:stuffed",
                        "farm_and_charm:satiation",
                        "farm_and_charm:sustenance",
                        "farm_and_charm:feast"
                ), value -> value instanceof String);
        REGEN_THIRST_EFFECTS = builder
                .comment("List of effects that regenerate thirst.",
                        "Format: 'effect_id' or 'effect_id operator amplifier'",
                        "  - 'effect_id': Matches the effect at any level (e.g. 'farmersdelight:nourishment')",
                        "  - 'effect_id operator amplifier': Matches based on effect level (0-indexed, i.e., 0 = level I, 1 = level II, etc.)",
                        "    Supported operators: >=, <=, ==, >, <, =",
                        "    Example: 'minecraft:regeneration >= 0'")
                .defineList("regenThirstEffects", List.of("farmersdelight:nourishment", "farm_and_charm:sustenance"), value -> value instanceof String);
        REGEN_THIRST_INTERVAL = builder
                .comment("Interval in ticks between thirst regeneration ticks when a regen effect is active")
                .defineInRange("regenThirstInterval", 40, 1, 72000);
        REGEN_THIRST_AMOUNT = builder
                .comment("Amount of thirst restored per regeneration tick")
                .defineInRange("regenThirstAmount", 1, 1, 20);
        builder.pop();

        builder.push("dehydration");
        DEHYDRATION_DAMAGE = builder
                .comment("Amount of damage dealt when dehydrated")
                .defineInRange("dehydrationDamage", 1.0, 0.0, 100.0);
        DAMAGE_INTERVAL_TICKS = builder
                .comment("Interval in ticks between dehydration damage ticks")
                .defineInRange("damageIntervalTicks", 40, 1, 72000);
        DEHYDRATION_DAMAGE_EASY_LIMIT = builder
                .comment("Minimum health remaining on Easy difficulty when taking dehydration damage")
                .defineInRange("dehydrationDamageEasyLimit", 10.0, 0.0, 20.0);
        DEHYDRATION_DAMAGE_NORMAL_LIMIT = builder
                .comment("Minimum health remaining on Normal difficulty when taking dehydration damage")
                .defineInRange("dehydrationDamageNormalLimit", 0.0, 0.0, 20.0);
        DEHYDRATION_DAMAGE_HARD_LIMIT = builder
                .comment("Minimum health remaining on Hard difficulty when taking dehydration damage")
                .defineInRange("dehydrationDamageHardLimit", 0.0, 0.0, 20.0);
        builder.pop();

        builder.push("depletion");
        THIRST_DEPLETION_MODIFIER = builder
                .comment("Thirst depletion speed relative to hunger (1.0 = same speed)")
                .defineInRange("thirstDepletionModifier", 1.2, 0.0, 10.0);
        NETHER_THIRST_DEPLETION_MODIFIER = builder
                .comment("Thirst depletion speed multiplier in ultra-warm dimensions (Nether)")
                .defineInRange("netherThirstDepletionModifier", 3.0, 1.0, 10.0);
        FIRE_RESISTANCE_DEHYDRATION_MODIFIER = builder
                .comment("Depletion speed multiplier when Fire Resistance is active (0.0 = no depletion)")
                .defineInRange("fireResistanceDehydrationModifier", 0.0, 0.0, 1.0);
        BIOME_DEHYDRATION_MODIFIER = builder
                .comment("Use biome temperature and humidity to adjust thirst depletion outside ultra-warm dimensions")
                .define("biomeDehydrationModifier", true);
        FIRE_PROTECTION_DEHYDRATION_MODIFIER = builder
                .comment("Reduce thirst depletion based on armor Fire Protection enchantment levels")
                .define("fireProtectionDehydrationModifier", true);
        ENVIRONMENT_MODIFIER_HARSHNESS = builder
                .comment("How strongly cool or humid biomes reduce dehydration when biome modifiers are enabled")
                .defineInRange("environmentModifierHarshness", 0.5, 0.0, 1.0);
        builder.pop();

        builder.push("purity");
        DEFAULT_PURITY = builder
                .comment("Default purity for water items that lack a Purity NBT tag (0=dirty, 1=slightly dirty, 2=acceptable, 3=purified)")
                .defineInRange("defaultPurity", 2, 0, 3);
        QUENCH_WHEN_DEBUFFED = builder
                .comment("Grant hydration even if the player received a purity-related debuff")
                .define("quenchWhenDebuffed", true);
        DIRTY_NAUSEA_CHANCE = builder
                .comment("Chance (0.0-1.0) of getting Nausea+Hunger after drinking dirty water")
                .defineInRange("dirtyNauseaChance", 1.0, 0.0, 1.0);
        DIRTY_POISON_CHANCE = builder
                .comment("Chance (0.0-1.0) of getting Poison after drinking dirty water")
                .defineInRange("dirtyPoisonChance", 0.3, 0.0, 1.0);
        SLIGHTLY_DIRTY_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger from slightly dirty water")
                .defineInRange("slightlyDirtyNauseaChance", 0.5, 0.0, 1.0);
        SLIGHTLY_DIRTY_POISON_CHANCE = builder
                .comment("Chance of Poison from slightly dirty water")
                .defineInRange("slightlyDirtyPoisonChance", 0.1, 0.0, 1.0);
        ACCEPTABLE_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger from acceptable water")
                .defineInRange("acceptableNauseaChance", 0.05, 0.0, 1.0);
        ACCEPTABLE_POISON_CHANCE = builder
                .comment("Chance of Poison from acceptable water")
                .defineInRange("acceptablePoisonChance", 0.0, 0.0, 1.0);
        PURIFIED_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger from purified water")
                .defineInRange("purifiedNauseaChance", 0.0, 0.0, 1.0);
        PURIFIED_POISON_CHANCE = builder
                .comment("Chance of Poison from purified water")
                .defineInRange("purifiedPoisonChance", 0.0, 0.0, 1.0);
        builder.pop();

        builder.push("world");
        MOUNTAINS_Y = builder
                .comment("Y-level above which water gains +1 purity (mountain spring water)")
                .defineInRange("mountainsY", 100, -64, 320);
        CAVES_Y = builder
                .comment("Y-level below which water gains +1 purity (cave spring water)")
                .defineInRange("cavesY", 48, -64, 320);
        RUNNING_WATER_PURIFICATION_AMOUNT = builder
                .comment("Purity levels added to non-source running water")
                .defineInRange("runningWaterPurificationAmount", 1, 0, 3);
        builder.pop();

        builder.push("items");
        WATER_BOTTLE_STACKSIZE = builder
                .comment("Maximum stack size for water bottles (1-64)")
                .defineInRange("waterBottleStackSize", 64, 1, 64);
        DRINK_VALUES = builder
                .comment("Items that restore thirst when drunk. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("drinks", defaultDrinkValues(), ForgeConfig::validItemValueEntry);
        FOOD_VALUES = builder
                .comment("Items that restore thirst when eaten. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("foods", defaultFoodValues(), ForgeConfig::validItemValueEntry);
        ITEMS_BLACKLIST = builder
                .comment("Items excluded from thirst restoration even if present in drink or food values")
                .defineList("itemsBlacklist", List.of("yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2"), value -> value instanceof String);
        builder.pop();

        builder.push("handDrinking");
        CAN_DRINK_BY_HAND = builder
                .comment("Allow players to drink by shift-right-clicking water with an empty hand")
                .define("canDrinkByHand", false);
        HAND_DRINKING_THIRST = builder
                .comment("Thirst restored when drinking by hand")
                .defineInRange("handDrinkingThirst", 3, 0, 20);
        HAND_DRINKING_QUENCHED = builder
                .comment("Quenched bonus restored when drinking by hand")
                .defineInRange("handDrinkingQuenched", 2, 0, 20);
        DRINK_BOTH_HANDS_NEEDED = builder
                .comment("Require both hands to be empty to drink by hand")
                .define("drinkBothHandsNeeded", false);
        builder.pop();

        SPEC = builder.build();
    }

    static void sync() {

        ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = EXTRA_HYDRATION_CONVERTS_TO_QUENCHED.get();
        ThirstConfig.THIRST_DEPLETES_IN_PEACEFUL = THIRST_DEPLETES_IN_PEACEFUL.get();
        ThirstConfig.DRINK_RAIN_WATER = DRINK_RAIN_WATER.get();
        ThirstConfig.DEPLETES_WHEN_NAUSEA = DEPLETES_WHEN_NAUSEA.get();
        ThirstConfig.SPRINT_PREVENTION = SPRINT_PREVENTION.get();
        ThirstConfig.SPRINT_THRESHOLD = SPRINT_THRESHOLD.get();
        ThirstConfig.DEHYDRATION_HALTS_HEALTH_REGEN = DEHYDRATION_HALTS_HEALTH_REGEN.get();
        ThirstConfig.setSuspendThirstEffects(SUSPEND_THIRST_EFFECTS.get());
        ThirstConfig.setPauseDepletionEffects(PAUSE_DEPLETION_EFFECTS.get());
        ThirstConfig.setRegenThirstEffects(REGEN_THIRST_EFFECTS.get());
        ThirstConfig.REGEN_THIRST_INTERVAL = REGEN_THIRST_INTERVAL.get();
        ThirstConfig.REGEN_THIRST_AMOUNT = REGEN_THIRST_AMOUNT.get();
        ThirstConfig.DAMAGE_INTERVAL_TICKS = DAMAGE_INTERVAL_TICKS.get();
        ThirstConfig.DEHYDRATION_DAMAGE = DEHYDRATION_DAMAGE.get().floatValue();
        ThirstConfig.DEHYDRATION_DAMAGE_EASY_LIMIT = DEHYDRATION_DAMAGE_EASY_LIMIT.get().floatValue();
        ThirstConfig.DEHYDRATION_DAMAGE_NORMAL_LIMIT = DEHYDRATION_DAMAGE_NORMAL_LIMIT.get().floatValue();
        ThirstConfig.DEHYDRATION_DAMAGE_HARD_LIMIT = DEHYDRATION_DAMAGE_HARD_LIMIT.get().floatValue();
        ThirstConfig.THIRST_DEPLETION_MODIFIER = THIRST_DEPLETION_MODIFIER.get().floatValue();
        ThirstConfig.NETHER_THIRST_DEPLETION_MODIFIER = NETHER_THIRST_DEPLETION_MODIFIER.get().floatValue();
        ThirstConfig.FIRE_RESISTANCE_DEHYDRATION_MODIFIER = FIRE_RESISTANCE_DEHYDRATION_MODIFIER.get().floatValue();
        ThirstConfig.BIOME_DEHYDRATION_MODIFIER = BIOME_DEHYDRATION_MODIFIER.get();
        ThirstConfig.FIRE_PROTECTION_DEHYDRATION_MODIFIER = FIRE_PROTECTION_DEHYDRATION_MODIFIER.get();
        ThirstConfig.ENVIRONMENT_MODIFIER_HARSHNESS = ENVIRONMENT_MODIFIER_HARSHNESS.get().floatValue();
        ThirstConfig.DEFAULT_PURITY = DEFAULT_PURITY.get();
        ThirstConfig.QUENCH_WHEN_DEBUFFED = QUENCH_WHEN_DEBUFFED.get();
        ThirstConfig.DIRTY_NAUSEA_CHANCE = DIRTY_NAUSEA_CHANCE.get().floatValue();
        ThirstConfig.DIRTY_POISON_CHANCE = DIRTY_POISON_CHANCE.get().floatValue();
        ThirstConfig.SLIGHTLY_DIRTY_NAUSEA_CHANCE = SLIGHTLY_DIRTY_NAUSEA_CHANCE.get().floatValue();
        ThirstConfig.SLIGHTLY_DIRTY_POISON_CHANCE = SLIGHTLY_DIRTY_POISON_CHANCE.get().floatValue();
        ThirstConfig.ACCEPTABLE_NAUSEA_CHANCE = ACCEPTABLE_NAUSEA_CHANCE.get().floatValue();
        ThirstConfig.ACCEPTABLE_POISON_CHANCE = ACCEPTABLE_POISON_CHANCE.get().floatValue();
        ThirstConfig.PURIFIED_NAUSEA_CHANCE = PURIFIED_NAUSEA_CHANCE.get().floatValue();
        ThirstConfig.PURIFIED_POISON_CHANCE = PURIFIED_POISON_CHANCE.get().floatValue();
        ThirstConfig.MOUNTAINS_Y = MOUNTAINS_Y.get();
        ThirstConfig.CAVES_Y = CAVES_Y.get();
        ThirstConfig.RUNNING_WATER_PURIFICATION_AMOUNT = RUNNING_WATER_PURIFICATION_AMOUNT.get();
        ThirstConfig.CAN_DRINK_BY_HAND = CAN_DRINK_BY_HAND.get();
        ThirstConfig.HAND_DRINKING_THIRST = HAND_DRINKING_THIRST.get();
        ThirstConfig.HAND_DRINKING_QUENCHED = HAND_DRINKING_QUENCHED.get();
        ThirstConfig.DRINK_BOTH_HANDS_NEEDED = DRINK_BOTH_HANDS_NEEDED.get();
        ThirstConfig.WATER_BOTTLE_STACKSIZE = WATER_BOTTLE_STACKSIZE.get();
    }

    static void reloadThirstValues() {

        ThirstValues.replaceConfiguredValues(
                parseItemValues(DRINK_VALUES.get()),
                parseItemValues(FOOD_VALUES.get()),
                parseItems(ITEMS_BLACKLIST.get()));
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
        } catch (Exception ignored) {
        }
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
        addValue(values, "collectorsreap:lime_slice", 1, 2);
        addValue(values, "collectorsreap:lime", 2, 3);
        addValue(values, "collectorsreap:portobello_rice_soup", 6, 8);
        addValue(values, "collectorsreap:lime_popsicle", 7, 9);
        return values;
    }

    private static void addValue(List<List<?>> values, String id, int thirst, int quenched) {

        values.add(List.of(id, thirst, quenched));
    }

    private ForgeConfig() {}
}
