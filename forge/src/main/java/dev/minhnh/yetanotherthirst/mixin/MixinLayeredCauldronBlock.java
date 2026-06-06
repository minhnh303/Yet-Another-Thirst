package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects BLOCK_PURITY into LayeredCauldronBlock's block state so that
 * WaterCauldronBlock can store water purity per block. Default value 0 means
 * unset; values 1–4 map to purity levels 0–3 (offset by 1).
 */
@Mixin(LayeredCauldronBlock.class)
public abstract class MixinLayeredCauldronBlock {

    @Inject(method = "createBlockStateDefinition", at = @At("HEAD"))
    protected void addPurityBlockState(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(WaterPurity.BLOCK_PURITY);
    }
}
