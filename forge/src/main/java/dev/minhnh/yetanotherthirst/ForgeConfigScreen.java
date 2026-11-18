package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.config.YatConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

/** Client-only: exposes the in-game config screen through the Config button of Forge's mod list. */
final class ForgeConfigScreen {

    private ForgeConfigScreen() {
    }

    static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new YatConfigScreen(parent)));
    }
}
