package dev.minhnh.yetanotherthirst.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Stub for the Water Boiler block on Fabric.
 * Immersive Engineering has no Fabric port for 1.21, so this is never registered at runtime.
 */
public class WaterBoilerBlock extends BaseEntityBlock {

    public WaterBoilerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends WaterBoilerBlock> codec() {
        return simpleCodec(WaterBoilerBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterBoilerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.WATER_BOILER_BLOCK_ENTITY, WaterBoilerBlockEntity::tick);
    }
}
