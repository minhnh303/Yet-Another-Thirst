package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.block.AbstractFilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlock;
import dev.minhnh.yetanotherthirst.core.block.WaterBoilerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.ContainerWithPurity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.client.ThirstTooltip;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import net.minecraft.world.item.Item;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class NeoForgeGameEvents {

    private NeoForgeGameEvents() {
    }

    // ── Server lifecycle ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        NeoForgeConfigItems.reloadThirstValues();
        ModAdvancements.checkDatapack(event.getServer());
    }

    // ── Tick / player state ───────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (event.getEntity() instanceof ServerPlayer player) {
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
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {

        ThirstCommands.register(event.getDispatcher());
    }

    // ── Purity: running-water pickup ──────────────────────────────────────────

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

        ItemStack held = event.getItemStack();
        if (!WaterPurity.canHarvestRunningWater(held)) return;

        var player = event.getEntity();
        var level = player.level();
        var pos = level.clip(new net.minecraft.world.level.ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(player.blockInteractionRange())),
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.ANY,
                player)).getBlockPos();

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return;

        ContainerWithPurity container = WaterPurity.getContainerForEmpty(held);
        if (container == null) return;

        var sound = held.is(net.minecraft.world.item.Items.GLASS_BOTTLE)
                ? SoundEvents.BOTTLE_FILL : SoundEvents.BUCKET_FILL;
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        ItemStack filled = WaterPurity.addPurity(container.getFilledItem().copy(), level, pos);
        ItemStack result = ItemUtils.createFilledResult(held, player, filled);
        player.setItemInHand(event.getHand(), result);
        event.setCanceled(true);
    }

    // ── Purity: hand drinking trigger (client-side detect → server packet) ────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide) {
            NeoForgeHandDrinkClient.handleRightClickBlock(event);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getHand() != net.minecraft.world.InteractionHand.MAIN_HAND) return;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide) {
            NeoForgeHandDrinkClient.tryDrink();
        }
    }

    // ── IE hammer: intercept before IE rotates the block ─────────────────────

    public static void onIEHammerBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ThirstConfig.COMPAT_IE_HEATER) return;

        // IE doesn't register in any neoforge:tools/hammers tag — check by registry key
        net.minecraft.resources.ResourceLocation itemKey =
                BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
        if (!"immersiveengineering".equals(itemKey.getNamespace())
                || !"hammer".equals(itemKey.getPath())) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof WaterBoilerBlock)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof WaterBoilerBlockEntity boiler) {
            boiler.hammerUseSide(event.getHitVec().getDirection(), event.getEntity(),
                    event.getHand(), event.getHitVec().getLocation());
        }

        if (level.isClientSide()) {
            // Deny item use to block IE's onItemUseFirst, but do NOT cancel the event —
            // cancelling would suppress the interaction packet and the server would never run.
            event.setUseItem(net.neoforged.neoforge.common.util.TriState.FALSE);
        } else {
            event.setCanceled(true);
        }
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

    // ── Advancements: wash filter ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Item crafted = event.getCrafting().getItem();
        if (crafted != ModItems.FABRIC_FILTER_CORE.get()
                && crafted != ModItems.SAND_FILTER_CORE.get()
                && crafted != ModItems.CARBON_FILTER_CORE.get()) return;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            if (AbstractFilterFrameBlockEntity.isClogged(event.getInventory().getItem(i))) {
                ModAdvancements.award(player, ModAdvancements.WASH_FILTER);
                return;
            }
        }
    }
}
