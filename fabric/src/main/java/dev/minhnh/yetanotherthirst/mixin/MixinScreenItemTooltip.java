package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.client.TooltipStackTracker;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinScreenItemTooltip {

    @Shadow
    abstract void renderTooltipInternal(Font font, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner);

    @Redirect(
        method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;II)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V"
        )
    )
    private void yat_appendThirstComponent(
        GuiGraphics guiGraphics,
        Font font,
        List<ClientTooltipComponent> components,
        int x,
        int y,
        ClientTooltipPositioner positioner
    ) {
        ItemStack stack = TooltipStackTracker.get();
        if (stack != null && !stack.isEmpty() && ThirstCompat.showsAppleSkinThirstTooltip()) {
            ThirstValues.get(stack)
                .filter(v -> v.thirst() > 0 || v.quenched() > 0)
                .ifPresent(thirstValue -> {
                    int insertIndex = -1;

                    // 1. Try to find the AppleSkin FoodOverlay component to insert right after it
                    for (int i = 0; i < components.size(); i++) {
                        if (yat_isAppleSkinComponent(components.get(i))) {
                            insertIndex = i + 1;
                            break;
                        }
                    }

                    // 2. If no AppleSkin component was found, insert before the mod name line
                    if (insertIndex == -1) {
                        insertIndex = components.size();
                        String modName = yat_getItemModName(stack);
                        if (!modName.isEmpty()) {
                            for (int i = components.size() - 1; i >= 0; i--) {
                                ClientTooltipComponent c = components.get(i);
                                String text = yat_getTooltipText(c);
                                if (text.trim().equalsIgnoreCase(modName)) {
                                    insertIndex = i;
                                    break;
                                }
                            }
                        }
                    }

                    components.add(insertIndex, ClientTooltipComponent.create(new ThirstTooltipComponent(thirstValue)));
                });
        }
        this.renderTooltipInternal(font, components, x, y, positioner);
        TooltipStackTracker.clear();
    }

    private static boolean yat_isAppleSkinComponent(ClientTooltipComponent component) {
        return component.getClass().getName().endsWith("TooltipOverlayHandler$FoodOverlay");
    }

    private static String yat_getTooltipText(ClientTooltipComponent component) {
        try {
            for (java.lang.reflect.Field field : component.getClass().getDeclaredFields()) {
                if (net.minecraft.util.FormattedCharSequence.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    net.minecraft.util.FormattedCharSequence sequence = (net.minecraft.util.FormattedCharSequence) field.get(component);
                    if (sequence != null) {
                        StringBuilder builder = new StringBuilder();
                        sequence.accept((index, style, codePoint) -> {
                            builder.appendCodePoint(codePoint);
                            return true;
                        });
                        return builder.toString();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private static String yat_getItemModName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "";
        }
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return "";
        }
        String namespace = id.getNamespace();
        return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer(namespace)
                .map(container -> container.getMetadata().getName())
                .orElse(namespace);
    }
}
