package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.minhnh.yetanotherthirst.core.block.AbstractFilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * Injects IWrenchable into FilterFrameBlock so Create's wrench can cycle the
 * REVERSED property to swap the filter's input/output sides.
 * Loaded only when Create is present (required:false config).
 */
@Pseudo
@Mixin(value = FilterFrameBlock.class, remap = false)
public abstract class MixinFilterFrameBlock implements IWrenchable {

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        level.setBlock(pos, state.cycle(AbstractFilterFrameBlock.REVERSED), 3);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }
}
