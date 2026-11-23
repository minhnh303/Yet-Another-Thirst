package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.gametest.ThirstGameTests;
import dev.minhnh.yetanotherthirst.platform.NeoForgePlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod(Constants.MOD_ID)
public class YetAnotherThirstNeoForge {

    public YetAnotherThirstNeoForge(IEventBus modBus, ModContainer modContainer) {

        ConfigMigration.run();

        String dir = Constants.CONFIG_DIR + "/";
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCommon.SPEC, dir + "common.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigItems.SPEC, dir + "items.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeConfigCompat.SPEC, dir + "compat.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, NeoForgeClientConfig.SPEC, dir + "client.toml");

        modBus.addListener(YetAnotherThirstNeoForge::onConfigLoading);
        modBus.addListener(YetAnotherThirstNeoForge::onConfigReloading);
        modBus.addListener(YetAnotherThirstNeoForge::onCommonSetup);
        modBus.addListener(YetAnotherThirstNeoForge::onRegister);
        modBus.addListener(YetAnotherThirstNeoForge::onRegisterGameTests);
        if (FMLEnvironment.getDist().isClient()) {
            NeoForgeConfigScreen.register(modContainer);
        }

        // Bind NeoForge DeferredItems/DeferredHolders into loader-agnostic ModItems/ModEffects
        NeoForgeItems.bindToCommon();
        NeoForgeEffects.bindToCommon();

        // Register DeferredRegisters onto the mod event bus
        NeoForgePlatformHelper.ATTACHMENT_TYPES.register(modBus);
        NeoForgeItems.ITEMS.register(modBus);
        NeoForgeEffects.EFFECTS.register(modBus);
        NeoForgeCreativeTab.TABS.register(modBus);
        NeoForgeLootModifier.SERIALIZERS.register(modBus);

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
        ThirstConfig.COMPAT_ENVIRONMENTZ = ModList.get().isLoaded("environmentz");
    }

    private static void onRegister(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, helper -> new ThirstGameTests().load(helper::register));
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(Constants.asResource("default"));
        new ThirstGameTests().load((key, function) -> event.registerTest(
                key.identifier(),
                new FunctionGameTestInstance(key, ThirstGameTests.testData(environment))
        ));
    }

    private static void onConfigLoading(ModConfigEvent.Loading event) { onConfigEvent(event); }

    /**
     * Fired on the config file-watcher thread, also after the in-game config screen saves a file. While a server
     * runs, the values are pushed on its thread so ticking never sees them change mid-tick.
     */
    private static void onConfigReloading(ModConfigEvent.Reloading event) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(() -> onConfigEvent(event));
        } else {
            onConfigEvent(event);
        }
    }

    private static void onConfigEvent(ModConfigEvent event) {
        var spec = event.getConfig().getSpec();
        if (spec == NeoForgeConfigCommon.SPEC) {
            NeoForgeConfigCommon.sync();
        } else if (spec == NeoForgeConfigItems.SPEC) {
            NeoForgeConfigItems.sync();
            reloadItemValuesOnServer();
        } else if (spec == NeoForgeConfigCompat.SPEC) {
            NeoForgeConfigCompat.sync();
        } else if (spec == NeoForgeClientConfig.SPEC) {
            NeoForgeClientConfig.sync();
        }
    }

    /**
     * Config events fire on the loading, render or file-watcher thread; item values are resolved on the
     * running server thread, where tags are bound. Without a server nothing runs; server start resolves them.
     */
    private static void reloadItemValuesOnServer() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.execute(NeoForgeConfigItems::reloadThirstValues);
        }
    }
}
