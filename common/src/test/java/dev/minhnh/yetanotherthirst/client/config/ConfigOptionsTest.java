package dev.minhnh.yetanotherthirst.client.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigOptionsTest {
    private static final Pattern DEFINE_IN_RANGE = Pattern.compile(
            "\\.defineInRange\\(\\s*\"([^\"]+)\"\\s*,\\s*([^,]+?)\\s*,\\s*([^,]+?)\\s*,\\s*([^)]+?)\\s*\\)");
    private static final Pattern DEFINE_BOOL = Pattern.compile(
            "\\.define\\(\\s*\"([^\"]+)\"\\s*,\\s*(true|false)\\s*\\)");
    private static final Pattern DEFINE_ENUM = Pattern.compile(
            "\\.defineEnum\\(\\s*\"([^\"]+)\"\\s*,\\s*([A-Za-z0-9_.]+)\\s*\\)");
    private static final Pattern DEFINE_LIST = Pattern.compile(
            "\\.defineList\\(\\s*\"([^\"]+)\"");
    private static final Pattern JSON_ADD_PROP = Pattern.compile(
            "obj\\.addProperty\\(\\s*\"([^\"]+)\"\\s*,\\s*(.+?)\\s*\\);");
    private static final Pattern JSON_ADD_LIST = Pattern.compile(
            "obj\\.add\\(\\s*\"([^\"]+)\"\\s*,");

    private record SpecEntry(ConfigFile file, String key, ConfigOption.Type type, Object defaultValue, double min, double max) {}

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void hudCategoryHasExactlyOnePreviewSectionWithHudOffsets() {
        List<ConfigCategory.Section> previews = ConfigOptions.CATEGORIES.stream()
                .flatMap(category -> category.sections().stream())
                .filter(ConfigCategory.Section::hudPreview)
                .toList();

        assertEquals(1, previews.size());
        assertTrue(previews.get(0).options().contains(ClientOptions.HUD_X_OFFSET));
        assertTrue(previews.get(0).options().contains(ClientOptions.HUD_Y_OFFSET));
    }

    @Test
    void keysAreUniqueWithinEachFileAndConstraintsAreValid() {
        Set<ConfigKey> keys = new HashSet<>();
        for (ConfigOption<?> option : ConfigOptions.all()) {
            assertTrue(keys.add(option.configKey()), "Duplicate key: " + option.configKey());
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
    void everyOptionBelongsToExactlyOneCategoryAndSection() {
        Map<ConfigOption<?>, String> seen = new IdentityHashMap<>();
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            for (ConfigCategory.Section section : category.sections()) {
                for (ConfigOption<?> option : section.options()) {
                    String location = category.id() + "/" + section.id();
                    assertFalse(seen.containsKey(option),
                            "Option " + option.configKey() + " appears in both " + seen.get(option) + " and " + location);
                    seen.put(option, location);
                    assertEquals(category.id(), option.categoryId());
                    assertEquals(section.id(), option.sectionId());
                }
            }
        }
        assertEquals(ConfigOptions.all().size(), seen.size());

        ConfigOption<Boolean> sample = ConfigOption.bool(ConfigFile.COMMON, "sampleKey");
        sample.bindOwner("general", "basics");
        assertThrows(IllegalStateException.class, () -> sample.bindOwner("depletion", "rates"));
    }

    @Test
    void controlFactoryExistsForEveryOptionTypeAndOption() {
        ConfigSession session = new ConfigSession(new TestConfigBackend(), false, Runnable::run);
        for (ConfigOption.Type type : ConfigOption.Type.values()) {
            assertNotNull(ConfigWidgets.factoryForType(type), "Missing control factory for type " + type);
        }
        for (ConfigOption<?> option : ConfigOptions.all()) {
            assertNotNull(ConfigWidgets.factoryFor(option), "Missing factory for " + option.configKey());
            assertNotNull(ConfigWidgets.createControl(option, session, () -> {}),
                    "Failed to create control widget for " + option.configKey());
        }
    }

    @Test
    void canonicalizationHandlesEveryOptionType() {
        assertEquals(Boolean.TRUE, GeneralOptions.SPRINT_PREVENTION.canonical("true"));
        assertEquals(Boolean.FALSE, GeneralOptions.SPRINT_PREVENTION.canonical(false));

        assertEquals(20, GeneralOptions.SPRINT_THRESHOLD.canonical(99));
        assertEquals(0, GeneralOptions.SPRINT_THRESHOLD.canonical(-5));
        assertEquals(12, GeneralOptions.SPRINT_THRESHOLD.canonical("12"));

        assertEquals(1.25, DepletionOptions.THIRST_DEPLETION_MODIFIER.canonical("1.2500001"), 1e-6);
        assertEquals(10.0, DepletionOptions.THIRST_DEPLETION_MODIFIER.canonical(50.0), 1e-6);

        assertEquals("FORCE_YAT", CompatOptions.TOUGH_AS_NAILS_MODE.canonical("force_yat"));
        assertEquals("OFF", CompatOptions.TOUGH_AS_NAILS_MODE.canonical(null));

        assertEquals("°C", CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT.canonical("°C"));
        assertEquals("", CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT.canonical(null));

        assertEquals(0x38BDF8, CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR.canonical(0xFF38BDF8));

        List<ItemValue> drinks = ItemOptions.DRINKS.canonical(List.of(
                List.of("minecraft:potion", "6", 8L),
                List.of("malformed_row")
        ));
        assertEquals(List.of(new ItemValue("minecraft:potion", 6, 8)), drinks);

        List<TemperatureTier> tiers = CompatOptions.ENVIRONMENTZ_TEMPERATURE_TIERS.canonical(List.of(
                List.of("20", "1.5"),
                List.of("only_one_col")
        ));
        assertEquals(List.of(new TemperatureTier(20, 1.5)), tiers);
    }

    @Test
    void searchCachesNormalizedTextAndProducesNoDuplicates() {
        ConfigOption<Integer> sprintThreshold = GeneralOptions.SPRINT_THRESHOLD;
        TranslatableContents title = (TranslatableContents) sprintThreshold.title().getContents();
        TranslatableContents desc = (TranslatableContents) sprintThreshold.description().getContents();
        assertEquals("yat.config.option.sprintThreshold", title.getKey());
        assertEquals("yat.config.option.sprintThreshold.desc", desc.getKey());

        String firstIndex = sprintThreshold.searchIndex();
        String secondIndex = sprintThreshold.searchIndex();
        assertSame(firstIndex, secondIndex, "Normalized search index must be cached");

        assertTrue(sprintThreshold.matches("SPRINTTHRESHOLD"));
        assertTrue(sprintThreshold.matches("sprint"));
        assertFalse(sprintThreshold.matches("nonexistent_query_xyz"));

        ConfigSession session = new ConfigSession(new TestConfigBackend(), false, Runnable::run);
        List<ConfigOption<?>> results = OptionList.searchOptions(session, "water");
        assertFalse(results.isEmpty());
        assertEquals(new HashSet<>(results).size(), results.size(), "Search must not return duplicate options");
    }

    @Test
    void metadataMatchesForgeNeoForgeAndFabricSpecs() throws Exception {
        Path repoRoot = findRepoRoot();
        Map<String, ConfigOption<?>> optionsById = new LinkedHashMap<>();
        for (ConfigOption<?> option : ConfigOptions.all()) {
            optionsById.put(option.file + "/" + option.key, option);
        }

        Map<String, SpecEntry> forgeSpecs = parseForgeStyleSpecs(repoRoot, "forge", "Forge");
        Map<String, SpecEntry> neoForgeSpecs = parseForgeStyleSpecs(repoRoot, "neoforge", "NeoForge");
        assertEquals(forgeSpecs, neoForgeSpecs, "Forge and NeoForge config specs must stay identical");

        for (var entry : forgeSpecs.entrySet()) {
            String id = entry.getKey();
            SpecEntry spec = entry.getValue();
            ConfigOption<?> option = optionsById.get(id);
            assertNotNull(option, "Missing ConfigOption for Forge/NeoForge spec key: " + id);
            assertEquals(spec.type, option.type, "Type mismatch for " + id);
            if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                assertEquals(spec.min, option.min(), 1e-6, "Min mismatch for " + id);
                assertEquals(spec.max, option.max(), 1e-6, "Max mismatch for " + id);
            }
            if (option.type != ConfigOption.Type.LIST) {
                assertEquals(
                        option.canonical(thirstConfigDefault(option)),
                        option.canonical(spec.defaultValue),
                        "Default mismatch between ThirstConfig and Forge/NeoForge spec for " + id
                );
            }
        }

        for (ConfigOption<?> option : ConfigOptions.all()) {
            if (option.key.startsWith("environmentz")) continue;
            String id = option.file + "/" + option.key;
            assertTrue(forgeSpecs.containsKey(id), "ConfigOption missing from Forge/NeoForge spec: " + id);
        }

        Map<String, Object> fabricDefaults = parseFabricDefaults(
                repoRoot.resolve("fabric/src/main/java/dev/minhnh/yetanotherthirst/FabricConfig.java"));
        assertEquals(optionsById.keySet(), fabricDefaults.keySet(),
                "FabricConfig defaults and ConfigOptions keys must match 1-to-1");

        for (ConfigOption<?> option : ConfigOptions.all()) {
            String id = option.file + "/" + option.key;
            Object fabricDefault = fabricDefaults.get(id);
            assertNotNull(fabricDefault, "Missing Fabric default for " + id);
            if (option.type != ConfigOption.Type.LIST) {
                Object expectedDefault = option.canonical(thirstConfigDefault(option));
                Object actualDefault = option.canonical(fabricDefault);
                assertEquals(expectedDefault, actualDefault,
                        "Default mismatch between ThirstConfig and FabricConfig for " + id);
                if (option.type == ConfigOption.Type.INT || option.type == ConfigOption.Type.DECIMAL) {
                    double numeric = ((Number) actualDefault).doubleValue();
                    assertTrue(numeric >= option.min() && numeric <= option.max(),
                            "Default out of range for " + id);
                }
            }
        }
    }

    @Test
    void everyCategorySectionAndOptionHasEnglishAndVietnameseTranslations() throws Exception {
        for (String locale : List.of("en_us", "vi_vn")) {
            JsonObject language = language(locale);
            for (ConfigCategory category : ConfigOptions.CATEGORIES) {
                require(language, "yat.config.category." + category.id());
                require(language, "yat.config.category." + category.id() + ".desc");
                for (ConfigCategory.Section section : category.sections()) {
                    require(language, "yat.config.section." + section.id());
                    for (ConfigOption<?> option : section.options()) {
                        require(language, "yat.config.option." + option.key);
                        require(language, "yat.config.option." + option.key + ".desc");
                    }
                }
            }
        }
    }

    private static Path findRepoRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isDirectory(current.resolve("forge")) && Files.isDirectory(current.resolve("common"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate repository root");
    }

    private static Map<String, SpecEntry> parseForgeStyleSpecs(Path repoRoot, String module, String prefix) throws Exception {
        Map<ConfigFile, Path> files = new EnumMap<>(ConfigFile.class);
        Path base = repoRoot.resolve(module).resolve("src/main/java/dev/minhnh/yetanotherthirst");
        files.put(ConfigFile.COMMON, base.resolve(prefix + "ConfigCommon.java"));
        files.put(ConfigFile.ITEMS, base.resolve(prefix + "ConfigItems.java"));
        files.put(ConfigFile.COMPAT, base.resolve(prefix + "ConfigCompat.java"));
        files.put(ConfigFile.CLIENT, base.resolve(prefix + "ClientConfig.java"));

        Map<String, SpecEntry> result = new LinkedHashMap<>();
        for (var entry : files.entrySet()) {
            ConfigFile file = entry.getKey();
            String source = Files.readString(entry.getValue(), StandardCharsets.UTF_8);

            Matcher rangeMatcher = DEFINE_IN_RANGE.matcher(source);
            while (rangeMatcher.find()) {
                String key = rangeMatcher.group(1);
                if ("configVersion".equals(key)) continue;
                String defRaw = rangeMatcher.group(2).trim();
                String minRaw = rangeMatcher.group(3).trim();
                String maxRaw = rangeMatcher.group(4).trim();
                boolean decimal = defRaw.contains(".") || minRaw.contains(".") || maxRaw.contains(".");
                ConfigOption.Type type = decimal ? ConfigOption.Type.DECIMAL : ConfigOption.Type.INT;
                double defVal = Double.parseDouble(defRaw);
                double minVal = Double.parseDouble(minRaw);
                double maxVal = Double.parseDouble(maxRaw);
                Object defObj = decimal ? defVal : (int) defVal;
                assertTrue(result.putIfAbsent(file + "/" + key, new SpecEntry(file, key, type, defObj, minVal, maxVal)) == null,
                        "Duplicate key in " + prefix + " " + file + ": " + key);
            }

            Matcher boolMatcher = DEFINE_BOOL.matcher(source);
            while (boolMatcher.find()) {
                String key = boolMatcher.group(1);
                boolean defVal = Boolean.parseBoolean(boolMatcher.group(2));
                assertTrue(result.putIfAbsent(file + "/" + key, new SpecEntry(file, key, ConfigOption.Type.BOOL, defVal, 0, 0)) == null,
                        "Duplicate key in " + prefix + " " + file + ": " + key);
            }

            Matcher enumMatcher = DEFINE_ENUM.matcher(source);
            while (enumMatcher.find()) {
                String key = enumMatcher.group(1);
                String rawEnum = enumMatcher.group(2);
                String enumName = rawEnum.substring(rawEnum.lastIndexOf('.') + 1);
                assertTrue(result.putIfAbsent(file + "/" + key, new SpecEntry(file, key, ConfigOption.Type.ENUM, enumName, 0, 0)) == null,
                        "Duplicate key in " + prefix + " " + file + ": " + key);
            }

            Matcher listMatcher = DEFINE_LIST.matcher(source);
            while (listMatcher.find()) {
                String key = listMatcher.group(1);
                assertTrue(result.putIfAbsent(file + "/" + key, new SpecEntry(file, key, ConfigOption.Type.LIST, List.of(), 0, 0)) == null,
                        "Duplicate key in " + prefix + " " + file + ": " + key);
            }
        }
        return result;
    }

    private static Map<String, Object> parseFabricDefaults(Path fabricConfigPath) throws Exception {
        String source = Files.readString(fabricConfigPath, StandardCharsets.UTF_8);
        Map<ConfigFile, String> methodNames = Map.of(
                ConfigFile.COMMON, "buildDefaultCommon",
                ConfigFile.ITEMS, "buildDefaultItems",
                ConfigFile.COMPAT, "buildDefaultCompat",
                ConfigFile.CLIENT, "buildDefaultClient"
        );

        Map<String, Object> result = new LinkedHashMap<>();
        for (ConfigFile file : ConfigFile.values()) {
            String methodName = methodNames.get(file);
            int start = source.indexOf("private static JsonObject " + methodName + "()");
            assertTrue(start >= 0, "Missing " + methodName + " in FabricConfig.java");
            int end = source.indexOf("return obj;", start);
            assertTrue(end > start, "Missing return in " + methodName);
            String body = source.substring(start, end);

            Matcher propMatcher = JSON_ADD_PROP.matcher(body);
            while (propMatcher.find()) {
                String key = propMatcher.group(1);
                if ("configVersion".equals(key)) continue;
                String rawVal = propMatcher.group(2).trim();
                result.put(file + "/" + key, parseLiteralExpression(rawVal));
            }

            Matcher listMatcher = JSON_ADD_LIST.matcher(body);
            while (listMatcher.find()) {
                String key = listMatcher.group(1);
                result.put(file + "/" + key, List.of());
            }
        }
        return result;
    }

    private static Object parseLiteralExpression(String expr) {
        if ("true".equals(expr) || "false".equals(expr)) {
            return Boolean.parseBoolean(expr);
        }
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length() - 1);
        }
        if (expr.endsWith(".name()")) {
            String enumRef = expr.substring(0, expr.length() - ".name()".length());
            return enumRef.substring(enumRef.lastIndexOf('.') + 1);
        }
        if (expr.startsWith("0x") || expr.startsWith("0X")) {
            return Integer.decode(expr);
        }
        if (expr.contains(".")) {
            return Double.parseDouble(expr);
        }
        return Integer.parseInt(expr);
    }

    private static Object thirstConfigDefault(ConfigOption<?> option) throws Exception {
        String fieldName = toConstantName(option.key);
        Object value = ThirstConfig.class.getField(fieldName).get(null);
        return value instanceof Enum<?> entry ? entry.name() : value;
    }

    private static String toConstantName(String key) {
        if ("waterBottleStackSize".equals(key)) return "WATER_BOTTLE_STACKSIZE";
        if (key.startsWith("appleSkin")) key = "appleskin" + key.substring("appleSkin".length());
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (Character.isUpperCase(c) && i > 0) result.append('_');
            result.append(Character.toUpperCase(c));
        }
        return result.toString();
    }

    private static JsonObject language(String locale) throws Exception {
        try (var stream = ConfigOptionsTest.class.getResourceAsStream(
                "/assets/yet_another_thirst/lang/" + locale + ".json")) {
            assertNotNull(stream, "Missing language resource " + locale);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static void require(JsonObject language, String key) {
        assertTrue(language.has(key), "Missing translation " + key);
        assertFalse(language.get(key).getAsString().isBlank(), "Blank translation " + key);
    }
}
