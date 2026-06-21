package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ForgeItems {

    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MOD_ID);

    static final RegistryObject<Item> CLAY_BOWL = ITEMS.register("clay_bowl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    static final RegistryObject<Item> TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    static final RegistryObject<Item> TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl",
            () -> new DrinkableItem(TERRACOTTA_BOWL));

    static final RegistryObject<Item> WOODEN_WATER_BOWL = ITEMS.register("wooden_water_bowl",
            () -> new DrinkableItem(() -> Items.BOWL));

    static final RegistryObject<Item> FILTER_FRAME = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("filter_frame", () -> new BlockItem(ModBlocks.FILTER_FRAME.get(), new Item.Properties())) : null;

    static final RegistryObject<Item> WATER_BOILER = net.minecraftforge.fml.ModList.get().isLoaded("immersiveengineering") ?
            ITEMS.register("water_boiler", () -> new BlockItem(ModBlocks.WATER_BOILER.get(), new Item.Properties())) : null;

    static final RegistryObject<Item> FABRIC_FILTER_CORE = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("fabric_filter_core", () -> new dev.minhnh.yetanotherthirst.core.item.FilterCoreItem(new Item.Properties().stacksTo(64))) : null;

    static final RegistryObject<Item> SAND_FILTER_CORE = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("sand_filter_core", () -> new dev.minhnh.yetanotherthirst.core.item.FilterCoreItem(new Item.Properties().stacksTo(64))) : null;

    static final RegistryObject<Item> CARBON_FILTER_CORE = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("carbon_filter_core", () -> new dev.minhnh.yetanotherthirst.core.item.FilterCoreItem(new Item.Properties().stacksTo(64))) : null;

    static final RegistryObject<Item> CLOGGED_FABRIC_FILTER = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("clogged_fabric_filter", () -> new Item(new Item.Properties().stacksTo(64))) : null;

    static final RegistryObject<Item> CLOGGED_SAND_FILTER = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("clogged_sand_filter", () -> new Item(new Item.Properties().stacksTo(64))) : null;

    static final RegistryObject<Item> CLOGGED_CARBON_FILTER = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            ITEMS.register("clogged_carbon_filter", () -> new Item(new Item.Properties().stacksTo(64))) : null;

    /** Binds Forge RegistryObjects into the loader-agnostic ModItems suppliers. */
    static void bindToCommon() {
        ModItems.CLAY_BOWL = CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = TERRACOTTA_WATER_BOWL;
        ModItems.WOODEN_WATER_BOWL = WOODEN_WATER_BOWL;
        if (FABRIC_FILTER_CORE != null) {
            ModItems.FABRIC_FILTER_CORE = FABRIC_FILTER_CORE;
            ModItems.SAND_FILTER_CORE = SAND_FILTER_CORE;
            ModItems.CARBON_FILTER_CORE = CARBON_FILTER_CORE;
            ModItems.CLOGGED_FABRIC_FILTER = CLOGGED_FABRIC_FILTER;
            ModItems.CLOGGED_SAND_FILTER = CLOGGED_SAND_FILTER;
            ModItems.CLOGGED_CARBON_FILTER = CLOGGED_CARBON_FILTER;
        }
    }

    private ForgeItems() {}
}
