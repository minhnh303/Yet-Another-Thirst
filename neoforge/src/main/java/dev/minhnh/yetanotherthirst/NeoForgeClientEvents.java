package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientEvents {

    private NeoForgeClientEvents() {}

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, Constants.asResource("thirst"), (guiGraphics, deltaTracker) -> {
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
