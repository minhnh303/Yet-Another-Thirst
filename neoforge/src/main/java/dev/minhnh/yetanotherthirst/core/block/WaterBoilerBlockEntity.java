package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.screen.WaterBoilerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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


public class WaterBoilerBlockEntity extends BlockEntity implements MenuProvider, IWaterBoiler {

    private static class PurityBlendingTank extends FluidTank {
        public PurityBlendingTank(int capacity, java.util.function.Predicate<FluidStack> validator) {
            super(capacity, validator);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !isFluidValid(resource)) {
                return 0;
            }
            if (fluid.isEmpty()) {
                int toFill = Math.min(capacity, resource.getAmount());
                if (action.execute()) {
                    fluid = new FluidStack(resource, toFill);
                    onContentsChanged();
                }
                return toFill;
            }
            if (fluid.getFluid() != resource.getFluid()) {
                return 0;
            }

            CompoundTag tag1 = fluid.getTag();
            CompoundTag tag2 = resource.getTag();
            boolean tagsMatch = true;
            if (tag1 != null || tag2 != null) {
                CompoundTag copy1 = tag1 != null ? tag1.copy() : new CompoundTag();
                CompoundTag copy2 = tag2 != null ? tag2.copy() : new CompoundTag();
                copy1.remove("Purity");
                copy2.remove("Purity");
                if (!copy1.equals(copy2)) {
                    tagsMatch = false;
                }
            }

            if (!tagsMatch) {
                return 0;
            }

            int p1 = FluidPurityHelper.getPurity(fluid);
            int p2 = FluidPurityHelper.getPurity(resource);
            if (p1 != p2) {
                return 0;
            }

            int currentAmount = fluid.getAmount();
            int space = capacity - currentAmount;
            if (space <= 0) {
                return 0;
            }
            int toFill = Math.min(space, resource.getAmount());
            if (action.execute()) {
                int totalAmount = currentAmount + toFill;
                fluid.setAmount(totalAmount);
                FluidPurityHelper.addPurity(fluid, p1);
                onContentsChanged();
            }
            return toFill;
        }
    }

    private final FluidTank inputTank = new PurityBlendingTank(ThirstConfig.WATER_BOILER_CAPACITY, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank outputTank = new PurityBlendingTank(ThirstConfig.WATER_BOILER_CAPACITY, fluid -> fluid.getFluid() == Fluids.WATER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 1) {
                return net.minecraftforge.common.ForgeHooks.getBurnTime(stack, net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0;
            }
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private long lastEnergyTick = -1;
    private int energyReceivedThisTick = 0;

    private final net.minecraftforge.energy.IEnergyStorage energyHandlerImpl = new net.minecraftforge.energy.IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (level == null || !canBoil()) {
                return 0;
            }
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
                litTime = 2;
                litDuration = 2;
                setChanged();
            }
            return toConsume;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    };

    private final LazyOptional<IFluidHandler> inputHandler = LazyOptional.of(InputFluidHandler::new);
    private final LazyOptional<IFluidHandler> outputHandler = LazyOptional.of(OutputFluidHandler::new);
    private final LazyOptional<IItemHandler> inventoryHandler = LazyOptional.of(() -> inventory);
    private final LazyOptional<IItemHandler> itemHandlerTop = LazyOptional.of(() -> new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 0, 1));
    private final LazyOptional<IItemHandler> itemHandlerBottom = LazyOptional.of(() -> new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 2, 3));
    private final LazyOptional<IItemHandler> itemHandlerSides = LazyOptional.of(() -> new net.minecraftforge.items.wrapper.RangedWrapper(inventory, 1, 2));
    private final LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyHandler = LazyOptional.of(() -> energyHandlerImpl);

    private int litTime = 0;
    private int litDuration = 0;
    private int cookTime = 0;
    private int cookTimeTotal = ThirstConfig.WATER_BOILER_BOIL_TIME;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            switch (index) {
                case 0: return litTime;
                case 1: return litDuration;
                case 2: return cookTime;
                case 3: return cookTimeTotal;
                default: return 0;
            }
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0: litTime = value; break;
                case 1: litDuration = value; break;
                case 2: cookTime = value; break;
                case 3: cookTimeTotal = value; break;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public WaterBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterBoilerBlockEntity entity) {
        if (level.isClientSide()) return;

        entity.cookTimeTotal = ThirstConfig.WATER_BOILER_BOIL_TIME;

        boolean wasLit = state.getValue(WaterBoilerBlock.LIT);
        boolean changed = false;

        if (entity.litTime > 0) {
            entity.litTime--;
        }

        changed |= entity.processItemSlots();

        boolean canBoil = entity.canBoil();



        // Try to heat using solid fuel if not lit
        if (canBoil && !entity.isLit() && entity.hasFuel()) {
            entity.consumeFuel();
            changed = true;
        }

        // 3. If lit, progress the boiling
        if (entity.isLit() && canBoil) {
            entity.cookTime++;
            if (entity.cookTime >= entity.cookTimeTotal) {
                entity.cookTime = 0;
                entity.boilWater();
                changed = true;
            }
        } else {
            if (entity.cookTime > 0) {
                entity.cookTime = Math.max(0, entity.cookTime - 2);
            }
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



    public boolean isLit() {
        return this.litTime > 0;
    }

    private boolean canBoil() {
        return inputTank.getFluidAmount() > 0 && outputTank.getFluidAmount() < outputTank.getCapacity();
    }

    private boolean hasFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(1);
        return !fuelStack.isEmpty() && net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, net.minecraft.world.item.crafting.RecipeType.SMELTING) > 0;
    }

    private void consumeFuel() {
        ItemStack fuelStack = inventory.getStackInSlot(1);
        if (!fuelStack.isEmpty()) {
            int burnTime = net.minecraftforge.common.ForgeHooks.getBurnTime(fuelStack, net.minecraft.world.item.crafting.RecipeType.SMELTING);
            if (burnTime > 0) {
                this.litTime = burnTime;
                this.litDuration = burnTime;
                ItemStack remaining = fuelStack.getCraftingRemainingItem();
                fuelStack.shrink(1);
                if (fuelStack.isEmpty() && !remaining.isEmpty()) {
                    inventory.setStackInSlot(1, remaining);
                }
            }
        }
    }

    private void boilWater() {
        // Boil 1000mB (or whatever is left in inputTank) per 200 ticks
        int amountToBoil = Math.min(1000, inputTank.getFluidAmount());
        amountToBoil = Math.min(amountToBoil, outputTank.getCapacity() - outputTank.getFluidAmount());
        if (amountToBoil <= 0) return;

        FluidStack moved = inputTank.drain(amountToBoil, IFluidHandler.FluidAction.EXECUTE);
        if (!moved.isEmpty()) {
            // boiled water becomes fully Purified (3)
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
            // 1. Drain water container into inputTank
            if (WaterPurity.isWaterFilledContainer(inputStack)) {
                int capacity = getCapacity(inputStack);
                int purity = WaterPurity.getPurity(inputStack);

                if (inputTank.getFluidAmount() > 0 && FluidPurityHelper.getPurity(inputTank.getFluid()) != purity) {
                    return false;
                }

                FluidStack fluidToAdd = new FluidStack(Fluids.WATER, capacity);
                FluidPurityHelper.addPurity(fluidToAdd, purity);

                int filled = inputTank.fill(fluidToAdd, IFluidHandler.FluidAction.SIMULATE);
                if (filled == capacity) {
                    ItemStack empty = getEmptyContainer(inputStack);
                    if (!empty.isEmpty()) {
                        if (outputStack.isEmpty() || (ItemStack.isSameItem(outputStack, empty) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                            inputTank.fill(fluidToAdd, IFluidHandler.FluidAction.EXECUTE);
                            inventory.extractItem(0, 1, false);
                            if (outputStack.isEmpty()) {
                                inventory.insertItem(2, empty.copy(), false);
                            } else {
                                outputStack.grow(1);
                            }
                            changed = true;
                        }
                    }
                }
            }
            // 2. Fill empty container from outputTank
            else if (WaterPurity.isEmptyWaterContainer(inputStack)) {
                ItemStack filled = getFilledContainer(inputStack);
                if (!filled.isEmpty()) {
                    int capacity = getCapacity(filled);
                    if (outputTank.getFluidAmount() >= capacity) {
                        int purity = FluidPurityHelper.getPurity(outputTank.getFluid());
                        ItemStack filledWithPurity = WaterPurity.addPurity(filled.copy(), purity);

                        if (outputStack.isEmpty() || (ItemStack.isSameItem(outputStack, filledWithPurity) && WaterPurity.waterBottleTagsMatchForStacking(outputStack, filledWithPurity) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                            outputTank.drain(capacity, IFluidHandler.FluidAction.EXECUTE);
                            inventory.extractItem(0, 1, false);
                            if (outputStack.isEmpty()) {
                                inventory.insertItem(2, filledWithPurity, false);
                            } else {
                                outputStack.grow(1);
                            }
                            changed = true;
                        }
                    }
                }
            }
            // 3. Dry wet sponge
            else if (inputStack.is(Items.WET_SPONGE)) {
                if (inputTank.getFluidAmount() > 0 && FluidPurityHelper.getPurity(inputTank.getFluid()) != 0) {
                    return false;
                }

                FluidStack dirtyWater = new FluidStack(Fluids.WATER, 1000);
                FluidPurityHelper.addPurity(dirtyWater, 0);

                int filled = inputTank.fill(dirtyWater, IFluidHandler.FluidAction.SIMULATE);
                if (filled == 1000) {
                    ItemStack drySponge = new ItemStack(Items.SPONGE);
                    if (outputStack.isEmpty() || (outputStack.is(Items.SPONGE) && outputStack.getCount() < outputStack.getMaxStackSize())) {
                        inputTank.fill(dirtyWater, IFluidHandler.FluidAction.EXECUTE);
                        inventory.extractItem(0, 1, false);
                        if (outputStack.isEmpty()) {
                            inventory.insertItem(2, drySponge, false);
                        } else {
                            outputStack.grow(1);
                        }
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

    private int getCapacity(ItemStack stack) {
        if (stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET)) {
            return 1000;
        }
        return 333;
    }

    private ItemStack getEmptyContainer(ItemStack filled) {
        if (filled.is(Items.WATER_BUCKET)) {
            return new ItemStack(Items.BUCKET);
        }
        if (filled.is(Items.POTION) && PotionUtils.getPotion(filled) == Potions.WATER) {
            return new ItemStack(Items.GLASS_BOTTLE);
        }
        if (filled.is(ModItems.TERRACOTTA_WATER_BOWL.get())) {
            return new ItemStack(ModItems.TERRACOTTA_BOWL.get());
        }
        if (filled.is(ModItems.WOODEN_WATER_BOWL.get())) {
            return new ItemStack(Items.BOWL);
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getFilledContainer(ItemStack empty) {
        if (empty.is(Items.BUCKET)) {
            return new ItemStack(Items.WATER_BUCKET);
        }
        if (empty.is(Items.GLASS_BOTTLE)) {
            return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        }
        if (empty.is(ModItems.TERRACOTTA_BOWL.get())) {
            return new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get());
        }
        if (empty.is(Items.BOWL)) {
            return new ItemStack(ModItems.WOODEN_WATER_BOWL.get());
        }
        return ItemStack.EMPTY;
    }

    public FluidTank getInputTank() {
        return inputTank;
    }

    public FluidTank getOutputTank() {
        return outputTank;
    }

    @Override
    public int getInputAmount() {
        return inputTank.getFluidAmount();
    }

    @Override
    public int getInputPurity() {
        return dev.minhnh.yetanotherthirst.compat.FluidPurityHelper.getPurity(inputTank.getFluid());
    }

    @Override
    public int getOutputAmount() {
        return outputTank.getFluidAmount();
    }

    @Override
    public int getOutputPurity() {
        return dev.minhnh.yetanotherthirst.compat.FluidPurityHelper.getPurity(outputTank.getFluid());
    }

    @Override
    public int getTankCapacity() {
        return ThirstConfig.WATER_BOILER_CAPACITY;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }



    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (isUpper()) {
            return;
        }
        inputTank.readFromNBT(tag.getCompound("InputTank"));
        outputTank.readFromNBT(tag.getCompound("OutputTank"));
        inventory.deserializeNBT(tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        cookTime = tag.getInt("CookTime");
        cookTimeTotal = tag.getInt("CookTimeTotal");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (isUpper()) {
            return;
        }
        tag.put("InputTank", inputTank.writeToNBT(new CompoundTag()));
        tag.put("OutputTank", outputTank.writeToNBT(new CompoundTag()));
        tag.put("Inventory", inventory.serializeNBT());
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
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        if (tag != null) {
            load(tag);
        }
    }

    public boolean isUpper() {
        BlockState state = getBlockState();
        return state.hasProperty(WaterBoilerBlock.HALF) && state.getValue(WaterBoilerBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER;
    }

    @Nullable
    public WaterBoilerBlockEntity getLowerEntity() {
        if (!isUpper()) return this;
        if (level == null) return null;
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        if (be instanceof WaterBoilerBlockEntity) {
            return (WaterBoilerBlockEntity) be;
        }
        return null;
    }

    public boolean hammerUseSide(Direction side, Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.Vec3 hitVec) {
        if (this.level != null && !this.level.isClientSide()) {
            BlockPos pos = this.getBlockPos();
            BlockState state = this.getBlockState();
            if (!state.is(state.getBlock())) return false;
            
            if (side == state.getValue(WaterBoilerBlock.FACING)) return true;

            BlockPos otherPos = isUpper() ? pos.below() : pos.above();
            BlockState otherState = this.level.getBlockState(otherPos);

            BlockState newSelfState = state;
            BlockState newOtherState = otherState.is(state.getBlock()) ? otherState : null;
            WaterBoilerBlock.PortSide clickedPortSide = WaterBoilerBlock.PortSide.fromDirection(side);
            String halfStr = isUpper() ? "upper" : "lower";

            if (state.getValue(WaterBoilerBlock.INLET) == clickedPortSide) {
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.INLET, WaterBoilerBlock.PortSide.NONE);
                 if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.OUTLET) != WaterBoilerBlock.PortSide.NONE) {
                     newOtherState = newOtherState.setValue(WaterBoilerBlock.OUTLET, WaterBoilerBlock.PortSide.NONE);
                 }
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.OUTLET, clickedPortSide);
            } else if (state.getValue(WaterBoilerBlock.OUTLET) == clickedPortSide) {
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.OUTLET, WaterBoilerBlock.PortSide.NONE);
                 if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.PORT) != WaterBoilerBlock.PortSide.NONE) {
                     newOtherState = newOtherState.setValue(WaterBoilerBlock.PORT, WaterBoilerBlock.PortSide.NONE);
                 }
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.PORT, clickedPortSide);
            } else if (state.getValue(WaterBoilerBlock.PORT) == clickedPortSide) {
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.PORT, WaterBoilerBlock.PortSide.NONE);
            } else {
                 if (newOtherState != null && newOtherState.getValue(WaterBoilerBlock.INLET) != WaterBoilerBlock.PortSide.NONE) {
                     newOtherState = newOtherState.setValue(WaterBoilerBlock.INLET, WaterBoilerBlock.PortSide.NONE);
                 }
                 newSelfState = newSelfState.setValue(WaterBoilerBlock.INLET, clickedPortSide);
            }

            this.level.setBlock(pos, newSelfState, 3);
            if (newOtherState != null) {
                this.level.setBlock(otherPos, newOtherState, 3);
            }
            this.level.playSound(null, pos, net.minecraft.sounds.SoundEvents.ANVIL_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 1.5F);
            return true;
        }
        return true;
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        BlockState state = getBlockState();
        if (!state.is(state.getBlock())) {
            return super.getCapability(cap, side);
        }

        WaterBoilerBlockEntity lower = getLowerEntity();
        if (lower == null) {
            return super.getCapability(cap, side);
        }

        if (side != null) {
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                if (state.hasProperty(WaterBoilerBlock.INLET) && state.getValue(WaterBoilerBlock.INLET).getDirection() == side) {
                    return lower.inputHandler.cast();
                }
                if (state.hasProperty(WaterBoilerBlock.OUTLET) && state.getValue(WaterBoilerBlock.OUTLET).getDirection() == side) {
                    return lower.outputHandler.cast();
                }
                return LazyOptional.empty();
            }
            if (cap == ForgeCapabilities.ENERGY) {
                if (state.hasProperty(WaterBoilerBlock.PORT) && state.getValue(WaterBoilerBlock.PORT).getDirection() == side) {
                    return lower.energyHandler.cast();
                }
                return LazyOptional.empty();
            }
        } else {
            if (cap == ForgeCapabilities.FLUID_HANDLER) {
                return lower.inputHandler.cast();
            }
            if (cap == ForgeCapabilities.ENERGY) {
                return lower.energyHandler.cast();
            }
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (isUpper()) {
                return lower.getCapability(cap, side);
            }
            if (side == Direction.UP) {
                return itemHandlerTop.cast();
            } else if (side == Direction.DOWN) {
                return itemHandlerBottom.cast();
            } else if (side != null) {
                return itemHandlerSides.cast();
            }
            return inventoryHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputHandler.invalidate();
        outputHandler.invalidate();
        inventoryHandler.invalidate();
        itemHandlerTop.invalidate();
        itemHandlerBottom.invalidate();
        itemHandlerSides.invalidate();
        energyHandler.invalidate();
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

    private final class InputFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return inputTank.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return inputTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return inputTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return inputTank.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return inputTank.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private final class OutputFluidHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return outputTank.getTanks();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return outputTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return outputTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return outputTank.drain(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return outputTank.drain(maxDrain, action);
        }
    }
}
