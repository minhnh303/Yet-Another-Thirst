package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ForgeClientGameEvents {

    private ForgeClientGameEvents() {}

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
    public static boolean onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return false;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide()) {
            return ForgeHandDrinkClient.handleRightClickBlock(event);
        }
        return false;
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (ThirstConfig.CAN_DRINK_BY_HAND && event.getEntity().level().isClientSide()) {
            ForgeHandDrinkClient.tryDrink();
        }
    }
}
