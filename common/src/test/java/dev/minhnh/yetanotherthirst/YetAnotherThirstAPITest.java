package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.api.YetAnotherThirstAPI;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class YetAnotherThirstAPITest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = Mockito.mock(Player.class);
        when(player.getUUID()).thenReturn(UUID.randomUUID());
        ThirstStorage.reset(player);
    }

    @Test
    @DisplayName("API getters and setters for thirst and quenched")
    void testThirstAndQuenchedAPI() {
        assertEquals(20, YetAnotherThirstAPI.getThirst(player));
        assertEquals(5, YetAnotherThirstAPI.getQuenched(player));

        YetAnotherThirstAPI.setThirst(player, 15);
        assertEquals(15, YetAnotherThirstAPI.getThirst(player));

        YetAnotherThirstAPI.setQuenched(player, 3);
        assertEquals(3, YetAnotherThirstAPI.getQuenched(player));
    }

    @Test
    @DisplayName("API getters and setters for exhaustion")
    void testExhaustionAPI() {
        assertEquals(0.0F, YetAnotherThirstAPI.getExhaustion(player));

        YetAnotherThirstAPI.setExhaustion(player, 2.5F);
        assertEquals(2.5F, YetAnotherThirstAPI.getExhaustion(player), 0.001F);

        YetAnotherThirstAPI.addExhaustion(player, 1.25F);
        assertEquals(3.75F, YetAnotherThirstAPI.getExhaustion(player), 0.001F);
    }

    @Test
    @DisplayName("API enable / disable toggle")
    void testEnabledAPI() {
        assertTrue(YetAnotherThirstAPI.isThirstEnabled(player));

        YetAnotherThirstAPI.setThirstEnabled(player, false);
        assertFalse(YetAnotherThirstAPI.isThirstEnabled(player));
    }

    @Test
    @DisplayName("API drink restores thirst and quenched")
    void testDrinkAPI() {
        YetAnotherThirstAPI.setThirst(player, 10);
        YetAnotherThirstAPI.setQuenched(player, 0);

        YetAnotherThirstAPI.drink(player, 6, 2);
        assertEquals(16, YetAnotherThirstAPI.getThirst(player));
        assertEquals(2, YetAnotherThirstAPI.getQuenched(player));
    }

    @Test
    @DisplayName("API immutable ThirstData snapshot getters and setters")
    void testThirstDataAPI() {
        ThirstData initial = YetAnotherThirstAPI.getThirstData(player);
        assertEquals(20, initial.thirst());
        assertEquals(5, initial.quenched());

        ThirstData custom = new ThirstData(17, 4, 1.0F, true);
        YetAnotherThirstAPI.setThirstData(player, custom);
        assertEquals(custom, YetAnotherThirstAPI.getThirstData(player));

        ThirstData updated = YetAnotherThirstAPI.updateThirstData(player, d -> d.withThirst(12));
        assertEquals(12, updated.thirst());
        assertEquals(12, YetAnotherThirstAPI.getThirstData(player).thirst());
    }
}

