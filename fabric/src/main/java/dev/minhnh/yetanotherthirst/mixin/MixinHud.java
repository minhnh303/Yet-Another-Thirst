package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.class)
public class MixinHud {

    /**
     * Render the thirst bar just before ChatComponent extracts its render state.
     */
    @Inject(
        method = "extractRenderState",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/Hud;extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
            shift = At.Shift.BEFORE
        )
    )
    private void yat_renderThirstBarBeforeChat(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            ThirstHudRenderer.render(guiGraphicsExtractor,
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight(),
                    49);
        }
    }

    /**
     * When the thirst bar is visible, push air bubbles up 10 pixels to avoid overlapping the thirst bar.
     */
    @Inject(
        method = "getAirBubbleYLine",
        at = @At("RETURN"),
        cancellable = true
    )
    private void yat_shiftAirBubbles(int vehicleHealth, int y, CallbackInfoReturnable<Integer> cir) {
        if (ThirstHudRenderer.shouldRender(Minecraft.getInstance())) {
            cir.setReturnValue(cir.getReturnValue() - 10);
        }
    }
}

