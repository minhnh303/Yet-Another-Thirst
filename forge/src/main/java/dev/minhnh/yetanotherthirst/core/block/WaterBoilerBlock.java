package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"null", "deprecation"})
public class WaterBoilerBlock extends BaseEntityBlock {

    public enum PortSide implements net.minecraft.util.StringRepresentable {
        NONE("none", null),
        DOWN("down", Direction.DOWN),
        UP("up", Direction.UP),
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST),
        EAST("east", Direction.EAST);

        private final String name;
        private final Direction direction;

        PortSide(String name, Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        @Nullable
        public Direction getDirection() {
            return this.direction;
        }

        public static PortSide fromDirection(Direction dir) {
            if (dir == null) return NONE;
            switch (dir) {
                case DOWN: return DOWN;
                case UP: return UP;
                case NORTH: return NORTH;
                case SOUTH: return SOUTH;
                case WEST: return WEST;
                case EAST: return EAST;
                default: return NONE;
            }
        }
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<PortSide> INLET = EnumProperty.create("inlet", PortSide.class);
    public static final EnumProperty<PortSide> OUTLET = EnumProperty.create("outlet", PortSide.class);
    public static final EnumProperty<PortSide> PORT = EnumProperty.create("port", PortSide.class);

    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public WaterBoilerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(LIT, false)
                .setValue(INLET, PortSide.NONE)
                .setValue(OUTLET, PortSide.NONE)
                .setValue(PORT, PortSide.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            Direction facing = context.getHorizontalDirection().getOpposite();
            return defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(HALF, DoubleBlockHalf.LOWER)
                    .setValue(LIT, false)
                    .setValue(INLET, PortSide.NONE)
                    .setValue(OUTLET, PortSide.NONE)
                    .setValue(PORT, PortSide.NONE);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER)
                .setValue(INLET, PortSide.NONE)
                .setValue(OUTLET, PortSide.NONE)
                .setValue(PORT, PortSide.NONE), 3);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = (half == DoubleBlockHalf.LOWER) ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(otherPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, otherPos, getId(otherState));
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER == (direction == Direction.UP))) {
            return neighborState.is(this) && neighborState.getValue(HALF) != half ?
                    state.setValue(FACING, neighborState.getValue(FACING))
                           .setValue(LIT, neighborState.getValue(LIT)) :
                    Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos boilerPos = (state.getValue(HALF) == DoubleBlockHalf.UPPER) ? pos.below() : pos;
        BlockState boilerState = (state.getValue(HALF) == DoubleBlockHalf.UPPER) ? level.getBlockState(boilerPos) : state;
        
        ItemStack held = player.getItemInHand(hand);
        if (dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig.COMPAT_IE_HEATER && held.is(blusunrize.immersiveengineering.api.IETags.hammers)) {
            BlockEntity blockEntity = level.getBlockEntity(boilerPos);
            if (blockEntity instanceof WaterBoilerBlockEntity boiler) {
                if (boiler.hammerUseSide(hit.getDirection(), player, hand, hit.getLocation())) {
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        BlockEntity blockEntity = level.getBlockEntity(boilerPos);
        if (blockEntity instanceof WaterBoilerBlockEntity boiler) {
            
            if (WaterPurity.isWaterFilledContainer(held)) {
                int capacity = (held.is(Items.WATER_BUCKET)) ? 1000 : 333;
                int purity = WaterPurity.getPurity(held);
                
                boolean canFill = true;
                if (boiler.getInputTank().getFluidAmount() > 0) {
                    if (FluidPurityHelper.getPurity(boiler.getInputTank().getFluid()) != purity) {
                        canFill = false;
                    }
                }
                
                if (canFill) {
                    FluidStack fluidToAdd = new FluidStack(Fluids.WATER, capacity);
                    FluidPurityHelper.addPurity(fluidToAdd, purity);
                    
                    int filled = boiler.getInputTank().fill(fluidToAdd, IFluidHandler.FluidAction.SIMULATE);
                    if (filled == capacity) {
                        if (!level.isClientSide()) {
                            boiler.getInputTank().fill(fluidToAdd, IFluidHandler.FluidAction.EXECUTE);
                            ItemStack emptyStack = getEmptyContainer(held);
                            player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(held, player, emptyStack));
                            net.minecraft.sounds.SoundEvent sound = (capacity == 1000) ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY;
                            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                            boiler.setChanged();
                            level.sendBlockUpdated(boilerPos, boilerState, boilerState, 3);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
            else if (WaterPurity.isEmptyWaterContainer(held)) {
                ItemStack filledVariant = getFilledContainer(held);
                if (!filledVariant.isEmpty()) {
                    int capacity = (held.is(Items.BUCKET)) ? 1000 : 333;
                    if (boiler.getOutputTank().getFluidAmount() >= capacity) {
                        if (!level.isClientSide()) {
                            int purity = FluidPurityHelper.getPurity(boiler.getOutputTank().getFluid());
                            ItemStack filledWithPurity = WaterPurity.addPurity(filledVariant.copy(), purity);
                            
                            boiler.getOutputTank().drain(capacity, IFluidHandler.FluidAction.EXECUTE);
                            player.setItemInHand(hand, net.minecraft.world.item.ItemUtils.createFilledResult(held, player, filledWithPurity));
                            
                            net.minecraft.sounds.SoundEvent sound = (capacity == 1000) ? SoundEvents.BUCKET_FILL : SoundEvents.BOTTLE_FILL;
                            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                            boiler.setChanged();
                            level.sendBlockUpdated(boilerPos, boilerState, boilerState, 3);
                        }
                        return InteractionResult.sidedSuccess(level.isClientSide());
                    }
                }
            }
            
            if (!level.isClientSide()) {
                net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, boiler, buf -> buf.writeBlockPos(boilerPos));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static ItemStack getEmptyContainer(ItemStack filled) {
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

    private static ItemStack getFilledContainer(ItemStack empty) {
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

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof WaterBoilerBlockEntity boiler) {
                    for (int i = 0; i < boiler.getInventory().getSlots(); i++) {
                        Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), boiler.getInventory().getStackInSlot(i));
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING, HALF, LIT, INLET, OUTLET, PORT);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WaterBoilerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return createTickerHelper(type, ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(), WaterBoilerBlockEntity::tick);
        }
        return null;
    }
}
