package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.client.TooltipStackTracker;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

/**
 * Appends the AppleSkin-style thirst tooltip component to item tooltips on Fabric
 * (Forge/NeoForge use RenderTooltipEvent.GatherComponents). The item is the one whose
 * tooltip lines were just built, captured by ItemTooltipCallback into TooltipStackTracker.
 */
@Mixin(GuiGraphics.class)
public class FabricMixinGuiGraphics {

    @ModifyArg(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V"),
            index = 1)
    private List<ClientTooltipComponent> yet_another_thirst$addThirstComponent(List<ClientTooltipComponent> components) {
        ItemStack stack = TooltipStackTracker.get();
        TooltipStackTracker.clear();
        if (stack.isEmpty() || !ThirstCompat.showsAppleSkinThirstTooltip()) {
            return components;
        }
        ThirstValues.get(stack).ifPresent(value ->
                components.add(new ClientThirstTooltipComponent(new ThirstTooltipComponent(value))));
        return components;
    }
}
