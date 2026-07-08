package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FilterFrameBlockEntity extends AbstractFilterFrameBlockEntity {

    // Blends purity on fill — uses lower of the two purity values
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
                    fluid = new FluidStack(resource.getFluid(), toFill);
                    // Treat untagged water (e.g. from Create's pump) as purity 0 (dirty)
                    int purity = FluidPurityHelper.hasPurity(resource) ? FluidPurityHelper.getPurity(resource) : 0;
                    FluidPurityHelper.addPurity(fluid, purity);
                    onContentsChanged();
                }
                return toFill;
            }
            if (!fluid.getFluid().isSame(resource.getFluid())) return 0;

            // Compare tags excluding Purity
            if (!nonPurityTagsMatch(fluid, resource)) return 0;

            int currentAmount = fluid.getAmount();
            int space = capacity - currentAmount;
            if (space <= 0) return 0;
            int toFill = Math.min(space, resource.getAmount());
            if (action.execute()) {
                int resourcePurity = FluidPurityHelper.hasPurity(resource) ? FluidPurityHelper.getPurity(resource) : 0;
                int blendedPurity = Math.min(FluidPurityHelper.getPurity(fluid), resourcePurity);
                fluid.setAmount(currentAmount + toFill);
                FluidPurityHelper.addPurity(fluid, blendedPurity);
                onContentsChanged();
            }
            return toFill;
        }

        private static boolean nonPurityTagsMatch(FluidStack a, FluidStack b) {
            CustomData da = a.get(DataComponents.CUSTOM_DATA);
            CustomData db = b.get(DataComponents.CUSTOM_DATA);
            CompoundTag ta = da != null ? da.copyTag() : new CompoundTag();
            CompoundTag tb = db != null ? db.copyTag() : new CompoundTag();
            ta.remove("Purity");
            tb.remove("Purity");
            return ta.equals(tb);
        }
    }

    final FluidTank inputTank = new PurityBlendingTank(TANK_SIZE, f -> f.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    final FluidTank outputTank = new PurityBlendingTank(TANK_SIZE, f -> f.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() { setChanged(); }
    };

    final ItemStackHandler inventory = new ItemStackHandler(1) {
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

    public FilterFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FILTER_FRAME_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FilterFrameBlockEntity be) {
        if (level.isClientSide()) return;
        be.doFilter();
    }

    @Override
    public ItemStack getFilterCore() {
        return inventory.getStackInSlot(0);
    }

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

    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }
    public IItemHandler getInventoryHandler() { return inventory; }

    private void doFilter() {
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
        if (resource.isEmpty() || !resource.getFluid().isSame(Fluids.WATER)) return;

        int p1 = FluidPurityHelper.getPurity(resource);
        int maxP = FilterCoreItem.getMaxPurity(core);
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
        FilterCoreItem.writeLifespan(core, newLifespan);
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

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    // Capability handlers — accessed directly by NeoForgeCommonSetup capability registration
    public final IFluidHandler inputFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return inputTank.getTanks(); }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return inputTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return inputTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return inputTank.isFluidValid(tank, stack); }
        @Override public int fill(FluidStack resource, FluidAction action) { return inputTank.fill(resource, action); }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return FluidStack.EMPTY; }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return FluidStack.EMPTY; }
    };

    public final IFluidHandler outputFluidHandler = new IFluidHandler() {
        @Override public int getTanks() { return outputTank.getTanks(); }
        @Override public @NotNull FluidStack getFluidInTank(int tank) { return outputTank.getFluidInTank(tank); }
        @Override public int getTankCapacity(int tank) { return outputTank.getTankCapacity(tank); }
        @Override public boolean isFluidValid(int tank, @NotNull FluidStack stack) { return false; }
        @Override public int fill(FluidStack resource, FluidAction action) { return 0; }
        @Override public @NotNull FluidStack drain(FluidStack resource, FluidAction action) { return outputTank.drain(resource, action); }
        @Override public @NotNull FluidStack drain(int maxDrain, FluidAction action) { return outputTank.drain(maxDrain, action); }
    };
}
