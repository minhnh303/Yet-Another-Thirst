package dev.minhnh.yetanotherthirst.core.purity;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class ContainerWithPurity {

    private final ItemStack emptyItem;
    private final ItemStack filledItem;
    private final boolean drinkable;
    private boolean harvestRunningWater = true;
    private Predicate<ItemStack> equalsFilledPredicate;

    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem, boolean drinkable) {
        this.emptyItem = emptyItem;
        this.filledItem = filledItem;
        this.drinkable = drinkable;
        this.equalsFilledPredicate = stack -> ItemStack.isSameItem(stack, filledItem);
    }

    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem) {
        this(emptyItem, filledItem, true);
    }

    /** Static-only container: drinkable but cannot be filled from the world. */
    public ContainerWithPurity(ItemStack filledItem) {
        this(ItemStack.EMPTY, filledItem, true);
    }

    public ContainerWithPurity canHarvestRunningWater(boolean val) {
        this.harvestRunningWater = val;
        return this;
    }

    public ContainerWithPurity setEqualsFilled(Predicate<ItemStack> predicate) {
        this.equalsFilledPredicate = predicate;
        return this;
    }

    public boolean equalsEmpty(ItemStack stack) {
        return !emptyItem.isEmpty() && ItemStack.isSameItem(stack, emptyItem);
    }

    public boolean equalsFilled(ItemStack stack) {
        return equalsFilledPredicate.test(stack);
    }

    public boolean isDrinkable() {
        return drinkable;
    }

    public boolean canHarvestRunningWater() {
        return harvestRunningWater;
    }

    public ItemStack getFilledItem() {
        return filledItem;
    }
}
