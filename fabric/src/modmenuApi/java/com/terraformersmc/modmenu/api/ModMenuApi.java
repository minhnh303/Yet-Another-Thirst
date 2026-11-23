package com.terraformersmc.modmenu.api;

import java.util.Map;

public interface ModMenuApi {

    default ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> null;
    }

    default Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        return Map.of();
    }
}

