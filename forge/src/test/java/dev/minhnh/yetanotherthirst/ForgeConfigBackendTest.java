package dev.minhnh.yetanotherthirst;

import com.electronwill.nightconfig.core.CommentedConfig;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption;
import dev.minhnh.yetanotherthirst.client.config.ConfigOptions;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraftforge.common.ForgeConfigSpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMPAT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeConfigBackendTest {

    private ForgeConfigBackend backend;

    @BeforeEach
    void setUp() {
        for (ConfigFile file : ConfigFile.values()) {
            ForgeConfigSpec spec = ForgeConfigBackend.spec(file);
            CommentedConfig memory = CommentedConfig.inMemory();
            spec.correct(memory);
            spec.setConfig(memory);
        }
        backend = new ForgeConfigBackend();
    }

    @Test
    void specsHaveUniqueLeafKeysAndCollectFailsFastOnCollision() {
        for (ConfigFile file : ConfigFile.values()) {
            Map<String, ForgeConfigSpec.ConfigValue<?>> values = backend.values(file);
            assertFalse(values.isEmpty(), file + " spec should not be empty");
        }

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("sectionA");
        builder.define("duplicateLeaf", true);
        builder.pop();
        builder.push("sectionB");
        builder.define("duplicateLeaf", false);
        builder.pop();
        ForgeConfigSpec collidingSpec = builder.build();

        assertThrows(IllegalStateException.class,
                () -> ForgeConfigBackend.collect(collidingSpec.getValues(), new HashMap<>()));
    }

    @Test
    void configOptionsMatchForgeSpecsRangesAndDefaultsWithoutDrift() {
        List<ConfigOption<?>> allOptions = ConfigOptions.CATEGORIES.stream()
                .flatMap(category -> category.options().stream())
                .toList();

        for (ConfigFile file : ConfigFile.values()) {
            Set<String> specKeys = backend.values(file).keySet();
            Set<String> expectedKeys = allOptions.stream()
                    .filter(option -> option.file == file && !"environmentz".equals(option.requiredMod()))
                    .map(option -> option.key)
                    .collect(Collectors.toSet());
            assertEquals(expectedKeys, specKeys, "Key set drift in " + file);
        }

        for (ConfigOption<?> option : allOptions) {
            if ("environmentz".equals(option.requiredMod())) {
                assertFalse(backend.has(option.file, option.key),
                        "EnvironmentZ option should not be present on Forge: " + option.key);
                continue;
            }

            assertTrue(backend.has(option.file, option.key),
                    "Missing Forge spec entry for " + option.file + "/" + option.key);

            ForgeConfigSpec spec = ForgeConfigBackend.spec(option.file);
            ForgeConfigSpec.ConfigValue<?> configValue = backend.values(option.file).get(option.key);
            ForgeConfigSpec.ValueSpec valueSpec = spec.getSpec().get(configValue.getPath());
            assertNotNull(valueSpec, "Missing ValueSpec for " + option.file + "/" + option.key);

            Object defaultValue = backend.getDefault(option.file, option.key);
            assertNotNull(defaultValue, "Null default for " + option.file + "/" + option.key);
            assertEquals(option.canonical(defaultValue), defaultValue,
                    "Default value is not canonical for " + option.file + "/" + option.key);
            assertTrue(valueSpec.test( configValue.getDefault() ),
                    "Spec rejects its own default for " + option.file + "/" + option.key);

            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                ForgeConfigSpec.Range<?> range = valueSpec.getRange();
                assertNotNull(range, "Missing range in Forge spec for " + option.file + "/" + option.key);
                assertEquals(option.min(), ((Number) range.getMin()).doubleValue(), 1e-6,
                        "Min drift for " + option.file + "/" + option.key);
                assertEquals(option.max(), ((Number) range.getMax()).doubleValue(), 1e-6,
                        "Max drift for " + option.file + "/" + option.key);
            } else if (option.type == ConfigOption.Type.ENUM) {
                assertTrue(option.enumValues().contains(defaultValue),
                        "Default enum value not listed in ConfigOption for " + option.key);
            }
        }
    }

    @Test
    void convertsEnumsNumbersAndCopiesListsSafely() {
        assertEquals("AUTO_DISABLE_YAT", backend.getDefault(COMPAT, "toughAsNailsMode"));
        assertEquals("AUTO_DISABLE_YAT", backend.get(COMPAT, "toughAsNailsMode"));

        backend.set(COMPAT, "toughAsNailsMode", "FORCE_YAT");
        assertEquals("FORCE_YAT", backend.get(COMPAT, "toughAsNailsMode"));
        ForgeConfigCompat.sync();
        assertEquals(ThirstConfig.ToughAsNailsMode.FORCE_YAT, ThirstConfig.TOUGH_AS_NAILS_MODE);

        backend.set(COMMON, "sprintThreshold", 12L);
        Object rawInt = backend.get(COMMON, "sprintThreshold");
        assertInstanceOf(Integer.class, rawInt);
        assertEquals(12, rawInt);
        ForgeConfigCommon.sync();
        assertEquals(12, ThirstConfig.SPRINT_THRESHOLD);

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

        backend.set(COMMON, "suspendThirstEffects", List.of("minecraft:speed >= 1"));
        assertEquals(List.of("minecraft:speed >= 1"), backend.get(COMMON, "suspendThirstEffects"));
    }
}

