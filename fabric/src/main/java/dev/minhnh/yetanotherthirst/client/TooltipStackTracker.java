package dev.minhnh.yetanotherthirst.client;

import net.minecraft.world.item.ItemStack;

public final class TooltipStackTracker {
    private static final ThreadLocal<ItemStack> CURRENT_STACK = new ThreadLocal<>();
    private static final ThreadLocal<Long> SET_TIME = new ThreadLocal<>();

    private TooltipStackTracker() {}

    public static void set(ItemStack stack) {
        CURRENT_STACK.set(stack);
        SET_TIME.set(System.currentTimeMillis());
    }

    public static ItemStack get() {
        Long time = SET_TIME.get();
        if (time != null && System.currentTimeMillis() - time < 50) {
            ItemStack stack = CURRENT_STACK.get();
            if (stack != null && !stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void clear() {
        CURRENT_STACK.remove();
        SET_TIME.remove();
    }
}
