package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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

    private static void onConfigLoading(ModConfigEvent.Loading event) { onConfigEvent(event); }
    private static void onConfigReloading(ModConfigEvent.Reloading event) { onConfigEvent(event); }

    private static void onConfigEvent(ModConfigEvent event) {
        Constants.LOG.info("onConfigEvent: {}", event.getConfig().getFileName());
        var spec = event.getConfig().getSpec();
        if (spec == ForgeConfigCommon.SPEC) {
            ForgeConfigCommon.sync();
        } else if (spec == ForgeConfigItems.SPEC) {
            ForgeConfigItems.sync();
            ForgeConfigItems.reloadThirstValues();
        } else if (spec == ForgeConfigCompat.SPEC) {
            ForgeConfigCompat.sync();
        } else if (spec == ForgeClientConfig.SPEC) {
            ForgeClientConfig.sync();
        }
    }
}
