package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;

import java.util.Objects;

/** Unique composite key identifying a config option by its file and leaf key. */
public record ConfigKey(ConfigFile file, String key) {
    public ConfigKey {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(key, "key");
    }

    @Override
    public String toString() {
        return file + "/" + key;
    }
}

