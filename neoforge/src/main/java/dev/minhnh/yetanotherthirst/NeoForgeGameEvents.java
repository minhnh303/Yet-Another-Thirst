package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.command.ThirstCommands;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class NeoForgeGameEvents {

    private NeoForgeGameEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer player) {
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
}
