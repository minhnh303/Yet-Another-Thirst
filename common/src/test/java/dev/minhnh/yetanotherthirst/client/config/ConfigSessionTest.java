package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.CLIENT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMPAT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigSessionTest {

    @Test
    void settingSavedValueRemovesPendingEdit() {
        ConfigOption option = integer(COMMON, "threshold");
        FakeBackend backend = backend(option, 5, 3);
        ConfigSession session = new ConfigSession(backend, false, NO_SERVER);

        session.set(option, 8);
        assertEquals(1, session.pendingCount());

        session.set(option, 5);
        assertEquals(0, session.pendingCount());
        assertFalse(session.modified(option));
    }

    @Test
    void resetUsesDefaultAndDiscardRestoresSavedView() {
        ConfigOption option = integer(COMMON, "threshold");
        ConfigSession session = new ConfigSession(backend(option, 5, 3), false, NO_SERVER);

        session.set(option, 8);
        session.reset(option);
        assertEquals(3, session.value(option));
        assertTrue(session.modified(option));

        session.discard();
        assertEquals(5, session.value(option));
        assertEquals(0, session.pendingCount());
    }

    @Test
    void remoteSessionLocksServerFilesOnly() {
        ConfigOption common = integer(COMMON, "commonValue");
        ConfigOption client = integer(CLIENT, "clientValue");
        FakeBackend backend = new FakeBackend();
        backend.add(common, 1, 0);
        backend.add(client, 1, 0);
        ConfigSession session = new ConfigSession(backend, true, NO_SERVER);

        session.set(common, 2);
        session.set(client, 2);

        assertTrue(session.locked(common));
        assertFalse(session.locked(client));
        assertEquals(1, session.pendingCount());
        assertEquals(1, session.value(common));
        assertEquals(2, session.value(client));
    }

    @Test
    void remoteSessionKeepsClientOnlyOptionsInServerFilesEditable() {
        ConfigOption visual = integer(COMPAT, "visualValue").clientOnly();
        ConfigOption gameplay = integer(COMPAT, "gameplayValue");
        FakeBackend backend = new FakeBackend();
        backend.add(visual, 1, 0);
        backend.add(gameplay, 1, 0);
        ConfigSession session = new ConfigSession(backend, true, NO_SERVER);

        session.set(visual, 2);
        session.set(gameplay, 2);

        assertFalse(session.locked(visual));
        assertTrue(session.locked(gameplay));
        assertEquals(1, session.pendingCount());
        assertEquals(1, session.save().saved());
        assertEquals(2, backend.get(COMPAT, "visualValue"));
    }

    @Test
    void saveWritesOnlyTouchedFiles() {
        ConfigOption common = integer(COMMON, "commonValue");
        ConfigOption client = integer(CLIENT, "clientValue");
        FakeBackend backend = new FakeBackend();
        backend.add(common, 1, 0);
        backend.add(client, 1, 0);
        ConfigSession session = new ConfigSession(backend, false, NO_SERVER);

        session.set(common, 2);
        session.set(client, 3);
        session.save();

        assertEquals(EnumSet.of(COMMON, CLIENT), backend.savedFiles);
        assertEquals(List.of(new Write(COMMON, "commonValue", 2), new Write(CLIENT, "clientValue", 3)), backend.writes);
        assertEquals(0, session.pendingCount());
        assertEquals(0, backend.reloads);
    }

    @Test
    void serverFilesAndItemReloadRunOnServerThreadWhileClientFileDoesNot() {
        ConfigOption item = integer(ITEMS, "itemValue");
        ConfigOption client = integer(CLIENT, "clientValue");
        FakeBackend backend = new FakeBackend();
        backend.add(item, 1, 0);
        backend.add(client, 1, 0);
        FakeServer server = new FakeServer();
        backend.onServerThread = () -> server.running;
        ConfigSession session = new ConfigSession(backend, false, server);

        session.set(item, 2);
        session.set(client, 3);
        ConfigSession.SaveResult result = session.save();

        assertEquals(new ConfigSession.SaveResult(2, 0), result);
        assertEquals(2, server.tasks);
        assertEquals(1, backend.reloads);
        assertEquals(List.of(ITEMS + ":true", CLIENT + ":false"), backend.saveThreads);
    }

    @Test
    void itemValuesAreNotReloadedWithoutIntegratedServer() {
        ConfigOption item = integer(ITEMS, "itemValue");
        FakeBackend backend = backend(item, 1, 0);
        ConfigSession session = new ConfigSession(backend, false, NO_SERVER);

        session.set(item, 2);
        session.save();

        assertEquals(Set.of(ITEMS), backend.savedFiles);
        assertEquals(0, backend.reloads);
    }

    @Test
    void failedFileWriteKeepsEditsPendingAndRollsBackMemory() {
        ConfigOption common = integer(COMMON, "commonValue");
        ConfigOption client = integer(CLIENT, "clientValue");
        FakeBackend backend = new FakeBackend();
        backend.add(common, 1, 0);
        backend.add(client, 1, 0);
        backend.failingFile = COMMON;
        ConfigSession session = new ConfigSession(backend, false, NO_SERVER);

        session.set(common, 2);
        session.set(client, 3);
        ConfigSession.SaveResult result = session.save();

        assertEquals(new ConfigSession.SaveResult(1, 1), result);
        assertTrue(session.modified(common));
        assertFalse(session.modified(client));
        assertEquals(2, session.value(common));
        assertEquals(1, backend.get(COMMON, "commonValue"));
        assertEquals(0, backend.reloads);

        backend.failingFile = null;
        assertEquals(new ConfigSession.SaveResult(1, 0), session.save());
        assertEquals(2, backend.get(COMMON, "commonValue"));
    }

    @Test
    void serverThreadFailureKeepsServerEditsPending() {
        ConfigOption common = integer(COMMON, "commonValue");
        FakeBackend backend = backend(common, 1, 0);
        ConfigSession session = new ConfigSession(backend, false, task -> {
            throw new IllegalStateException("timed out");
        });

        session.set(common, 2);

        assertEquals(new ConfigSession.SaveResult(0, 1), session.save());
        assertTrue(session.modified(common));
    }

    @Test
    void deadlineWithdrawsTaskThatHasNotStarted() {
        List<Runnable> queue = new ArrayList<>();
        AtomicInteger runs = new AtomicInteger();

        assertThrows(IllegalStateException.class,
                () -> ConfigSession.runWithDeadline(queue::add, runs::incrementAndGet, 10, TimeUnit.MILLISECONDS));
        queue.forEach(Runnable::run);

        assertEquals(0, runs.get());
    }

    @Test
    void deadlineWaitsForTaskThatAlreadyStarted() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        AtomicInteger runs = new AtomicInteger();
        Runnable slowTask = () -> {
            started.countDown();
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            runs.incrementAndGet();
        };
        ExecutorService server = Executors.newSingleThreadExecutor();
        try {
            ConfigSession.runWithDeadline(server, slowTask, 20, TimeUnit.MILLISECONDS);
            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertEquals(1, runs.get());
        } finally {
            server.shutdownNow();
        }
    }

    private static final ConfigSession.ServerThread NO_SERVER = task -> {
        task.run();
        return false;
    };

    /** Runs tasks in place but flags them as server-thread work, like a waiting integrated server would. */
    private static final class FakeServer implements ConfigSession.ServerThread {
        private boolean running;
        private int tasks;

        @Override
        public boolean runAndWait(Runnable task) {
            tasks++;
            running = true;
            try {
                task.run();
            } finally {
                running = false;
            }
            return true;
        }
    }

    private static ConfigOption<Integer> integer(IConfigBackend.ConfigFile file, String key) {
        ConfigOption<Integer> option = new ConfigOption<>(file, key, ConfigOption.Type.INT);
        option.min = 0;
        option.max = 20;
        return option;
    }

    private static FakeBackend backend(ConfigOption<?> option, Object value, Object defaultValue) {
        FakeBackend backend = new FakeBackend();
        backend.add(option, value, defaultValue);
        return backend;
    }

    private record Key(IConfigBackend.ConfigFile file, String key) {
    }

    private record Write(IConfigBackend.ConfigFile file, String key, Object value) {
    }

    private static final class FakeBackend implements IConfigBackend {
        private final Map<Key, Object> values = new HashMap<>();
        private final Map<Key, Object> defaults = new HashMap<>();
        private final List<Write> writes = new ArrayList<>();
        private final List<String> saveThreads = new ArrayList<>();
        private Set<ConfigFile> savedFiles = EnumSet.noneOf(ConfigFile.class);
        private ConfigFile failingFile;
        private BooleanSupplier onServerThread = () -> false;
        private int reloads;

        void add(ConfigOption<?> option, Object value, Object defaultValue) {
            Key key = new Key(option.file, option.key);
            values.put(key, value);
            defaults.put(key, defaultValue);
        }

        @Override
        public boolean has(ConfigFile file, String key) {
            return values.containsKey(new Key(file, key));
        }

        @Override
        public Object get(ConfigFile file, String key) {
            return values.get(new Key(file, key));
        }

        @Override
        public Object getDefault(ConfigFile file, String key) {
            return defaults.get(new Key(file, key));
        }

        @Override
        public void set(ConfigFile file, String key, Object value) {
            values.put(new Key(file, key), value);
            writes.add(new Write(file, key, value));
        }

        @Override
        public void save(Set<ConfigFile> files) {
            if (failingFile != null && files.contains(failingFile)) {
                throw new UncheckedIOException(new IOException("disk full"));
            }
            savedFiles.addAll(files);
            for (ConfigFile file : files) saveThreads.add(file + ":" + onServerThread.getAsBoolean());
        }

        @Override
        public void reloadItemValues() {
            reloads++;
        }
    }
}
