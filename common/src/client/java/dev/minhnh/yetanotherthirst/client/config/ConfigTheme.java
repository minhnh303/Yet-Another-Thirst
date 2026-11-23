package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Colors and small drawing helpers shared by the config screens. */
final class ConfigTheme {

    static final int ACCENT = 0xFF4FC3F7;
    static final int ACCENT_DIM = 0xFF2B7FA8;
    static final int TEXT = 0xFFFFFFFF;
    static final int MUTED = 0xFFA8A8A8;
    static final int DIM = 0xFF6E6E6E;
    static final int MODIFIED = 0xFFFFC857;
    static final int ERROR = 0xFFFF6B6B;
    static final int SUCCESS = 0xFF6BE38A;
    static final int SIDEBAR = 0x66000000;
    static final int ROW_HOVER = 0x18FFFFFF;
    static final int SELECTED = 0x33FFFFFF;
    static final int LINE = 0x33FFFFFF;
    static final int SWITCH_ON = 0xFF3DA35D;
    static final int SWITCH_OFF = 0xFF4A4A4A;
    static final int BANNER = 0xA05A3A00;

    private ConfigTheme() {
    }

    static void outline(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
        graphics.fill(x, y, x + width, y + 1, color);
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /** Item components are bound only while world data is loaded (not on the title screen). */
    private static boolean itemsReady() {
        return BuiltInRegistries.ITEM.wrapAsHolder(Items.STONE).areComponentsBound();
    }

    static ItemStack displayStack(Item item) {
        return displayStack(item, DataComponentMap.EMPTY);
    }

    /**
     * A stack for drawing an item icon and name. In a world it is a normal stack; on the title screen, where
     * {@code new ItemStack} throws because item components are not bound yet, it is a display-only stack
     * carrying just the components rendering reads: the model (the item's registry id, the vanilla default),
     * the name, and {@code extra}.
     */
    static ItemStack displayStack(Item item, DataComponentMap extra) {
        if (itemsReady()) {
            ItemStack stack = new ItemStack(item);
            stack.applyComponents(extra);
            return stack;
        }
        DataComponentMap components = DataComponentMap.builder()
                .set(DataComponents.ITEM_MODEL, BuiltInRegistries.ITEM.getKey(item))
                .set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()))
                .addAll(extra)
                .build();
        return new ItemStack(Holder.direct(item, components));
    }

    /** The full thirst droplet from the HUD sheet, scaled up. */
    static void droplet(GuiGraphicsExtractor graphics, int x, int y, int scale) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        graphics.blit(RenderPipelines.GUI_TEXTURED, Constants.THIRST_ICONS, 0, 0, 18.0F, 0.0F, 9, 9, 36, 25);
        graphics.pose().popMatrix();
    }

    /** Draws text cut to {@code maxWidth} with an ellipsis; returns true when it was cut. */
    static boolean clipped(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int maxWidth, int color) {
        if (font.width(text) <= maxWidth) {
            graphics.text(font, text, x, y, color);
            return false;
        }
        String cut = font.plainSubstrByWidth(text.getString(), Math.max(0, maxWidth - font.width("…"))) + "…";
        graphics.text(font, cut, x, y, color);
        return true;
    }
}

