package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/** Minimal shared color constants and drawing helpers for the config screens. */
final class ConfigTheme {

    static final int ACCENT = 0xFF4FC3F7;
    static final int TEXT = 0xFFFFFFFF;
    static final int MUTED = 0xFFA8A8A8;
    static final int DIM = 0xFF6E6E6E;
    static final int MODIFIED = 0xFFFFC857;
    static final int ERROR = 0xFFFF6B6B;
    static final int SUCCESS = 0xFF6BE38A;
    static final int SIDEBAR = 0x66000000;
    static final int LIST_BG = 0x44000000;
    static final int ROW_HOVER = 0x18FFFFFF;
    static final int SELECTED = 0x33FFFFFF;
    static final int LINE = 0x33FFFFFF;
    static final int SWITCH_ON = 0xFF3DA35D;
    static final int SWITCH_OFF = 0xFF4A4A4A;
    static final int BANNER = 0xA05A3A00;

    private ConfigTheme() {
    }

    static void outline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    static void droplet(GuiGraphics graphics, int x, int y, int scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.blit(Constants.THIRST_ICONS, 0, 0, 18, 0, 9, 9, 36, 25);
        graphics.pose().popPose();
    }

    private static final FormattedText ELLIPSIS = FormattedText.of("…");

    /** Draws {@code text}, cutting it with an ellipsis (keeping its style) when wider than {@code maxWidth}. */
    static boolean clipped(GuiGraphics graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        if (font.width(text) <= maxWidth) {
            graphics.drawString(font, text, x, y, color);
            return false;
        }
        FormattedText cut = font.substrByWidth(text, Math.max(0, maxWidth - font.width(ELLIPSIS)));
        graphics.drawString(font, Language.getInstance().getVisualOrder(FormattedText.composite(cut, ELLIPSIS)), x, y, color);
        return true;
    }
}

