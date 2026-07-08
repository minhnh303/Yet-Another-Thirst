package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.purity.ContainerWithPurity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.gameevent.GameEvent;

public final class FabricGameEvents {

    private FabricGameEvents() {}

    public static void register() {

        // Server lifecycle
        ServerLifecycleEvents.SERVER_STARTED.register(server -> FabricConfig.reloadThirstValues());

        // Player tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                ThirstEvents.onPlayerTick(player);
            }
        });

        // Login/logout/clone
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ThirstEvents.onPlayerLoggedIn(handler.getPlayer());
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ThirstEvents.onPlayerLoggedOut(handler.getPlayer());
        });
        net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            ThirstEvents.onPlayerClone(oldPlayer, newPlayer, !alive);
            ThirstStorage.sync(newPlayer);
        });

        // Commands
        net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> ThirstCommands.register(dispatcher));

        // Purity: right-click water fill
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(net.minecraft.world.item.Items.BUCKET)) {
                var eyePos = player.getEyePosition();
                var pos = world.clip(new net.minecraft.world.level.ClipContext(
                        eyePos,
                        eyePos.add(player.getLookAngle().scale(player.isCreative() ? 5.0 : 4.5)),
                        net.minecraft.world.level.ClipContext.Block.OUTLINE,
                        net.minecraft.world.level.ClipContext.Fluid.SOURCE_ONLY,
                        player));
                if (pos.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    var fluidState = world.getFluidState(pos.getBlockPos());
                    if (fluidState.is(FluidTags.WATER) && fluidState.isSource()) {
                        var blockState = world.getBlockState(pos.getBlockPos());
                        if (blockState.getBlock() instanceof net.minecraft.world.level.block.BucketPickup pickup) {
                            ItemStack filled = pickup.pickupBlock(null, world, pos.getBlockPos(), blockState);
                            if (!filled.isEmpty()) {
                                WaterPurity.addPurity(filled, world, pos.getBlockPos());
                                world.gameEvent(player, GameEvent.FLUID_PICKUP, pos.getBlockPos());
                                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, filled));
                                return InteractionResultHolder.success(filled);
                            }
                        }
                    }
                }
                return InteractionResultHolder.pass(held);
            }

            ContainerWithPurity container = WaterPurity.getContainerForEmpty(held);
            if (container == null) {
                return InteractionResultHolder.pass(held);
            }
            var eyePos = player.getEyePosition();
            var pos = world.clip(new net.minecraft.world.level.ClipContext(
                    eyePos,
                    eyePos.add(player.getLookAngle().scale(player.isCreative() ? 5.0 : 4.5)),
                    net.minecraft.world.level.ClipContext.Block.OUTLINE,
                    net.minecraft.world.level.ClipContext.Fluid.ANY,
                    player));
            if (pos.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) {
                return InteractionResultHolder.pass(held);
            }
            if (!world.getFluidState(pos.getBlockPos()).is(FluidTags.WATER)) {
                return InteractionResultHolder.pass(held);
            }
            var fillSound = held.is(net.minecraft.world.item.Items.GLASS_BOTTLE)
                    ? SoundEvents.BOTTLE_FILL : SoundEvents.BUCKET_FILL;
            world.playSound(player, player.getX(), player.getY(), player.getZ(),
                    fillSound, SoundSource.NEUTRAL, 1.0F, 1.0F);
            world.gameEvent(player, GameEvent.FLUID_PICKUP, pos.getBlockPos());
            ItemStack filled = WaterPurity.addPurity(container.getFilledItem().copy(), world, pos.getBlockPos());
            ItemStack result = ItemUtils.createFilledResult(held, player, filled);
            player.setItemInHand(hand, result);
            return InteractionResultHolder.success(result);
        });

        // Client→Server: hand drink
        ServerPlayNetworking.registerGlobalReceiver(FabricNetwork.DrinkByHandPayload.TYPE, (payload, context) -> {
            BlockPos pos = payload.pos();
            context.server().execute(() -> {
                ServerPlayer player = context.player();
                double maxReach = 6.0;
                if (player.getEyePosition().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxReach * maxReach) return;
                if (!player.isCrouching() && !player.isSecondaryUseActive()) return;
                boolean handAvailable = ThirstConfig.DRINK_BOTH_HANDS_NEEDED
                        ? player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()
                                && player.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND).isEmpty()
                        : player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty();
                if (!handAvailable) return;
                var level = player.level();
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
            });
        });

        // Tags updated
        net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents.TAGS_LOADED.register(
                (registries, client) -> FabricConfig.reloadThirstValues());
    }
}
