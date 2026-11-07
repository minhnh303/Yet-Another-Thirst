package dev.minhnh.yetanotherthirst.core.purity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ContainerWithPurity {

    private final ItemLike emptyItem;
    private final Supplier<ItemStack> filledItemSupplier;
    private final boolean drinkable;
    private boolean harvestRunningWater = true;
    private Predicate<ItemStack> equalsFilledPredicate;

    public ContainerWithPurity(ItemLike emptyItem, Supplier<ItemStack> filledItemSupplier, ItemLike filledItem, boolean drinkable) {
        this.emptyItem = emptyItem;
        this.filledItemSupplier = filledItemSupplier;
        this.drinkable = drinkable;
        this.equalsFilledPredicate = stack -> filledItem != null && stack.is(filledItem.asItem());
    }

    public ContainerWithPurity(ItemLike emptyItem, Supplier<ItemStack> filledItemSupplier, ItemLike filledItem) {
        this(emptyItem, filledItemSupplier, filledItem, true);
    }

    public ContainerWithPurity(ItemLike emptyItem, ItemLike filledItem, boolean drinkable) {
        this(emptyItem, () -> new ItemStack(filledItem), filledItem, drinkable);
    }

    public ContainerWithPurity(ItemLike emptyItem, ItemLike filledItem) {
        this(emptyItem, filledItem, true);
    }

    /** Static-only container: drinkable but cannot be filled from the world. */
    public ContainerWithPurity(Supplier<ItemStack> filledItemSupplier, ItemLike filledItem) {
        this(null, filledItemSupplier, filledItem, true);
    }

    @Deprecated
    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem, boolean drinkable) {
        this(emptyItem.getItem(), filledItem::copy, filledItem.getItem(), drinkable);
    }

    @Deprecated
    public ContainerWithPurity(ItemStack emptyItem, ItemStack filledItem) {
        this(emptyItem, filledItem, true);
    }

    @Deprecated
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
        return emptyItem != null && stack.is(emptyItem.asItem());
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
        return filledItemSupplier.get();
    }
}
