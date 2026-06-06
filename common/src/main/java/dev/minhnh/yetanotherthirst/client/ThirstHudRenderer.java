package dev.minhnh.yetanotherthirst.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

public final class ThirstHudRenderer {

    private static final ResourceLocation THIRST_ICONS = Constants.asResource("textures/gui/thirst_icons.png");
    private static final RandomSource RANDOM = RandomSource.create();

    private ThirstHudRenderer() {
    }

    public static boolean render(GuiGraphics guiGraphics, int width, int height, int rightHeight) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isAlive() || minecraft.options.hideGui) {
            return false;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.showVehicleHealth()) {
            return false;
        }

        ThirstState state = ThirstStorage.get(player);
        if (!state.isEnabled()) {
            return false;
        }

        RenderSystem.enableBlend();
        int left = width / 2 + 91 + ThirstConfig.HUD_X_OFFSET;
        int top = height - rightHeight + ThirstConfig.HUD_Y_OFFSET;
        int level = state.getThirst();

        for (int i = 0; i < 10; ++i) {
            int index = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;

            if (state.getQuenched() <= 0 && player.tickCount % (level * 3 + 1) == 0) {
                y = top + RANDOM.nextInt(3) - 1;
            }

            guiGraphics.blit(THIRST_ICONS, x, y, 0, 0, 9, 9, 25, 9);
            if (index < level) {
                guiGraphics.blit(THIRST_ICONS, x, y, 16, 0, 9, 9, 25, 9);
            } else if (index == level) {
                guiGraphics.blit(THIRST_ICONS, x, y, 8, 0, 9, 9, 25, 9);
            }
        }

        RenderSystem.disableBlend();
        return true;
    }
}
