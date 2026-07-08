package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.screen.ModMenuTypes;
import dev.minhnh.yetanotherthirst.screen.WaterBoilerScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ForgeClientEvents {

    private ForgeClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterTooltipComponentFactories(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent.class, dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModMenuTypes.WATER_BOILER != null) {
                MenuScreens.register(ModMenuTypes.WATER_BOILER.get(), WaterBoilerScreen::new);
            }
        });
    }
}
