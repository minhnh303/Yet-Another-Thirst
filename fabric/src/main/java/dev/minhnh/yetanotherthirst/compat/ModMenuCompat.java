package dev.minhnh.yetanotherthirst.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.minhnh.yetanotherthirst.client.config.YatConfigScreen;

public final class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return YatConfigScreen::new;
    }
}
