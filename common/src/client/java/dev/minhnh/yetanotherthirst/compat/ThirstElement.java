package dev.minhnh.yetanotherthirst.compat;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

public final class ThirstElement extends Element {
    private static final ResourceLocation THIRST_ICONS = Constants.THIRST_ICONS;
    private final ThirstValue value;

    public ThirstElement(ThirstValue value) {
        this.value = value;
        int thirstDroplets = (value.thirst() + 1) / 2;
        int quenchedDroplets = (value.quenched() + 1) / 2;
        int width = Math.max(10, Math.max(thirstDroplets * 8 + 2, quenchedDroplets * 6 + 2));
        int height = value.quenched() > 0 ? 18 : 10;
        this.size = new Vec2(width, height);
    }

    @Override
    public Vec2 getSize() {
        return this.size;
    }

    @Override
    public void render(GuiGraphics guiGraphics, float x, float y, float w, float h) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int ix = (int) x;
        int iy = (int) y;

        // Row 1: Thirst restored (Row 1, u=0/9/18, v=0, size 9x9)
        int thirst = value.thirst();
        for (int i = 0; i < (thirst + 1) / 2; i++) {
            int dx = ix + i * 8;
            if (i * 2 + 1 < thirst) {
                guiGraphics.blit(THIRST_ICONS, dx, iy, 18, 0, 9, 9, 36, 25);
            } else {
                guiGraphics.blit(THIRST_ICONS, dx, iy, 9, 0, 9, 9, 36, 25);
            }
        }

        // Row 2: Quenched restored (Row 3, u=0/7/14, v=18, size 7x7)
        int quenched = value.quenched();
        if (quenched > 0) {
            for (int i = 0; i < (quenched + 1) / 2; i++) {
                int dx = ix + i * 6;
                int dy = iy + 10;
                if (i * 2 + 1 < quenched) {
                    guiGraphics.blit(THIRST_ICONS, dx, dy, 0, 18, 7, 7, 36, 25);
                } else {
                    guiGraphics.blit(THIRST_ICONS, dx, dy, 7, 18, 7, 7, 36, 25);
                }
            }
        }

        RenderSystem.disableBlend();
    }
}
