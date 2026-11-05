package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class NeoForgeNetwork {

    public static final String PROTOCOL_VERSION = "1";

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

    private NeoForgeNetwork() {}

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).versioned(PROTOCOL_VERSION);

        registrar.playToClient(
                ThirstSyncPayload.TYPE,
                ThirstSyncPayload.CODEC,
                (payload, context) -> context.enqueueWork(() ->
                        NeoForgeClientPacketHandler.handleThirstSync(
                                payload.thirst(),
                                payload.quenched(),
                                payload.exhaustion(),
                                payload.enabled()
                        )
                )
        );

        registrar.playToServer(
                DrinkByHandPayload.TYPE,
                DrinkByHandPayload.CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    Player p = context.player();
                    if (p instanceof ServerPlayer player) {
                        handleDrinkByHand(payload.pos(), player);
                    }
                })
        );
    }

    public static void sendToPlayer(ServerPlayer player, int thirst, int quenched, float exhaustion, boolean enabled) {
        PacketDistributor.sendToPlayer(player, new ThirstSyncPayload(thirst, quenched, exhaustion, enabled));
    }

    public static void sendDrinkByHand(BlockPos pos) {
        ClientPacketDistributor.sendToServer(new DrinkByHandPayload(pos));
    }

    private static void handleDrinkByHand(BlockPos pos, ServerPlayer player) {
        double maxReach = 6.0;
        if (player.getEyePosition().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxReach * maxReach) {
            return;
        }

        if (!player.isCrouching() && !player.isSecondaryUseActive()) {
            return;
        }

        boolean handAvailable = ThirstConfig.DRINK_BOTH_HANDS_NEEDED
                ? player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()
                        && player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND).isEmpty()
                : player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty();
        if (!handAvailable) {
            return;
        }

        Level level = player.level();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) return;

        var state = ThirstStorage.get(player);
        if (!state.isEnabled() || state.getThirst() >= ThirstConfig.MAX_THIRST) return;

        int purity = WaterPurity.getBlockPurity(level, pos);
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);

        boolean shouldDrink = WaterPurity.givePurityEffects(player, purity);
        if (shouldDrink) {
            state.drink(ThirstConfig.HAND_DRINKING_THIRST, ThirstConfig.HAND_DRINKING_QUENCHED);
        }
        ModAdvancements.award(player, ModAdvancements.HAND_DRINKING);
        ThirstStorage.sync(player);
    }
}
