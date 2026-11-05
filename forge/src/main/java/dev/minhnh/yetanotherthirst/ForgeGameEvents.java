package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ThirstTooltip;
import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.purity.ContainerWithPurity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ForgeGameEvents {

    private ForgeGameEvents() {}

    // ── Server lifecycle ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        ForgeConfigItems.reloadThirstValues();
    }

    // ── Tick / player state ───────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.player() instanceof ServerPlayer player) {
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

    // ── Purity: running-water pickup ──────────────────────────────────────────

    @SubscribeEvent
    public static boolean onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack held = event.getItemStack();
        if (!WaterPurity.canHarvestRunningWater(held)) return false;

        var player = event.getEntity();
        var level = player.level();
        var eyePos = player.getEyePosition();
        var pos = level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.getLookAngle().scale(player.isCreative() ? 5.0 : 4.5)),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player)).getBlockPos();

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return false;

        ContainerWithPurity container = WaterPurity.getContainerForEmpty(held);
        if (container == null) return false;

        var sound = held.is(Items.GLASS_BOTTLE) ? SoundEvents.BOTTLE_FILL : SoundEvents.BUCKET_FILL;
        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);

        ItemStack filled = WaterPurity.addPurity(container.getFilledItem().copy(), level, pos);
        ItemStack result = ItemUtils.createFilledResult(held, player, filled);
        player.setItemInHand(event.getHand(), result);
        event.setCancellationResult(InteractionResult.SUCCESS);
        return true;
    }

    // ── Purity: tooltip ───────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        WaterPurity.appendTooltip(event.getItemStack(), event.getToolTip());
        ThirstTooltip.append(event.getItemStack(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ForgeConfigItems.reloadThirstValues();
    }

    @SubscribeEvent
    public static void onFillBucket(FillBucketEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!event.getEmptyBucket().is(Items.BUCKET)) return;
        if (!(event.getTarget() instanceof BlockHitResult blockHit)) return;

        BlockPos pos = blockHit.getBlockPos();
        Level level = event.getLevel();
        if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getFluidState(pos).isSource()) return;

        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof BucketPickup pickup)) return;

        int purity = WaterPurity.getBlockPurity(level, pos);
        ItemStack filled = pickup.pickupBlock(event.getEntity(), level, pos, state);
        if (filled.isEmpty()) return;

        WaterPurity.addPurity(filled, purity);
        pickup.getPickupSound().ifPresent(sound -> event.getEntity().playSound(sound, 1.0F, 1.0F));
        level.gameEvent(event.getEntity(), GameEvent.FLUID_PICKUP, pos);
        event.setFilledBucket(filled);
        event.setResult(Result.ALLOW);
    }
}
