package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThirstConfigTest {

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
}

