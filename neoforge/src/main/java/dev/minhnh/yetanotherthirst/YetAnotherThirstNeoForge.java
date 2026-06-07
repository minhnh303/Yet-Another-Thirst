package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstNeoForge {

    public YetAnotherThirstNeoForge(IEventBus modBus, ModContainer container) {

        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfig.SPEC, Constants.MOD_ID + "/common.toml");
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC, Constants.MOD_ID + "/client.toml");

        modBus.addListener(YetAnotherThirstNeoForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstNeoForge::onCommonSetup);
        modBus.addListener(NeoForgeCommonSetup::onCommonSetup);
        modBus.addListener(NeoForgeNetwork::register);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(NeoForgeClientEvents::onRegisterTooltipComponentFactories);
        }
        registerGameEvents();

        // Bind NeoForge RegistryObjects into loader-agnostic ModItems before registration fires
        NeoForgeItems.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgeEffects.EFFECTS.register(modBus);
        NeoForgeCreativeTab.TABS.register(modBus);
        NeoForgeLootModifier.SERIALIZERS.register(modBus);

        CommonClass.init();
    }

    private static void registerGameEvents() {
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onServerStarted);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onItemFinished);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onPlayerLoggedIn);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onItemTooltip);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRightClickBlock);
            NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRightClickEmpty);
            NeoForge.EVENT_BUS.addListener(NeoForgeClientGameEvents::onGatherTooltipComponents);
        }
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
