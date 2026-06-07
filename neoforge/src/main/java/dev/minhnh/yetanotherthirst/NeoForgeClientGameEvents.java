package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Optional;

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
}
