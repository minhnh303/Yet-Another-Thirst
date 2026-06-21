package dev.minhnh.yetanotherthirst.core.block;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Constants.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Constants.MOD_ID);

    public static final RegistryObject<Block> FILTER_FRAME = net.minecraftforge.fml.ModList.get().isLoaded("create") ?
            BLOCKS.register("filter_frame", () -> new FilterFrameBlock(Block.Properties.copy(Blocks.COPPER_BLOCK).noOcclusion())) : null;

    public static final RegistryObject<BlockEntityType<FilterFrameBlockEntity>> FILTER_FRAME_BLOCK_ENTITY = FILTER_FRAME != null ?
            BLOCK_ENTITIES.register("filter_frame", () -> BlockEntityType.Builder.of(FilterFrameBlockEntity::new, FILTER_FRAME.get()).build(null)) : null;

    public static final RegistryObject<Block> WATER_BOILER = net.minecraftforge.fml.ModList.get().isLoaded("immersiveengineering") ?
            BLOCKS.register("water_boiler", () -> new WaterBoilerBlock(Block.Properties.copy(Blocks.FURNACE).noOcclusion())) : null;

    public static final RegistryObject<BlockEntityType<WaterBoilerBlockEntity>> WATER_BOILER_BLOCK_ENTITY = WATER_BOILER != null ?
            BLOCK_ENTITIES.register("water_boiler", () -> BlockEntityType.Builder.of(WaterBoilerBlockEntity::new, WATER_BOILER.get()).build(null)) : null;

    private ModBlocks() {}
}
