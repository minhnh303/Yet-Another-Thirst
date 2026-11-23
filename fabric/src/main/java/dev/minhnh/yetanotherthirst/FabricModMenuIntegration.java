package dev.minhnh.yetanotherthirst;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.minhnh.yetanotherthirst.client.config.YatConfigScreen;

/**
 * Mod Menu entrypoint ({@code modmenu} in fabric.mod.json). Mod Menu is compile-only, so this class is
 * loaded only by Mod Menu itself.
 */
public final class FabricModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YatConfigScreen::new;
    }
}

