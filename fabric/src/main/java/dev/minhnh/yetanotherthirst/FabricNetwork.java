package dev.minhnh.yetanotherthirst;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetwork {

    public record ThirstSyncPayload(int thirst, int quenched, float exhaustion, boolean enabled) implements CustomPacketPayload {
        public static final Type<ThirstSyncPayload> TYPE = new Type<>(Constants.asResource("thirst_sync"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPayload> CODEC = StreamCodec.ofMember(
                ThirstSyncPayload::write,
                ThirstSyncPayload::new
        );

        public ThirstSyncPayload(RegistryFriendlyByteBuf buf) {
            this(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean());
        }

        public void write(RegistryFriendlyByteBuf buf) {
            buf.writeInt(thirst);
            buf.writeInt(quenched);
            buf.writeFloat(exhaustion);
            buf.writeBoolean(enabled);
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

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        ServerPlayNetworking.send(player, new ThirstSyncPayload(thirst, quenched, exhaustion, enabled));
    }
}
