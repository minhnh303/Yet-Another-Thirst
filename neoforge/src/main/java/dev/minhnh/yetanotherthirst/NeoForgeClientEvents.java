package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.WaterBoilerRenderer;
import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.screen.ModMenuTypes;
import dev.minhnh.yetanotherthirst.screen.WaterBoilerScreen;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.resources.ResourceLocation;

public final class NeoForgeClientEvents {

    private NeoForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent.class, dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        if (ModMenuTypes.WATER_BOILER != null) {
            event.register(ModMenuTypes.WATER_BOILER.get(), WaterBoilerScreen::new);
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (ModBlocks.WATER_BOILER_BLOCK_ENTITY != null) {
            event.registerBlockEntityRenderer(
                    ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(),
                    context -> new WaterBoilerRenderer<>(context)
            );
        }
    }

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, 
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "thirst_level"), 
            (guiGraphics, deltaTracker) -> {
                dev.minhnh.yetanotherthirst.client.ThirstHudRenderer.render(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 49);
            }
        );
    }
}
