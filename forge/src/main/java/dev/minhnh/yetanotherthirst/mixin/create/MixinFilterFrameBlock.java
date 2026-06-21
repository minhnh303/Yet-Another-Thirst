package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FilterFrameBlock.class, remap = false)
public class MixinFilterFrameBlock implements IWrenchable {

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockState newState = state.cycle(FilterFrameBlock.REVERSED);
        level.setBlock(pos, newState, 3);
        IWrenchable.playRotateSound(level, pos);
        return InteractionResult.SUCCESS;
    }
}
