package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThirstDataTest {

    @Test
    @DisplayName("Default state has standard values")
    void testDefaultValues() {
        ThirstData data = ThirstData.DEFAULT;
        assertEquals(20, data.thirst());
        assertEquals(5, data.quenched());
        assertEquals(0.0F, data.exhaustion());
        assertTrue(data.enabled());
    }

    @Test
    @DisplayName("Constructor clamps inputs to valid ranges")
    void testClamping() {
        ThirstData negative = new ThirstData(-5, -10, -1.0F, true);
        assertEquals(0, negative.thirst());
        assertEquals(0, negative.quenched());
        assertEquals(0.0F, negative.exhaustion());

        ThirstData overflow = new ThirstData(100, 100, 10.0F, false);
        assertEquals(ThirstConfig.MAX_THIRST, overflow.thirst());
        assertEquals(ThirstConfig.MAX_THIRST, overflow.quenched());
        assertEquals(10.0F, overflow.exhaustion());
        assertFalse(overflow.enabled());
    }

    @Test
    @DisplayName("Drink increases thirst and quenched levels")
    void testDrink() {
        ThirstData data = new ThirstData(10, 2, 0.0F, true);
        ThirstData after = data.drink(4, 3);
        assertEquals(14, after.thirst());
        assertEquals(5, after.quenched());
    }

    @Test
    @DisplayName("Drink converts overflow to quenched when enabled")
    void testDrinkOverflowConversion() {
        ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = true;
        ThirstData data = new ThirstData(18, 2, 0.0F, true);
        // +5 thirst (max 20, 3 overflow), +2 quenched -> quenched = min(2 + 2 + 3, 20) = 7
        ThirstData after = data.drink(5, 2);
        assertEquals(20, after.thirst());
        assertEquals(7, after.quenched());
    }

    @Test
    @DisplayName("Depleting one level consumes quenched first")
    void testDepleteConsumesQuenchedFirst() {
        ThirstData data = new ThirstData(20, 5, 4.5F, true);
        ThirstData depleted = data.depleteOneLevel();
        assertEquals(20, depleted.thirst(), "Thirst should remain untouched while quenched > 0");
        assertEquals(4, depleted.quenched(), "Quenched should decrease by 1");
        assertEquals(0.5F, depleted.exhaustion(), 0.001F);
    }

    @Test
    @DisplayName("Depleting one level consumes thirst when quenched is zero")
    void testDepleteConsumesThirstWhenQuenchedZero() {
        ThirstData data = new ThirstData(20, 0, 4.5F, true);
        ThirstData depleted = data.depleteOneLevel();
        assertEquals(19, depleted.thirst(), "Thirst should decrease by 1 when quenched is 0");
        assertEquals(0, depleted.quenched());
        assertEquals(0.5F, depleted.exhaustion(), 0.001F);
    }

    @Test
    @DisplayName("NBT serialization and deserialization roundtrip")
    void testNbtRoundtrip() {
        ThirstData original = new ThirstData(15, 8, 2.75F, true);
        CompoundTag tag = original.save();
        ThirstData loaded = ThirstData.load(tag);
        assertEquals(original, loaded);
    }

    @Test
    @DisplayName("Codec serialization and deserialization roundtrip")
    void testCodecRoundtrip() {
        ThirstData original = new ThirstData(16, 9, 3.25F, true);
        Tag tag = ThirstData.CODEC.encodeStart(NbtOps.INSTANCE, original)
                .getOrThrow(false, msg -> fail("Failed to encode ThirstData: " + msg));
        ThirstData decoded = ThirstData.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, msg -> fail("Failed to decode ThirstData: " + msg));
        assertEquals(original, decoded);
    }

    @Test
    @DisplayName("With-mutators return updated immutable instances")
    void testWithMutators() {
        ThirstData initial = new ThirstData(10, 5, 1.0F, true);

        ThirstData updatedThirst = initial.withThirst(15);
        assertEquals(15, updatedThirst.thirst());
        assertEquals(10, initial.thirst(), "Original must remain unchanged");

        ThirstData updatedQuenched = initial.withQuenched(8);
        assertEquals(8, updatedQuenched.quenched());

        ThirstData updatedExhaustion = initial.withExhaustion(3.5F);
        assertEquals(3.5F, updatedExhaustion.exhaustion());

        ThirstData disabled = initial.withEnabled(false);
        assertFalse(disabled.enabled());
    }
}

