package dev.minhnh.yetanotherthirst.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minhnh.yetanotherthirst.Lang;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigOptionsTest {

    @Test
    void optionKeysAreUniqueWithinEachFileAndIndexedByKey() {
        Set<String> keys = new HashSet<>();
        for (ConfigOption<?> option : options()) {
            assertTrue(keys.add(option.file + "/" + option.key),
                    () -> "Duplicate option key " + option.file + "/" + option.key);
            assertSame(option, ConfigOptions.byKey(option.key));
        }
    }

    @Test
    void optionMetadataAndControlFactoriesAreValid() {
        for (ConfigOption.Type type : ConfigOption.Type.values()) {
            assertNotNull(ConfigControlFactory.forType(type), "Missing factory for " + type);
        }

        for (ConfigOption<?> option : options()) {
            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                assertTrue(Double.isFinite(option.min()), option.key + " min must be finite");
                assertTrue(Double.isFinite(option.max()), option.key + " max must be finite");
                assertTrue(option.min() <= option.max(), option.key + " range is reversed");
                assertTrue(option.step() > 0, option.key + " step must be positive");
            }
            if (option.type == ConfigOption.Type.ENUM) {
                assertFalse(option.enumValues().isEmpty(), option.key + " enum has no values");
            }
            if (option.type == ConfigOption.Type.LIST) {
                assertNotNull(option.schema(), option.key + " list has no schema");
            }
        }
    }

    @Test
    void searchIsCaseInsensitiveAndMatchesKeys() {
        ConfigOption<?> option = ConfigOptions.byKey("sprintPrevention");
        assertNotNull(option);
        assertEquals("sprintPrevention", option.key);

        assertTrue(option.matches("SPRINTPREVENTION"));
        assertFalse(option.matches("definitely-not-an-option"));
    }

    @Test
    void everyCategorySectionAndOptionHasEnglishAndVietnameseTranslations() {
        for (String locale : List.of("en_us", "vi_vn")) {
            JsonObject language = language(locale);
            for (ConfigCategory category : ConfigOptions.CATEGORIES) {
                require(language, Lang.CONFIG_PREFIX + "category." + category.id());
                require(language, Lang.CONFIG_PREFIX + "category." + category.id() + ".desc");
                for (ConfigCategory.Section section : category.sections()) {
                    require(language, Lang.CONFIG_PREFIX + "section." + section.id());
                    for (ConfigOption<?> option : section.options()) {
                        require(language, Lang.CONFIG_PREFIX + "option." + option.key);
                        require(language, Lang.CONFIG_PREFIX + "option." + option.key + ".desc");
                    }
                }
            }
        }
    }

    private static List<ConfigOption<?>> options() {
        return ConfigOptions.CATEGORIES.stream().flatMap(category -> category.options().stream()).toList();
    }

    private static JsonObject language(String locale) {
        String path = "/assets/yet_another_thirst/lang/" + locale + ".json";
        InputStream stream = ConfigOptionsTest.class.getResourceAsStream(path);
        assertNotNull(stream, "Missing language resource " + path);
        return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static void require(JsonObject language, String key) {
        assertTrue(language.has(key), "Missing translation " + key);
    }
}

