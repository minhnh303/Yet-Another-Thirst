package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigSessionTest {
    @Test
    void pendingEditsResetDiscardAndReturnToSavedValue() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigOption<Integer> option = GeneralOptions.SPRINT_THRESHOLD;
        backend.seed(option, 6);
        ConfigSession session = new ConfigSession(backend, false, Runnable::run);

        session.set(option, 12);
        assertEquals(1, session.pendingCount());
        assertEquals(12, session.value(option));
        assertTrue(session.modified(option));
        assertFalse(session.isDefault(option));

        session.set(option, 6);
        assertEquals(0, session.pendingCount());
        assertFalse(session.modified(option));

        session.set(option, 15);
        session.reset(option);
        assertEquals(6, session.value(option));
        assertTrue(session.isDefault(option));

        session.set(option, 9);
        session.discard();
        assertEquals(6, session.value(option));
        assertEquals(0, session.pendingCount());
    }

    @Test
    void saveTouchesOnlyChangedFilesAndAppliesOnExecutor() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigOption<Integer> common = GeneralOptions.SPRINT_THRESHOLD;
        ConfigOption<List<ItemValue>> drinks = ItemOptions.DRINKS;
        backend.seed(common, 6);
        backend.seed(drinks, List.of());
        List<Runnable> queued = new ArrayList<>();
        ConfigSession session = new ConfigSession(backend, false, queued::add);

        session.set(common, 7);
        session.save();
        assertEquals(1, backend.saveCalls);
        assertEquals(Set.of(ConfigFile.COMMON), backend.savedFiles);
        assertEquals(1, queued.size());
        assertEquals(0, backend.applyCalls);
        queued.remove(0).run();
        assertEquals(Set.of(ConfigFile.COMMON), backend.appliedFiles);

        backend.savedFiles.clear();
        session.set(drinks, List.of(new ItemValue("minecraft:potion", 4, 2)));
        session.save();
        assertEquals(Set.of(ConfigFile.ITEMS), backend.savedFiles);
        assertEquals(List.of(List.of("minecraft:potion", 4, 2)), backend.get(ConfigFile.ITEMS, "drinks"));
        assertEquals(1, queued.size());
        backend.appliedFiles.clear();
        queued.get(0).run();
        assertEquals(Set.of(ConfigFile.ITEMS), backend.appliedFiles);
    }

    @Test
    void failedSaveKeepsEditsPendingAndRollsBackBackend() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigOption<Integer> option = GeneralOptions.SPRINT_THRESHOLD;
        backend.seed(option, 6);
        List<Runnable> queued = new ArrayList<>();
        ConfigSession session = new ConfigSession(backend, false, queued::add);

        session.set(option, 7);
        backend.failOnSave = new java.io.UncheckedIOException(new java.io.IOException("disk full"));
        assertFalse(session.save());
        assertEquals(1, session.pendingCount());
        assertEquals(7, session.value(option));
        assertEquals(6, ((Number) backend.get(ConfigFile.COMMON, option.key)).intValue());
        assertTrue(queued.isEmpty());

        backend.failOnSave = null;
        assertTrue(session.save());
        assertEquals(0, session.pendingCount());
        assertEquals(7, ((Number) backend.get(ConfigFile.COMMON, option.key)).intValue());
    }

    @Test
    void remoteSessionLocksServerOptionsButNotClientOnlyCompatOptions() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigSession session = new ConfigSession(backend, true, Runnable::run);

        assertTrue(session.locked(GeneralOptions.SPRINT_THRESHOLD));
        assertTrue(session.locked(CompatOptions.TOUGH_AS_NAILS_MODE));
        assertFalse(session.locked(CompatOptions.APPLE_SKIN_THIRST_TOOLTIP));
        assertFalse(session.locked(CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR));

        session.set(CompatOptions.APPLE_SKIN_THIRST_TOOLTIP, true);
        assertTrue(session.save());
        assertEquals(Set.of(ConfigFile.COMPAT), backend.savedFiles);
    }

    @Test
    void failedLaterFileRewritesEarlierFilesWithSavedValues() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigOption<Integer> common = GeneralOptions.SPRINT_THRESHOLD;
        ConfigOption<List<ItemValue>> drinks = ItemOptions.DRINKS;
        backend.seed(common, 6);
        backend.seed(drinks, List.of());
        List<Runnable> queued = new ArrayList<>();
        ConfigSession session = new ConfigSession(backend, false, queued::add);

        session.set(common, 7);
        session.set(drinks, List.of(new ItemValue("minecraft:potion", 4, 2)));
        backend.failOnFile = ConfigFile.ITEMS;
        backend.failOnSave = new java.io.UncheckedIOException(new java.io.IOException("locked"));

        assertFalse(session.save());
        assertEquals(2, session.pendingCount());
        assertTrue(queued.isEmpty());
        Object persistedCommon = backend.persisted.get(ConfigFile.COMMON).get(common.configKey());
        assertEquals(6, ((Number) persistedCommon).intValue());
        assertFalse(backend.persisted.containsKey(ConfigFile.ITEMS));
    }

    @Test
    void saveSortsTemperatureTiersAscendingByThreshold() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigOption<List<TemperatureTier>> tiers = CompatOptions.ENVIRONMENTZ_TEMPERATURE_TIERS;
        backend.seed(tiers, List.of());
        ConfigSession session = new ConfigSession(backend, false, Runnable::run);

        session.set(tiers, List.of(
                new TemperatureTier(100, 2.5),
                new TemperatureTier(-50, 0.85),
                new TemperatureTier(20, 1.0)
        ));
        session.save();

        assertEquals(
                List.of(
                        new TemperatureTier(-50, 0.85),
                        new TemperatureTier(20, 1.0),
                        new TemperatureTier(100, 2.5)
                ),
                session.value(tiers)
        );
        assertEquals(
                List.of(
                        List.of(-50, 0.85),
                        List.of(20, 1.0),
                        List.of(100, 2.5)
                ),
                backend.get(ConfigFile.COMPAT, "environmentzTemperatureTiers")
        );
    }

    @Test
    void remoteSessionLocksServerFilesOnly() {
        TestConfigBackend backend = new TestConfigBackend();
        ConfigSession session = new ConfigSession(backend, true, Runnable::run);
        ConfigOption<Integer> common = GeneralOptions.SPRINT_THRESHOLD;
        ConfigOption<List<ItemValue>> items = ItemOptions.DRINKS;
        ConfigOption<String> compat = CompatOptions.TOUGH_AS_NAILS_MODE;
        ConfigOption<Integer> client = ClientOptions.HUD_X_OFFSET;

        assertTrue(session.locked(common));
        assertTrue(session.locked(items));
        assertTrue(session.locked(compat));
        assertFalse(session.locked(client));

        session.set(common, 10);
        session.set(client, 10);
        assertEquals(1, session.pendingCount());
        assertEquals(10, session.value(client));
    }
}
