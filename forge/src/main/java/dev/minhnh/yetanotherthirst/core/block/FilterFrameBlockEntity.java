package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FilterFrameBlockEntity extends AbstractFilterFrameBlockEntity {

    private static class PurityBlendingTank extends FluidTank {
        public PurityBlendingTank(int capacity, java.util.function.Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(resource)) return 0;
            if (fluid.isEmpty()) {
                int toFill = Math.min(capacity, resource.getAmount());
                if (action.execute()) {
                    fluid = new FluidStack(resource, toFill);
                    onContentsChanged();
                }
                return toFill;
            }
            if (fluid.getFluid() != resource.getFluid()) return 0;

            CompoundTag tag1 = fluid.getTag();
            CompoundTag tag2 = resource.getTag();
            boolean tagsMatch = true;
            if (tag1 != null || tag2 != null) {
                CompoundTag copy1 = tag1 != null ? tag1.copy() : new CompoundTag();
                CompoundTag copy2 = tag2 != null ? tag2.copy() : new CompoundTag();
                copy1.remove("Purity");
                copy2.remove("Purity");
                if (!copy1.equals(copy2)) tagsMatch = false;
            }
            if (!tagsMatch) return 0;

            int currentAmount = fluid.getAmount();
            int space = capacity - currentAmount;
            if (space <= 0) return 0;
            int toFill = Math.min(space, resource.getAmount());
            if (action.execute()) {
                int blendedPurity = Math.min(FluidPurityHelper.getPurity(fluid), FluidPurityHelper.getPurity(resource));
                fluid.setAmount(currentAmount + toFill);
                FluidPurityHelper.addPurity(fluid, blendedPurity);
                onContentsChanged();
            }
            return toFill;
        }
    }

    private final FluidTank inputTank = new PurityBlendingTank(TANK_SIZE, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };
    private final FluidTank outputTank = new PurityBlendingTank(TANK_SIZE, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isFilterCore(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                updateBlockState();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final LazyOptional<IFluidHandler> inputHandler = LazyOptional.of(InputFluidHandler::new);
    private final LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(OutputFluidHandler::new);
    private final LazyOptional<IItemHandler> inventoryHandler = LazyOptional.of(() -> inventory);

    public FilterFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FILTER_FRAME_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, FilterFrameBlockEntity blockEntity) {
        if (level.isClientSide()) return;
        blockEntity.filter();
    }

    @Override
    public ItemStack getFilterCore() {
        return inventory.getStackInSlot(0);
    }

    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }

    @Override
    public void setFilterCore(ItemStack stack) {
        inventory.setStackInSlot(0, stack);
        updateBlockState();
    }

    @Override
    public ItemStack removeFilterCore() {
        ItemStack stack = inventory.extractItem(0, 64, false);
        updateBlockState();
        return stack;
    }

    private void filter() {
        ItemStack core = inventory.getStackInSlot(0);
        if (core.isEmpty() || isClogged(core)) return;

        double remainingLifespan = getLifespan(core);
        if (remainingLifespan <= 0) {
            clogFilter(core.getItem());
            return;
        }

        int amountToFilter = Math.min(ThirstConfig.FILTER_BASE_SPEED, inputTank.getFluidAmount());
        amountToFilter = Math.min(amountToFilter, outputTank.getCapacity() - outputTank.getFluidAmount());
        if (amountToFilter <= 0) return;

        FluidStack resource = inputTank.drain(amountToFilter, IFluidHandler.FluidAction.SIMULATE);
        if (resource.isEmpty() || resource.getFluid() != Fluids.WATER) return;

        int p1 = FluidPurityHelper.getPurity(resource);
        int maxP = getMaxPurity(core);
        if (p1 >= maxP) return;

        double decayPerMb = getDecayMultiplier(p1);
        if (decayPerMb > 0) {
            amountToFilter = Math.min(amountToFilter, (int) (remainingLifespan / decayPerMb));
        }
        if (amountToFilter <= 0) {
            clogFilter(core.getItem());
            return;
        }

        double newLifespan = Math.max(0.0, remainingLifespan - amountToFilter * decayPerMb);
        core.getOrCreateTag().putDouble("Lifespan", newLifespan);
        if (newLifespan <= 0) clogFilter(core.getItem());

        FluidStack moved = inputTank.drain(amountToFilter, IFluidHandler.FluidAction.EXECUTE);
        if (!moved.isEmpty()) {
            FluidPurityHelper.addPurity(moved, Math.min(p1 + 1, maxP));
            outputTank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        }
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void clogFilter(Item coreItem) {
        inventory.setStackInSlot(0, new ItemStack(getCloggedVariant(coreItem)));
        updateBlockState();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputTank.readFromNBT(tag.getCompound("InputTank"));
        outputTank.readFromNBT(tag.getCompound("OutputTank"));
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) load(tag);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER && side != null) {
            BlockState state = getBlockState();
            Direction facing = state.getValue(AbstractFilterFrameBlock.FACING);
            boolean reversed = state.hasProperty(AbstractFilterFrameBlock.REVERSED) && state.getValue(AbstractFilterFrameBlock.REVERSED);
            Direction inputSide = reversed ? facing : facing.getOpposite();
            Direction outputSide = reversed ? facing.getOpposite() : facing;
            if (side == inputSide) return inputHandler.cast();
            if (side == outputSide) return outputHandler.cast();
        }
        if (capability == ForgeCapabilities.ITEM_HANDLER) return inventoryHandler.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputHandler.invalidate();
        outputHandler.invalidate();
        inventoryHandler.invalidate();
    }

    private final class InputFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return inputTank.getTanks(); }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return inputTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return inputTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return inputTank.isFluidValid(tank, stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return inputTank.fill(resource, action); }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    }

    private final class OutputFluidHandler implements IFluidHandler {
        @Override public int getTanks() { return outputTank.getTanks(); }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return outputTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return outputTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return outputTank.drain(resource, action); }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return outputTank.drain(maxDrain, action); }
    }
}
