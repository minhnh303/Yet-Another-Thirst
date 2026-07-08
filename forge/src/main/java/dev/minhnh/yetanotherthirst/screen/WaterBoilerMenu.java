package dev.minhnh.yetanotherthirst.screen;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class WaterBoilerMenu extends AbstractWaterBoilerMenu {

    private final WaterBoilerBlockEntity blockEntity;

    /** Client-side constructor — called via IForgeMenuType network factory. */
    public WaterBoilerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory,
                (WaterBoilerBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(4));
    }

    /** Server-side constructor. */
    public WaterBoilerMenu(int containerId, Inventory playerInventory, WaterBoilerBlockEntity blockEntity,
            ContainerData data) {
        super(ModMenuTypes.WATER_BOILER != null ? ModMenuTypes.WATER_BOILER.get() : null, containerId, data);
        this.blockEntity = blockEntity;

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 50, 17));
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 50, 53));
        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 2, 110, 35));

        addPlayerInventorySlots(playerInventory);
    }

    @Override
    protected boolean isFuelItem(ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING) > 0;
    }

    @Override
    public int getInputAmount() {
        return blockEntity != null ? blockEntity.getInputTank().getFluidAmount() : 0;
    }

    @Override
    public int getInputCapacity() {
        return blockEntity != null ? blockEntity.getInputTank().getCapacity() : 1;
    }

    @Override
    public int getInputPurity() {
        return blockEntity != null ? FluidPurityHelper.getPurity(blockEntity.getInputTank().getFluid()) : 0;
    }

    @Override
    public int getOutputAmount() {
        return blockEntity != null ? blockEntity.getOutputTank().getFluidAmount() : 0;
    }

    @Override
    public int getOutputCapacity() {
        return blockEntity != null ? blockEntity.getOutputTank().getCapacity() : 1;
    }

    @Override
    public int getOutputPurity() {
        return blockEntity != null ? FluidPurityHelper.getPurity(blockEntity.getOutputTank().getFluid()) : 0;
    }

    public WaterBoilerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        if (blockEntity == null) return false;
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(),
                blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }
}
