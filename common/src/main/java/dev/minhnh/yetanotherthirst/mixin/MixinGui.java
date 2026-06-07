package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ThirstHudRenderer;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(
            method = "renderPlayerHealth",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V",
                    shift = At.Shift.AFTER),
            remap = false)
    private void yet_another_thirst$renderThirstAfterFood(GuiGraphics guiGraphics, CallbackInfo ci) {
        ThirstHudRenderer.render(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight(), 49);
    }

    @Redirect(
            method = "renderPlayerHealth",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
            ),
            remap = false)
    private void yet_another_thirst$shiftAirBubbles(GuiGraphics guiGraphics, ResourceLocation sprite, int x, int y, int width, int height) {
        if ("minecraft".equals(sprite.getNamespace()) &&
                ("hud/air".equals(sprite.getPath()) || "hud/air_bursting".equals(sprite.getPath())) &&
                yet_another_thirst$shouldShiftAir()) {
            guiGraphics.blitSprite(sprite, x, y - 10, width, height);
        } else {
            guiGraphics.blitSprite(sprite, x, y, width, height);
        }
    }

    private boolean yet_another_thirst$shouldShiftAir() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isAlive()) {
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
        return state.isEnabled();
    }
}
