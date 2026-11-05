package dev.minhnh.yetanotherthirst.client;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class ThirstHudRenderer {

    @Nonnull
    private static final Identifier THIRST_ICONS = Constants.THIRST_ICONS;
    private static final RandomSource RANDOM = RandomSource.create();

    private ThirstHudRenderer() {
    }

    /**
     * Mirrors the guard checks at the top of {@link #render}, exposed so loaders without a shared
     * HUD-height accumulator (Fabric) can decide ahead of time whether the thirst bar will draw
     * this frame, e.g. to know whether to make room for it next to other HUD elements.
     */
    public static boolean shouldRender(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator() || minecraft.gui.hud.isHidden()) {
            return false;
        }
        if (ThirstCompat.hidesThirstHud(player)) {
            return false;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle != null && vehicle.showVehicleHealth()) {
            return false;
        }

        return ThirstStorage.get(player).isEnabled();
    }

    public static boolean render(GuiGraphicsExtractor extractor, int width, int height, int rightHeight) {

        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return false;
        }
        LocalPlayer player = minecraft.player;
        ThirstState state = ThirstStorage.get(player);

        int left = width / 2 + 91 + ThirstConfig.HUD_X_OFFSET;
        int top = height - rightHeight + ThirstConfig.HUD_Y_OFFSET;
        int level = state.getThirst();
        int quenched = state.getQuenched();

        // 1. Draw exhaustion progress over background (Row 2, u=27, v=9)
        float exhaustion = state.getExhaustion();
        if (exhaustion > 0.0F) {
            float ratio = Math.min(1.0F, exhaustion / ThirstConfig.EXHAUSTION_LIMIT);
            int exhaustionWidth = (int) (ratio * 81.0F);
            int exhaustionColor = 0xC0FFFFFF; // 75% alpha
            for (int i = 0; i < 10; ++i) {
                int x = left - i * 8 - 9;
                int xLeft = x;
                int xRight = x + 9;
                int iLeft = Math.max(xLeft, left - exhaustionWidth);
                int iRight = Math.min(xRight, left);
                if (iLeft < iRight) {
                    int w = iRight - iLeft;
                    int u = iLeft - xLeft;
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, iLeft, top, (float) (27 + u), 9.0F, w, 9, 36, 25, exhaustionColor);
                }
            }
        }

        // 2. Draw backgrounds
        for (int i = 0; i < 10; ++i) {
            int index = i * 2 + 1;
            if (index > level) {
                int x = left - i * 8 - 9;
                int y = top;
                if (quenched <= 0 && player.tickCount % (level * 3 + 1) == 0) {
                    y = top + RANDOM.nextInt(3) - 1;
                }
                extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, y, 0.0F, 0.0F, 9, 9, 36, 25);
            }
        }

        // 3. Draw thirst droplets
        boolean isPoisoned = player.hasEffect(MobEffects.NAUSEA)
                || player.hasEffect(MobEffects.POISON);

        for (int i = 0; i < 10; ++i) {
            int index = i * 2 + 1;
            int x = left - i * 8 - 9;
            int y = top;
            if (quenched <= 0 && player.tickCount % (level * 3 + 1) == 0) {
                y = top + RANDOM.nextInt(3) - 1;
            }

            if (index < level) {
                extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, y, isPoisoned ? 27.0F : 18.0F, 0.0F, 9, 9, 36, 25);
            } else if (index == level) {
                extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, y, 9.0F, 0.0F, 9, 9, 36, 25);
            }
        }

        // 4. Draw quenched (saturation) borders (Row 2, u=0/9, v=9)
        for (int i = 0; i < 10; ++i) {
            int x = left - i * 8 - 9;
            int slotMin = i * 2;
            int slotMax = i * 2 + 2;
            if (quenched > slotMin) {
                if (quenched >= slotMax) {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 0.0F, 9.0F, 9, 9, 36, 25);
                } else {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 9.0F, 9.0F, 9, 9, 36, 25);
                }
            }
        }

        // 5. Draw item preview overlays (thirst and quenched)
        renderHeldItemPreview(extractor, player, state, left, top);

        // 6. Draw EnvironmentZ temperature text next to thermometer icon
        renderEnvironmentzTemperature(extractor, minecraft, player, width, height);

        return true;
    }

    private static void renderEnvironmentzTemperature(GuiGraphicsExtractor extractor, Minecraft minecraft, LocalPlayer player, int width, int height) {
        if (!ThirstConfig.COMPAT_ENVIRONMENTZ
                || !ThirstConfig.ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT
                || !ThirstCompat.isEnvironmentzThermometerVisible()) {
            return;
        }

        int temp;
        if ("THERMOMETER".equalsIgnoreCase(ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE)
                || "ENVIRONMENT".equalsIgnoreCase(ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE)) {
            temp = ThirstCompat.getEnvironmentzThermometerTemperature(player);
        } else {
            temp = ThirstCompat.getEnvironmentzPlayerTemperature(player);
        }

        String unit = ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT != null ? ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT : "";
        String text = temp + unit;

        int iconX = (width / 2) - ThirstCompat.getEnvironmentzThermometerIconX();
        int iconY = height - ThirstCompat.getEnvironmentzThermometerIconY();

        int textX = iconX + 18 + ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET;
        int textY = iconY + 11 + ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET;

        extractor.text(minecraft.font, text, textX, textY, ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR, true);
    }

    private static void renderHeldItemPreview(GuiGraphicsExtractor extractor, LocalPlayer player, ThirstState state, int left, int top) {

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
        int alphaInt = Math.max(0, Math.min(255, (int) (alpha * 255.0F)));
        int color = (alphaInt << 24) | 0x00FFFFFF;

        // Preview thirst level increase
        if (previewThirst > currentThirst) {
            for (int i = 0; i < 10; ++i) {
                int index = i * 2 + 1;
                if (index <= currentThirst) {
                    continue;
                }
                int x = left - i * 8 - 9;
                if (index < previewThirst) {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 18.0F, 0.0F, 9, 9, 36, 25, color);
                } else if (index == previewThirst) {
                    extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 9.0F, 0.0F, 9, 9, 36, 25, color);
                }
            }
        }

        // Preview quenched level increase
        if (previewQuenched > currentQuenched) {
            for (int i = 0; i < 10; ++i) {
                int x = left - i * 8 - 9;
                int slotMin = i * 2;
                int slotMax = i * 2 + 2;

                if (currentQuenched >= slotMax) {
                    continue;
                }

                if (previewQuenched > slotMin) {
                    if (previewQuenched >= slotMax) {
                        extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 0.0F, 9.0F, 9, 9, 36, 25, color);
                    } else {
                        extractor.blit(RenderPipelines.GUI_TEXTURED, THIRST_ICONS, x, top, 9.0F, 9.0F, 9, 9, 36, 25, color);
                    }
                }
            }
        }
    }
}
