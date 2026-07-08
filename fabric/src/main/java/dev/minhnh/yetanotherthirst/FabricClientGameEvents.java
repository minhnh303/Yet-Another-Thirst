package dev.minhnh.yetanotherthirst;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

@Environment(EnvType.CLIENT)
public final class FabricClientGameEvents {

    private FabricClientGameEvents() {}

    public static void register() {

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            dev.minhnh.yetanotherthirst.client.TooltipStackTracker.set(stack);
            dev.minhnh.yetanotherthirst.core.purity.WaterPurity.appendTooltip(stack, lines);
            dev.minhnh.yetanotherthirst.client.ThirstTooltip.append(stack, lines);
        });
    }
}
