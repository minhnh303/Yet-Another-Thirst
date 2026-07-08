package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class FabricFluidStorage {

    private FabricFluidStorage() {}

    public static void register() {
        if (ModBlocks.FILTER_FRAME_BLOCK_ENTITY != null) {
            FluidStorage.SIDED.registerForBlockEntities((be, direction) -> {
                if (!(be instanceof FilterFrameBlockEntity filterFrame)) return null;
                BlockState state = filterFrame.getBlockState();
                if (!(state.getBlock() instanceof FilterFrameBlock)) return null;

                Direction facing = state.getValue(FilterFrameBlock.FACING);
                boolean reversed = state.hasProperty(FilterFrameBlock.REVERSED) && state.getValue(FilterFrameBlock.REVERSED);
                Direction inputSide = reversed ? facing : facing.getOpposite();
                Direction outputSide = reversed ? facing.getOpposite() : facing;

                if (direction == inputSide) {
                    return filterFrame.inputStorage;
                }
                if (direction == outputSide) {
                    return filterFrame.outputStorage;
                }
                return null;
            }, ModBlocks.FILTER_FRAME_BLOCK_ENTITY);
        }
    }
}
