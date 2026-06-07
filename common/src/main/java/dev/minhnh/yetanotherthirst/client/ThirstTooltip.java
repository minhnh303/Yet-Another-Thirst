package dev.minhnh.yetanotherthirst.client;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public final class ThirstTooltip {

    private ThirstTooltip() {
    }

    public static void append(ItemStack stack, List<Component> tooltip) {
        // Disabled: now rendered visually using ThirstTooltipComponent
    }
}
