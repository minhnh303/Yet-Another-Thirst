package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;

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

    static void register() {

        CLAY_BOWL = reg("clay_bowl", props -> new Item(props.stacksTo(64)));
        TERRACOTTA_BOWL = reg("terracotta_bowl", props -> new Item(props.stacksTo(64)));
        TERRACOTTA_WATER_BOWL = reg("terracotta_water_bowl", props -> new DrinkableItem(props, () -> TERRACOTTA_BOWL));
        WOODEN_WATER_BOWL = reg("wooden_water_bowl", props -> new DrinkableItem(props, () -> Items.BOWL));

        if (dev.minhnh.yetanotherthirst.platform.Services.PLATFORM.isModLoaded("create")) {
            FILTER_FRAME = reg("filter_frame", props -> new BlockItem(ModBlocks.FILTER_FRAME, props.useBlockDescriptionPrefix()));
            FABRIC_FILTER_CORE = reg("fabric_filter_core", FilterCoreItem::new);
            SAND_FILTER_CORE = reg("sand_filter_core", FilterCoreItem::new);
            CARBON_FILTER_CORE = reg("carbon_filter_core", FilterCoreItem::new);
            CLOGGED_FABRIC_FILTER = reg("clogged_fabric_filter", props -> new Item(props.stacksTo(64)));
            CLOGGED_SAND_FILTER = reg("clogged_sand_filter", props -> new Item(props.stacksTo(64)));
            CLOGGED_CARBON_FILTER = reg("clogged_carbon_filter", props -> new Item(props.stacksTo(64)));
        }

        bindToCommon();
    }

    private static <T extends Item> T reg(String name, Function<Item.Properties, T> factory) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Constants.asResource(name));
        Item.Properties props = new Item.Properties().setId(key);
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props));
    }

    static void bindToCommon() {

        ModItems.CLAY_BOWL = () -> CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = () -> TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = () -> TERRACOTTA_WATER_BOWL;
        ModItems.WOODEN_WATER_BOWL = () -> WOODEN_WATER_BOWL;

        if (FABRIC_FILTER_CORE != null) {
            ModItems.FABRIC_FILTER_CORE = () -> FABRIC_FILTER_CORE;
            ModItems.SAND_FILTER_CORE = () -> SAND_FILTER_CORE;
            ModItems.CARBON_FILTER_CORE = () -> CARBON_FILTER_CORE;
            ModItems.CLOGGED_FABRIC_FILTER = () -> CLOGGED_FABRIC_FILTER;
            ModItems.CLOGGED_SAND_FILTER = () -> CLOGGED_SAND_FILTER;
            ModItems.CLOGGED_CARBON_FILTER = () -> CLOGGED_CARBON_FILTER;
        }
    }

    private FabricItems() {}
}
