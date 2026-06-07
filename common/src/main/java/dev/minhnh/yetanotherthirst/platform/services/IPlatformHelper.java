package dev.minhnh.yetanotherthirst.platform.services;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    CompoundTag loadThirstData(Player player);

    void saveThirstData(Player player, CompoundTag tag);

    default CompoundTag loadPersistentData(Player player, String key) {

        return new CompoundTag();
    }

    void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled);

    default void sendThirstSync(ServerPlayer player, ThirstState state) {

        sendThirstSync(player, state.getThirst(), state.getQuenched(), state.getExhaustion(), state.isEnabled());
    }

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }
}
