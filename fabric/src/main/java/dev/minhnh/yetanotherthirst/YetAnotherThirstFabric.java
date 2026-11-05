package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class YetAnotherThirstFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        FabricConfig.load();
        ThirstConfig.COMPAT_APPLESKIN = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("appleskin");
        ThirstConfig.COMPAT_ENVIRONMENTZ = net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("environmentz");
        FabricNetwork.register();
        FabricEffects.register();
        dev.minhnh.yetanotherthirst.core.block.ModBlocks.register();
        FabricFluidStorage.register();
        FabricItems.register();
        WaterPurity.init();
        registerDispenserBehaviors();
        FabricCreativeTab.register();
        dev.minhnh.yetanotherthirst.screen.ModMenuTypes.register();
        FabricGameEvents.register();
        FabricLootModifier.register();
    }

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
                ItemStack result = PotionContents.createItemStack(Items.POTION, Potions.WATER);
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
