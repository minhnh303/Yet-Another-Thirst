package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TestPlatformHelper implements IPlatformHelper {

    private final Map<UUID, CompoundTag> savedTags = new HashMap<>();

    @Override
    public String getPlatformName() {
        return "JUnit";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return true;
    }

    @Override
    public CompoundTag loadThirstData(Player player) {
        return savedTags.getOrDefault(player.getUUID(), new CompoundTag());
    }

    @Override
    public void saveThirstData(Player player, CompoundTag tag) {
        savedTags.put(player.getUUID(), tag.copy());
    }

    @Override
    public void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        // no-op in tests
    }
}

