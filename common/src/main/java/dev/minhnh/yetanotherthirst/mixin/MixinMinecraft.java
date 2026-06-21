package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.Minecraft")
public class MixinMinecraft {

    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void yat_startUseItem(CallbackInfo ci) {
        if (ThirstConfig.CAN_DRINK_BY_HAND) {
            if (Services.PLATFORM.tryHandDrink()) {
                ci.cancel();
            }
        }
    }
}
