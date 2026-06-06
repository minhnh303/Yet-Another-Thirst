package dev.minhnh.yetanotherthirst.core.thirst;

import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ThirstStorage {

    private static final Map<UUID, ThirstState> STATES = new HashMap<>();

    private ThirstStorage() {
    }

    public static ThirstState get(Player player) {

        return STATES.computeIfAbsent(player.getUUID(), uuid -> load(player));
    }

    public static void applySync(Player player, int thirst, int quenched, float exhaustion, boolean enabled) {

        ThirstState state = get(player);
        state.setThirst(thirst);
        state.setQuenched(quenched);
        state.setExhaustion(exhaustion);
        state.setEnabled(enabled);
    }

    public static void copy(Player oldPlayer, Player newPlayer, boolean resetOnDeath) {

        ThirstState newState = get(newPlayer);
        if (resetOnDeath) {
            newState.reset();
        } else {
            newState.copyFrom(get(oldPlayer));
        }
        save(newPlayer);
    }

    public static void reset(Player player) {

        get(player).reset();
        save(player);
    }

    public static void save(Player player) {

        Services.PLATFORM.saveThirstData(player, get(player).save());
    }

    public static void remove(Player player) {

        save(player);
        STATES.remove(player.getUUID());
    }

    public static void sync(ServerPlayer player) {

        ThirstState state = get(player);
        save(player);
        Services.PLATFORM.sendThirstSync(player, state);
    }

    private static ThirstState load(Player player) {

        ThirstState state = new ThirstState();
        CompoundTag persistentData = Services.PLATFORM.loadThirstData(player);
        if (!persistentData.isEmpty()) {
            state.load(persistentData);
        }
        return state;
    }
}
