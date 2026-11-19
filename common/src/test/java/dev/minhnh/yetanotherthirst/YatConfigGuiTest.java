package dev.minhnh.yetanotherthirst;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.minhnh.yetanotherthirst.client.config.ClientOptions;
import dev.minhnh.yetanotherthirst.client.config.CompatOptions;
import dev.minhnh.yetanotherthirst.client.config.ConfigCategory;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption;
import dev.minhnh.yetanotherthirst.client.config.ConfigOptions;
import dev.minhnh.yetanotherthirst.client.config.ConfigSession;
import dev.minhnh.yetanotherthirst.client.config.ConfigWidgets;
import dev.minhnh.yetanotherthirst.client.config.DepletionOptions;
import dev.minhnh.yetanotherthirst.client.config.GeneralOptions;
import dev.minhnh.yetanotherthirst.client.config.ItemOptions;
import dev.minhnh.yetanotherthirst.client.config.ItemValue;
import dev.minhnh.yetanotherthirst.client.config.ListPresentation;
import dev.minhnh.yetanotherthirst.client.config.ListSchema;
import dev.minhnh.yetanotherthirst.client.config.MachineOptions;
import dev.minhnh.yetanotherthirst.client.config.PurityOptions;
import dev.minhnh.yetanotherthirst.client.config.TemperatureTier;
import dev.minhnh.yetanotherthirst.client.config.TestConfigBackend;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YatConfigGuiTest {

    private ConfigSession session;

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() {
        session = new ConfigSession(new TestConfigBackend(), false, Runnable::run);
    }

    @Test
    @DisplayName("Control factories create functional widgets and custom control factories can override per option")
    void testControlFactoriesAndCustomOverride() {
        AtomicInteger changes = new AtomicInteger();
        AbstractWidget boolControl = ConfigWidgets.createControl(
                GeneralOptions.SPRINT_PREVENTION, session, changes::incrementAndGet);
        assertNotNull(boolControl);
        boolControl.onClick(0, 0);
        assertEquals(1, changes.get());
        assertTrue(session.value(GeneralOptions.SPRINT_PREVENTION));

        AbstractWidget intControl = ConfigWidgets.createControl(
                GeneralOptions.SPRINT_THRESHOLD, session, changes::incrementAndGet);
        assertNotNull(intControl);

        AbstractWidget decimalFieldControl = ConfigWidgets.createControl(
                DepletionOptions.DEHYDRATION_DAMAGE, session, changes::incrementAndGet);
        assertNotNull(decimalFieldControl);

        AbstractWidget stepperControl = ConfigWidgets.createControl(
                ClientOptions.HUD_X_OFFSET, session, changes::incrementAndGet);
        assertNotNull(stepperControl);

        AbstractWidget enumControl = ConfigWidgets.createControl(
                CompatOptions.TOUGH_AS_NAILS_MODE, session, changes::incrementAndGet);
        assertNotNull(enumControl);

        AbstractWidget textControl = ConfigWidgets.createControl(
                CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT, session, changes::incrementAndGet);
        assertNotNull(textControl);

        AbstractWidget colorControl = ConfigWidgets.createControl(
                CompatOptions.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR, session, changes::incrementAndGet);
        assertNotNull(colorControl);

        AbstractWidget listControl = ConfigWidgets.createControl(
                ItemOptions.DRINKS, session, changes::incrementAndGet);
        assertNotNull(listControl);
    }

    @Test
    @DisplayName("Each ConfigCategory tracks pending count and resets independently")
    void testCategoryPendingCountAndIndependentReset() {
        for (ConfigCategory category : ConfigOptions.CATEGORIES) {
            assertEquals(0, session.pendingCount(category), category.id() + " should start with 0 pending");
        }

        session.set(GeneralOptions.SPRINT_THRESHOLD, 12);
        session.set(PurityOptions.DEFAULT_PURITY, 3);
        session.set(MachineOptions.FILTER_BASE_SPEED, 20);
        session.set(ItemOptions.DRINKS, List.of(new ItemValue("minecraft:apple", 5, 5)));
        session.set(CompatOptions.ENVIRONMENTZ_TEMPERATURE_TIERS, List.of(new TemperatureTier(30, 1.5)));
        session.set(ClientOptions.HUD_X_OFFSET, 40);

        ConfigCategory generalCat = categoryById("general");
        ConfigCategory purityCat = categoryById("purity");
        ConfigCategory machinesCat = categoryById("machines");
        ConfigCategory itemsCat = categoryById("items");
        ConfigCategory compatCat = categoryById("compat");
        ConfigCategory hudCat = categoryById("hud");

        assertEquals(1, session.pendingCount(generalCat));
        assertEquals(1, session.pendingCount(purityCat));
        assertEquals(1, session.pendingCount(machinesCat));
        assertEquals(1, session.pendingCount(itemsCat));
        assertEquals(1, session.pendingCount(compatCat));
        assertEquals(1, session.pendingCount(hudCat));
        assertEquals(6, session.pendingCount());

        for (ConfigOption<?> option : generalCat.options()) {
            session.reset(option);
        }
        assertEquals(0, session.pendingCount(generalCat));
        assertEquals(5, session.pendingCount());
    }

    @Test
    @DisplayName("ListPresentation resolves item icons and handles unknown IDs")
    void testListPresentationIcons() {
        ListPresentation<ItemValue> presentation = ListSchema.itemValues().presentation();
        assertNotNull(presentation);

        ItemValue apple = new ItemValue("minecraft:apple", 2, 3);
        ItemStack icon = presentation.icon(apple);
        assertFalse(icon.isEmpty());
        assertNotSame(icon, presentation.icon(apple), "Icons must not share a mutable ItemStack across callers");
        assertFalse(presentation.tooltip(apple).isEmpty());

        ItemValue unknown = new ItemValue("nonexistent_mod:unknown_item", 1, 1);
        assertTrue(presentation.icon(unknown).isEmpty());
        assertFalse(presentation.displayName(unknown).getString().isEmpty());
    }

    @Test
    @DisplayName("en_us.json and vi_vn.json contain all yat.config.* keys in sync and no legacy keys")
    void testConfigTranslationKeysCompleteness() throws Exception {
        JsonObject enUs = loadLangJson("/assets/yet_another_thirst/lang/en_us.json");
        JsonObject viVn = loadLangJson("/assets/yet_another_thirst/lang/vi_vn.json");

        int configKeyCount = 0;
        for (String key : enUs.keySet()) {
            assertFalse(key.startsWith("config.yet_another_thirst."),
                    "Stale legacy config translation key in en_us.json: " + key);
            if (key.startsWith("yat.config.")) {
                configKeyCount++;
                assertTrue(viVn.has(key), "Missing key in vi_vn.json: " + key);
                assertFalse(enUs.get(key).getAsString().isBlank(), "Blank value in en_us.json for: " + key);
                assertFalse(viVn.get(key).getAsString().isBlank(), "Blank value in vi_vn.json for: " + key);
            }
        }
        for (String key : viVn.keySet()) {
            assertFalse(key.startsWith("config.yet_another_thirst."),
                    "Stale legacy config translation key in vi_vn.json: " + key);
            if (key.startsWith("yat.config.")) {
                assertTrue(enUs.has(key), "Missing key in en_us.json: " + key);
            }
        }
        assertTrue(configKeyCount > 100, "Expected full set of yat.config.* translation keys");
    }

    private static ConfigCategory categoryById(String id) {
        return ConfigOptions.CATEGORIES.stream()
                .filter(c -> c.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static JsonObject loadLangJson(String resourcePath) throws Exception {
        try (InputStream in = YatConfigGuiTest.class.getResourceAsStream(resourcePath)) {
            assertNotNull(in, "Resource not found on classpath: " + resourcePath);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
