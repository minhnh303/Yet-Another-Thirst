package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.compat.gui.AppleSkinCompat;
import dev.minhnh.yetanotherthirst.compat.status.SupernaturalCompat;
import dev.minhnh.yetanotherthirst.compat.status.VampirismCompat;
import dev.minhnh.yetanotherthirst.compat.temperature.ColdSweatCompat;
import dev.minhnh.yetanotherthirst.compat.temperature.EnvironmentzCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThirstCompatTest {

    @Test
    @DisplayName("AppleSkin compat delegates cleanly to provider")
    void testAppleSkinCompat() {
        ThirstConfig.COMPAT_APPLESKIN = true;
        ThirstConfig.APPLESKIN_THIRST_TOOLTIP = true;
        ThirstConfig.APPLESKIN_THIRST_HUD_PREVIEW = false;

        assertTrue(AppleSkinCompat.showsThirstTooltip());
        assertTrue(ThirstCompat.showsAppleSkinThirstTooltip());
        assertFalse(AppleSkinCompat.showsHudPreview());
        assertFalse(ThirstCompat.showsAppleSkinHudPreview());

        ThirstConfig.COMPAT_APPLESKIN = false;
        assertFalse(ThirstCompat.showsAppleSkinThirstTooltip());
        assertFalse(ThirstCompat.showsAppleSkinHudPreview());
    }

    @Test
    @DisplayName("Temperature modifier falls back gracefully when mods are absent")
    void testTemperatureFallbacks() {
        assertFalse(ColdSweatCompat.replacesEnvironmentModifiers());
        assertFalse(EnvironmentzCompat.replacesEnvironmentModifiers());
        assertFalse(ThirstCompat.replacesEnvironmentModifiers());

        assertEquals(140, EnvironmentzCompat.getThermometerIconX());
        assertEquals(32, EnvironmentzCompat.getThermometerIconY());
        assertEquals(140, ThirstCompat.getEnvironmentzThermometerIconX());
        assertEquals(32, ThirstCompat.getEnvironmentzThermometerIconY());
        assertTrue(ThirstCompat.isEnvironmentzThermometerVisible());
    }

    @Test
    @DisplayName("Vampire checks return false when optional mods are absent")
    void testVampireFallbacks() {
        ThirstConfig.COMPAT_VAMPIRISM = false;
        assertFalse(VampirismCompat.isVampire(null));
        assertFalse(ThirstCompat.isVampire(null));

        ThirstConfig.COMPAT_SUPERNATURAL = false;
        assertFalse(SupernaturalCompat.isVampire(null));
        assertFalse(ThirstCompat.isSupernaturalVampire(null));
    }
}

