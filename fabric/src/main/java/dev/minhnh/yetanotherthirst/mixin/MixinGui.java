package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    /**
     * Render the thirst bar just before ChatComponent draws the chat history. This gives the
     * correct z-order (game world → thirst bar → chat → screen overlay) without any scissor
     * interference: ChatComponent enables a GL scissor clipped to the chat area, and thirst
     * icons outside that region would be invisible if we injected after it instead.
     *
     * <p>The row is fixed at height-49 (directly above the food bar), matching where Forge/NeoForge
     * place the thirst overlay in their FOOD_LEVEL/AIR_LEVEL chain. {@link #yat_shiftAirBubbles}
     * makes room for it by pushing vanilla's air bubble icons up a row when both are visible.
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
        LocalPlayer player = mc.player;
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            ThirstHudRenderer.render(guiGraphics,
                    mc.getWindow().getGuiScaledWidth(),
                    mc.getWindow().getGuiScaledHeight(),
                    49);
        }
    }

    /**
     * Vanilla draws every HUD icon in {@code Gui#renderPlayerHealth} through this same blit
     * overload; the air bubble icons are the only ones using v=18. When the thirst bar is about to
     * occupy the row directly above the food bar, push the air bubbles up one row (10px) so the two
     * don't overlap, instead of leaving them stacked on top of each other underwater.
     */
    @Redirect(
        method = "renderPlayerHealth",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"
        )
    )
    private void yat_shiftAirBubbles(GuiGraphics guiGraphics, ResourceLocation location, int x, int y, int u, int v, int width, int height) {
        if (v == 18 && ThirstHudRenderer.shouldRender(Minecraft.getInstance())) {
            y -= 10;
        }
        guiGraphics.blit(location, x, y, u, v, width, height);
    }
}
