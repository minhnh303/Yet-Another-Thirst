package dev.minhnh.yetanotherthirst.platform.services;

import java.util.Set;

/**
 * Loader-agnostic read/write access to the mod's on-disk config, used by the in-game config screen.
 *
 * <p>Values cross this boundary in normalized Java types so the screen never touches loader config APIs:
 * {@link Boolean}, {@link Number} (integers and decimals), {@link String} (also enum names),
 * {@code List<String>} and {@code List<List<Object>>} (rows of {@link String}/{@link Number}).
 */
public interface IConfigBackend {

    /** Config files the mod writes. Every file except {@link #CLIENT} is authoritative on the logical server. */
    enum ConfigFile {
        COMMON,
        ITEMS,
        COMPAT,
        CLIENT;

        public boolean serverSide() {
            return this != CLIENT;
        }
    }

    /** True when the loader defines {@code key} in {@code file} and the file is loaded. */
    boolean has(ConfigFile file, String key);

    Object get(ConfigFile file, String key);

    Object getDefault(ConfigFile file, String key);

    /** Stores a value in memory; nothing is written until {@link #save(Set)}. */
    void set(ConfigFile file, String key, Object value);

    /**
     * Writes the given files to disk and pushes their values into {@code ThirstConfig}. Throws a
     * {@link RuntimeException} when a file cannot be written, so callers never report unsaved data as saved.
     * Called on the logical server thread for server-side files while a world is open.
     */
    void save(Set<ConfigFile> files);

    /** Re-resolves configured drink/food/blacklist item values. Needs live registries and bound tags. */
    void reloadItemValues();
}
