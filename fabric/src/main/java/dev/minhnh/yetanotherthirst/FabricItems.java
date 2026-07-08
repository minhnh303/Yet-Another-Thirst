package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class FabricItems {

    static Item CLAY_BOWL;
    static Item TERRACOTTA_BOWL;
    static Item TERRACOTTA_WATER_BOWL;
    static Item WOODEN_WATER_BOWL;
    static Item FILTER_FRAME;
    static Item FABRIC_FILTER_CORE;
    static Item SAND_FILTER_CORE;
    static Item CARBON_FILTER_CORE;
    static Item CLOGGED_FABRIC_FILTER;
    static Item CLOGGED_SAND_FILTER;
    static Item CLOGGED_CARBON_FILTER;
    static Item WATER_BOILER;

    static void register() {

        CLAY_BOWL = reg("clay_bowl", new Item(new Item.Properties().stacksTo(64)));
        TERRACOTTA_BOWL = reg("terracotta_bowl", new Item(new Item.Properties().stacksTo(64)));
        TERRACOTTA_WATER_BOWL = reg("terracotta_water_bowl", new DrinkableItem(() -> TERRACOTTA_BOWL));
        WOODEN_WATER_BOWL = reg("wooden_water_bowl", new DrinkableItem(() -> Items.BOWL));

        FILTER_FRAME = reg("filter_frame", new BlockItem(ModBlocks.FILTER_FRAME, new Item.Properties()));
        FABRIC_FILTER_CORE = reg("fabric_filter_core", new FilterCoreItem(new Item.Properties().stacksTo(64)));
        SAND_FILTER_CORE = reg("sand_filter_core", new FilterCoreItem(new Item.Properties().stacksTo(64)));
        CARBON_FILTER_CORE = reg("carbon_filter_core", new FilterCoreItem(new Item.Properties().stacksTo(64)));
        CLOGGED_FABRIC_FILTER = reg("clogged_fabric_filter", new Item(new Item.Properties().stacksTo(64)));
        CLOGGED_SAND_FILTER = reg("clogged_sand_filter", new Item(new Item.Properties().stacksTo(64)));
        CLOGGED_CARBON_FILTER = reg("clogged_carbon_filter", new Item(new Item.Properties().stacksTo(64)));

        WATER_BOILER = reg("water_boiler", new BlockItem(ModBlocks.WATER_BOILER, new Item.Properties().stacksTo(64)));

        bindToCommon();
    }

    private static <T extends Item> T reg(String name, T item) {

        return Registry.register(BuiltInRegistries.ITEM, Constants.asResource(name), item);
    }

    static void bindToCommon() {

        ModItems.CLAY_BOWL = () -> CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = () -> TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = () -> TERRACOTTA_WATER_BOWL;
        ModItems.WOODEN_WATER_BOWL = () -> WOODEN_WATER_BOWL;

        ModItems.FABRIC_FILTER_CORE = () -> FABRIC_FILTER_CORE;
        ModItems.SAND_FILTER_CORE = () -> SAND_FILTER_CORE;
        ModItems.CARBON_FILTER_CORE = () -> CARBON_FILTER_CORE;
        ModItems.CLOGGED_FABRIC_FILTER = () -> CLOGGED_FABRIC_FILTER;
        ModItems.CLOGGED_SAND_FILTER = () -> CLOGGED_SAND_FILTER;
        ModItems.CLOGGED_CARBON_FILTER = () -> CLOGGED_CARBON_FILTER;
    }

    private FabricItems() {}
}
