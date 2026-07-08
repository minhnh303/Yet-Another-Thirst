package dev.minhnh.yetanotherthirst.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class WaterBoilerScreen extends AbstractContainerScreen<AbstractWaterBoilerMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/water_boiler.png");

    public WaterBoilerScreen(AbstractWaterBoilerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.inventoryLabelY = this.imageHeight - 91;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 1. Render active flame (lit time)
        if (menu.isLit()) {
            int p = menu.getLitProgress();
            guiGraphics.blit(TEXTURE, x + 51, y + 37 + 14 - p, 176, 14 - p, 14, p);
        }

        // 2. Render progress arrow
        int cook = menu.getCookProgress();
        if (cook > 0) {
            guiGraphics.blit(TEXTURE, x + 74, y + 35, 176, 14, cook, 16);
        }

        // 3. Render Input Tank fluid
        int inputAmount = menu.getInputAmount();
        int inputCapacity = menu.getInputCapacity();
        if (inputAmount > 0 && inputCapacity > 0) {
            int inputHeight = (int) ((long) inputAmount * 47 / inputCapacity);
            int color = getPurityColor(menu.getInputPurity());
            int tankX = x + 13;
            int tankY = y + 20 + 47 - inputHeight;
            // Draw solid water block
            guiGraphics.fill(tankX, tankY, tankX + 16, tankY + inputHeight, 0xFF000000 | color);
            // Draw subtle glass highlight / reflection
            guiGraphics.fill(tankX, tankY, tankX + 4, tankY + inputHeight, 0x40FFFFFF);
        }
        guiGraphics.blit(TEXTURE, x + 11, y + 18, 177, 31, 20, 51);

        // 4. Render Output Tank fluid
        int outputAmount = menu.getOutputAmount();
        int outputCapacity = menu.getOutputCapacity();
        if (outputAmount > 0 && outputCapacity > 0) {
            int outputHeight = (int) ((long) outputAmount * 47 / outputCapacity);
            int color = getPurityColor(menu.getOutputPurity());
            int tankX = x + 147;
            int tankY = y + 20 + 47 - outputHeight;
            guiGraphics.fill(tankX, tankY, tankX + 16, tankY + outputHeight, 0xFF000000 | color);
            guiGraphics.fill(tankX, tankY, tankX + 4, tankY + outputHeight, 0x40FFFFFF);
        }
        guiGraphics.blit(TEXTURE, x + 145, y + 18, 177, 31, 20, 51);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFFF78034, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFFF78034, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Hover over Input Tank: x=11 to 31, y=18 to 69
        if (mouseX >= x + 11 && mouseX <= x + 31 && mouseY >= y + 18 && mouseY <= y + 69) {
            int amount = menu.getInputAmount();
            int capacity = menu.getInputCapacity();
            int purity = menu.getInputPurity();

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Lang.Gui.inputTank());
            tooltip.add(Component.literal(amount + " / " + capacity + " mB").withStyle(s -> s.withColor(0xAAAAAA)));
            if (amount > 0) {
                tooltip.add(WaterPurity.purityComponent(purity));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }

        // Hover over Output Tank: x=145 to 165, y=18 to 69
        if (mouseX >= x + 145 && mouseX <= x + 165 && mouseY >= y + 18 && mouseY <= y + 69) {
            int amount = menu.getOutputAmount();
            int capacity = menu.getOutputCapacity();
            int purity = menu.getOutputPurity();

            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Lang.Gui.outputTank());
            tooltip.add(Component.literal(amount + " / " + capacity + " mB").withStyle(s -> s.withColor(0xAAAAAA)));
            if (amount > 0) {
                tooltip.add(WaterPurity.purityComponent(purity));
            }
            guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
        }
    }

    private int getPurityColor(int purity) {
        return switch (purity) {
            case 0 -> 0x8A4825;  // dirty — brown
            case 1 -> 0x6E6266;  // slightly dirty — gray-brown
            case 2 -> 0x486C85;  // acceptable — blue-gray
            default -> 0x2864C8; // purified — bright blue
        };
    }
}
