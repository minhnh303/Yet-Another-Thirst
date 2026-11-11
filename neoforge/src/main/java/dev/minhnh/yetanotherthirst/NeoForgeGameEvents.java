package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstTooltip;
import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.purity.ContainerWithPurity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Constants.MOD_ID)
public final class NeoForgeGameEvents {

    private NeoForgeGameEvents() {}

    // ── Server lifecycle ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        NeoForgeConfigItems.reloadThirstValues();
    }

    // ── Tick / player state ───────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ThirstEvents.onPlayerTick(player);
        }
    }

    @SubscribeEvent
    public static void onItemFinished(LivingEntityUseItemEvent.Finish event) {
        ThirstEvents.onItemFinished(event.getEntity(), event.getItem());
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ThirstEvents.onPlayerLoggedIn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ThirstEvents.onPlayerLoggedOut(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ThirstEvents.onPlayerClone(event.getOriginal(), event.getEntity(), event.isWasDeath());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            ThirstStorage.sync(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ThirstCommands.register(event.getDispatcher());
    }

    // ── Purity: water fill & running-water pickup ─────────────────────────────

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack held = event.getItemStack();
        Player player = event.getEntity();
        Level level = player.level();
        InteractionHand hand = event.getHand();

        // 1. Water bucket filling with purity tracking
        if (held.is(Items.BUCKET)) {
            var eyePos = player.getEyePosition();
            BlockHitResult hit = level.clip(new ClipContext(
                    eyePos,
                    eyePos.add(player.getLookAngle().scale(player.isCreative() ? 5.0 : 4.5)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.ANY,
                    player));
            BlockPos pos = hit.getBlockPos();

            if (level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos).isSource()) {
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof BucketPickup pickup) {
                    int purity = WaterPurity.getBlockPurity(level, pos);
                    ItemStack taken = pickup.pickupBlock(player, level, pos, state);
                    if (!taken.isEmpty()) {
                        if (!level.isClientSide()) {
                            ItemStack filledBucket = new ItemStack(Items.WATER_BUCKET);
                            WaterPurity.addPurity(filledBucket, purity);
                            player.awardStat(Stats.ITEM_USED.get(Items.BUCKET));
                            pickup.getPickupSound().ifPresent(sound -> level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F));
                            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

                            ItemStack result = ItemUtils.createFilledResult(held, player, filledBucket);
                            player.setItemInHand(hand, result);
                        }
                        event.setCancellationResult(InteractionResult.SUCCESS);
                        event.setCanceled(true);
                        return;
                    }
                }
            }
        }

        // 2. Running-water pickup
        if (!WaterPurity.canHarvestRunningWater(held)) return;

        var eyePos = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.getLookAngle().scale(player.isCreative() ? 5.0 : 4.5)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));
        BlockPos pos = hit.getBlockPos();

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return;

        ContainerWithPurity container = WaterPurity.getContainerForEmpty(held);
        if (container == null) return;

        var sound = held.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL : SoundEvents.BUCKET_FILL;
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        ItemStack filled = WaterPurity.addPurity(container.getFilledItem().copy(), level, pos);
        ItemStack result = ItemUtils.createFilledResult(held, player, filled);
        player.setItemInHand(hand, result);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // ── Purity: tooltip ───────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        WaterPurity.appendTooltip(event.getItemStack(), event.getToolTip());
        ThirstTooltip.append(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        NeoForgeConfigItems.reloadThirstValues();
    }
}
