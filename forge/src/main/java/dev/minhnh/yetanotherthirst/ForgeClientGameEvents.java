package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
}
