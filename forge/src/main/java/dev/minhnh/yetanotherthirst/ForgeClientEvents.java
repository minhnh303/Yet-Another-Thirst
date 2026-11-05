package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.gui.overlay.ForgeLayeredDraw;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ForgeClientEvents {

    private ForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onAddGuiOverlayLayers(AddGuiOverlayLayersEvent event) {
        event.getLayeredDraw().addAbove(ForgeLayeredDraw.HOTBAR_AND_DECOS, ForgeLayeredDraw.HEALTH_BAR, Constants.asResource("thirst"), (guiGraphics, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (ThirstHudRenderer.shouldRender(mc)) {
                ThirstHudRenderer.render(guiGraphics, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), 49);
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent.class, dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent::new);
    }
}
