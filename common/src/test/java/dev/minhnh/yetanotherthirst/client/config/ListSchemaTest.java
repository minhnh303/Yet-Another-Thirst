package dev.minhnh.yetanotherthirst.client.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListSchemaTest {

    @Test
    void itemAndTagIdsAreValidated() {
        ListSchema.Column item = ListSchema.items().columns().get(0);

        assertTrue(ListSchema.valid(item, "minecraft:apple"));
        assertTrue(ListSchema.valid(item, "#minecraft:planks"));
        assertTrue(ListSchema.valid(item, "#forge:milk"));
        assertFalse(ListSchema.valid(item, ""));
        assertFalse(ListSchema.valid(item, "#"));
        assertFalse(ListSchema.valid(item, "not an id"));
        assertFalse(ListSchema.valid(item, "INVALID_UPPERCASE:item"));
    }

    @Test
    void effectRulesRequireAValidIdAndAmplifier() {
        ListSchema.Column effect = ListSchema.effects().columns().get(0);

        assertTrue(ListSchema.valid(effect, "minecraft:speed"));
        assertTrue(ListSchema.valid(effect, "minecraft:speed >= 2"));
        assertTrue(ListSchema.valid(effect, "farmersdelight:nourishment == 2"));
        assertTrue(ListSchema.valid(effect, "tombstone:ghostly_shape <= 255"));
        assertTrue(ListSchema.valid(effect, "yet_another_thirst:hydration != 1"));
        assertFalse(ListSchema.valid(effect, ""));
        assertFalse(ListSchema.valid(effect, "not an id"));
        assertFalse(ListSchema.valid(effect, "minecraft:speed >= nope"));
        assertFalse(ListSchema.valid(effect, "minecraft:speed >="));
        assertFalse(ListSchema.valid(effect, "minecraft:speed >= -1"));
        assertFalse(ListSchema.valid(effect, "minecraft:speed >= 256"));
    }

    @Test
    void temperatureTierColumnsEnforceRangesAndNumericTypes() {
        List<ListSchema.Column> columns = ListSchema.temperatureTiers().columns();

        assertTrue(ListSchema.valid(columns.get(0), "-10000"));
        assertTrue(ListSchema.valid(columns.get(0), "10000"));
        assertFalse(ListSchema.valid(columns.get(0), "1.5"));
        assertFalse(ListSchema.valid(columns.get(0), "10001"));
        assertTrue(ListSchema.valid(columns.get(1), "0.25"));
        assertTrue(ListSchema.valid(columns.get(1), "10"));
        assertFalse(ListSchema.valid(columns.get(1), "-0.1"));
        assertFalse(ListSchema.valid(columns.get(1), "NaN"));
        assertFalse(ListSchema.valid(columns.get(1), "10.01"));
    }

    @Test
    void itemValueRowsAreCanonicalizedAndMalformedRowsAreDropped() {
        ListSchema<ItemValue> schema = ListSchema.itemValues();
        List<ItemValue> result = schema.canonical(List.of(
                List.of("minecraft:apple", "3", 4L),
                List.of("incomplete")
        ));

        assertEquals(List.of(new ItemValue("minecraft:apple", 3, 4)), result);
        assertEquals(List.of(List.of("minecraft:apple", 3, 4)), schema.toBackend(result));
    }
}

