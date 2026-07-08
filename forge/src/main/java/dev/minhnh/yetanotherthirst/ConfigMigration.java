package dev.minhnh.yetanotherthirst;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;

public final class ConfigMigration {

    private static final String OLD_DIR = Constants.MOD_ID;
    private static final String NEW_DIR = Constants.CONFIG_DIR;

    public static void run() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path oldFolder = configDir.resolve(OLD_DIR);
        Path newFolder = configDir.resolve(NEW_DIR);
        Path oldCommon = oldFolder.resolve("common.toml");

        if (!Files.exists(oldCommon)) return;

        Constants.LOG.info("[YAT] Migrating config from '{}' to '{}'", OLD_DIR, NEW_DIR);

        try {
            Files.createDirectories(newFolder);
        } catch (IOException e) {
            Constants.LOG.error("[YAT] Failed to create new config dir", e);
            return;
        }

        try (CommentedFileConfig old = CommentedFileConfig.of(oldCommon, TomlFormat.instance())) {
            old.load();
            migrateSection(old, newFolder.resolve("common.toml"),
                    "general", "effects", "dehydration", "depletion", "purity", "world", "handDrinking");
            migrateSection(old, newFolder.resolve("items.toml"),
                    "items", "filter", "boiler");
            migrateSection(old, newFolder.resolve("compat.toml"),
                    "compatibility");
        } catch (Exception e) {
            Constants.LOG.error("[YAT] Config migration failed", e);
            return;
        }

        try {
            Files.delete(oldCommon);
            Path oldClient = oldFolder.resolve("client.toml");
            if (Files.exists(oldClient)) {
                Path newClient = newFolder.resolve("client.toml");
                if (!Files.exists(newClient)) Files.copy(oldClient, newClient);
                Files.delete(oldClient);
            }
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(oldFolder)) {
                if (!stream.iterator().hasNext()) Files.delete(oldFolder);
            }
        } catch (IOException e) {
            Constants.LOG.warn("[YAT] Could not clean up old config files", e);
        }

        Constants.LOG.info("[YAT] Config migration complete.");
    }

    private static void migrateSection(Config source, Path target, String... sections) throws IOException {
        if (Files.exists(target)) return;
        CommentedConfig dest = CommentedConfig.inMemory();
        for (String section : sections) {
            Config data = source.get(section);
            if (data != null) dest.set(section, data);
        }
        try (Writer w = Files.newBufferedWriter(target)) {
            TomlFormat.instance().createWriter().write(dest, w);
        }
    }

    private ConfigMigration() {}
}
