package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public final class ForgeNetwork {

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Constants.asResource("main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);
    private static int packetId;

    private ForgeNetwork() {}

    public static void register() {

        CHANNEL.registerMessage(
                packetId++,
                ThirstSyncPacket.class,
                ThirstSyncPacket::encode,
                ThirstSyncPacket::decode,
                ThirstSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(
                packetId++,
                DrinkByHandPacket.class,
                DrinkByHandPacket::encode,
                DrinkByHandPacket::decode,
                DrinkByHandPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {

        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ThirstSyncPacket(thirst, quenched, exhaustion, enabled));
    }

    public static void sendDrinkByHand(BlockPos pos) {

        CHANNEL.sendToServer(new DrinkByHandPacket(pos));
    }

    // ── Server → Client: thirst state sync ───────────────────────────────────

    private record ThirstSyncPacket(int thirst, int quenched, float exhaustion, boolean enabled) {

        private static void encode(ThirstSyncPacket packet, FriendlyByteBuf buf) {
            buf.writeInt(packet.thirst);
            buf.writeInt(packet.quenched);
            buf.writeFloat(packet.exhaustion);
            buf.writeBoolean(packet.enabled);
        }

        private static ThirstSyncPacket decode(FriendlyByteBuf buf) {
            return new ThirstSyncPacket(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean());
        }

        private static void handle(ThirstSyncPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() ->
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                            ForgeClientPacketHandler.handleThirstSync(
                                    packet.thirst, packet.quenched, packet.exhaustion, packet.enabled)));
            ctx.get().setPacketHandled(true);
        }
    }

    // ── Client → Server: hand drinking ───────────────────────────────────────

    private record DrinkByHandPacket(BlockPos pos) {

        private static void encode(DrinkByHandPacket packet, FriendlyByteBuf buf) {
            buf.writeBlockPos(packet.pos);
        }

        private static DrinkByHandPacket decode(FriendlyByteBuf buf) {
            return new DrinkByHandPacket(buf.readBlockPos());
        }

        private static void handle(DrinkByHandPacket packet, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;

                // 1. Distance check
                double maxReach = 6.0;
                if (player.getEyePosition().distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > maxReach * maxReach) {
                    return;
                }

                // 2. Sneaking check
                if (!player.isCrouching() && !player.isSecondaryUseActive()) {
                    return;
                }

                // 3. Hand availability check
                boolean handAvailable = ThirstConfig.DRINK_BOTH_HANDS_NEEDED
                        ? player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()
                                && player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND).isEmpty()
                        : player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty();
                if (!handAvailable) {
                    return;
                }

                Level level = player.level();
                if (!level.getFluidState(packet.pos).is(FluidTags.WATER)) return;

                var state = ThirstStorage.get(player);
                if (!state.isEnabled() || state.getThirst() >= ThirstConfig.MAX_THIRST) return;

                int purity = WaterPurity.getBlockPurity(level, packet.pos);
                level.playSound(player, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);

                boolean shouldDrink = WaterPurity.givePurityEffects(player, purity);
                if (shouldDrink) {
                    state.drink(ThirstConfig.HAND_DRINKING_THIRST, ThirstConfig.HAND_DRINKING_QUENCHED);
                }
                ThirstStorage.sync(player);
            });
            ctx.get().setPacketHandled(true);
        }
    }
}
