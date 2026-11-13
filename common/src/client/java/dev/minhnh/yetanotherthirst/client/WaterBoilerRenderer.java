package dev.minhnh.yetanotherthirst.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.minhnh.yetanotherthirst.core.block.IWaterBoiler;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class WaterBoilerRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");

    public WaterBoilerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        if (!(blockEntity instanceof IWaterBoiler boiler) || !boiler.isUpper()) {
            return;
        }

        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        IWaterBoiler lower = boiler.getLowerEntity(level);
        if (lower == null) {
            return;
        }

        Direction facing = boiler.getFacing();
        if (facing == null) {
            return;
        }

        int inputAmount = lower.getInputAmount();
        int inputCapacity = lower.getInputCapacity();
        int inputPurity = lower.getInputPurity();

        int outputAmount = lower.getOutputAmount();
        int outputCapacity = lower.getOutputCapacity();
        int outputPurity = lower.getOutputPurity();

        // 10 pixels total height
        float inputRatio = inputCapacity > 0 ? (float) inputAmount / inputCapacity : 0.0F;
        float outputRatio = outputCapacity > 0 ? (float) outputAmount / outputCapacity : 0.0F;

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(WHITE_TEXTURE));

        poseStack.pushPose();
        // Translate to block center, rotate according to facing, and translate back
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // Render Input Bar (Left side on the screen, starts at X=4, Y=3)
        renderSingleBar(poseStack, consumer, 4.0F / 16.0F, 3.0F / 16.0F, inputRatio, inputPurity, combinedLight);

        // Render Output Bar (Right side on the screen, starts at X=10, Y=3)
        renderSingleBar(poseStack, consumer, 10.0F / 16.0F, 3.0F / 16.0F, outputRatio, outputPurity, combinedLight);

        poseStack.popPose();
    }

    private void renderSingleBar(PoseStack poseStack, VertexConsumer consumer, float leftX, float topY, float fillRatio, int purity, int combinedLight) {
        float pixelSize = 1.0F / 16.0F;

        // Calculate water height in pixels (0 to 10)
        int waterPixels = Math.round(fillRatio * 10.0F);
        int emptyPixels = 10 - waterPixels;

        PurityColorPalette palette = getPalette(purity);

        // --- 1. Render empty part (from top Y down to water level) ---
        float currentY = topY;
        for (int i = 0; i < emptyPixels; i++) {
            int colorLeft;
            int colorRight;
            if (i < 2) {
                colorLeft = palette.emptyTopLeft;
                colorRight = palette.emptyTopRight;
            } else {
                colorLeft = palette.emptyTopRight;
                colorRight = palette.emptyBottomRight;
            }
            drawPixelRow(poseStack, consumer, leftX, currentY, colorLeft, colorRight, combinedLight);
            currentY += pixelSize;
        }

        // --- 2. Render water/fluid part (from currentY to topY + totalHeight) ---
        for (int i = 0; i < waterPixels; i++) {
            int colorLeft;
            int colorRight;
            if (i == 0) {
                colorLeft = palette.highlight;
                colorRight = palette.base;
            } else {
                colorLeft = palette.base;
                colorRight = palette.shadow;
            }
            drawPixelRow(poseStack, consumer, leftX, currentY, colorLeft, colorRight, combinedLight);
            currentY += pixelSize;
        }
    }

    private void drawPixelRow(PoseStack poseStack, VertexConsumer consumer, float leftX, float currentY, int colorLeft, int colorRight, int combinedLight) {
        float pixelSize = 1.0F / 16.0F;

        // Draw Left column of the pixel row
        float col1_x1 = 1.0F - leftX;
        float col1_x2 = 1.0F - (leftX + pixelSize);
        drawQuad(poseStack, consumer, col1_x1, col1_x2, currentY, currentY + pixelSize, colorLeft, combinedLight);

        // Draw Right column of the pixel row
        float col2_x1 = 1.0F - (leftX + pixelSize);
        float col2_x2 = 1.0F - (leftX + 2.0F * pixelSize);
        drawQuad(poseStack, consumer, col2_x1, col2_x2, currentY, currentY + pixelSize, colorRight, combinedLight);
    }

    private void drawQuad(PoseStack poseStack, VertexConsumer consumer, float x1, float x2, float y1_screen, float y2_screen, int color, int combinedLight) {
        float y1 = 1.0F - y1_screen;
        float y2 = 1.0F - y2_screen;

        float z = -0.001F;

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        var matrix = poseStack.last().pose();

        consumer.addVertex(matrix, x2, y2, z).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, x1, y2, z).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, x1, y1, z).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, x2, y1, z).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(combinedLight).setNormal(0.0F, 0.0F, -1.0F);
    }

    private static class PurityColorPalette {
        final int base;
        final int highlight;
        final int shadow;
        final int emptyTopLeft;
        final int emptyTopRight;
        final int emptyBottomRight;

        PurityColorPalette(int base, int highlight, int shadow, int emptyTopLeft, int emptyTopRight, int emptyBottomRight) {
            this.base = base;
            this.highlight = highlight;
            this.shadow = shadow;
            this.emptyTopLeft = emptyTopLeft;
            this.emptyTopRight = emptyTopRight;
            this.emptyBottomRight = emptyBottomRight;
        }
    }

    private static PurityColorPalette getPalette(int purity) {
        return switch (purity) {
            case 0 -> new PurityColorPalette(
                0xFF8A4825, // base
                0xFFBC6E44, // highlight
                0xFF67361C, // shadow
                0xFFF8F2EE, // emptyTopLeft
                0xFFE0D0C6, // emptyTopRight
                0xFFC4B0A2  // emptyBottomRight
            );
            case 1 -> new PurityColorPalette(
                0xFF6E6266, // base
                0xFFA09196, // highlight
                0xFF52494C, // shadow
                0xFFF6F4F4, // emptyTopLeft
                0xFFD8D2D4, // emptyTopRight
                0xFFBAB2B4  // emptyBottomRight
            );
            case 2 -> new PurityColorPalette(
                0xFF486C85, // base
                0xFF78A0BE, // highlight
                0xFF365164, // shadow
                0xFFF0F4F8, // emptyTopLeft
                0xFFC8D4DC, // emptyTopRight
                0xFFAAB9C3  // emptyBottomRight
            );
            default -> new PurityColorPalette(
                0xFF2864C8, // base (40, 100, 200)
                0xFF64AAFF, // highlight (100, 170, 255)
                0xFF1E50AA, // shadow (30, 80, 170)
                0xFFEBFAFF, // emptyTopLeft (235, 250, 255)
                0xFFB4DCF0, // emptyTopRight (180, 220, 240)
                0xFF8CB4C8  // emptyBottomRight (140, 180, 200)
            );
        };
    }
}
