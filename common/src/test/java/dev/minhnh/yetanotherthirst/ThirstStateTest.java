package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThirstStateTest {

    @Test
    @DisplayName("State initializes with default ThirstData")
    void testInitialState() {
        ThirstState state = new ThirstState();
        assertEquals(ThirstData.DEFAULT, state.getData());
        assertEquals(20, state.getThirst());
        assertEquals(5, state.getQuenched());
        assertEquals(0.0F, state.getExhaustion());
        assertTrue(state.isEnabled());
    }

    @Test
    @DisplayName("Atomic drink updates snapshot")
    void testAtomicDrink() {
        ThirstState state = new ThirstState();
        state.setThirst(10);
        state.setQuenched(0);

        state.drink(5, 2);
        assertEquals(15, state.getThirst());
        assertEquals(2, state.getQuenched());
    }

    @Test
    @DisplayName("Atomic depletion updates snapshot")
    void testAtomicDepletion() {
        ThirstState state = new ThirstState();
        state.setThirst(20);
        state.setQuenched(3);
        state.setExhaustion(5.0F);

        state.depleteOneLevel();
        assertEquals(20, state.getThirst());
        assertEquals(2, state.getQuenched());
        assertEquals(1.0F, state.getExhaustion(), 0.001F);
    }

    @Test
    @DisplayName("Reset returns state back to default")
    void testReset() {
        ThirstState state = new ThirstState();
        state.setThirst(5);
        state.setQuenched(0);
        state.setExhaustion(3.0F);

        state.reset();
        assertEquals(20, state.getThirst());
        assertEquals(5, state.getQuenched());
        assertEquals(0.0F, state.getExhaustion());
    }

    @Test
    @DisplayName("copyFrom transfers ThirstData snapshot")
    void testCopyFrom() {
        ThirstState source = new ThirstState();
        source.setThirst(12);
        source.setQuenched(4);
        source.setExhaustion(2.5F);

        ThirstState destination = new ThirstState();
        destination.copyFrom(source);

        assertEquals(source.getData(), destination.getData());
    }
}

