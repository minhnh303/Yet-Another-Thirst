package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ThirstConfigTest {

    private ThirstConfig.Snapshot baseline;

    @BeforeEach
    void captureBaseline() {
        baseline = ThirstConfig.takeSnapshot();
    }

    @AfterEach
    void restoreBaseline() {
        if (baseline != null) {
            ThirstConfig.restoreSnapshot(baseline);
        }
    }

    @Test
    @DisplayName("Default thirst boundaries and constants are valid")
    void testConfigDefaults() {
        assertEquals(20, ThirstConfig.MAX_THIRST);
        assertEquals(20, ThirstConfig.DEFAULT_THIRST);
        assertEquals(5, ThirstConfig.DEFAULT_QUENCHED);
        assertEquals(4.0F, ThirstConfig.EXHAUSTION_LIMIT);
        assertEquals(6, ThirstConfig.SPRINT_THRESHOLD);

        assertTrue(ThirstConfig.DEFAULT_THIRST <= ThirstConfig.MAX_THIRST);
        assertTrue(ThirstConfig.DEFAULT_QUENCHED <= ThirstConfig.MAX_THIRST);
        assertTrue(ThirstConfig.SPRINT_THRESHOLD < ThirstConfig.MAX_THIRST);
    }

    @Test
    @DisplayName("Filter durability configs are positive numbers")
    void testFilterDurabilities() {
        assertTrue(ThirstConfig.FABRIC_FILTER_DURABILITY > 0);
        assertTrue(ThirstConfig.SAND_FILTER_DURABILITY > 0);
        assertTrue(ThirstConfig.CARBON_FILTER_DURABILITY > 0);

        assertTrue(ThirstConfig.FABRIC_FILTER_MAX_PURITY >= 0 && ThirstConfig.FABRIC_FILTER_MAX_PURITY <= 3);
        assertTrue(ThirstConfig.SAND_FILTER_MAX_PURITY >= 0 && ThirstConfig.SAND_FILTER_MAX_PURITY <= 3);
        assertTrue(ThirstConfig.CARBON_FILTER_MAX_PURITY >= 0 && ThirstConfig.CARBON_FILTER_MAX_PURITY <= 3);
    }

    @Test
    @DisplayName("Snapshot captures and restores scalars, effect lists, tiers, and item maps with deep-copy isolation")
    void testSnapshotCaptureAndRestore() {
        ThirstConfig.Snapshot initial = ThirstConfig.takeSnapshot();

        ThirstConfig.THIRST_DEPLETION_MODIFIER = 4.5F;
        ThirstConfig.DAMAGE_INTERVAL_TICKS = 100;
        ThirstConfig.DEHYDRATION_DAMAGE = 3.5F;
        ThirstConfig.DEFAULT_PURITY = 0;
        ThirstConfig.CAN_DRINK_BY_HAND = true;
        ThirstConfig.HUD_X_OFFSET = -24;
        ThirstConfig.HUD_Y_OFFSET = 16;
        ThirstConfig.TOUGH_AS_NAILS_MODE = ThirstConfig.ToughAsNailsMode.FORCE_YAT;
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR = 0x123456;
        ThirstConfig.WATER_BOILER_CAPACITY = 8000;

        ThirstConfig.setSuspendThirstEffects(List.of("minecraft:water_breathing >= 1"));
        ThirstConfig.setPauseDepletionEffects(List.of("minecraft:regeneration == 0"));
        ThirstConfig.setRegenThirstEffects(List.of("minecraft:saturation >= 0"));
        ThirstConfig.setEnvironmentzTemperatureTiers(List.of(new ThirstConfig.TemperatureTier(42, 3.0F)));

        ThirstConfig.CONFIGURED_DRINKS.put("minecraft:golden_apple", new ThirstValue(8, 10));
        ThirstConfig.CONFIGURED_FOODS.put("minecraft:cooked_beef", new ThirstValue(2, 1));
        ThirstConfig.CONFIGURED_BLACKLIST.add("minecraft:rotten_flesh");

        ThirstConfig.Snapshot modifiedSnapshot = ThirstConfig.takeSnapshot();

        // Mutate collections after taking modifiedSnapshot to verify deep-copy isolation
        ThirstConfig.CONFIGURED_DRINKS.clear();
        ThirstConfig.CONFIGURED_BLACKLIST.clear();
        ThirstConfig.setSuspendThirstEffects(List.of());

        ThirstConfig.restoreSnapshot(modifiedSnapshot);
        assertEquals(new ThirstValue(8, 10), ThirstConfig.CONFIGURED_DRINKS.get("minecraft:golden_apple"));
        assertTrue(ThirstConfig.CONFIGURED_BLACKLIST.contains("minecraft:rotten_flesh"));
        assertEquals(List.of("minecraft:water_breathing >= 1"), ThirstConfig.SUSPENDED_THIRST_EFFECTS_RAW);
        assertEquals(1, ThirstConfig.SUSPENDED_THIRST_EFFECTS.size());

        // Restore initial snapshot and verify all fields return to baseline
        ThirstConfig.restoreSnapshot(initial);
        assertEquals(1.2F, ThirstConfig.THIRST_DEPLETION_MODIFIER, 1e-4F);
        assertEquals(40, ThirstConfig.DAMAGE_INTERVAL_TICKS);
        assertEquals(1.0F, ThirstConfig.DEHYDRATION_DAMAGE, 1e-4F);
        assertEquals(2, ThirstConfig.DEFAULT_PURITY);
        assertFalse(ThirstConfig.CAN_DRINK_BY_HAND);
        assertEquals(0, ThirstConfig.HUD_X_OFFSET);
        assertEquals(0, ThirstConfig.HUD_Y_OFFSET);
        assertEquals(ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT, ThirstConfig.TOUGH_AS_NAILS_MODE);
        assertEquals(0xFFFFFF, ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR);
        assertEquals(2000, ThirstConfig.WATER_BOILER_CAPACITY);
        assertEquals(ThirstConfig.defaultSuspendThirstEffects(), ThirstConfig.SUSPENDED_THIRST_EFFECTS_RAW);
        assertEquals(ThirstConfig.defaultPauseDepletionEffects(), ThirstConfig.PAUSE_DEPLETION_EFFECTS_RAW);
        assertEquals(ThirstConfig.defaultRegenThirstEffects(), ThirstConfig.REGEN_THIRST_EFFECTS_RAW);
        assertEquals(ThirstConfig.defaultEnvironmentzTemperatureTiers(), ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS);
        assertFalse(ThirstConfig.CONFIGURED_DRINKS.containsKey("minecraft:golden_apple"));
        assertFalse(ThirstConfig.CONFIGURED_FOODS.containsKey("minecraft:cooked_beef"));
        assertFalse(ThirstConfig.CONFIGURED_BLACKLIST.contains("minecraft:rotten_flesh"));
    }

    @Test
    @DisplayName("setEnvironmentzTemperatureTiers sorts tiers ascending by threshold")
    void testEnvironmentzTemperatureTierSorting() {
        ThirstConfig.setEnvironmentzTemperatureTiers(List.of(
                new ThirstConfig.TemperatureTier(100, 2.5F),
                new ThirstConfig.TemperatureTier(-50, 0.8F),
                new ThirstConfig.TemperatureTier(20, 1.0F)
        ));

        assertEquals(List.of(
                new ThirstConfig.TemperatureTier(-50, 0.8F),
                new ThirstConfig.TemperatureTier(20, 1.0F),
                new ThirstConfig.TemperatureTier(100, 2.5F)
        ), ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS);
    }

    @Test
    @DisplayName("findActiveTemperatureTier resolves correctly on an unsorted tier list")
    void testFindActiveTemperatureTierUnsorted() {
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS = new java.util.ArrayList<>(List.of(
                new ThirstConfig.TemperatureTier(-1000, 0.5F),
                new ThirstConfig.TemperatureTier(50, 1.5F),
                new ThirstConfig.TemperatureTier(20, 1.0F),
                new ThirstConfig.TemperatureTier(-60, 0.85F)
        ));

        assertNull(ThirstConfig.findActiveTemperatureTier(-2000));
        assertEquals(0.5F, ThirstConfig.findActiveTemperatureTier(-100).modifier);
        assertEquals(0.85F, ThirstConfig.findActiveTemperatureTier(-60).modifier);
        assertEquals(1.0F, ThirstConfig.findActiveTemperatureTier(30).modifier);
        assertEquals(1.5F, ThirstConfig.findActiveTemperatureTier(60).modifier);
    }

    @Test
    @DisplayName("parseRawItemMap and toRawItemList round-trip cleanly and ignore malformed entries")
    void testRawItemMapRoundTrip() {
        List<List<?>> rawInput = List.of(
                List.of("minecraft:apple", 2, 3),
                List.of("#forge:milk", 4, 6),
                List.of("incomplete_row", 1)
        );

        Map<String, ThirstValue> parsed = ThirstConfig.parseRawItemMap(rawInput);
        assertEquals(2, parsed.size());
        assertEquals(new ThirstValue(2, 3), parsed.get("minecraft:apple"));
        assertEquals(new ThirstValue(4, 6), parsed.get("#forge:milk"));

        List<List<Object>> serialized = ThirstConfig.toRawItemList(parsed);
        assertEquals(parsed, ThirstConfig.parseRawItemMap(serialized));
    }
}
