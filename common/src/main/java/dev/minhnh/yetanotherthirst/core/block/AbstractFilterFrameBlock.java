package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFilterFrameBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<FilterCoreType> CORE_TYPE = EnumProperty.create("core", FilterCoreType.class);
    public static final BooleanProperty REVERSED = BooleanProperty.create("reversed");
    private static final VoxelShape SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

    protected AbstractFilterFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(CORE_TYPE, FilterCoreType.EMPTY).setValue(REVERSED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(REVERSED, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate((Direction) state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, CORE_TYPE, REVERSED);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AbstractFilterFrameBlockEntity filter)) return InteractionResult.PASS;

        ItemStack held = player.getMainHandItem();
        ItemStack existing = filter.getFilterCore();

        if (existing.isEmpty()) {
            if (AbstractFilterFrameBlockEntity.isFilterCore(held) && !AbstractFilterFrameBlockEntity.isClogged(held)) {
                ItemStack toInsert = held.copy();
                toInsert.setCount(1);
                filter.setFilterCore(toInsert);
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                ModAdvancements.award((ServerPlayer) player, ModAdvancements.FILTER_FRAME);
                return InteractionResult.CONSUME;
            }
        } else {
            boolean existingClogged = AbstractFilterFrameBlockEntity.isClogged(existing);
            boolean heldClean = AbstractFilterFrameBlockEntity.isFilterCore(held) && !AbstractFilterFrameBlockEntity.isClogged(held);

            if (existingClogged && heldClean) {
                ItemStack clogged = filter.removeFilterCore();
                ItemStack cleanInsert = held.copy();
                cleanInsert.setCount(1);
                filter.setFilterCore(cleanInsert);
                if (!player.getAbilities().instabuild) held.shrink(1);
                if (!player.getInventory().add(clogged)) player.drop(clogged, false);
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                ModAdvancements.award((ServerPlayer) player, ModAdvancements.CLOGGED_FILTER);
                return InteractionResult.CONSUME;
            } else if (held.isEmpty() || existingClogged) {
                ItemStack extracted = filter.removeFilterCore();
                if (!extracted.isEmpty()) {
                    if (!player.getInventory().add(extracted)) player.drop(extracted, false);
                    level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (AbstractFilterFrameBlockEntity.isClogged(extracted)) {
                        ModAdvancements.award((ServerPlayer) player, ModAdvancements.CLOGGED_FILTER);
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractFilterFrameBlockEntity filter) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), filter.getFilterCore());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public abstract @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public abstract @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type);
}
