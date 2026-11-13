package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetwork {

    public record ThirstSyncPayload(ThirstData data) implements CustomPacketPayload {
        public static final Type<ThirstSyncPayload> TYPE = new Type<>(Constants.asResource("thirst_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPayload> CODEC = StreamCodec.ofMember(
                ThirstSyncPayload::write,
                ThirstSyncPayload::new
        );

        public ThirstSyncPayload(int thirst, int quenched, float exhaustion, boolean enabled) {
            this(new ThirstData(thirst, quenched, exhaustion, enabled));
        }

        public ThirstSyncPayload(RegistryFriendlyByteBuf buf) {
            this(ThirstData.read(buf));
        }

        public void write(RegistryFriendlyByteBuf buf) {
            data.write(buf);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record DrinkByHandPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<DrinkByHandPayload> TYPE = new Type<>(Constants.asResource("drink_by_hand"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DrinkByHandPayload> CODEC = StreamCodec.ofMember(
                DrinkByHandPayload::write,
                DrinkByHandPayload::new
        );

        public DrinkByHandPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readBlockPos());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeBlockPos(pos);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private FabricNetwork() {}

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(ThirstSyncPayload.TYPE, ThirstSyncPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(DrinkByHandPayload.TYPE, DrinkByHandPayload.CODEC);
    }

    public static void sendToPlayer(ServerPlayer player, ThirstData data) {
        ServerPlayNetworking.send(player, new ThirstSyncPayload(data));
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        sendToPlayer(player, new ThirstData(thirst, quenched, exhaustion, enabled));
    }
}
