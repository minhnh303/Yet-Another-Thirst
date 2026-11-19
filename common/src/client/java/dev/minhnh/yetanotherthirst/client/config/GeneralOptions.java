package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.section;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.bool;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;

/** General gameplay and sprinting options. */
public final class GeneralOptions {
    public static final ConfigOption<Boolean> EXTRA_HYDRATION_CONVERTS_TO_QUENCHED =
            bool(COMMON, "extraHydrationConvertsToQuenched");
    public static final ConfigOption<Boolean> THIRST_DEPLETES_IN_PEACEFUL =
            bool(COMMON, "thirstDepletesInPeaceful");
    public static final ConfigOption<Boolean> DEPLETES_WHEN_NAUSEA =
            bool(COMMON, "depletesWhenNausea");
    public static final ConfigOption<Boolean> DEHYDRATION_HALTS_HEALTH_REGEN =
            bool(COMMON, "dehydrationHaltsHealthRegen");
    public static final ConfigOption<Boolean> SPRINT_PREVENTION =
            bool(COMMON, "sprintPrevention");
    public static final ConfigOption<Integer> SPRINT_THRESHOLD =
            integer(COMMON, "sprintThreshold", 0, 20).format(Format.DROPLETS);

    private GeneralOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("general", () -> PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER),
                        section("basics",
                                EXTRA_HYDRATION_CONVERTS_TO_QUENCHED,
                                THIRST_DEPLETES_IN_PEACEFUL,
                                DEPLETES_WHEN_NAUSEA,
                                DEHYDRATION_HALTS_HEALTH_REGEN),
                        section("sprint",
                                SPRINT_PREVENTION,
                                SPRINT_THRESHOLD))
        );
    }
}

