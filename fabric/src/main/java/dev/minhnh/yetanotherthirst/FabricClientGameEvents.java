package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class FabricClientGameEvents {

    private FabricClientGameEvents() {}

    public static void register() {

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, lines) -> {
            dev.minhnh.yetanotherthirst.client.TooltipStackTracker.set(stack);
            dev.minhnh.yetanotherthirst.core.purity.WaterPurity.appendTooltip(stack, lines);
            dev.minhnh.yetanotherthirst.client.ThirstTooltip.append(stack, lines);
        });
    }
}
