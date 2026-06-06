package dev.minhnh.yetanotherthirst;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class NeoForgeNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Constants.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int packetId;

    private NeoForgeNetwork() {
    }

    public static void register() {

        CHANNEL.registerMessage(
                packetId++,
                ThirstSyncPacket.class,
                ThirstSyncPacket::encode,
                ThirstSyncPacket::decode,
                ThirstSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {

        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ThirstSyncPacket(thirst, quenched, exhaustion, enabled));
    }

    private record ThirstSyncPacket(int thirst, int quenched, float exhaustion, boolean enabled) {

        private static void encode(ThirstSyncPacket packet, FriendlyByteBuf buffer) {

            buffer.writeInt(packet.thirst);
            buffer.writeInt(packet.quenched);
            buffer.writeFloat(packet.exhaustion);
            buffer.writeBoolean(packet.enabled);
        }

        private static ThirstSyncPacket decode(FriendlyByteBuf buffer) {

            return new ThirstSyncPacket(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readBoolean());
        }

        private static void handle(ThirstSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {

            NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> NeoForgeClientPacketHandler.handleThirstSync(
                        packet.thirst,
                        packet.quenched,
                        packet.exhaustion,
                        packet.enabled));
            });
            context.setPacketHandled(true);
        }
    }
}
