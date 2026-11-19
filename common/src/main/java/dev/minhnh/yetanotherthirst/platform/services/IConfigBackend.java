package dev.minhnh.yetanotherthirst.platform.services;

import java.util.Set;

/** Loader-agnostic read/write access to the mod config files. */
public interface IConfigBackend {
    enum ConfigFile {
        COMMON,
        ITEMS,
        COMPAT,
        CLIENT;

        public boolean serverSide() {
            return this != CLIENT;
        }
    }

    boolean has(ConfigFile file, String key);

    Object get(ConfigFile file, String key);

    Object getDefault(ConfigFile file, String key);

    void set(ConfigFile file, String key, Object value);

    /** Persists the given files to disk. Throws on I/O failure so callers never report an unwritten save. */
    void save(Set<ConfigFile> files);

    /**
     * Pushes the stored values of the given files into runtime state ({@code ThirstConfig}, item values).
     * Called on the logical server thread when an integrated server runs, otherwise on the caller's thread.
     */
    void apply(Set<ConfigFile> files);
}
