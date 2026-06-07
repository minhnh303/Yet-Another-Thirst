package dev.minhnh.yetanotherthirst.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

public final class ClientThirstTooltipComponent implements ClientTooltipComponent {
    private static final ResourceLocation THIRST_ICONS = Constants.asResource("textures/gui/thirst_icons.png");
    private final ThirstValue value;

    public ClientThirstTooltipComponent(ThirstTooltipComponent component) {
        this.value = component.getValue();
    }

    @Override
    public int getHeight() {
        return value.quenched() > 0 ? 18 : 10;
    }

    @Override
    public int getWidth(Font font) {
        int thirstDroplets = (value.thirst() + 1) / 2;
        int quenchedDroplets = (value.quenched() + 1) / 2;
        return Math.max(10, Math.max(thirstDroplets * 8 + 2, quenchedDroplets * 6 + 2));
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // Row 1: Thirst restored (Row 1, u=0/9/18, v=0, size 9x9)
        int thirst = value.thirst();
        for (int i = 0; i < (thirst + 1) / 2; i++) {
            int dx = x + i * 8;
            guiGraphics.blit(THIRST_ICONS, dx, y, 0, 0, 9, 9, 36, 36);
            if (i * 2 + 1 < thirst) {
                guiGraphics.blit(THIRST_ICONS, dx, y, 18, 0, 9, 9, 36, 36);
            } else {
                guiGraphics.blit(THIRST_ICONS, dx, y, 9, 0, 9, 9, 36, 36);
            }
        }

        // Row 2: Quenched restored (Row 4, u=0/7/14, v=27, size 7x7, tinted yellow)
        int quenched = value.quenched();
        if (quenched > 0) {
            for (int i = 0; i < (quenched + 1) / 2; i++) {
                int dx = x + i * 6;
                int dy = y + 10;
                guiGraphics.blit(THIRST_ICONS, dx, dy, 14, 27, 7, 7, 36, 36);
                if (i * 2 + 1 < quenched) {
                    guiGraphics.blit(THIRST_ICONS, dx, dy, 0, 27, 7, 7, 36, 36);
                } else {
                    guiGraphics.blit(THIRST_ICONS, dx, dy, 7, 27, 7, 7, 36, 36);
                }
            }
        }

        RenderSystem.disableBlend();
    }
}
