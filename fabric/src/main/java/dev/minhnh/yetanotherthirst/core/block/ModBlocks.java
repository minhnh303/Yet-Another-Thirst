package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlocks {

    public static Block FILTER_FRAME;
    public static BlockEntityType<FilterFrameBlockEntity> FILTER_FRAME_BLOCK_ENTITY;

    public static void register() {

        if (dev.minhnh.yetanotherthirst.platform.Services.PLATFORM.isModLoaded("create")) {
            FILTER_FRAME = Registry.register(BuiltInRegistries.BLOCK,
                    Constants.asResource("filter_frame"),
                    new FilterFrameBlock(Block.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion()));

            FILTER_FRAME_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Constants.asResource("filter_frame"),
                    BlockEntityType.Builder.of(FilterFrameBlockEntity::new, FILTER_FRAME).build(null));
        }
    }

    private ModBlocks() {}
}
