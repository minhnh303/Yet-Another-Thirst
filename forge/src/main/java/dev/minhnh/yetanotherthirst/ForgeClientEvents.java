package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import dev.minhnh.yetanotherthirst.compat.VampirismCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeClientEvents {

    private ForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {

        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "thirst", (gui, guiGraphics, partialTick, width, height) -> {
            if (ThirstConfig.COMPAT_VAMPIRISM) {
                var player = Minecraft.getInstance().player;
                if (player != null && VampirismCompat.isVampire(player)) return;
            }
            if (gui.shouldDrawSurvivalElements() && ThirstHudRenderer.render(guiGraphics, width, height, gui.rightHeight)) {
                gui.rightHeight += 10;
            }
        });
    }
}
