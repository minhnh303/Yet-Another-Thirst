package dev.minhnh.yetanotherthirst.screen;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractWaterBoilerMenu extends AbstractContainerMenu {

    protected final ContainerData data;

    protected AbstractWaterBoilerMenu(@Nullable MenuType<?> menuType, int containerId, ContainerData data) {
        super(menuType, containerId);
        checkContainerDataCount(data, 4);
        this.data = data;
        addDataSlots(data);
    }

    protected void addPlayerInventorySlots(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    protected abstract boolean isFuelItem(ItemStack stack);

    public boolean isLit() {
        return data.get(0) > 0;
    }

    public int getLitProgress() {
        int duration = data.get(1);
        if (duration == 0) duration = 200;
        return data.get(0) * 14 / duration;
    }

    public int getCookProgress() {
        int total = data.get(3);
        if (total == 0) total = 200;
        return data.get(2) * 22 / total;
    }

    public abstract int getInputAmount();
    public abstract int getInputCapacity();
    public abstract int getInputPurity();

    public abstract int getOutputAmount();
    public abstract int getOutputCapacity();
    public abstract int getOutputPurity();

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (isFuelItem(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                        if (WaterPurity.isWaterFilledContainer(itemstack1)
                                || WaterPurity.isEmptyWaterContainer(itemstack1) || itemstack1.is(Items.WET_SPONGE)) {
                            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                                return ItemStack.EMPTY;
                            }
                        } else {
                            return ItemStack.EMPTY;
                        }
                    }
                } else if (WaterPurity.isWaterFilledContainer(itemstack1)
                        || WaterPurity.isEmptyWaterContainer(itemstack1) || itemstack1.is(Items.WET_SPONGE)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (index >= 3 && index < 30) {
                        if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }
}
