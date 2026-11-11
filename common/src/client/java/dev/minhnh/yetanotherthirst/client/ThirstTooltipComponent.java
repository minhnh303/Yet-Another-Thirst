package dev.minhnh.yetanotherthirst.client;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public final class ThirstTooltipComponent implements TooltipComponent {
    private final ThirstValue value;

    public ThirstTooltipComponent(ThirstValue value) {
        this.value = value;
    }

    public ThirstValue getValue() {
        return value;
    }
}
