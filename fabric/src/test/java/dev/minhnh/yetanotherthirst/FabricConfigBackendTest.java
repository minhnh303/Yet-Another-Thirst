package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption;
import dev.minhnh.yetanotherthirst.client.config.ConfigOptions;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricConfigBackendTest {
    private FabricConfigBackend backend;

    @BeforeEach
    void setUp() {
        FabricConfig.resetInMemoryDefaults();
        backend = new FabricConfigBackend();
    }

    @AfterEach
    void tearDown() {
        FabricConfig.resetInMemoryDefaults();
    }

    @Test
    void allOptionsExistInFabricDefaultsExceptWaterBoiler() {
        for (ConfigOption<?> option : ConfigOptions.all()) {
            if (option.file == ConfigFile.ITEMS && option.key.startsWith("waterBoiler")) {
                assertFalse(backend.has(option.file, option.key), "Water Boiler should be hidden on Fabric: " + option.key);
                continue;
            }
            assertTrue(backend.has(option.file, option.key), "Missing Fabric key: " + option.file + "/" + option.key);
            Object defaultValue = backend.getDefault(option.file, option.key);
            assertNotNull(defaultValue, "Null default for " + option.file + "/" + option.key);
            assertEquals(option.canonical(defaultValue), option.canonical(backend.get(option.file, option.key)));

            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                double numeric = ((Number) defaultValue).doubleValue();
                assertTrue(numeric >= option.min() && numeric <= option.max(),
                        "Fabric default out of range for " + option.file + "/" + option.key);
            }
        }
    }

    @Test
    void convertsEnumsNumbersListsAndSyncsRoundTrip() {
        assertEquals("AUTO_DISABLE_YAT", backend.get(ConfigFile.COMPAT, "toughAsNailsMode"));
        backend.set(ConfigFile.COMPAT, "toughAsNailsMode", ThirstConfig.ToughAsNailsMode.FORCE_YAT);
        assertEquals("FORCE_YAT", backend.get(ConfigFile.COMPAT, "toughAsNailsMode"));

        backend.set(ConfigFile.COMMON, "sprintThreshold", 12);
        assertEquals(12, ((Number) backend.get(ConfigFile.COMMON, "sprintThreshold")).intValue());

        backend.set(ConfigFile.COMMON, "thirstDepletionModifier", 2.25f);
        assertEquals(2.25f, ((Number) backend.get(ConfigFile.COMMON, "thirstDepletionModifier")).floatValue(), 1e-4f);

        List<String> effects = List.of("minecraft:water_breathing >= 1");
        backend.set(ConfigFile.COMMON, "suspendThirstEffects", effects);
        assertEquals(effects, backend.get(ConfigFile.COMMON, "suspendThirstEffects"));

        List<List<Object>> drinks = List.of(List.of("minecraft:apple", 3, 4));
        backend.set(ConfigFile.ITEMS, "drinks", drinks);
        assertEquals(drinks, backend.get(ConfigFile.ITEMS, "drinks"));

        List<List<Object>> tiers = List.of(List.of(-100, 0.5), List.of(40, 1.75));
        backend.set(ConfigFile.COMPAT, "environmentzTemperatureTiers", tiers);
        FabricConfig.sync();

        assertEquals(ThirstConfig.ToughAsNailsMode.FORCE_YAT, ThirstConfig.TOUGH_AS_NAILS_MODE);
        assertEquals(12, ThirstConfig.SPRINT_THRESHOLD);
        assertEquals(2.25f, ThirstConfig.THIRST_DEPLETION_MODIFIER, 1e-4f);
        assertEquals(effects, ThirstConfig.SUSPENDED_THIRST_EFFECTS_RAW);
        assertEquals(
                List.of(new ThirstConfig.TemperatureTier(-100, 0.5f), new ThirstConfig.TemperatureTier(40, 1.75f)),
                ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS
        );

        assertThrows(IllegalArgumentException.class, () -> backend.get(ConfigFile.COMMON, "nonExistentKey"));
        assertThrows(IllegalArgumentException.class, () -> backend.get(ConfigFile.ITEMS, "waterBoilerCapacity"));
    }
}

