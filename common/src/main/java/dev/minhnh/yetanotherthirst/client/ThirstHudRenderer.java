package dev.minhnh.yetanotherthirst.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class ThirstHudRenderer {

    @Nonnull
    private static final ResourceLocation THIRST_ICONS = Constants.asResource("textures/gui/thirst_icons.png");
    private static final RandomSource RANDOM = RandomSource.create();

    private ThirstHudRenderer() {
    }

    @SuppressWarnings("deprecation")
    public static boolean render(GuiGraphics guiGraphics, int width, int height, int rightHeight) {

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isAlive() || minecraft.options.hideGui) {
            return false;
        }
        if (ThirstCompat.hidesThirstHud(player)) {
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
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = width / 2 + 91 + ThirstConfig.HUD_X_OFFSET;
        int top = height - rightHeight + ThirstConfig.HUD_Y_OFFSET;
        int level = state.getThirst();
        int quenched = state.getQuenched();

        // 1. Draw backgrounds
        for (int i = 0; i < 10; ++i) {
            int x = left - i * 8 - 9;
            int y = top;
            if (quenched <= 0 && player.tickCount % (level * 3 + 1) == 0) {
                y = top + RANDOM.nextInt(3) - 1;
            }
            guiGraphics.blit(THIRST_ICONS, x, y, 0, 0, 9, 9, 36, 36);
        }

        // 2. Draw exhaustion progress over background (Row 3, u=0, v=18)
        float exhaustion = state.getExhaustion();
        if (exhaustion > 0.0F) {
            float ratio = Math.min(1.0F, exhaustion / ThirstConfig.EXHAUSTION_LIMIT);
            int exhaustionWidth = (int) (ratio * 81.0F);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
            for (int i = 0; i < 10; ++i) {
                int x = left - i * 8 - 9;
                int xLeft = x;
                int xRight = x + 9;
                int iLeft = Math.max(xLeft, left - exhaustionWidth);
                int iRight = Math.min(xRight, left);
                if (iLeft < iRight) {
                    int w = iRight - iLeft;
                    int u = iLeft - xLeft;
                    guiGraphics.blit(THIRST_ICONS, iLeft, top, u, 18, w, 9, 36, 36);
                }
            }
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        // 3. Draw thirst droplets
        boolean isPoisoned = player.hasEffect(net.minecraft.world.effect.MobEffects.CONFUSION)
                || player.hasEffect(net.minecraft.world.effect.MobEffects.POISON);

        for (int i = 0; i < 10; ++i) {
            int index = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            if (quenched <= 0 && player.tickCount % (level * 3 + 1) == 0) {
                y = top + RANDOM.nextInt(3) - 1;
            }

            if (index < level) {
                guiGraphics.blit(THIRST_ICONS, x, y, isPoisoned ? 27 : 18, 0, 9, 9, 36, 36);
            } else if (index == level) {
                guiGraphics.blit(THIRST_ICONS, x, y, 9, 0, 9, 9, 36, 36);
            }
        }

        // 4. Draw quenched (saturation) borders (Row 2, u=0/9, v=9)
        for (int i = 0; i < 10; ++i) {
            int x = left - i * 8 - 9;
            int slotMin = i * 2;
            int slotMax = i * 2 + 2;
            if (quenched > slotMin) {
                if (quenched >= slotMax) {
                    guiGraphics.blit(THIRST_ICONS, x, top, 0, 9, 9, 9, 36, 36);
                } else {
                    guiGraphics.blit(THIRST_ICONS, x, top, 9, 9, 9, 9, 36, 36);
                }
            }
        }

        // 5. Draw item preview overlays (thirst and quenched)
        renderHeldItemPreview(guiGraphics, player, state, left, top);

        RenderSystem.disableBlend();
        return true;
    }

    private static void renderHeldItemPreview(GuiGraphics guiGraphics, LocalPlayer player, ThirstState state, int left, int top) {

        if (!ThirstCompat.showsAppleSkinHudPreview()) {
            return;
        }

        Optional<ThirstValue> value = ThirstValues.get(player.getMainHandItem());
        if (value.isEmpty()) {
            value = ThirstValues.get(player.getOffhandItem());
        }
        if (value.isEmpty()) {
            return;
        }

        int thirstRestored = value.get().thirst();
        int quenchedRestored = value.get().quenched();
        if (thirstRestored <= 0 && quenchedRestored <= 0) {
            return;
        }

        int currentThirst = state.getThirst();
        int currentQuenched = state.getQuenched();

        int overflow = Math.max(currentThirst + thirstRestored - ThirstConfig.MAX_THIRST, 0);
        if (!ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED) {
            overflow = 0;
            if (currentThirst >= ThirstConfig.MAX_THIRST) {
                quenchedRestored = 0;
            }
        }
        int previewThirst = Math.min(currentThirst + thirstRestored, ThirstConfig.MAX_THIRST);
        int previewQuenched = Math.min(currentQuenched + quenchedRestored + overflow, previewThirst);

        float alpha = 0.3F + 0.2F * (float) Math.sin(player.tickCount / 3.0);

        // Preview thirst level increase
        if (previewThirst > currentThirst) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            for (int i = 0; i < 10; ++i) {
                int index = i * 2 + 1;
                if (index <= currentThirst) {
                    continue;
                }
                int x = left - i * 8 - 9;
                if (index < previewThirst) {
                    guiGraphics.blit(THIRST_ICONS, x, top, 18, 0, 9, 9, 36, 36);
                } else if (index == previewThirst) {
                    guiGraphics.blit(THIRST_ICONS, x, top, 9, 0, 9, 9, 36, 36);
                }
            }
        }

        // Preview quenched level increase
        if (previewQuenched > currentQuenched) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            for (int i = 0; i < 10; ++i) {
                int x = left - i * 8 - 9;
                int slotMin = i * 2;
                int slotMax = i * 2 + 2;

                if (currentQuenched >= slotMax) {
                    continue;
                }

                if (previewQuenched > slotMin) {
                    if (previewQuenched >= slotMax) {
                        guiGraphics.blit(THIRST_ICONS, x, top, 0, 9, 9, 9, 36, 36);
                    } else {
                        guiGraphics.blit(THIRST_ICONS, x, top, 9, 9, 9, 9, 36, 36);
                    }
                }
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
