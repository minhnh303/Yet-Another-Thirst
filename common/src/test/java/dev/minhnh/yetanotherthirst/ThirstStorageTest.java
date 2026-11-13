package dev.minhnh.yetanotherthirst;

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

class ThirstStorageTest {

    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        player1 = Mockito.mock(Player.class);
        player2 = Mockito.mock(Player.class);
        when(player1.getUUID()).thenReturn(UUID.randomUUID());
        when(player2.getUUID()).thenReturn(UUID.randomUUID());
    }

    @Test
    @DisplayName("ThirstStorage retrieves and updates player data")
    void testGetAndSetData() {
        ThirstData initial = ThirstStorage.getData(player1);
        assertEquals(20, initial.thirst());
        assertEquals(5, initial.quenched());

        ThirstData custom = new ThirstData(14, 2, 1.5F, true);
        ThirstStorage.setData(player1, custom);

        assertEquals(custom, ThirstStorage.getData(player1));
    }

    @Test
    @DisplayName("ThirstStorage update applies atomic functional transformations")
    void testUpdateData() {
        ThirstData updated = ThirstStorage.update(player1, d -> d.withThirst(12).withQuenched(3));
        assertEquals(12, updated.thirst());
        assertEquals(3, updated.quenched());
        assertEquals(updated, ThirstStorage.getData(player1));
    }

    @Test
    @DisplayName("Apply sync packet directly updates state without saving")
    void testApplySync() {
        ThirstStorage.applySync(player1, 10, 2, 0.5F, false);
        ThirstData data = ThirstStorage.getData(player1);
        assertEquals(10, data.thirst());
        assertEquals(2, data.quenched());
        assertEquals(0.5F, data.exhaustion());
        assertFalse(data.enabled());
    }

    @Test
    @DisplayName("Copy transfers data between players and respects resetOnDeath")
    void testCopy() {
        ThirstStorage.setData(player1, new ThirstData(7, 1, 2.0F, true));

        // Copy on respawn (resetOnDeath = true)
        ThirstStorage.copy(player1, player2, true);
        ThirstData respawnData = ThirstStorage.getData(player2);
        assertEquals(20, respawnData.thirst());
        assertEquals(5, respawnData.quenched());

        // Copy on teleport/dimension change (resetOnDeath = false)
        Player player3 = Mockito.mock(Player.class);
        when(player3.getUUID()).thenReturn(UUID.randomUUID());
        ThirstStorage.copy(player1, player3, false);
        ThirstData dimData = ThirstStorage.getData(player3);
        assertEquals(7, dimData.thirst());
        assertEquals(1, dimData.quenched());
        assertEquals(2.0F, dimData.exhaustion());
    }

    @Test
    @DisplayName("Reset returns player state to default values")
    void testReset() {
        ThirstStorage.setData(player1, new ThirstData(5, 0, 3.0F, true));
        ThirstStorage.reset(player1);

        ThirstData defaultData = ThirstStorage.getData(player1);
        assertEquals(20, defaultData.thirst());
        assertEquals(5, defaultData.quenched());
        assertEquals(0.0F, defaultData.exhaustion());
    }

    @Test
    @DisplayName("Remove cleans up cached player state")
    void testRemove() {
        ThirstStorage.setData(player1, new ThirstData(11, 2, 0.0F, true));
        ThirstStorage.remove(player1);

        // When retrieved again, it loads fresh from saved platform helper
        ThirstData reloaded = ThirstStorage.getData(player1);
        assertEquals(11, reloaded.thirst());
        assertEquals(2, reloaded.quenched());
    }
}

