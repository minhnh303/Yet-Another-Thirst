package dev.minhnh.yetanotherthirst.client;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class ClientThirstTooltipComponent implements ClientTooltipComponent {
    private static final Identifier THIRST_ICONS = Constants.THIRST_ICONS;
    private final ThirstValue value;

    public ClientThirstTooltipComponent(ThirstTooltipComponent component) {
        this.value = component.getValue();
    }

    @Override
    public int getHeight(Font font) {
        return value.quenched() > 0 ? 18 : 10;
    }

    @Override
    public int getWidth(Font font) {
        int thirstDroplets = (value.thirst() + 1) / 2;
        int quenchedDroplets = (value.quenched() + 1) / 2;
        return Math.max(10, Math.max(thirstDroplets * 8 + 2, quenchedDroplets * 6 + 2));
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor extractor) {
        // Row 1: Thirst restored (Row 1, u=0/9/18, v=0, size 9x9)
        int thirst = value.thirst();
        for (int i = 0; i < (thirst + 1) / 2; i++) {
            int dx = x + i * 8;
            if (i * 2 + 1 < thirst) {
                extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, dx, y, 18.0F, 0.0F, 9, 9, 36, 25);
            } else {
                extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, dx, y, 9.0F, 0.0F, 9, 9, 36, 25);
            }
        }

        // Row 2: Quenched restored (Row 3, u=0/7/14, v=18, size 7x7)
        int quenched = value.quenched();
        if (quenched > 0) {
            for (int i = 0; i < (quenched + 1) / 2; i++) {
                int dx = x + i * 6;
                int dy = y + 10;
                if (i * 2 + 1 < quenched) {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, dx, dy, 0.0F, 18.0F, 7, 7, 36, 25);
                } else {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, dx, dy, 7.0F, 18.0F, 7, 7, 36, 25);
                }
            }
        }
    }
}
