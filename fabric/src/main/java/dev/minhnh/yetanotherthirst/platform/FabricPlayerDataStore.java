package dev.minhnh.yetanotherthirst.platform;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.WeakHashMap;

/**
 * In-memory store for per-player mod data on Fabric.
 * Populated/persisted by MixinServerPlayer injecting into addAdditionalSaveData/readAdditionalSaveData.
 */
public final class FabricPlayerDataStore {

    private static final WeakHashMap<Player, CompoundTag> DATA = new WeakHashMap<>();

    private FabricPlayerDataStore() {}

    public static CompoundTag get(Player player) {

        return DATA.getOrDefault(player, new CompoundTag());
    }

    public static void put(Player player, CompoundTag tag) {

        DATA.put(player, tag);
    }
}
