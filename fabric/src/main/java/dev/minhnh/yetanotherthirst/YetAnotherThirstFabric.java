package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.fabricmc.api.ModInitializer;
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

public class YetAnotherThirstFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        FabricConfig.load();
        var loader = net.fabricmc.loader.api.FabricLoader.getInstance();
        ThirstConfig.COMPAT_TOMBSTONE = loader.isModLoaded("tombstone");
        ThirstConfig.COMPAT_VAMPIRISM = loader.isModLoaded("vampirism");
        ThirstConfig.COMPAT_FARMERS_DELIGHT = loader.isModLoaded("farmersdelight");
        ThirstConfig.COMPAT_LETS_DO_BAKERY = loader.isModLoaded("bakery");
        ThirstConfig.COMPAT_LETS_DO_BREWERY = loader.isModLoaded("brewery");
        ThirstConfig.COMPAT_LETS_DO_FARM_AND_CHARM = loader.isModLoaded("farm_and_charm");
        ThirstConfig.COMPAT_APPLESKIN = loader.isModLoaded("appleskin");
        ThirstConfig.COMPAT_JADE = loader.isModLoaded("jade");
        ThirstConfig.COMPAT_TOUGH_AS_NAILS = loader.isModLoaded("toughasnails");
        ThirstConfig.COMPAT_COLD_SWEAT = loader.isModLoaded("cold_sweat");
        ThirstConfig.COMPAT_SUPERNATURAL = loader.isModLoaded("supernatural");
        ThirstConfig.COMPAT_IE_HEATER = loader.isModLoaded("immersiveengineering");
        FabricEffects.register();
        dev.minhnh.yetanotherthirst.core.block.ModBlocks.register();
        FabricFluidStorage.register();
        FabricItems.register();
        WaterPurity.init();
        registerDispenserBehaviors();
        FabricCreativeTab.register();
        FabricNetwork.register();
        FabricGameEvents.register();
        FabricLootModifier.register();

        CommonClass.init();
    }

    private static void registerDispenserBehaviors() {

        // ── Empty bucket ──────────────────────────────────────────────────────
        DispenserBlock.registerBehavior(Items.BUCKET, (source, item) -> {
            Level level = source.level();
            BlockPos facing = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            BlockState blockState = level.getBlockState(facing);

            if (level.getFluidState(facing).is(FluidTags.WATER) && level.getFluidState(facing).isSource()
                    && blockState.getBlock() instanceof BucketPickup pickup) {
                ItemStack filled = pickup.pickupBlock(null, level, facing, blockState);
                if (!filled.isEmpty()) {
                    WaterPurity.addPurity(filled, level, facing);
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
