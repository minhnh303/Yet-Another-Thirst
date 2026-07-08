package dev.minhnh.yetanotherthirst.mixin.ie;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlock;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import javax.annotation.Nullable;

@Mixin(value = WaterBoilerBlockEntity.class, remap = false)
public abstract class MixinWaterBoilerBlockEntity implements IEBlockInterfaces.IHammerInteraction, IEBlockInterfaces.IBlockOverlayText {

    @Override
    @Shadow
    public abstract boolean hammerUseSide(Direction side, Player player, InteractionHand hand, Vec3 hitVec);

    @Nullable
    @Override
    public Component[] getOverlayText(BlockState state, Player player, HitResult mop, boolean hammer) {
        if (hammer && IEClientConfig.showTextOverlay.get() && mop instanceof BlockHitResult brtr) {
            Direction side = brtr.getDirection();
            Direction oppSide = side.getOpposite();
            if (state != null) {
                return new Component[]{
                    Component.translatable("desc.immersiveengineering.info.blockSide.facing")
                        .append(": ")
                        .append(getSideConfigComponent(state, side)),
                    Component.translatable("desc.immersiveengineering.info.blockSide.opposite")
                        .append(": ")
                        .append(getSideConfigComponent(state, oppSide))
                };
            }
        }
        return null;
    }

    private Component getSideConfigComponent(BlockState state, Direction d) {
        if (state.hasProperty(WaterBoilerBlock.FACING) && d == state.getValue(WaterBoilerBlock.FACING)) {
            return Component.translatable("yet_another_thirst.blockSide.front");
        }
        if (state.hasProperty(WaterBoilerBlock.INLET) && state.getValue(WaterBoilerBlock.INLET).getDirection() == d) {
            return Component.translatable("yet_another_thirst.blockSide.inlet");
        }
        if (state.hasProperty(WaterBoilerBlock.OUTLET) && state.getValue(WaterBoilerBlock.OUTLET).getDirection() == d) {
            return Component.translatable("yet_another_thirst.blockSide.outlet");
        }
        if (state.hasProperty(WaterBoilerBlock.PORT) && state.getValue(WaterBoilerBlock.PORT).getDirection() == d) {
            return Component.translatable("yet_another_thirst.blockSide.port");
        }
        return Component.translatable("yet_another_thirst.blockSide.none");
    }
}
