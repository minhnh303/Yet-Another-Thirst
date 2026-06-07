package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeItems {

    static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Constants.MOD_ID);

    static final DeferredHolder<Item, Item> CLAY_BOWL = ITEMS.register("clay_bowl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    static final DeferredHolder<Item, Item> TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl",
            () -> new Item(new Item.Properties().stacksTo(64)));

    static final DeferredHolder<Item, DrinkableItem> TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl",
            () -> new DrinkableItem(TERRACOTTA_BOWL));

    static final DeferredHolder<Item, DrinkableItem> WOODEN_WATER_BOWL = ITEMS.register("wooden_water_bowl",
            () -> new DrinkableItem(() -> Items.BOWL));

    /** Binds NeoForge RegistryObjects into the loader-agnostic ModItems suppliers. */
    static void bindToCommon() {
        ModItems.CLAY_BOWL = CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = TERRACOTTA_WATER_BOWL::get;
        ModItems.WOODEN_WATER_BOWL = WOODEN_WATER_BOWL::get;
    }

    private NeoForgeItems() {}
}
