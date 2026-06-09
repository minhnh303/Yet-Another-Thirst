package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
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

public final class NeoForgeCommonSetup {

    private NeoForgeCommonSetup() {}

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
