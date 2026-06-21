package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public final class ForgeConfigCommon {

    static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.IntValue CONFIG_VERSION;
    // [general]
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
    // [effects]
    private static final ForgeConfigSpec.IntValue HYDRATION_EFFECT_THIRST_PER_TICK;
    private static final ForgeConfigSpec.IntValue HYDRATION_EFFECT_QUENCHED_PER_TICK;
    private static final ForgeConfigSpec.DoubleValue THIRSTY_EFFECT_EXHAUSTION_PER_TICK;
    // [dehydration]
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE;
    private static final ForgeConfigSpec.IntValue DAMAGE_INTERVAL_TICKS;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_EASY_LIMIT;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_NORMAL_LIMIT;
    private static final ForgeConfigSpec.DoubleValue DEHYDRATION_DAMAGE_HARD_LIMIT;
    // [depletion]
    private static final ForgeConfigSpec.DoubleValue THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue NETHER_THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue FIRE_RESISTANCE_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue BIOME_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue FIRE_PROTECTION_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue ENVIRONMENT_MODIFIER_HARSHNESS;
    // [purity]
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
    // [world]
    private static final ForgeConfigSpec.IntValue MOUNTAINS_Y;
    private static final ForgeConfigSpec.IntValue CAVES_Y;
    private static final ForgeConfigSpec.IntValue RUNNING_WATER_PURIFICATION_AMOUNT;
    // [handDrinking]
    private static final ForgeConfigSpec.BooleanValue CAN_DRINK_BY_HAND;
    private static final ForgeConfigSpec.IntValue HAND_DRINKING_THIRST;
    private static final ForgeConfigSpec.IntValue HAND_DRINKING_QUENCHED;
    private static final ForgeConfigSpec.BooleanValue DRINK_BOTH_HANDS_NEEDED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CONFIG_VERSION = builder
                .comment("Config file version — do not modify")
                .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);

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
                        "Format: 'effect_id' or 'effect_id operator amplifier'")
                .defineList("pauseDepletionEffects", List.of(
                        "farmersdelight:nourishment",
                        "bakery:stuffed",
                        "farm_and_charm:satiation",
                        "farm_and_charm:sustenance",
                        "farm_and_charm:feast"
                ), value -> value instanceof String);
        REGEN_THIRST_EFFECTS = builder
                .comment("List of effects that regenerate thirst.",
                        "Format: 'effect_id' or 'effect_id operator amplifier'")
                .defineList("regenThirstEffects", List.of("farmersdelight:nourishment", "farm_and_charm:sustenance"), value -> value instanceof String);
        REGEN_THIRST_INTERVAL = builder
                .comment("Interval in ticks between thirst regeneration ticks when a regen effect is active")
                .defineInRange("regenThirstInterval", 40, 1, 72000);
        REGEN_THIRST_AMOUNT = builder
                .comment("Amount of thirst restored per regeneration tick")
                .defineInRange("regenThirstAmount", 1, 1, 20);
        builder.pop();

        builder.push("effects");
        HYDRATION_EFFECT_THIRST_PER_TICK = builder
                .comment("Base thirst restored by Hydration each tick per effect level")
                .defineInRange("hydrationEffectThirstPerTick", 1, 0, 20);
        HYDRATION_EFFECT_QUENCHED_PER_TICK = builder
                .comment("Base quenched restored by Hydration each tick per effect level")
                .defineInRange("hydrationEffectQuenchedPerTick", 1, 0, 20);
        THIRSTY_EFFECT_EXHAUSTION_PER_TICK = builder
                .comment("Base thirst exhaustion added by Thirsty each tick per effect level")
                .defineInRange("thirstyEffectExhaustionPerTick", 0.005, 0.0, 4.0);
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
                .comment("Chance (0.0-1.0) of getting Nausea+Hunger+Thirsty after drinking dirty water")
                .defineInRange("dirtyNauseaChance", 1.0, 0.0, 1.0);
        DIRTY_POISON_CHANCE = builder
                .comment("Chance (0.0-1.0) of getting Poison after drinking dirty water")
                .defineInRange("dirtyPoisonChance", 0.3, 0.0, 1.0);
        SLIGHTLY_DIRTY_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger+Thirsty from slightly dirty water")
                .defineInRange("slightlyDirtyNauseaChance", 0.5, 0.0, 1.0);
        SLIGHTLY_DIRTY_POISON_CHANCE = builder
                .comment("Chance of Poison from slightly dirty water")
                .defineInRange("slightlyDirtyPoisonChance", 0.1, 0.0, 1.0);
        ACCEPTABLE_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger+Thirsty from acceptable water")
                .defineInRange("acceptableNauseaChance", 0.05, 0.0, 1.0);
        ACCEPTABLE_POISON_CHANCE = builder
                .comment("Chance of Poison from acceptable water")
                .defineInRange("acceptablePoisonChance", 0.0, 0.0, 1.0);
        PURIFIED_NAUSEA_CHANCE = builder
                .comment("Chance of Nausea+Hunger+Thirsty from purified water")
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
        ThirstConfig.HYDRATION_EFFECT_THIRST_PER_TICK = HYDRATION_EFFECT_THIRST_PER_TICK.get();
        ThirstConfig.HYDRATION_EFFECT_QUENCHED_PER_TICK = HYDRATION_EFFECT_QUENCHED_PER_TICK.get();
        ThirstConfig.THIRSTY_EFFECT_EXHAUSTION_PER_TICK = THIRSTY_EFFECT_EXHAUSTION_PER_TICK.get().floatValue();
        ThirstConfig.DEHYDRATION_DAMAGE = DEHYDRATION_DAMAGE.get().floatValue();
        ThirstConfig.DAMAGE_INTERVAL_TICKS = DAMAGE_INTERVAL_TICKS.get();
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
    }

    private ForgeConfigCommon() {}
}
