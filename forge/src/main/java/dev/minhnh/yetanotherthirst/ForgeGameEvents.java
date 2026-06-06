package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.compat.VampirismCompat;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.purity.ContainerWithPurity;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class ForgeGameEvents {

    private ForgeGameEvents() {}

    // ── Server lifecycle ──────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // Items are registered; initialise purity containers now
        WaterPurity.init();
        // Register custom item thirst values after items exist
        ThirstValues.registerDrink(ModItems.TERRACOTTA_WATER_BOWL.get(), 5, 7);
    }

    // ── Tick / player state ───────────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
            if (ThirstConfig.COMPAT_VAMPIRISM && VampirismCompat.isVampire(player)) return;
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

    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) {

        if (!ThirstConfig.DEHYDRATION_HALTS_HEALTH_REGEN) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (ThirstStorage.get(player).isEnabled() && ThirstStorage.get(player).getThirst() < ThirstConfig.MAX_THIRST) {
            event.setCanceled(true);
        }
    }

    // ── Purity: running-water pickup ──────────────────────────────────────────

    /**
     * When a player right-clicks a water fluid with an empty container that supports
     * running-water harvest (glass bottle, terracotta bowl), fill it with the
     * block's purity value even if the fluid isn't a source block.
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {

        ItemStack held = event.getItemStack();
        if (!WaterPurity.canHarvestRunningWater(held)) return;

        var player = event.getEntity();
        var level = player.level();
        var pos = level.clip(new net.minecraft.world.level.ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(player.getBlockReach())),
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.ANY,
                player)).getBlockPos();

        if (!level.getFluidState(pos).is(FluidTags.WATER)) return;
        if (level.getFluidState(pos).isSource()) return; // source blocks handled by vanilla

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

        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeHandDrinkClient::tryDrink);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {

        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ForgeHandDrinkClient::tryDrink);
        }
    }

    // ── Purity: tooltip ───────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {

        WaterPurity.appendTooltip(event.getItemStack(), event.getToolTip());
    }
}
