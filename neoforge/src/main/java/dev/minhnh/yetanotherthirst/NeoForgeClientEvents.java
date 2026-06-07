package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoForgeClientEvents {

    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {

        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "thirst", (gui, guiGraphics, partialTick, width, height) -> {
            if (gui.shouldDrawSurvivalElements() && ThirstHudRenderer.render(guiGraphics, width, height, gui.rightHeight)) {
                gui.rightHeight += 10;
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent.class, dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent::new);
    }
}
