package dev.minhnh.yetanotherthirst.mixin.create;

import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Implements Create's IWrenchable on FilterFrameBlock at runtime via Mixin,
 * without a compile-time dependency on Create. The interface is applied by the
 * mixin processor; only the method body needs to compile against vanilla types.
 */
@Pseudo
@Mixin(value = FilterFrameBlock.class, remap = false)
public abstract class MixinFilterFrameBlock {

    // onWrenched signature matches IWrenchable — applied at load time by the mixin.
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState newState = state.cycle(FilterFrameBlock.REVERSED);
        level.setBlock(pos, newState, 3);
        // Play rotate sound via reflection to avoid hard Create dep at compile time
        try {
            Class<?> wrenchable = Class.forName("com.simibubi.create.api.equipment.wrench.IWrenchable");
            wrenchable.getMethod("playRotateSound", Level.class, BlockPos.class).invoke(null, level, pos);
        } catch (Throwable ignored) {}
        return InteractionResult.SUCCESS;
    }
}
