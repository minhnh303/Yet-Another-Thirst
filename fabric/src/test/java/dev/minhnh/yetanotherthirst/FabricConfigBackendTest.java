package dev.minhnh.yetanotherthirst;

import com.google.gson.JsonObject;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption;
import dev.minhnh.yetanotherthirst.client.config.ConfigOptions;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMPAT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricConfigBackendTest {

    private FabricConfigBackend backend;

    @BeforeEach
    void setUp() {
        FabricConfig.resetToDefaults();
        backend = new FabricConfigBackend();
    }

    @Test
    void keysAreUniqueAcrossFilesAndMatchConfigOptionsWithoutDrift() {
        List<ConfigOption<?>> allOptions = ConfigOptions.CATEGORIES.stream()
                .flatMap(category -> category.options().stream())
                .toList();

        Set<String> seenKeys = new HashSet<>();
        for (ConfigFile file : ConfigFile.values()) {
            JsonObject defaults = FabricConfig.defaults(file);
            Set<String> jsonKeys = defaults.keySet().stream()
                    .filter(key -> !"configVersion".equals(key))
                    .collect(Collectors.toSet());
            Set<String> expectedKeys = allOptions.stream()
                    .filter(option -> option.file == file)
                    .map(option -> option.key)
                    .collect(Collectors.toSet());

            assertEquals(expectedKeys, jsonKeys, "Key set drift in Fabric " + file);
            for (String key : jsonKeys) {
                assertTrue(seenKeys.add(key), "Duplicate leaf key across Fabric config files: " + key);
            }
        }

        assertEquals(FabricConfig.defaults(ITEMS).keySet().stream()
                .filter(k -> !"configVersion".equals(k)).collect(Collectors.toSet()), FabricConfig.ITEMS_KEYS);
        assertEquals(FabricConfig.defaults(COMPAT).keySet().stream()
                .filter(k -> !"configVersion".equals(k)).collect(Collectors.toSet()), FabricConfig.COMPAT_KEYS);
    }

    @Test
    void defaultsMatchConfigOptionCanonicalFormAndRanges() {
        List<ConfigOption<?>> allOptions = ConfigOptions.CATEGORIES.stream()
                .flatMap(category -> category.options().stream())
                .toList();

        for (ConfigOption<?> option : allOptions) {
            assertTrue(backend.has(option.file, option.key),
                    "Missing Fabric config key " + option.file + "/" + option.key);
            Object defaultValue = backend.getDefault(option.file, option.key);
            assertNotNull(defaultValue, "Null default for " + option.file + "/" + option.key);
            assertEquals(option.canonical(defaultValue), defaultValue,
                    "Default value is not canonical for " + option.file + "/" + option.key);

            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                double num = ((Number) defaultValue).doubleValue();
                assertTrue(num >= option.min() && num <= option.max(),
                        "Default out of range for " + option.file + "/" + option.key + ": " + num);
            } else if (option.type == ConfigOption.Type.ENUM) {
                assertTrue(option.enumValues().contains(defaultValue),
                        "Default enum value not listed in ConfigOption for " + option.key);
            }
        }
    }

    @Test
    void convertsEnumsNumbersAndCopiesListsSafely() {
        assertEquals("AUTO_DISABLE_YAT", backend.getDefault(COMPAT, "toughAsNailsMode"));
        backend.set(COMPAT, "toughAsNailsMode", ThirstConfig.ToughAsNailsMode.FORCE_YAT);
        assertEquals("FORCE_YAT", backend.get(COMPAT, "toughAsNailsMode"));
        FabricConfig.sync();
        assertEquals(ThirstConfig.ToughAsNailsMode.FORCE_YAT, ThirstConfig.TOUGH_AS_NAILS_MODE);

        backend.set(COMMON, "sprintThreshold", 14L);
        Object rawInt = backend.get(COMMON, "sprintThreshold");
        assertInstanceOf(Integer.class, rawInt);
        assertEquals(14, rawInt);
        FabricConfig.sync();
        assertEquals(14, ThirstConfig.SPRINT_THRESHOLD);

        backend.set(COMMON, "thirstDepletionModifier", 2);
        Object rawDouble = backend.get(COMMON, "thirstDepletionModifier");
        assertInstanceOf(Double.class, rawDouble);
        assertEquals(2.0, (Double) rawDouble, 1e-6);

        @SuppressWarnings("unchecked")
        List<String> effects = (List<String>) backend.get(COMMON, "suspendThirstEffects");
        effects.add("minecraft:resistance");
        assertEquals(List.of("tombstone:ghostly_shape"), backend.get(COMMON, "suspendThirstEffects"));

        @SuppressWarnings("unchecked")
        List<List<Object>> drinks = (List<List<Object>>) backend.get(ITEMS, "drinks");
        drinks.get(0).set(1, 999);
        @SuppressWarnings("unchecked")
        List<List<Object>> drinksAfter = (List<List<Object>>) backend.get(ITEMS, "drinks");
        assertEquals(6, drinksAfter.get(0).get(1));

        assertThrows(IllegalArgumentException.class, () -> backend.get(COMMON, "nonExistentKey"));
        assertThrows(IllegalArgumentException.class, () -> backend.set(COMMON, "nonExistentKey", 1));
    }

    @Test
    void parsedNumbersFollowTheDefaultsIntegerOrDecimalType() {
        JsonObject parsed = com.google.gson.JsonParser.parseString(
                "{\"dirtyPoisonChance\": 1e-3, \"sprintThreshold\": 1e1, \"tiers\": [[40, 1e-1]]}").getAsJsonObject();
        FabricConfig.json(COMMON).add("dirtyPoisonChance", parsed.get("dirtyPoisonChance"));
        FabricConfig.json(COMMON).add("sprintThreshold", parsed.get("sprintThreshold"));
        FabricConfig.json(COMPAT).add("environmentzTemperatureTiers", parsed.get("tiers"));

        assertEquals(0.001, backend.get(COMMON, "dirtyPoisonChance"));
        assertEquals(10, backend.get(COMMON, "sprintThreshold"));
        assertEquals(List.of(List.of(40, 0.1)), backend.get(COMPAT, "environmentzTemperatureTiers"));
    }
}

