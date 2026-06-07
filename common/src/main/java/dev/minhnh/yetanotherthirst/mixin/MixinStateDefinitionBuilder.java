package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;

@Mixin(StateDefinition.Builder.class)
public class MixinStateDefinitionBuilder {
    @Shadow @Final private Object owner;

    @Inject(method = "create", at = @At("HEAD"), remap = false)
    private void addPurityProperty(Function defaultState, StateDefinition.Factory factory, CallbackInfoReturnable<?> cir) {
        if (this.owner instanceof LayeredCauldronBlock) {
            ((StateDefinition.Builder) (Object) this).add(WaterPurity.BLOCK_PURITY);
        }
    }
}
