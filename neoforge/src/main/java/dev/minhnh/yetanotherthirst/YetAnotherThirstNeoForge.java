package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.block.ModBlocks;
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

        ConfigMigration.run();

        ModLoadingContext ctx = ModLoadingContext.get();
        String dir = Constants.CONFIG_DIR + "/";
        ctx.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCommon.SPEC, dir + "common.toml");
        ctx.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigItems.SPEC, dir + "items.toml");
        ctx.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCompat.SPEC, dir + "compat.toml");
        ctx.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC, dir + "client.toml");

        modBus.addListener(YetAnotherThirstNeoForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstNeoForge::onCommonSetup);

        // Bind NeoForge RegistryObjects into loader-agnostic ModItems before registration fires
        NeoForgeItems.bindToCommon();
        NeoForgeEffects.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.BLOCK_ENTITIES.register(modBus);
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgeEffects.EFFECTS.register(modBus);
        NeoForgeCreativeTab.TABS.register(modBus);
        NeoForgeLootModifier.SERIALIZERS.register(modBus);
        dev.minhnh.yetanotherthirst.screen.ModMenuTypes.MENU_TYPES.register(modBus);

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
        ThirstConfig.COMPAT_IE_HEATER = ModList.get().isLoaded("immersiveengineering");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) { onConfigEvent(event); }
    private static void onConfigReloading(ModConfigEvent.Reloading event) { onConfigEvent(event); }

    private static void onConfigEvent(ModConfigEvent event) {
        Constants.LOG.info("onConfigEvent: {}", event.getConfig().getFileName());
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
