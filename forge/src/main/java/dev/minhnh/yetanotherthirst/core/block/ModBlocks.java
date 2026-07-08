package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public static final RegistryObject<Block> FILTER_FRAME =
            BLOCKS.register("filter_frame", () -> new FilterFrameBlock(Block.Properties.ofFullCopy(Blocks.COPPER_BLOCK).noOcclusion()));

    public static final RegistryObject<BlockEntityType<FilterFrameBlockEntity>> FILTER_FRAME_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("filter_frame", () -> BlockEntityType.Builder.of(FilterFrameBlockEntity::new, FILTER_FRAME.get()).build(null));

    public static final RegistryObject<Block> WATER_BOILER =
            BLOCKS.register("water_boiler", () -> new WaterBoilerBlock(Block.Properties.ofFullCopy(Blocks.FURNACE).noOcclusion()));

    public static final RegistryObject<BlockEntityType<WaterBoilerBlockEntity>> WATER_BOILER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("water_boiler", () -> BlockEntityType.Builder.of(WaterBoilerBlockEntity::new, WATER_BOILER.get()).build(null));

    private ModBlocks() {}
}
