package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.config.YatConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Client-only: exposes the in-game config screen through the Config button of Forge's mod list. */
final class ForgeConfigScreen {

    private ForgeConfigScreen() {
    }

    static void register(FMLJavaModLoadingContext context) {
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new YatConfigScreen(parent)));
    }
}

