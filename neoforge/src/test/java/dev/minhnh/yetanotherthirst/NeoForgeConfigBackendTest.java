package dev.minhnh.yetanotherthirst;

import com.electronwill.nightconfig.core.CommentedConfig;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption;
import dev.minhnh.yetanotherthirst.client.config.ConfigOptions;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeConfigBackendTest {
    private NeoForgeConfigBackend backend;

    @BeforeEach
    void setUp() {
        for (ConfigFile file : ConfigFile.values()) {
            NeoForgeConfigBackend.spec(file).setConfig(CommentedConfig.inMemory());
        }
        backend = new NeoForgeConfigBackend();
    }

    @Test
    void collectRejectsDuplicateLeafKeysInSameFile() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("sectionA");
        builder.define("duplicateLeaf", true);
        builder.pop();
        builder.push("sectionB");
        builder.define("duplicateLeaf", false);
        builder.pop();
        ForgeConfigSpec duplicateSpec = builder.build();

        Map<String, ForgeConfigSpec.ConfigValue<?>> out = new HashMap<>();
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> NeoForgeConfigBackend.collect(duplicateSpec.getValues(), out, ConfigFile.COMMON)
        );
        assertTrue(error.getMessage().contains("duplicateLeaf"));
    }

    @Test
    void allOptionsMatchNeoForgeSpecDefaultsAndRanges() throws Exception {
        Map<ConfigFile, Map<String, ForgeConfigSpec.ConfigValue<?>>> specValues = new HashMap<>();
        for (ConfigFile file : ConfigFile.values()) {
            Map<String, ForgeConfigSpec.ConfigValue<?>> map = new HashMap<>();
            NeoForgeConfigBackend.collect(NeoForgeConfigBackend.spec(file).getValues(), map, file);
            specValues.put(file, map);
        }

        for (ConfigOption<?> option : ConfigOptions.all()) {
            if (option.key.startsWith("environmentz")) {
                assertFalse(backend.has(option.file, option.key), "EnvironmentZ key should not exist in NeoForge spec: " + option.key);
                continue;
            }
            assertTrue(backend.has(option.file, option.key), "Missing NeoForge spec key: " + option.file + "/" + option.key);
            Object defaultValue = backend.getDefault(option.file, option.key);
            assertNotNull(defaultValue, "Null default for " + option.file + "/" + option.key);
            assertEquals(defaultValue, backend.get(option.file, option.key));

            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                ForgeConfigSpec.ConfigValue<?> configValue = specValues.get(option.file).get(option.key);
                ForgeConfigSpec.ValueSpec valueSpec = NeoForgeConfigBackend.spec(option.file).getSpec().get(configValue.getPath());
                Object range = valueSpec.getRange();
                assertNotNull(range, "Expected range on " + option.file + "/" + option.key);
                assertEquals(option.min(), readRangeBound(range, "min"), 1e-6, "Min mismatch for " + option.key);
                assertEquals(option.max(), readRangeBound(range, "max"), 1e-6, "Max mismatch for " + option.key);
            }
        }
    }

    @Test
    void convertsEnumsNumbersAndListsRoundTrip() {
        assertEquals("AUTO_DISABLE_YAT", backend.get(ConfigFile.COMPAT, "toughAsNailsMode"));
        backend.set(ConfigFile.COMPAT, "toughAsNailsMode", "FORCE_YAT");
        assertEquals("FORCE_YAT", backend.get(ConfigFile.COMPAT, "toughAsNailsMode"));
        NeoForgeConfigCompat.sync();
        assertEquals(ThirstConfig.ToughAsNailsMode.FORCE_YAT, ThirstConfig.TOUGH_AS_NAILS_MODE);

        // Passing Double into Integer ConfigValue converts cleanly
        backend.set(ConfigFile.COMMON, "sprintThreshold", 14.0);
        assertEquals(14, backend.get(ConfigFile.COMMON, "sprintThreshold"));

        // Passing Float into Double ConfigValue converts cleanly
        backend.set(ConfigFile.COMMON, "thirstDepletionModifier", 2.5f);
        assertEquals(2.5, (Double) backend.get(ConfigFile.COMMON, "thirstDepletionModifier"), 1e-6);

        // List round-trip
        List<String> effects = List.of("minecraft:water_breathing >= 1", "minecraft:regeneration");
        backend.set(ConfigFile.COMMON, "suspendThirstEffects", effects);
        assertEquals(effects, backend.get(ConfigFile.COMMON, "suspendThirstEffects"));

        List<List<Object>> drinks = List.of(List.of("minecraft:apple", 3, 4));
        backend.set(ConfigFile.ITEMS, "drinks", drinks);
        assertEquals(drinks, backend.get(ConfigFile.ITEMS, "drinks"));

        assertThrows(IllegalArgumentException.class, () -> backend.get(ConfigFile.COMMON, "nonExistentKey"));
    }

    private static double readRangeBound(Object range, String fieldName) throws Exception {
        Field field = range.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return ((Number) field.get(range)).doubleValue();
    }
}

