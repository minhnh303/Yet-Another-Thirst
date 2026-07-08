package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoForgeNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private NeoForgeNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(ThirstSyncPacket.TYPE, ThirstSyncPacket.STREAM_CODEC, ThirstSyncPacket::handle);
        registrar.playToServer(DrinkByHandPacket.TYPE, DrinkByHandPacket.STREAM_CODEC, DrinkByHandPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        PacketDistributor.sendToPlayer(player, new ThirstSyncPacket(thirst, quenched, exhaustion, enabled));
    }

    public static void sendDrinkByHand(BlockPos pos) {
        PacketDistributor.sendToServer(new DrinkByHandPacket(pos));
    }

    private record ThirstSyncPacket(int thirst, int quenched, float exhaustion, boolean enabled) implements CustomPacketPayload {

        private static final Type<ThirstSyncPacket> TYPE = new Type<>(Constants.asResource("thirst_sync"));
        private static final StreamCodec<RegistryFriendlyByteBuf, ThirstSyncPacket> STREAM_CODEC =
                CustomPacketPayload.codec(ThirstSyncPacket::encode, ThirstSyncPacket::decode);

        private static void encode(ThirstSyncPacket packet, FriendlyByteBuf buffer) {
            buffer.writeInt(packet.thirst);
            buffer.writeInt(packet.quenched);
            buffer.writeFloat(packet.exhaustion);
            buffer.writeBoolean(packet.enabled);
        }

        private static ThirstSyncPacket decode(FriendlyByteBuf buffer) {
            return new ThirstSyncPacket(buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readBoolean());
        }

        private static void handle(ThirstSyncPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> NeoForgeClientPacketHandler.handleThirstSync(
                    packet.thirst,
                    packet.quenched,
                    packet.exhaustion,
                    packet.enabled));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    private record DrinkByHandPacket(BlockPos pos) implements CustomPacketPayload {

        private static final Type<DrinkByHandPacket> TYPE = new Type<>(Constants.asResource("drink_by_hand"));
        private static final StreamCodec<RegistryFriendlyByteBuf, DrinkByHandPacket> STREAM_CODEC =
                CustomPacketPayload.codec(DrinkByHandPacket::encode, DrinkByHandPacket::decode);

        private static void encode(DrinkByHandPacket packet, FriendlyByteBuf buffer) {
            buffer.writeBlockPos(packet.pos);
        }

        private static DrinkByHandPacket decode(FriendlyByteBuf buffer) {
            return new DrinkByHandPacket(buffer.readBlockPos());
        }

        private static void handle(DrinkByHandPacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                ServerPlayer player = (ServerPlayer) context.player();
                boolean debug = ThirstConfig.DEBUG_LOGGING;
                if (debug) Constants.LOG.info("[HandDrink] Packet received from '{}' at {}", player.getName().getString(), packet.pos);

                double maxReach = 6.0;
                if (player.getEyePosition().distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > maxReach * maxReach) {
                    if (debug) Constants.LOG.info("[HandDrink] Rejected: too far from pos {}", packet.pos);
                    return;
                }

                if (!player.isCrouching() && !player.isSecondaryUseActive()) {
                    if (debug) Constants.LOG.info("[HandDrink] Rejected: not crouching/secondary use");
                    return;
                }

                boolean handAvailable = ThirstConfig.DRINK_BOTH_HANDS_NEEDED
                        ? player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()
                        && player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND).isEmpty()
                        : player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty();
                if (!handAvailable) {
                    if (debug) Constants.LOG.info("[HandDrink] Rejected: hands not empty");
                    return;
                }

                Level level = player.level();
                if (!level.getFluidState(packet.pos).is(FluidTags.WATER)) {
                    if (debug) Constants.LOG.info("[HandDrink] Rejected: no water at {}", packet.pos);
                    return;
                }

                var state = ThirstStorage.get(player);
                if (!state.isEnabled() || state.getThirst() >= ThirstConfig.MAX_THIRST) {
                    if (debug) Constants.LOG.info("[HandDrink] Rejected: enabled={} thirst={}/{}", state.isEnabled(), state.getThirst(), ThirstConfig.MAX_THIRST);
                    return;
                }

                int purity = WaterPurity.getBlockPurity(level, packet.pos);
                if (debug) Constants.LOG.info("[HandDrink] Drinking: player='{}' purity={}", player.getName().getString(), purity);
                level.playSound(player, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);

                boolean shouldDrink = WaterPurity.givePurityEffects(player, purity);
                if (shouldDrink) {
                    state.drink(ThirstConfig.HAND_DRINKING_THIRST, ThirstConfig.HAND_DRINKING_QUENCHED);
                }
                ModAdvancements.award(player, ModAdvancements.HAND_DRINKING);
                ThirstStorage.sync(player);
            });
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
