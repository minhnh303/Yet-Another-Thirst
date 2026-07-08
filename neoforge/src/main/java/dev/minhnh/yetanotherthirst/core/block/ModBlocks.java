package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Constants.MOD_ID);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Constants.MOD_ID);

    // Filter Frame
    public static final DeferredBlock<FilterFrameBlock> FILTER_FRAME =
            BLOCKS.registerBlock("filter_frame",
                    FilterFrameBlock::new,
                    Block.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterFrameBlockEntity>> FILTER_FRAME_BLOCK_ENTITY =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<FilterFrameBlockEntity>>)
            (DeferredHolder<?, ?>) BLOCK_ENTITIES.register("filter_frame",
                    () -> BlockEntityType.Builder.of(FilterFrameBlockEntity::new, FILTER_FRAME.get()).build(null));

    // Water Boiler
    public static final DeferredBlock<WaterBoilerBlock> WATER_BOILER =
            BLOCKS.registerBlock("water_boiler",
                    WaterBoilerBlock::new,
                    Block.Properties.ofFullCopy(Blocks.FURNACE).noOcclusion());

    @SuppressWarnings("unchecked")
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterBoilerBlockEntity>> WATER_BOILER_BLOCK_ENTITY =
            (DeferredHolder<BlockEntityType<?>, BlockEntityType<WaterBoilerBlockEntity>>)
            (DeferredHolder<?, ?>) BLOCK_ENTITIES.register("water_boiler",
                    () -> BlockEntityType.Builder.of(WaterBoilerBlockEntity::new, WATER_BOILER.get()).build(null));

    private ModBlocks() {}
}
