package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.AbstractFilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlock;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlockEntity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class NeoForgeCommonSetup {

    private NeoForgeCommonSetup() {}

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        if (ModBlocks.WATER_BOILER_BLOCK_ENTITY != null) {
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(),
                    (be, side) -> {
                        WaterBoilerBlockEntity lower = be.getLowerEntity();
                        if (lower == null) return null;
                        if (side == Direction.UP) return lower.itemHandlerTop;
                        if (side == Direction.DOWN) return lower.itemHandlerBottom;
                        if (side != null) return lower.itemHandlerSides;
                        return lower.inventoryHandler;
                    }
            );

            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(),
                    (be, side) -> {
                        BlockState state = be.getBlockState();
                        WaterBoilerBlockEntity lower = be.getLowerEntity();
                        if (lower == null) return null;
                        if (side != null) {
                            if (state.hasProperty(WaterBoilerBlock.INLET)
                                    && state.getValue(WaterBoilerBlock.INLET).getDirection() == side)
                                return lower.inputFluidHandler;
                            if (state.hasProperty(WaterBoilerBlock.OUTLET)
                                    && state.getValue(WaterBoilerBlock.OUTLET).getDirection() == side)
                                return lower.outputFluidHandler;
                            return null;
                        }
                        return lower.inputFluidHandler;
                    }
            );

            event.registerBlockEntity(
                    Capabilities.EnergyStorage.BLOCK,
                    ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(),
                    (be, side) -> {
                        BlockState state = be.getBlockState();
                        WaterBoilerBlockEntity lower = be.getLowerEntity();
                        if (lower == null) return null;
                        if (side != null) {
                            if (state.hasProperty(WaterBoilerBlock.PORT)
                                    && state.getValue(WaterBoilerBlock.PORT).getDirection() == side)
                                return lower.energyHandlerImpl;
                            return null;
                        }
                        return lower.energyHandlerImpl;
                    }
            );
        }

        if (ModBlocks.FILTER_FRAME_BLOCK_ENTITY != null) {
            event.registerBlockEntity(
                    Capabilities.FluidHandler.BLOCK,
                    ModBlocks.FILTER_FRAME_BLOCK_ENTITY.get(),
                    (be, side) -> {
                        if (side == null) return null;
                        BlockState state = be.getBlockState();
                        if (!state.hasProperty(AbstractFilterFrameBlock.FACING)) return null;
                        Direction facing = state.getValue(AbstractFilterFrameBlock.FACING);
                        boolean reversed = state.hasProperty(AbstractFilterFrameBlock.REVERSED)
                                && state.getValue(AbstractFilterFrameBlock.REVERSED);
                        Direction inputSide = reversed ? facing : facing.getOpposite();
                        Direction outputSide = reversed ? facing.getOpposite() : facing;
                        if (side == inputSide) return be.inputFluidHandler;
                        if (side == outputSide) return be.outputFluidHandler;
                        return null;
                    }
            );

            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK,
                    ModBlocks.FILTER_FRAME_BLOCK_ENTITY.get(),
                    (be, side) -> be.getInventoryHandler()
            );
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WaterPurity.init();
            registerDispenserBehaviors();
        });
    }

    /**
     * Replaces vanilla bucket and glass-bottle dispenser behaviors with purity-aware
     * versions. Non-water source fluids (lava, powder snow) are handled via the
     * block's BucketPickup interface so vanilla functionality is preserved.
     */
    private static void registerDispenserBehaviors() {

        // ── Empty bucket ──────────────────────────────────────────────────────
        DispenserBlock.registerBehavior(Items.BUCKET, (source, item) -> {
            Level level = source.level();
            BlockPos facing = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            BlockState blockState = level.getBlockState(facing);

            if (level.getFluidState(facing).is(FluidTags.WATER) && level.getFluidState(facing).isSource()) {
                ItemStack result = WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), level, facing);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, facing);
                if (blockState.getBlock() instanceof BucketPickup pickup)
                    pickup.pickupBlock(null, level, facing, blockState);
                item.shrink(1);
                if (item.isEmpty()) return result;
                ItemStack remainder = source.blockEntity().insertItem(result);
                if (!remainder.isEmpty())
                    new DefaultDispenseItemBehavior().dispense(source, remainder);
                return item;
            }

            if (!level.getFluidState(facing).isEmpty() && level.getFluidState(facing).isSource()
                    && blockState.getBlock() instanceof BucketPickup pickup) {
                ItemStack filled = pickup.pickupBlock(null, level, facing, blockState);
                if (!filled.isEmpty()) {
                    level.gameEvent(null, GameEvent.FLUID_PICKUP, facing);
                    item.shrink(1);
                    if (item.isEmpty()) return filled;
                    ItemStack remainder = source.blockEntity().insertItem(filled);
                    if (!remainder.isEmpty())
                        new DefaultDispenseItemBehavior().dispense(source, remainder);
                    return item;
                }
            }

            return new DefaultDispenseItemBehavior().dispense(source, item);
        });

        // ── Glass bottle ──────────────────────────────────────────────────────
        DispenserBlock.registerBehavior(Items.GLASS_BOTTLE, (source, item) -> {
            Level level = source.level();
            BlockPos facing = source.pos().relative(source.state().getValue(DispenserBlock.FACING));

            if (level.getFluidState(facing).is(FluidTags.WATER)) {
                ItemStack result = WaterPurity.waterPotion();
                WaterPurity.addPurity(result, level, facing);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, facing);
                item.shrink(1);
                if (item.isEmpty()) return result;
                ItemStack remainder = source.blockEntity().insertItem(result);
                if (!remainder.isEmpty())
                    new DefaultDispenseItemBehavior().dispense(source, remainder);
                return item;
            }

            return new DefaultDispenseItemBehavior().dispense(source, item);
        });
    }
}
