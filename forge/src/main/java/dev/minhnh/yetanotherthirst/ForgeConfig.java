package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.common.ForgeConfigSpec;

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

    // Depletion
    private static final ForgeConfigSpec.DoubleValue THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue NETHER_THIRST_DEPLETION_MODIFIER;
    private static final ForgeConfigSpec.DoubleValue FIRE_RESISTANCE_DEHYDRATION_MODIFIER;

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

    // Items
    private static final ForgeConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;

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
        builder.pop();

        builder.push("items");
        WATER_BOTTLE_STACKSIZE = builder
                .comment("Maximum stack size for water bottles (1-64)")
                .defineInRange("waterBottleStackSize", 64, 1, 64);
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
        ThirstConfig.THIRST_DEPLETION_MODIFIER = THIRST_DEPLETION_MODIFIER.get().floatValue();
        ThirstConfig.NETHER_THIRST_DEPLETION_MODIFIER = NETHER_THIRST_DEPLETION_MODIFIER.get().floatValue();
        ThirstConfig.FIRE_RESISTANCE_DEHYDRATION_MODIFIER = FIRE_RESISTANCE_DEHYDRATION_MODIFIER.get().floatValue();
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
        ThirstConfig.CAN_DRINK_BY_HAND = CAN_DRINK_BY_HAND.get();
        ThirstConfig.HAND_DRINKING_THIRST = HAND_DRINKING_THIRST.get();
        ThirstConfig.HAND_DRINKING_QUENCHED = HAND_DRINKING_QUENCHED.get();
        ThirstConfig.DRINK_BOTH_HANDS_NEEDED = DRINK_BOTH_HANDS_NEEDED.get();
        ThirstConfig.WATER_BOTTLE_STACKSIZE = WATER_BOTTLE_STACKSIZE.get();
    }

    private ForgeConfig() {}
}
