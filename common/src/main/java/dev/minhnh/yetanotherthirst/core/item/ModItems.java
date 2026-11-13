package dev.minhnh.yetanotherthirst.core.item;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/** Loader-agnostic item references. Populated by each loader's item registration. */
public final class ModItems {

    public static Supplier<Item> CLAY_BOWL = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> TERRACOTTA_BOWL = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> TERRACOTTA_WATER_BOWL = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> WOODEN_WATER_BOWL = () -> net.minecraft.world.item.Items.AIR;

    public static Supplier<Item> FABRIC_FILTER_CORE = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> SAND_FILTER_CORE = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> CARBON_FILTER_CORE = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> CLOGGED_FABRIC_FILTER = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> CLOGGED_SAND_FILTER = () -> net.minecraft.world.item.Items.AIR;
    public static Supplier<Item> CLOGGED_CARBON_FILTER = () -> net.minecraft.world.item.Items.AIR;

    private ModItems() {}
}
