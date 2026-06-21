package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
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
            if (gui.shouldDrawSurvivalElements() && ThirstHudRenderer.render(guiGraphics, width, height, gui.rightHeight)) {
                gui.rightHeight += 10;
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent.class, dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onClientSetup(net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (dev.minhnh.yetanotherthirst.screen.ModMenuTypes.WATER_BOILER != null) {
                net.minecraft.client.gui.screens.MenuScreens.register(
                        dev.minhnh.yetanotherthirst.screen.ModMenuTypes.WATER_BOILER.get(),
                        dev.minhnh.yetanotherthirst.screen.WaterBoilerScreen::new
                );
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        if (dev.minhnh.yetanotherthirst.core.block.ModBlocks.WATER_BOILER_BLOCK_ENTITY != null) {
            event.registerBlockEntityRenderer(
                    dev.minhnh.yetanotherthirst.core.block.ModBlocks.WATER_BOILER_BLOCK_ENTITY.get(),
                    context -> new dev.minhnh.yetanotherthirst.client.WaterBoilerRenderer(context)
            );
        }
    }
}
