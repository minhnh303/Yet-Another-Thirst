package dev.minhnh.yetanotherthirst.core.item;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/** Loader-agnostic item references. Populated by each loader's item registration. */
public final class ModItems {

    public static Supplier<Item> CLAY_BOWL;
    public static Supplier<Item> TERRACOTTA_BOWL;
    public static Supplier<Item> TERRACOTTA_WATER_BOWL;
    public static Supplier<Item> WOODEN_WATER_BOWL;

    private ModItems() {}
}
