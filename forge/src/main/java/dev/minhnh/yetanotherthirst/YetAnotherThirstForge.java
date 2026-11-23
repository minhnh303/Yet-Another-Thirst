package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstForge {

    public YetAnotherThirstForge(FMLJavaModLoadingContext context) {

        var modBusGroup = context.getModBusGroup();

        ConfigMigration.run();

        String dir = Constants.CONFIG_DIR + "/";
        context.registerConfig(ModConfig.Type.COMMON, ForgeConfigCommon.SPEC, dir + "common.toml");
        context.registerConfig(ModConfig.Type.COMMON, ForgeConfigItems.SPEC, dir + "items.toml");
        context.registerConfig(ModConfig.Type.COMMON, ForgeConfigCompat.SPEC, dir + "compat.toml");
        context.registerConfig(ModConfig.Type.CLIENT, ForgeClientConfig.SPEC, dir + "client.toml");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeConfigScreen.register(context);
        }

        ModConfigEvent.Loading.getBus(modBusGroup).addListener(YetAnotherThirstForge::onConfigLoading);
        ModConfigEvent.Reloading.getBus(modBusGroup).addListener(YetAnotherThirstForge::onConfigReloading);
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(YetAnotherThirstForge::onCommonSetup);

        // Bind Forge RegistryObjects into loader-agnostic ModItems before registration fires
        ForgeItems.bindToCommon();
        ForgeEffects.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        ForgeItems.ITEMS.register(modBusGroup);
        ForgeEffects.EFFECTS.register(modBusGroup);
        ForgeCreativeTab.TABS.register(modBusGroup);
        ForgeLootModifier.SERIALIZERS.register(modBusGroup);
        ForgeLootModifier.LOOT_CONDITION_TYPES.register(modBusGroup);

        ForgeNetwork.register();
        CommonClass.init();
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        ForgeConfigCommon.sync();
        ForgeConfigItems.sync();
        ForgeConfigCompat.sync();

        ThirstConfig.COMPAT_TOMBSTONE = ModList.isLoaded("tombstone");
        ThirstConfig.COMPAT_VAMPIRISM = ModList.isLoaded("vampirism");
        ThirstConfig.COMPAT_FARMERS_DELIGHT = ModList.isLoaded("farmersdelight");
        ThirstConfig.COMPAT_LETS_DO_BAKERY = ModList.isLoaded("bakery");
        ThirstConfig.COMPAT_LETS_DO_BREWERY = ModList.isLoaded("brewery");
        ThirstConfig.COMPAT_LETS_DO_FARM_AND_CHARM = ModList.isLoaded("farm_and_charm");
        ThirstConfig.COMPAT_APPLESKIN = ModList.isLoaded("appleskin");
        ThirstConfig.COMPAT_JADE = ModList.isLoaded("jade");
        ThirstConfig.COMPAT_TOUGH_AS_NAILS = ModList.isLoaded("toughasnails");
        ThirstConfig.COMPAT_COLD_SWEAT = ModList.isLoaded("cold_sweat");
        ThirstConfig.COMPAT_SUPERNATURAL = ModList.isLoaded("supernatural");
        ThirstConfig.COMPAT_IE_HEATER = ModList.isLoaded("immersiveengineering");
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigEvent(event.getConfig().getSpec(), false);
    }

    /**
     * Fired on the config file-watcher thread, also after the in-game config screen saves a file. While a server
     * runs, the values are pushed on its thread so ticking never sees them change mid-tick.
     */
    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        var spec = event.getConfig().getSpec();
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> onConfigEvent(spec, true));
        } else {
            onConfigEvent(spec, true);
        }
    }

    private static void onConfigEvent(net.minecraftforge.fml.config.IConfigSpec<?> spec, boolean reload) {
        if (spec == ForgeConfigCommon.SPEC) {
            ForgeConfigCommon.sync();
        } else if (spec == ForgeConfigItems.SPEC) {
            ForgeConfigItems.sync();
            if (reload) reloadItemValuesOnServer();
        } else if (spec == ForgeConfigCompat.SPEC) {
            ForgeConfigCompat.sync();
        } else if (spec == ForgeClientConfig.SPEC) {
            ForgeClientConfig.sync();
        }
    }

    /**
     * Schedules item value resolution on the running server thread, where tags are bound. Without a server
     * (title screen) nothing runs; onServerStarted resolves them when a world starts.
     */
    private static void reloadItemValuesOnServer() {
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(ForgeConfigItems::reloadThirstValues);
        }
    }
}
