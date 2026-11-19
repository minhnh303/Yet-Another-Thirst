package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.section;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.decimal;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;

/** Filter Frame, filter cores, and Water Boiler machine options. */
public final class MachineOptions {
    public static final ConfigOption<Integer> FILTER_BASE_SPEED =
            integer(ITEMS, "filterBaseSpeed", 1, 1000).field();
    public static final ConfigOption<Double> DIRTY_WATER_DECAY_MULTIPLIER =
            decimal(ITEMS, "dirtyWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field();
    public static final ConfigOption<Double> SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER =
            decimal(ITEMS, "slightlyDirtyWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field();
    public static final ConfigOption<Double> ACCEPTABLE_WATER_DECAY_MULTIPLIER =
            decimal(ITEMS, "acceptableWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field();

    public static final ConfigOption<Integer> FABRIC_FILTER_DURABILITY =
            integer(ITEMS, "fabricFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field();
    public static final ConfigOption<Integer> FABRIC_FILTER_MAX_PURITY =
            integer(ITEMS, "fabricFilterMaxPurity", 0, 3).format(Format.PURITY);

    public static final ConfigOption<Integer> SAND_FILTER_DURABILITY =
            integer(ITEMS, "sandFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field();
    public static final ConfigOption<Integer> SAND_FILTER_MAX_PURITY =
            integer(ITEMS, "sandFilterMaxPurity", 0, 3).format(Format.PURITY);

    public static final ConfigOption<Integer> CARBON_FILTER_DURABILITY =
            integer(ITEMS, "carbonFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field();
    public static final ConfigOption<Integer> CARBON_FILTER_MAX_PURITY =
            integer(ITEMS, "carbonFilterMaxPurity", 0, 3).format(Format.PURITY);

    public static final ConfigOption<Integer> WATER_BOILER_CAPACITY =
            integer(ITEMS, "waterBoilerCapacity", 1000, 1_000_000).format(Format.MILLIBUCKETS).field();
    public static final ConfigOption<Integer> WATER_BOILER_BOIL_TIME =
            integer(ITEMS, "waterBoilerBoilTime", 1, 72000).format(Format.TICKS).field();
    public static final ConfigOption<Integer> WATER_BOILER_ENERGY_CONSUMPTION =
            integer(ITEMS, "waterBoilerEnergyConsumption", 1, 100_000).field();

    private MachineOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("machines", () -> new ItemStack(Items.HOPPER),
                        section("filter",
                                FILTER_BASE_SPEED,
                                DIRTY_WATER_DECAY_MULTIPLIER,
                                SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER,
                                ACCEPTABLE_WATER_DECAY_MULTIPLIER),
                        section("core_fabric",
                                FABRIC_FILTER_DURABILITY,
                                FABRIC_FILTER_MAX_PURITY),
                        section("core_sand",
                                SAND_FILTER_DURABILITY,
                                SAND_FILTER_MAX_PURITY),
                        section("core_carbon",
                                CARBON_FILTER_DURABILITY,
                                CARBON_FILTER_MAX_PURITY),
                        section("boiler",
                                WATER_BOILER_CAPACITY,
                                WATER_BOILER_BOIL_TIME,
                                WATER_BOILER_ENERGY_CONSUMPTION))
        );
    }
}

