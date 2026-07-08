package dev.minhnh.yetanotherthirst.client;

import net.minecraft.world.item.ItemStack;

/** Tracks the item stack currently being rendered in a tooltip (set by FabricClientGameEvents). */
public final class TooltipStackTracker {

    private static ItemStack current = ItemStack.EMPTY;

    private TooltipStackTracker() {}

    public static void set(ItemStack stack) {

        current = stack;
    }

    public static ItemStack get() {

        return current;
    }

    public static void clear() {

        current = ItemStack.EMPTY;
    }
}
