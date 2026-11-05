package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlocks {

    public static Block FILTER_FRAME;
    public static BlockEntityType<FilterFrameBlockEntity> FILTER_FRAME_BLOCK_ENTITY;

    public static void register() {

        if (dev.minhnh.yetanotherthirst.platform.Services.PLATFORM.isModLoaded("create")) {
            ResourceKey<Block> filterFrameKey = ResourceKey.create(Registries.BLOCK, Constants.asResource("filter_frame"));
            FILTER_FRAME = Registry.register(BuiltInRegistries.BLOCK,
                    filterFrameKey,
                    new FilterFrameBlock(Block.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().setId(filterFrameKey)));

            FILTER_FRAME_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    Constants.asResource("filter_frame"),
                    new BlockEntityType<>(FilterFrameBlockEntity::new, java.util.Set.of(FILTER_FRAME)));
        }
    }

    private ModBlocks() {}
}
