package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeItems {

    static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

    static final DeferredItem<Item> CLAY_BOWL = ITEMS.registerSimpleItem("clay_bowl",
            p -> p.stacksTo(64));

    static final DeferredItem<Item> TERRACOTTA_BOWL = ITEMS.registerSimpleItem("terracotta_bowl",
            p -> p.stacksTo(64));

    static final DeferredItem<Item> TERRACOTTA_WATER_BOWL = ITEMS.registerItem("terracotta_water_bowl",
            props -> new DrinkableItem(props, TERRACOTTA_BOWL));

    static final DeferredItem<Item> WOODEN_WATER_BOWL = ITEMS.registerItem("wooden_water_bowl",
            props -> new DrinkableItem(props, () -> Items.BOWL));

    /** Binds NeoForge DeferredItems into the loader-agnostic ModItems suppliers. */
    static void bindToCommon() {
        ModItems.CLAY_BOWL = CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = TERRACOTTA_WATER_BOWL;
        ModItems.WOODEN_WATER_BOWL = WOODEN_WATER_BOWL;
    }

    private NeoForgeItems() {}
}
