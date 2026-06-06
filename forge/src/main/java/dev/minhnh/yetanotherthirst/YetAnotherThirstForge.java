package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstForge {

    public YetAnotherThirstForge() {

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(ModConfig.Type.COMMON, ForgeConfig.SPEC, Constants.MOD_ID + "/common.toml");
        ctx.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.SPEC, Constants.MOD_ID + "/client.toml");

        modBus.addListener(YetAnotherThirstForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstForge::onCommonSetup);

        // Bind Forge RegistryObjects into loader-agnostic ModItems before registration fires
        ForgeItems.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        ForgeItems.ITEMS.register(modBus);
        ForgeEffects.EFFECTS.register(modBus);
        ForgeCreativeTab.TABS.register(modBus);
        ForgeLootModifier.SERIALIZERS.register(modBus);

        ForgeNetwork.register();
        CommonClass.init();
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        ThirstConfig.COMPAT_TOMBSTONE = ModList.get().isLoaded("tombstone");
        ThirstConfig.COMPAT_VAMPIRISM = ModList.get().isLoaded("vampirism");
        ThirstConfig.COMPAT_FARMERS_DELIGHT = ModList.get().isLoaded("farmersdelight");
        ThirstConfig.COMPAT_LETS_DO_BAKERY = ModList.get().isLoaded("bakery");
        ThirstConfig.COMPAT_LETS_DO_BREWERY = ModList.get().isLoaded("brewery");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigEvent(event);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigEvent(event);
    }

    private static void onConfigEvent(ModConfigEvent event) {
        Constants.LOG.info("onConfigEvent called for config: {}", event.getConfig().getFileName());
        if (event.getConfig().getSpec() == ForgeConfig.SPEC) {
            ForgeConfig.sync();
            ForgeConfig.reloadThirstValues();
            Constants.LOG.info("ForgeConfig synced. extraHydrationConvertsToQuenched: {}", ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED);
        } else if (event.getConfig().getSpec() == ForgeClientConfig.SPEC) {
            ForgeClientConfig.sync();
            Constants.LOG.info("ForgeClientConfig synced.");
        }
    }
}
