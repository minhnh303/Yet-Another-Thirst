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
        if (ThirstCompat.showsAppleSkinThirstTooltip()) {
            return;
        }

        Optional<ThirstValue> value = ThirstValues.get(stack);
        value.filter(ThirstTooltip::shouldShow)
                .ifPresent(thirstValue -> tooltip.add(Component.translatable(
                        "yet_another_thirst.tooltip.thirst_value",
                        thirstValue.thirst(),
                        thirstValue.quenched()
                ).withStyle(ChatFormatting.AQUA)));
    }

    private static boolean shouldShow(ThirstValue value) {
        return value.thirst() > 0 || value.quenched() > 0;
    }
}
