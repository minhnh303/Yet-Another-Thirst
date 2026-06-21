package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class FilterFrameBlockEntity extends AbstractFilterFrameBlockEntity {

    // Input tank: water to be filtered
    private int inputAmount;
    private int inputPurity;
    // Tracks worst purity across inserts in current transaction; reset after each commit
    private int pendingInsertPurity = WaterPurity.MAX_PURITY;

    // Output tank: filtered water
    private int outputAmount;
    private int outputPurity;

    public final SingleVariantStorage<FluidVariant> inputStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() { return FluidVariant.blank(); }

        @Override
        protected long getCapacity(FluidVariant variant) { return TANK_SIZE * 81L; }

        @Override
        protected boolean canExtract(FluidVariant variant) { return false; }

        @Override
        protected boolean canInsert(FluidVariant variant) { return variant.getFluid() == Fluids.WATER; }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.getFluid() != Fluids.WATER) return 0;
            // Capture purity from NBT before stripping it; track worst-case across inserts
            CompoundTag tag = resource.getNbt();
            int purity = (tag != null && tag.contains("Purity")) ? tag.getInt("Purity") : WaterPurity.MIN_PURITY;
            pendingInsertPurity = Math.min(pendingInsertPurity, purity);
            // Always insert as plain water so the stored variant never has NBT.
            // This prevents variant mismatch when filter() writes tagged water back via updateStorageFromFields().
            return super.insert(FluidVariant.of(Fluids.WATER), maxAmount, transaction);
        }

        @Override
        protected void onFinalCommit() {
            if (variant.isBlank() || amount == 0) {
                inputAmount = 0;
                inputPurity = 0;
            } else {
                inputPurity = inputAmount == 0 ? pendingInsertPurity : Math.min(inputPurity, pendingInsertPurity);
                inputAmount = (int) (amount / 81);
            }
            pendingInsertPurity = WaterPurity.MAX_PURITY;
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public final SingleVariantStorage<FluidVariant> outputStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() { return FluidVariant.blank(); }

        @Override
        protected long getCapacity(FluidVariant variant) { return TANK_SIZE * 81L; }

        @Override
        protected boolean canInsert(FluidVariant variant) { return false; }

        @Override
        protected void onFinalCommit() {
            outputAmount = (int) (amount / 81);
            if (variant.isBlank() || amount == 0) {
                outputPurity = 0;
            } else {
                CompoundTag tag = variant.getNbt();
                outputPurity = (tag != null && tag.contains("Purity")) ? tag.getInt("Purity") : ThirstConfig.DEFAULT_PURITY;
            }
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public void updateStorageFromFields() {
        if (inputAmount > 0) {
            // Plain water (no NBT) so pumps can always insert without variant mismatch.
            // Purity is tracked separately in inputPurity.
            inputStorage.variant = FluidVariant.of(Fluids.WATER);
            inputStorage.amount = inputAmount * 81L;
        } else {
            inputStorage.variant = FluidVariant.blank();
            inputStorage.amount = 0;
        }

        if (outputAmount > 0) {
            CompoundTag fluidTag = new CompoundTag();
            fluidTag.putInt("Purity", outputPurity);
            outputStorage.variant = FluidVariant.of(Fluids.WATER, fluidTag);
            outputStorage.amount = outputAmount * 81L;
        } else {
            outputStorage.variant = FluidVariant.blank();
            outputStorage.amount = 0;
        }
    }

    private final SimpleContainer inventory = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            FilterFrameBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                updateBlockState();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public FilterFrameBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.FILTER_FRAME_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FilterFrameBlockEntity blockEntity) {
        if (level.isClientSide()) return;
        blockEntity.filter();
    }

    @Override
    public ItemStack getFilterCore() {
        return inventory.getItem(0);
    }

    public SimpleContainer getInventory() {
        return inventory;
    }

    public int getInputAmount() { return inputAmount; }
    public int getInputPurity() { return inputPurity; }
    public int getOutputAmount() { return outputAmount; }
    public int getOutputPurity() { return outputPurity; }
    public int getTankCapacity() { return TANK_SIZE; }

    public int fillInput(int amount, int purity) {
        int space = TANK_SIZE - inputAmount;
        int filled = Math.min(space, amount);
        if (filled > 0) {
            inputPurity = inputAmount == 0 ? purity : Math.min(inputPurity, purity);
            inputAmount += filled;
            setChanged();
            updateStorageFromFields();
        }
        return filled;
    }

    public void drainOutput(int amount) {
        outputAmount = Math.max(0, outputAmount - amount);
        if (outputAmount == 0) outputPurity = 0;
        setChanged();
        updateStorageFromFields();
    }

    @Override
    public void setFilterCore(ItemStack stack) {
        inventory.setItem(0, stack);
        updateBlockState();
    }

    @Override
    public ItemStack removeFilterCore() {
        ItemStack stack = inventory.removeItem(0, 64);
        updateBlockState();
        return stack;
    }

    private void filter() {
        ItemStack core = inventory.getItem(0);
        if (core.isEmpty() || isClogged(core)) return;

        double remainingLifespan = getLifespan(core);
        if (remainingLifespan <= 0) {
            clogFilter(core.getItem());
            return;
        }

        int amountToFilter = Math.min(ThirstConfig.FILTER_BASE_SPEED, inputAmount);
        amountToFilter = Math.min(amountToFilter, TANK_SIZE - outputAmount);
        if (amountToFilter <= 0) return;

        int maxP = getMaxPurity(core);
        if (inputPurity >= maxP) return;

        double decayPerMb = getDecayMultiplier(inputPurity);
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

        int outPurity = Math.min(inputPurity + 1, maxP);
        inputAmount -= amountToFilter;
        if (inputAmount <= 0) { inputAmount = 0; inputPurity = 0; }
        outputPurity = outputAmount == 0 ? outPurity : Math.min(outputPurity, outPurity);
        outputAmount += amountToFilter;

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        updateStorageFromFields();
    }

    private void clogFilter(Item coreItem) {
        inventory.setItem(0, new ItemStack(getCloggedVariant(coreItem)));
        updateBlockState();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag inputTag = tag.getCompound("InputTank");
        inputAmount = inputTag.getInt("Amount");
        inputPurity = inputTag.getInt("Purity");
        CompoundTag outputTag = tag.getCompound("OutputTank");
        outputAmount = outputTag.getInt("Amount");
        outputPurity = outputTag.getInt("Purity");
        CompoundTag inv = tag.getCompound("Inventory");
        if (inv.contains("Item")) {
            inventory.setItem(0, ItemStack.of(inv.getCompound("Item")));
        }
        updateStorageFromFields();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag inputTag = new CompoundTag();
        inputTag.putInt("Amount", inputAmount);
        inputTag.putInt("Purity", inputPurity);
        tag.put("InputTank", inputTag);
        CompoundTag outputTag = new CompoundTag();
        outputTag.putInt("Amount", outputAmount);
        outputTag.putInt("Purity", outputPurity);
        tag.put("OutputTank", outputTag);
        CompoundTag inv = new CompoundTag();
        ItemStack core = inventory.getItem(0);
        if (!core.isEmpty()) {
            inv.put("Item", core.save(new CompoundTag()));
        }
        tag.put("Inventory", inv);
    }
}
