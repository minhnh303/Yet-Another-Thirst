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
import net.minecraftforge.event.AddPackFindersEvent;
import java.util.Optional;
import net.minecraftforge.eventbus.api.IEventBus;
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
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigMigration.run();

        String dir = Constants.CONFIG_DIR + "/";
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfigCommon.SPEC, dir + "common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfigItems.SPEC, dir + "items.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfigCompat.SPEC, dir + "compat.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.SPEC, dir + "client.toml");

        modBus.addListener(YetAnotherThirstForge::onAddPackFinders);
        modBus.addListener(YetAnotherThirstForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstForge::onCommonSetup);

        // Bind Forge RegistryObjects into loader-agnostic ModItems before registration fires
        ForgeItems.bindToCommon();
        ForgeEffects.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        ForgeItems.ITEMS.register(modBus);
        ForgeEffects.EFFECTS.register(modBus);
        ForgeCreativeTab.TABS.register(modBus);
        ForgeLootModifier.SERIALIZERS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.BLOCK_ENTITIES.register(modBus);
        ModMenuTypes.MENU_TYPES.register(modBus);

        ForgeNetwork.register();
        CommonClass.init();
    }

    private static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.SERVER_DATA) return;
        var modFile = ModList.get().getModFileById(Constants.MOD_ID).getFile();
        var packRoot = modFile.findResource("pack.mcmeta").getParent();
        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(
                new PackLocationInfo("mod:" + Constants.MOD_ID, Component.literal(Constants.MOD_NAME), PackSource.BUILT_IN, Optional.empty()),
                new PathPackResources.PathResourcesSupplier(packRoot),
                PackType.SERVER_DATA,
                new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
            );
            if (pack != null) consumer.accept(pack);
        });
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
        // sync() only — item registries aren't populated yet, so reloadThirstValues() would
        // silently drop all mod items. It runs correctly in onServerStarted/onTagsUpdated.
        onConfigEvent(event.getConfig().getSpec(), false);
    }

    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        // In-game reload: registries are live, safe to resolve item values.
        onConfigEvent(event.getConfig().getSpec(), true);
    }

    private static void onConfigEvent(net.minecraftforge.fml.config.IConfigSpec<?> spec, boolean reload) {
        if (spec == ForgeConfigCommon.SPEC) {
            ForgeConfigCommon.sync();
        } else if (spec == ForgeConfigItems.SPEC) {
            ForgeConfigItems.sync();
            if (reload) ForgeConfigItems.reloadThirstValues();
        } else if (spec == ForgeConfigCompat.SPEC) {
            ForgeConfigCompat.sync();
        } else if (spec == ForgeClientConfig.SPEC) {
            ForgeClientConfig.sync();
        }
    }
}
