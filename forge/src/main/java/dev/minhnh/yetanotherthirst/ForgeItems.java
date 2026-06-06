package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import net.minecraft.world.item.Item;
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

    /** Binds Forge RegistryObjects into the loader-agnostic ModItems suppliers. */
    static void bindToCommon() {
        ModItems.CLAY_BOWL = CLAY_BOWL;
        ModItems.TERRACOTTA_BOWL = TERRACOTTA_BOWL;
        ModItems.TERRACOTTA_WATER_BOWL = TERRACOTTA_WATER_BOWL;
    }

    private ForgeItems() {}
}
