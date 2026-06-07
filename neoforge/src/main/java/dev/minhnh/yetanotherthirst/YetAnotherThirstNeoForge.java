package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstNeoForge {

    public YetAnotherThirstNeoForge() {

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext ctx = ModLoadingContext.get();
        ctx.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.SPEC, Constants.MOD_ID + "/common.toml");
        ctx.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC, Constants.MOD_ID + "/client.toml");

        modBus.addListener(YetAnotherThirstNeoForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstNeoForge::onCommonSetup);

        // Bind NeoForge RegistryObjects into loader-agnostic ModItems before registration fires
        NeoForgeItems.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgeEffects.EFFECTS.register(modBus);
        NeoForgeCreativeTab.TABS.register(modBus);
        NeoForgeLootModifier.SERIALIZERS.register(modBus);

        NeoForgeNetwork.register();
        CommonClass.init();
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        ThirstConfig.COMPAT_TOMBSTONE = ModList.get().isLoaded("tombstone");
        ThirstConfig.COMPAT_VAMPIRISM = ModList.get().isLoaded("vampirism");
        ThirstConfig.COMPAT_FARMERS_DELIGHT = ModList.get().isLoaded("farmersdelight");
        ThirstConfig.COMPAT_LETS_DO_BAKERY = ModList.get().isLoaded("bakery");
        ThirstConfig.COMPAT_LETS_DO_BREWERY = ModList.get().isLoaded("brewery");
        ThirstConfig.COMPAT_LETS_DO_FARM_AND_CHARM = ModList.get().isLoaded("farm_and_charm");
        ThirstConfig.COMPAT_APPLESKIN = ModList.get().isLoaded("appleskin");
        ThirstConfig.COMPAT_JADE = ModList.get().isLoaded("jade");
        ThirstConfig.COMPAT_TOUGH_AS_NAILS = ModList.get().isLoaded("toughasnails");
        ThirstConfig.COMPAT_COLD_SWEAT = ModList.get().isLoaded("cold_sweat");
        ThirstConfig.COMPAT_SUPERNATURAL = ModList.get().isLoaded("supernatural");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigEvent(event);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigEvent(event);
    }

    private static void onConfigEvent(ModConfigEvent event) {
        Constants.LOG.info("onConfigEvent called for config: {}", event.getConfig().getFileName());
        if (event.getConfig().getSpec() == NeoForgeConfig.SPEC) {
            NeoForgeConfig.sync();
            NeoForgeConfig.reloadThirstValues();
            Constants.LOG.info("NeoForgeConfig synced. extraHydrationConvertsToQuenched: {}", ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED);
        } else if (event.getConfig().getSpec() == NeoForgeClientConfig.SPEC) {
            NeoForgeClientConfig.sync();
            Constants.LOG.info("NeoForgeClientConfig synced.");
        }
    }
}
