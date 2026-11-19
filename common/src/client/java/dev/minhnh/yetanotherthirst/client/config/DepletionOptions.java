package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.section;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.bool;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.decimal;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.list;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;

/** Depletion rate, dehydration damage, and status effect options. */
public final class DepletionOptions {
    public static final ConfigOption<Double> THIRST_DEPLETION_MODIFIER =
            decimal(COMMON, "thirstDepletionModifier", 0, 10, 0.05).format(Format.MULTIPLIER);
    public static final ConfigOption<Double> NETHER_THIRST_DEPLETION_MODIFIER =
            decimal(COMMON, "netherThirstDepletionModifier", 1, 10, 0.05).format(Format.MULTIPLIER);
    public static final ConfigOption<Boolean> BIOME_DEHYDRATION_MODIFIER =
            bool(COMMON, "biomeDehydrationModifier");
    public static final ConfigOption<Double> ENVIRONMENT_MODIFIER_HARSHNESS =
            decimal(COMMON, "environmentModifierHarshness", 0, 1, 0.05).format(Format.PERCENT);
    public static final ConfigOption<Boolean> FIRE_PROTECTION_DEHYDRATION_MODIFIER =
            bool(COMMON, "fireProtectionDehydrationModifier");
    public static final ConfigOption<Double> FIRE_RESISTANCE_DEHYDRATION_MODIFIER =
            decimal(COMMON, "fireResistanceDehydrationModifier", 0, 1, 0.05).format(Format.MULTIPLIER);

    public static final ConfigOption<Double> DEHYDRATION_DAMAGE =
            decimal(COMMON, "dehydrationDamage", 0, 100, 0.5).format(Format.HEARTS).field();
    public static final ConfigOption<Integer> DAMAGE_INTERVAL_TICKS =
            integer(COMMON, "damageIntervalTicks", 1, 72000).format(Format.TICKS).field();
    public static final ConfigOption<Double> DEHYDRATION_DAMAGE_EASY_LIMIT =
            decimal(COMMON, "dehydrationDamageEasyLimit", 0, 20, 0.5).format(Format.HEARTS);
    public static final ConfigOption<Double> DEHYDRATION_DAMAGE_NORMAL_LIMIT =
            decimal(COMMON, "dehydrationDamageNormalLimit", 0, 20, 0.5).format(Format.HEARTS);
    public static final ConfigOption<Double> DEHYDRATION_DAMAGE_HARD_LIMIT =
            decimal(COMMON, "dehydrationDamageHardLimit", 0, 20, 0.5).format(Format.HEARTS);

    public static final ConfigOption<Integer> HYDRATION_EFFECT_THIRST_PER_TICK =
            integer(COMMON, "hydrationEffectThirstPerTick", 0, 20);
    public static final ConfigOption<Integer> HYDRATION_EFFECT_QUENCHED_PER_TICK =
            integer(COMMON, "hydrationEffectQuenchedPerTick", 0, 20);
    public static final ConfigOption<Double> THIRSTY_EFFECT_EXHAUSTION_PER_TICK =
            decimal(COMMON, "thirstyEffectExhaustionPerTick", 0, 4, 0.005).field();
    public static final ConfigOption<List<String>> SUSPEND_THIRST_EFFECTS =
            list(COMMON, "suspendThirstEffects", ListSchema.effects());
    public static final ConfigOption<List<String>> PAUSE_DEPLETION_EFFECTS =
            list(COMMON, "pauseDepletionEffects", ListSchema.effects());
    public static final ConfigOption<List<String>> REGEN_THIRST_EFFECTS =
            list(COMMON, "regenThirstEffects", ListSchema.effects());
    public static final ConfigOption<Integer> REGEN_THIRST_INTERVAL =
            integer(COMMON, "regenThirstInterval", 1, 72000).format(Format.TICKS).field();
    public static final ConfigOption<Integer> REGEN_THIRST_AMOUNT =
            integer(COMMON, "regenThirstAmount", 1, 20);

    private DepletionOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("depletion", () -> new ItemStack(Items.BLAZE_POWDER),
                        section("rates",
                                THIRST_DEPLETION_MODIFIER,
                                NETHER_THIRST_DEPLETION_MODIFIER),
                        section("environment",
                                BIOME_DEHYDRATION_MODIFIER,
                                ENVIRONMENT_MODIFIER_HARSHNESS,
                                FIRE_PROTECTION_DEHYDRATION_MODIFIER,
                                FIRE_RESISTANCE_DEHYDRATION_MODIFIER)),

                category("dehydration", () -> new ItemStack(Items.DEAD_BUSH),
                        section("damage",
                                DEHYDRATION_DAMAGE,
                                DAMAGE_INTERVAL_TICKS),
                        section("health_floor",
                                DEHYDRATION_DAMAGE_EASY_LIMIT,
                                DEHYDRATION_DAMAGE_NORMAL_LIMIT,
                                DEHYDRATION_DAMAGE_HARD_LIMIT)),

                category("effects", () -> new ItemStack(Items.BREWING_STAND),
                        section("potency",
                                HYDRATION_EFFECT_THIRST_PER_TICK,
                                HYDRATION_EFFECT_QUENCHED_PER_TICK,
                                THIRSTY_EFFECT_EXHAUSTION_PER_TICK),
                        section("effect_rules",
                                SUSPEND_THIRST_EFFECTS,
                                PAUSE_DEPLETION_EFFECTS,
                                REGEN_THIRST_EFFECTS),
                        section("regen",
                                REGEN_THIRST_INTERVAL,
                                REGEN_THIRST_AMOUNT))
        );
    }
}

