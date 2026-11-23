package dev.minhnh.yetanotherthirst;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Enforces the translation key convention and keeps lang files in sync with code.
 * Mod-owned keys use the short {@code yat.} namespace; keys whose format is fixed by
 * Minecraft (registry ids, advancements, death messages) or Create Ponder keep the full mod id.
 */
class LangKeysTest {

    private static final String LANG_DIR = "/assets/yet_another_thirst/lang/";
    private static final List<String> LOCALES = List.of(
            "en_us", "vi_vn", "fr_fr", "ja_jp", "ko_kr", "pl_pl", "ru_ru", "zh_cn", "zh_tw");
    /** Locales that must translate every key; others may be partial and fall back to en_us. */
    private static final List<String> COMPLETE_LOCALES = List.of(
            "vi_vn", "fr_fr", "ja_jp", "ko_kr", "pl_pl", "ru_ru", "zh_cn", "zh_tw");

    private static final List<Pattern> KEY_PATTERNS = List.of(
            // segment after yat. is a snake_case group; deeper segments may mirror camelCase config ids
            Pattern.compile("yat\\.[a-z_]+(\\.[A-Za-z0-9_]+)+"),
            Pattern.compile("(block|item|effect|container)\\.yet_another_thirst\\.[a-z0-9_]+"),
            Pattern.compile("advancements\\.yet_another_thirst\\.[a-z0-9_]+\\.(title|description)"),
            Pattern.compile("itemGroup\\.yet_another_thirst"),
            Pattern.compile("death\\.attack\\.yet_another_thirst\\.[a-z0-9_]+(\\.player|\\.item)?"),
            Pattern.compile("yet_another_thirst\\.ponder\\.[a-z0-9_]+\\.[a-z0-9_]+"));

    /** Complete key literals in source (prefix literals ending with '.' are excluded). */
    private static final Pattern KEY_LITERAL = Pattern.compile(
            "\"((?:yat|container\\.yet_another_thirst)(?:\\.[A-Za-z0-9_]+)+|itemGroup\\.yet_another_thirst)\"");
    /** Keys passed to Lang.t(...), which prepends Lang.PREFIX. */
    private static final Pattern LANG_T_CALL = Pattern.compile("\\bt\\(\"([A-Za-z0-9_.]*[A-Za-z0-9_])\"");

    private static final List<String> SOURCE_ROOTS = List.of(
            "src/main/java", "src/client/java",
            "../forge/src/main/java", "../neoforge/src/main/java", "../fabric/src/main/java");

    @Test
    @DisplayName("Every key in every locale follows the naming convention")
    void keysFollowConvention() throws Exception {
        List<String> bad = new ArrayList<>();
        for (String locale : LOCALES) {
            for (String key : load(locale).keySet()) {
                if (KEY_PATTERNS.stream().noneMatch(p -> p.matcher(key).matches())) bad.add(locale + ": " + key);
            }
        }
        assertTrue(bad.isEmpty(), "Keys violating convention:\n" + String.join("\n", bad));
    }

    @Test
    @DisplayName("Complete locales match en_us exactly; partial locales contain no unknown keys")
    void localesInSync() throws Exception {
        Set<String> en = load("en_us").keySet();
        for (String locale : LOCALES) {
            if (locale.equals("en_us")) continue;
            Set<String> keys = load(locale).keySet();
            Set<String> unknown = new TreeSet<>(keys);
            unknown.removeAll(en);
            assertTrue(unknown.isEmpty(), locale + " has keys missing from en_us: " + unknown);
            if (COMPLETE_LOCALES.contains(locale)) {
                Set<String> missing = new TreeSet<>(en);
                missing.removeAll(keys);
                assertTrue(missing.isEmpty(), locale + " is missing keys: " + missing);
            }
        }
    }

    @Test
    @DisplayName("Every translation key referenced in source exists in en_us")
    void sourceKeysExist() throws Exception {
        JsonObject en = load("en_us");
        Set<String> missing = new TreeSet<>();
        for (String root : SOURCE_ROOTS) {
            Path dir = Path.of(root);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> files = Files.walk(dir)) {
                for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                    String src = Files.readString(file);
                    collect(KEY_LITERAL.matcher(src), "", en, missing, file);
                    if (file.getFileName().toString().equals("Lang.java")) {
                        collect(LANG_T_CALL.matcher(src), Lang.PREFIX, en, missing, file);
                    }
                }
            }
        }
        for (int purity = 0; purity <= 3; purity++) {
            String key = Lang.purityKey(purity);
            if (!en.has(key)) missing.add(key + " (Lang.purityKey)");
        }
        List<MutableComponent> dynamic = new ArrayList<>();
        for (Direction direction : Direction.values()) dynamic.add(Lang.direction(direction));
        dynamic.add(Lang.half(true));
        dynamic.add(Lang.half(false));
        for (MutableComponent component : dynamic) {
            String key = ((TranslatableContents) component.getContents()).getKey();
            if (!en.has(key)) missing.add(key + " (Lang dynamic)");
        }
        assertTrue(missing.isEmpty(), "Keys referenced in source but missing from en_us:\n" + String.join("\n", missing));
    }

    @Test
    @DisplayName("Lang prefixes use the short namespace")
    void prefixes() {
        assertEquals("yat.", Lang.PREFIX);
        assertEquals("yat.config.", Lang.CONFIG_PREFIX);
    }

    private static void collect(Matcher m, String prefix, JsonObject en, Set<String> missing, Path file) {
        while (m.find()) {
            String key = prefix + m.group(1);
            if (!en.has(key)) missing.add(key + " (" + file.getFileName() + ")");
        }
    }

    private static JsonObject load(String locale) throws IOException {
        try (InputStream in = LangKeysTest.class.getResourceAsStream(LANG_DIR + locale + ".json")) {
            assertNotNull(in, "Missing language resource " + locale);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}

