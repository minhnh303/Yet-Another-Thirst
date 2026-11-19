package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.section;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.bool;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.chance;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;

/** Rain/hand drinking and water purity options. */
public final class PurityOptions {
    public static final ConfigOption<Boolean> DRINK_RAIN_WATER =
            bool(COMMON, "drinkRainWater");
    public static final ConfigOption<Boolean> CAN_DRINK_BY_HAND =
            bool(COMMON, "canDrinkByHand");
    public static final ConfigOption<Integer> HAND_DRINKING_THIRST =
            integer(COMMON, "handDrinkingThirst", 0, 20);
    public static final ConfigOption<Integer> HAND_DRINKING_QUENCHED =
            integer(COMMON, "handDrinkingQuenched", 0, 20);
    public static final ConfigOption<Boolean> DRINK_BOTH_HANDS_NEEDED =
            bool(COMMON, "drinkBothHandsNeeded");

    public static final ConfigOption<Integer> DEFAULT_PURITY =
            integer(COMMON, "defaultPurity", 0, 3).format(Format.PURITY);
    public static final ConfigOption<Boolean> QUENCH_WHEN_DEBUFFED =
            bool(COMMON, "quenchWhenDebuffed");
    public static final ConfigOption<Double> DIRTY_NAUSEA_CHANCE = chance("dirtyNauseaChance");
    public static final ConfigOption<Double> DIRTY_POISON_CHANCE = chance("dirtyPoisonChance");
    public static final ConfigOption<Double> SLIGHTLY_DIRTY_NAUSEA_CHANCE = chance("slightlyDirtyNauseaChance");
    public static final ConfigOption<Double> SLIGHTLY_DIRTY_POISON_CHANCE = chance("slightlyDirtyPoisonChance");
    public static final ConfigOption<Double> ACCEPTABLE_NAUSEA_CHANCE = chance("acceptableNauseaChance");
    public static final ConfigOption<Double> ACCEPTABLE_POISON_CHANCE = chance("acceptablePoisonChance");
    public static final ConfigOption<Double> PURIFIED_NAUSEA_CHANCE = chance("purifiedNauseaChance");
    public static final ConfigOption<Double> PURIFIED_POISON_CHANCE = chance("purifiedPoisonChance");
    public static final ConfigOption<Integer> MOUNTAINS_Y =
            integer(COMMON, "mountainsY", -64, 320).format(Format.BLOCK_Y);
    public static final ConfigOption<Integer> CAVES_Y =
            integer(COMMON, "cavesY", -64, 320).format(Format.BLOCK_Y);
    public static final ConfigOption<Integer> RUNNING_WATER_PURIFICATION_AMOUNT =
            integer(COMMON, "runningWaterPurificationAmount", 0, 3);

    private PurityOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("drinking", () -> new ItemStack(Items.WATER_BUCKET),
                        section("rain", DRINK_RAIN_WATER),
                        section("hand",
                                CAN_DRINK_BY_HAND,
                                HAND_DRINKING_THIRST,
                                HAND_DRINKING_QUENCHED,
                                DRINK_BOTH_HANDS_NEEDED)),

                category("purity", () -> new ItemStack(Items.CAULDRON),
                        section("water", DEFAULT_PURITY, QUENCH_WHEN_DEBUFFED),
                        section("sickness_dirty", DIRTY_NAUSEA_CHANCE, DIRTY_POISON_CHANCE),
                        section("sickness_slightly_dirty", SLIGHTLY_DIRTY_NAUSEA_CHANCE, SLIGHTLY_DIRTY_POISON_CHANCE),
                        section("sickness_acceptable", ACCEPTABLE_NAUSEA_CHANCE, ACCEPTABLE_POISON_CHANCE),
                        section("sickness_purified", PURIFIED_NAUSEA_CHANCE, PURIFIED_POISON_CHANCE),
                        section("sources", MOUNTAINS_Y, CAVES_Y, RUNNING_WATER_PURIFICATION_AMOUNT))
        );
    }
}

