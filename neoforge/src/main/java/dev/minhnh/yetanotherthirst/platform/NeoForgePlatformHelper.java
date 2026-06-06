package dev.minhnh.yetanotherthirst.platform;

import dev.minhnh.yetanotherthirst.NeoForgeNetwork;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.platform.services.IPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public CompoundTag loadThirstData(Player player) {

        return player.getPersistentData().getCompound(Constants.MOD_ID);
    }

    @Override
    public void saveThirstData(Player player, CompoundTag tag) {

        player.getPersistentData().put(Constants.MOD_ID, tag);
    }

    @Override
    public void sendThirstSync(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {

        NeoForgeNetwork.sendToPlayer(player, thirst, quenched, exhaustion, enabled);
    }
}
