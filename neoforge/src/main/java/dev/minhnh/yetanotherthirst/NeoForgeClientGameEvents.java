package dev.minhnh.yetanotherthirst;

import com.mojang.datafixers.util.Either;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Optional;

public final class NeoForgeClientGameEvents {

    private NeoForgeClientGameEvents() {}

    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!ThirstCompat.showsAppleSkinThirstTooltip()) {
            return;
        }
        Optional<ThirstValue> value = ThirstValues.get(event.getItemStack());
        value.ifPresent(thirstValue -> {
            event.getTooltipElements().add(Either.right(new ThirstTooltipComponent(thirstValue)));
        });
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.AIR_LEVEL.equals(event.getName())) {
            if (shouldShiftAir()) {
                event.getGuiGraphics().pose().pushPose();
                event.getGuiGraphics().pose().translate(0, -10, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPost(RenderGuiLayerEvent.Post event) {
        if (VanillaGuiLayers.AIR_LEVEL.equals(event.getName())) {
            if (shouldShiftAir()) {
                event.getGuiGraphics().pose().popPose();
            }
        }
    }

    private static boolean shouldShiftAir() {
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
