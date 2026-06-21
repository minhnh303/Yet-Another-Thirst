package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    /**
     * Render the thirst bar just before ChatComponent draws the chat history. This gives the
     * correct z-order (game world → thirst bar → chat → screen overlay) without any scissor
     * interference: ChatComponent enables a GL scissor clipped to the chat area, and thirst
     * icons outside that region would be invisible if we injected after it instead.
     */
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/ChatComponent;render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            shift = At.Shift.BEFORE
        )
    )
    private void yat_renderThirstBarBeforeChat(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isAlive() && !mc.options.hideGui
                && !mc.player.isCreative() && !mc.player.isSpectator()) {
            ThirstHudRenderer.render(guiGraphics,
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight(),
                    49);
        }
    }
}
