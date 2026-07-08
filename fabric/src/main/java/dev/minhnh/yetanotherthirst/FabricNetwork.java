package dev.minhnh.yetanotherthirst;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetwork {

    // Server→Client: sync thirst state
    public record ThirstSyncPayload(int thirst, int quenched, float exhaustion, boolean enabled)
            implements CustomPacketPayload {

        public static final Type<ThirstSyncPayload> TYPE =
                new Type<>(Constants.asResource("thirst_sync"));

        public static final StreamCodec<FriendlyByteBuf, ThirstSyncPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> {
                            buf.writeInt(p.thirst());
                            buf.writeInt(p.quenched());
                            buf.writeFloat(p.exhaustion());
                            buf.writeBoolean(p.enabled());
                        },
                        buf -> new ThirstSyncPayload(
                                buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean()));

        @Override
        public Type<ThirstSyncPayload> type() { return TYPE; }
    }

    // Client→Server: drink by hand
    public record DrinkByHandPayload(net.minecraft.core.BlockPos pos)
            implements CustomPacketPayload {

        public static final Type<DrinkByHandPayload> TYPE =
                new Type<>(Constants.asResource("drink_by_hand"));

        public static final StreamCodec<FriendlyByteBuf, DrinkByHandPayload> CODEC =
                StreamCodec.of(
                        (buf, p) -> buf.writeBlockPos(p.pos()),
                        buf -> new DrinkByHandPayload(buf.readBlockPos()));

        @Override
        public Type<DrinkByHandPayload> type() { return TYPE; }
    }

    private FabricNetwork() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ThirstSyncPayload.TYPE, ThirstSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(DrinkByHandPayload.TYPE, DrinkByHandPayload.CODEC);
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, quenched, exhaustion, enabled));
    }
}
