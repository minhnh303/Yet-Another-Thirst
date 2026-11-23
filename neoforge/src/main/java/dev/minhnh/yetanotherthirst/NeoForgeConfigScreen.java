package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.config.YatConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Client-only: exposes the in-game config screen through the Config button of NeoForge's mod list. */
final class NeoForgeConfigScreen {

    private NeoForgeConfigScreen() {
    }

    static void register(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (IConfigScreenFactory) (mod, parent) -> new YatConfigScreen(parent));
    }
}

