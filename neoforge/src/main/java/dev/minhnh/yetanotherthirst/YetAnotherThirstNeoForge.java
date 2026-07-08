package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.screen.ModMenuTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import java.util.Optional;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstNeoForge {

    public YetAnotherThirstNeoForge(IEventBus modBus, ModContainer container) {

        ConfigMigration.run();

        String dir = Constants.CONFIG_DIR + "/";
        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCommon.SPEC, dir + "common.toml");
        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigItems.SPEC, dir + "items.toml");
        container.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCompat.SPEC, dir + "compat.toml");
        container.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC, dir + "client.toml");

        modBus.addListener(YetAnotherThirstNeoForge::onAddPackFinders);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstNeoForge::onCommonSetup);
        modBus.addListener(NeoForgeCommonSetup::onCommonSetup);
        modBus.addListener(NeoForgeCommonSetup::onRegisterCapabilities);
        modBus.addListener(NeoForgeNetwork::register);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(NeoForgeClientEvents::onRegisterTooltipComponentFactories);
            modBus.addListener(NeoForgeClientEvents::onRegisterGuiLayers);
            modBus.addListener(NeoForgeClientEvents::onRegisterMenuScreens);
            modBus.addListener(NeoForgeClientEvents::onRegisterRenderers);
        }
        registerGameEvents();

        // Bind NeoForge RegistryObjects into loader-agnostic ModItems before registration fires
        NeoForgeItems.bindToCommon();
        NeoForgeEffects.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgeEffects.EFFECTS.register(modBus);
        NeoForgeCreativeTab.TABS.register(modBus);
        NeoForgeLootModifier.SERIALIZERS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);

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
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onTagsUpdated);
        NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onItemCrafted);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, NeoForgeGameEvents::onIEHammerBlock);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRightClickBlock);
            NeoForge.EVENT_BUS.addListener(NeoForgeGameEvents::onRightClickEmpty);
            NeoForge.EVENT_BUS.addListener(NeoForgeClientGameEvents::onGatherTooltipComponents);
            NeoForge.EVENT_BUS.addListener(NeoForgeClientGameEvents::onRenderGuiLayerPre);
            NeoForge.EVENT_BUS.addListener(NeoForgeClientGameEvents::onRenderGuiLayerPost);
        }
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        var modFile = ModList.get().getModFileById(Constants.MOD_ID).getFile();
        if (ModList.get().isLoaded("create")) {
            var createPackRoot = modFile.findResource("compat_packs/create_compat");
            event.addRepositorySource(consumer -> {
                Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo("yet_another_thirst_create_compat", Component.literal("YAT Create Compat"), PackSource.BUILT_IN, Optional.empty()),
                    new PathPackResources.PathResourcesSupplier(createPackRoot),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
                );
                if (pack != null) consumer.accept(pack);
            });
        }
        if (ModList.get().isLoaded("immersiveengineering")) {
            var iePackRoot = modFile.findResource("compat_packs/ie_compat");
            event.addRepositorySource(consumer -> {
                Pack pack = Pack.readMetaAndCreate(
                    new PackLocationInfo("yet_another_thirst_ie_compat", Component.literal("YAT IE Compat"), PackSource.BUILT_IN, Optional.empty()),
                    new PathPackResources.PathResourcesSupplier(iePackRoot),
                    PackType.SERVER_DATA,
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
                );
                if (pack != null) consumer.accept(pack);
            });
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
        ThirstConfig.COMPAT_IE_HEATER = ModList.get().isLoaded("immersiveengineering");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigEvent(event);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigEvent(event);
    }

    private static void onConfigEvent(ModConfigEvent event) {
        var spec = event.getConfig().getSpec();
        if (spec == NeoForgeConfigCommon.SPEC) {
            NeoForgeConfigCommon.sync();
        } else if (spec == NeoForgeConfigItems.SPEC) {
            NeoForgeConfigItems.sync();
            NeoForgeConfigItems.reloadThirstValues();
        } else if (spec == NeoForgeConfigCompat.SPEC) {
            NeoForgeConfigCompat.sync();
        } else if (spec == NeoForgeClientConfig.SPEC) {
            NeoForgeClientConfig.sync();
        }
    }
}
