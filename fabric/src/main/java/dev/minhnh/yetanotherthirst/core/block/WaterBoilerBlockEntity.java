package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Stub for the Water Boiler block entity on Fabric.
 * Immersive Engineering has no Fabric port for 1.21, so this is never registered at runtime.
 * Kept for source-level compatibility.
 */
public class WaterBoilerBlockEntity extends BlockEntity implements IWaterBoiler {

    private int inputAmount;
    private int outputAmount;
    private int outputPurity;

    public WaterBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.WATER_BOILER_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WaterBoilerBlockEntity be) {
        // no-op on Fabric (IE not available)
    }

    @Override public boolean isUpper() { return false; }
    @Override public IWaterBoiler getLowerEntity(Level level) { return this; }
    @Override public int getInputAmount() { return inputAmount; }
    @Override public int getInputCapacity() { return ThirstConfig.WATER_BOILER_CAPACITY; }
    @Override public int getInputPurity() { return 0; }
    @Override public int getOutputAmount() { return outputAmount; }
    @Override public int getOutputCapacity() { return ThirstConfig.WATER_BOILER_CAPACITY; }
    @Override public int getOutputPurity() { return outputPurity; }
    @Override public Direction getFacing() { return Direction.NORTH; }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }
}
