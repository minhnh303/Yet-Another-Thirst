package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetwork {

    public static final ResourceLocation THIRST_SYNC = Constants.asResource("thirst_sync");
    public static final ResourceLocation DRINK_BY_HAND = Constants.asResource("drink_by_hand");

    private FabricNetwork() {}

    public static void register() {
        // Client→Server: hand drinking — registered in FabricGameEvents
    }

    public static void sendToPlayer(ServerPlayer player, ThirstData data) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        data.write(buf);
        ServerPlayNetworking.send(player, THIRST_SYNC, buf);
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        sendToPlayer(player, new ThirstData(thirst, quenched, exhaustion, enabled));
    }
}
