package dev.minhnh.yetanotherthirst.mixin;

import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.FluidView;
import snownee.jade.util.CommonProxy;

/**
 * Intercepts Jade's generic fluid display (used for Create fluid tanks,
 * IE fluid tanks, and any block with an IFluidHandler capability).
 * When the displayed fluid is water, prepends the contamination level to the name.
 */
@Pseudo
@Mixin(value = FluidView.class, remap = false)
public class MixinFluidView {

    @Redirect(
            method = "readDefault",
            at = @At(value = "INVOKE",
                     target = "Lsnownee/jade/util/CommonProxy;getFluidName(Lsnownee/jade/api/fluid/JadeFluidObject;)Lnet/minecraft/network/chat/Component;"),
            remap = false
    )
    private static Component appendPurityToFluidName(JadeFluidObject fluid) {
        return CommonProxy.getFluidName(fluid);
    }
}
