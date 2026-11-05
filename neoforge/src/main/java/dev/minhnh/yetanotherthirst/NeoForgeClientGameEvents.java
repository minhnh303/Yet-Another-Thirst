package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class NeoForgeClientGameEvents {

    private NeoForgeClientGameEvents() {}

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!ThirstCompat.showsAppleSkinThirstTooltip()) {
            return;
        }
        Optional<ThirstValue> value = ThirstValues.get(event.getItemStack());
        value.ifPresent(thirstValue -> {
            event.getTooltipElements().add(Either.right(new ThirstTooltipComponent(thirstValue)));
        });
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide()) {
            NeoForgeHandDrinkClient.handleRightClickBlock(event);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide()) {
            NeoForgeHandDrinkClient.tryDrink();
        }
    }
}
