package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFilterFrameBlockEntity extends BlockEntity {

    protected static final int TANK_SIZE = 1000;

    protected AbstractFilterFrameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract ItemStack getFilterCore();
    public abstract void setFilterCore(ItemStack stack);
    public abstract ItemStack removeFilterCore();

    public static boolean isFilterCore(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.FABRIC_FILTER_CORE.get()
                || item == ModItems.SAND_FILTER_CORE.get()
                || item == ModItems.CARBON_FILTER_CORE.get()
                || isClogged(stack);
    }

    public static boolean isClogged(ItemStack stack) {
        Item item = stack.getItem();
        return item == ModItems.CLOGGED_FABRIC_FILTER.get()
                || item == ModItems.CLOGGED_SAND_FILTER.get()
                || item == ModItems.CLOGGED_CARBON_FILTER.get();
    }

    public static FilterCoreType getCoreTypeFromStack(ItemStack stack) {
        if (stack.isEmpty()) return FilterCoreType.EMPTY;
        Item item = stack.getItem();
        if (item == ModItems.FABRIC_FILTER_CORE.get()) return FilterCoreType.FABRIC;
        if (item == ModItems.SAND_FILTER_CORE.get()) return FilterCoreType.SAND;
        if (item == ModItems.CARBON_FILTER_CORE.get()) return FilterCoreType.CARBON;
        if (item == ModItems.CLOGGED_FABRIC_FILTER.get()) return FilterCoreType.CLOGGED_FABRIC;
        if (item == ModItems.CLOGGED_SAND_FILTER.get()) return FilterCoreType.CLOGGED_SAND;
        if (item == ModItems.CLOGGED_CARBON_FILTER.get()) return FilterCoreType.CLOGGED_CARBON;
        return FilterCoreType.EMPTY;
    }

    protected static double getLifespan(ItemStack stack) {
        double maxL = FilterCoreItem.getMaxLifespan(stack);
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains("Lifespan") ? data.getDouble("Lifespan") : maxL;
    }

    protected void clogFilter(Item coreItem) {
        setFilterCore(new ItemStack(getCloggedVariant(coreItem)));
        updateBlockState();
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    protected Item getCloggedVariant(Item cleanItem) {
        if (cleanItem == ModItems.FABRIC_FILTER_CORE.get()) return ModItems.CLOGGED_FABRIC_FILTER.get();
        if (cleanItem == ModItems.SAND_FILTER_CORE.get()) return ModItems.CLOGGED_SAND_FILTER.get();
        if (cleanItem == ModItems.CARBON_FILTER_CORE.get()) return ModItems.CLOGGED_CARBON_FILTER.get();
        return cleanItem;
    }

    protected double getDecayMultiplier(int purity) {
        if (purity == 0) return ThirstConfig.DIRTY_WATER_DECAY_MULTIPLIER;
        if (purity == 1) return ThirstConfig.SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER;
        if (purity == 2) return ThirstConfig.ACCEPTABLE_WATER_DECAY_MULTIPLIER;
        return 1.0;
    }

    public void updateBlockState() {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof AbstractFilterFrameBlock)) return;
        FilterCoreType type = getCoreTypeFromStack(getFilterCore());
        if (state.getValue(AbstractFilterFrameBlock.CORE_TYPE) != type) {
            level.setBlock(worldPosition, state.setValue(AbstractFilterFrameBlock.CORE_TYPE, type), 3);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, pRegistries);
        return tag;
    }
}
