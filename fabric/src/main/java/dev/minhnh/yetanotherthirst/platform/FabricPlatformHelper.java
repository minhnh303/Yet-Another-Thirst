package dev.minhnh.yetanotherthirst.platform;

import dev.minhnh.yetanotherthirst.FabricNetwork;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public CompoundTag loadThirstData(Player player) {

        return FabricPlayerDataStore.get(player);
    }

    @Override
    public void saveThirstData(Player player, CompoundTag tag) {

        FabricPlayerDataStore.put(player, tag);
    }

    @Override
    public CompoundTag loadPersistentData(Player player, String key) {

        CompoundTag root = FabricPlayerDataStore.get(player);
        return root.getCompound(key);
    }

    @Override
    public void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {

        FabricNetwork.sendToPlayer(player, thirst, quenched, exhaustion, enabled);
    }

    @Override
    public boolean tryHandDrink() {
        if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT) {
            return ClientWrapper.tryDrink();
        }
        return false;
    }

    private static class ClientWrapper {
        private static boolean tryDrink() {
            return dev.minhnh.yetanotherthirst.FabricHandDrinkClient.tryDrink();
        }
    }
}
