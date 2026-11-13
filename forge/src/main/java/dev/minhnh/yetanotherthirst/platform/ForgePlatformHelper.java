package dev.minhnh.yetanotherthirst.platform;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.ForgeHandDrinkClient;
import dev.minhnh.yetanotherthirst.ForgeNetwork;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public CompoundTag loadThirstData(Player player) {
        return player.getPersistentData().getCompoundOrEmpty(Constants.MOD_ID);
    }

    @Override
    public void saveThirstData(Player player, CompoundTag tag) {
        player.getPersistentData().put(Constants.MOD_ID, tag);
    }

    @Override
    public CompoundTag loadPersistentData(Player player, String key) {
        return player.getPersistentData().getCompoundOrEmpty(key);
    }

    @Override
    public void sendThirstSync(ServerPlayer player, ThirstData data) {
        ForgeNetwork.sendToPlayer(player, data);
    }

    @Override
    public void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        sendThirstSync(player, new ThirstData(thirst, quenched, exhaustion, enabled));
    }

    @Override
    public boolean tryHandDrink() {
        if (FMLLoader.getDist().isClient()) {
            return ForgeHandDrinkClient.tryDrink();
        }
        return false;
    }
}
