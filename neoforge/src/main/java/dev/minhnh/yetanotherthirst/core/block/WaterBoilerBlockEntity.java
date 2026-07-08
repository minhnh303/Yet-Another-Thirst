package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.screen.WaterBoilerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaterBoilerBlockEntity extends BlockEntity implements MenuProvider, IWaterBoiler {

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
                    int purity = FluidPurityHelper.getPurity(resource);
                    FluidPurityHelper.addPurity(fluid, purity);
                    onContentsChanged();
                }
                return toFill;
            }
            if (!fluid.getFluid().isSame(resource.getFluid())) return 0;

            int p1 = FluidPurityHelper.getPurity(fluid);
            int p2 = FluidPurityHelper.getPurity(resource);
            if (p1 != p2) return 0;

            int currentAmount = fluid.getAmount();
            int space = capacity - currentAmount;
            if (space <= 0) return 0;
            int toFill = Math.min(space, resource.getAmount());
            if (action.execute()) {
                fluid.setAmount(currentAmount + toFill);
                FluidPurityHelper.addPurity(fluid, p1);
                onContentsChanged();
            }
            return toFill;
        }
    }

    final FluidTank inputTank = new PurityBlendingTank(ThirstConfig.WATER_BOILER_CAPACITY,
            f -> f.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide())
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    final FluidTank outputTank = new PurityBlendingTank(ThirstConfig.WATER_BOILER_CAPACITY,
            f -> f.getFluid().isSame(Fluids.WATER)) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide())
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    };

    final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) {
                return stack.getItem().getBurnTime(stack, RecipeType.SMELTING) > 0;
            }
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private long lastEnergyTick = -1;
    private int energyReceivedThisTick = 0;

    // Energy handler accessed by NeoForgeCommonSetup capability registration
    public final net.neoforged.neoforge.energy.IEnergyStorage energyHandlerImpl = new net.neoforged.neoforge.energy.IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (level == null || !canBoil()) return 0;
            long currentTick = level.getGameTime();
            int limit = ThirstConfig.WATER_BOILER_ENERGY_CONSUMPTION;
            int receivedThisTick = (currentTick == lastEnergyTick) ? energyReceivedThisTick : 0;
            int remaining = Math.max(0, limit - receivedThisTick);
            int toConsume = Math.min(remaining, maxReceive);
            if (toConsume > 0 && !simulate) {
                if (currentTick != lastEnergyTick) {
                    lastEnergyTick = currentTick;
                    energyReceivedThisTick = 0;
                }
                energyReceivedThisTick += toConsume;
                litTime = 3;
                litDuration = 2;
                setChanged();
            }
            return toConsume;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored() { return 0; }
        @Override public int getMaxEnergyStored() { return 0; }
        @Override public boolean canExtract() { return false; }
        @Override public boolean canReceive() { return true; }
    };

    // Fluid handlers accessed by NeoForgeCommonSetup capability registration
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

    // Item handlers exposed per-face via capability
    public final IItemHandler inventoryHandler = inventory;
    public final IItemHandler itemHandlerTop = new RangedWrapper(inventory, 0, 1);
    public final IItemHandler itemHandlerBottom = new RangedWrapper(inventory, 2, 3);
    public final IItemHandler itemHandlerSides = new RangedWrapper(inventory, 1, 2);

    private int litTime = 0;
    private int litDuration = 0;
    private int cookTime = 0;
    private int cookTimeTotal = ThirstConfig.WATER_BOILER_BOIL_TIME;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> cookTime;
                case 3 -> cookTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> cookTime = value;
                case 3 -> cookTimeTotal = value;
            }
        }

        @Override
        public int getCount() { return 4; }
    };

    public WaterBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterBoilerBlockEntity entity) {
        if (level.isClientSide()) return;

        entity.cookTimeTotal = ThirstConfig.WATER_BOILER_BOIL_TIME;
        boolean wasLit = state.getValue(WaterBoilerBlock.LIT);
        boolean changed = false;

        if (entity.litTime > 0) entity.litTime--;

        changed |= entity.processItemSlots();

        boolean canBoil = entity.canBoil();

        if (canBoil && !entity.isLit() && entity.hasFuel()) {
            entity.consumeFuel();
            changed = true;
        }

        if (entity.isLit() && canBoil) {
            entity.cookTime++;
            if (entity.cookTime >= entity.cookTimeTotal) {
                entity.cookTime = 0;
                entity.boilWater();
                changed = true;
            }
        } else {
            if (entity.cookTime > 0) entity.cookTime = Math.max(0, entity.cookTime - 2);
        }

        if (wasLit != entity.isLit()) {
            changed = true;
            state = state.setValue(WaterBoilerBlock.LIT, entity.isLit());
            level.setBlock(pos, state, 3);
        }

        if (changed) {
            entity.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public boolean isLit() { return this.litTime > 0; }

    private boolean canBoil() {
        return inputTank.getFluidAmount() > 0 && outputTank.getFluidAmount() < outputTank.getCapacity();
    }

    private boolean hasFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(1);
        return !fuelStack.isEmpty() && fuelStack.getItem().getBurnTime(fuelStack, RecipeType.SMELTING) > 0;
    }

    private void consumeFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(1);
        if (fuelStack.isEmpty()) return;
        int burnTime = fuelStack.getItem().getBurnTime(fuelStack, RecipeType.SMELTING);
        if (burnTime > 0) {
            this.litTime = burnTime;
            this.litDuration = burnTime;
            ItemStack remaining = fuelStack.getCraftingRemainingItem();
            fuelStack.shrink(1);
            if (fuelStack.isEmpty() && !remaining.isEmpty()) inventory.setStackInSlot(1, remaining);
        }
    }

    private void boilWater() {
        int amountToBoil = Math.min(1000, inputTank.getFluidAmount());
        amountToBoil = Math.min(amountToBoil, outputTank.getCapacity() - outputTank.getFluidAmount());
        if (amountToBoil <= 0) return;

        FluidStack moved = inputTank.drain(amountToBoil, IFluidHandler.FluidAction.EXECUTE);
        if (!moved.isEmpty()) {
            FluidPurityHelper.addPurity(moved, 3);
            outputTank.fill(moved, IFluidHandler.FluidAction.EXECUTE);
            FluidPurityHelper.addPurity(outputTank.getFluid(), 3);
        }
    }

    private boolean processItemSlots() {
        ItemStack inputStack = inventory.getStackInSlot(0);
        ItemStack outputStack = inventory.getStackInSlot(2);
        boolean changed = false;

        if (!inputStack.isEmpty()) {
            if (WaterPurity.isWaterFilledContainer(inputStack)) {
                int capacity = getCapacity(inputStack);
                int purity = WaterPurity.getPurity(inputStack);

                if (inputTank.getFluidAmount() > 0 && FluidPurityHelper.getPurity(inputTank.getFluid()) != purity) return false;

                FluidStack fluidToAdd = new FluidStack(Fluids.WATER, capacity);
                FluidPurityHelper.addPurity(fluidToAdd, purity);

                int filled = inputTank.fill(fluidToAdd, IFluidHandler.FluidAction.SIMULATE);
                if (filled == capacity) {
                    ItemStack empty = getEmptyContainer(inputStack);
                    if (!empty.isEmpty()) {
                        if (outputStack.isEmpty() || (ItemStack.isSameItem(outputStack, empty) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                            inputTank.fill(fluidToAdd, IFluidHandler.FluidAction.EXECUTE);
                            inventory.extractItem(0, 1, false);
                            if (outputStack.isEmpty()) inventory.insertItem(2, empty.copy(), false);
                            else outputStack.grow(1);
                            changed = true;
                        }
                    }
                }
            } else if (WaterPurity.isEmptyWaterContainer(inputStack)) {
                ItemStack filled = getFilledContainer(inputStack);
                if (!filled.isEmpty()) {
                    int capacity = getCapacity(filled);
                    if (outputTank.getFluidAmount() >= capacity) {
                        int purity = FluidPurityHelper.getPurity(outputTank.getFluid());
                        ItemStack filledWithPurity = WaterPurity.addPurity(filled.copy(), purity);

                        if (outputStack.isEmpty() || (ItemStack.isSameItem(outputStack, filledWithPurity)
                                && WaterPurity.waterBottleTagsMatchForStacking(outputStack, filledWithPurity)
                                && outputStack.getCount() < outputStack.getMaxStackSize())) {
                            outputTank.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
                            inventory.extractItem(0, 1, false);
                            if (outputStack.isEmpty()) inventory.insertItem(2, filledWithPurity, false);
                            else outputStack.grow(1);
                            changed = true;
                        }
                    }
                }
            } else if (inputStack.is(Items.WET_SPONGE)) {
                if (inputTank.getFluidAmount() > 0 && FluidPurityHelper.getPurity(inputTank.getFluid()) != 0) return false;

                FluidStack dirtyWater = new FluidStack(Fluids.WATER, 1000);
                FluidPurityHelper.addPurity(dirtyWater, 0);

                int filled = inputTank.fill(dirtyWater, IFluidHandler.FluidAction.SIMULATE);
                if (filled == 1000) {
                    ItemStack drySponge = new ItemStack(Items.SPONGE);
                    if (outputStack.isEmpty() || (outputStack.is(Items.SPONGE) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                        inputTank.fill(dirtyWater, IFluidHandler.FluidAction.EXECUTE);
                        inventory.extractItem(0, 1, false);
                        if (outputStack.isEmpty()) inventory.insertItem(2, drySponge, false);
                        else outputStack.grow(1);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private int getCapacity(ItemStack stack) {
        return (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) ? 1000 : 333;
    }

    private ItemStack getEmptyContainer(ItemStack filled) {
        if (filled.is(Items.WATER_BUCKET)) return new ItemStack(Items.BUCKET);
        if (filled.is(Items.POTION) && isWaterPotion(filled)) return new ItemStack(Items.GLASS_BOTTLE);
        if (filled.is(ModItems.TERRACOTTA_WATER_BOWL.get())) return new ItemStack(ModItems.TERRACOTTA_BOWL.get());
        if (filled.is(ModItems.WOODEN_WATER_BOWL.get())) return new ItemStack(Items.BOWL);
        return ItemStack.EMPTY;
    }

    private ItemStack getFilledContainer(ItemStack empty) {
        if (empty.is(Items.BUCKET)) return new ItemStack(Items.WATER_BUCKET);
        if (empty.is(Items.GLASS_BOTTLE)) {
            ItemStack potion = new ItemStack(Items.POTION);
            potion.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
            return potion;
        }
        if (empty.is(ModItems.TERRACOTTA_BOWL.get())) return new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get());
        if (empty.is(Items.BOWL)) return new ItemStack(ModItems.WOODEN_WATER_BOWL.get());
        return ItemStack.EMPTY;
    }

    private static boolean isWaterPotion(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        return contents != null && contents.is(Potions.WATER);
    }

    public FluidTank getInputTank() { return inputTank; }
    public FluidTank getOutputTank() { return outputTank; }
    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getDataAccess() { return dataAccess; }

    // IWaterBoiler

    @Override
    public boolean isUpper() {
        BlockState state = getBlockState();
        return state.hasProperty(WaterBoilerBlock.HALF) && state.getValue(WaterBoilerBlock.HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public IWaterBoiler getLowerEntity(Level level) {
        if (!isUpper()) return this;
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return (be instanceof WaterBoilerBlockEntity w) ? w : null;
    }

    @Override
    public int getInputAmount() { return inputTank.getFluidAmount(); }

    @Override
    public int getInputCapacity() { return ThirstConfig.WATER_BOILER_CAPACITY; }

    @Override
    public int getInputPurity() { return FluidPurityHelper.getPurity(inputTank.getFluid()); }

    @Override
    public int getOutputAmount() { return outputTank.getFluidAmount(); }

    @Override
    public int getOutputCapacity() { return ThirstConfig.WATER_BOILER_CAPACITY; }

    @Override
    public int getOutputPurity() { return FluidPurityHelper.getPurity(outputTank.getFluid()); }

    @Override
    public Direction getFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(WaterBoilerBlock.FACING) ? state.getValue(WaterBoilerBlock.FACING) : Direction.NORTH;
    }

    @Nullable
    public WaterBoilerBlockEntity getLowerEntity() {
        if (!isUpper()) return this;
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        return (be instanceof WaterBoilerBlockEntity w) ? w : null;
    }

    public boolean hammerUseSide(Direction side, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.Vec3 hitVec) {
        if (this.level == null || this.level.isClientSide()) return true;
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        if (!state.is(state.getBlock())) return false;
        if (side == state.getValue(WaterBoilerBlock.FACING)) return true;

        BlockPos otherPos = isUpper() ? pos.below() : pos.above();
        BlockState otherState = this.level.getBlockState(otherPos);

        BlockState newSelfState = state;
        BlockState newOtherState = otherState.is(state.getBlock()) ? otherState : null;
        WaterBoilerBlock.PortSide clickedPortSide = WaterBoilerBlock.PortSide.fromDirection(side);

        if (state.getValue(WaterBoilerBlock.INLET) == clickedPortSide) {
            newSelfState = newSelfState.setValue(WaterBoilerBlock.INLET, WaterBoilerBlock.PortSide.NONE);
            if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.OUTLET) != WaterBoilerBlock.PortSide.NONE)
                newOtherState = newOtherState.setValue(WaterBoilerBlock.OUTLET, WaterBoilerBlock.PortSide.NONE);
            newSelfState = newSelfState.setValue(WaterBoilerBlock.OUTLET, clickedPortSide);
        } else if (state.getValue(WaterBoilerBlock.OUTLET) == clickedPortSide) {
            newSelfState = newSelfState.setValue(WaterBoilerBlock.OUTLET, WaterBoilerBlock.PortSide.NONE);
            if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.PORT) != WaterBoilerBlock.PortSide.NONE)
                newOtherState = newOtherState.setValue(WaterBoilerBlock.PORT, WaterBoilerBlock.PortSide.NONE);
            newSelfState = newSelfState.setValue(WaterBoilerBlock.PORT, clickedPortSide);
        } else if (state.getValue(WaterBoilerBlock.PORT) == clickedPortSide) {
            newSelfState = newSelfState.setValue(WaterBoilerBlock.PORT, WaterBoilerBlock.PortSide.NONE);
        } else {
            if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.INLET) != WaterBoilerBlock.PortSide.NONE)
                newOtherState = newOtherState.setValue(WaterBoilerBlock.INLET, WaterBoilerBlock.PortSide.NONE);
            newSelfState = newSelfState.setValue(WaterBoilerBlock.INLET, clickedPortSide);
        }

        this.level.setBlock(pos, newSelfState, 3);
        if (newOtherState != null) this.level.setBlock(otherPos, newOtherState, 3);
        this.level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.5F);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.yet_another_thirst.water_boiler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new WaterBoilerMenu(id, inventory, this, this.dataAccess);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (isUpper()) return;
        inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        outputTank.readFromNBT(registries, tag.getCompound("OutputTank"));
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        cookTime = tag.getInt("CookTime");
        cookTimeTotal = tag.getInt("CookTimeTotal");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (isUpper()) return;
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("CookTime", cookTime);
        tag.putInt("CookTimeTotal", cookTimeTotal);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt,
                             HolderLookup.Provider lookupProvider) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) loadAdditional(tag, lookupProvider);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        if (tag != null) loadAdditional(tag, lookupProvider);
    }
}
