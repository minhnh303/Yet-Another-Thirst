package dev.minhnh.yetanotherthirst.mixin.create;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.fluids.tank.FluidTankBlockEntity", remap = false)
public class MixinFluidTankBlockEntity {

    @SuppressWarnings("null")
    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), remap = false)
    private void onAddToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        try {
            Object controller = this.getClass().getMethod("getControllerBE").invoke(this);
            if (controller == null) return;

            Object tankInventory = controller.getClass().getMethod("getTankInventory").invoke(controller);
            if (!(tankInventory instanceof IFluidHandler handler)) return;

            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (fluid.isEmpty() || !fluid.is(FluidTags.WATER)) continue;

                int purity = FluidPurityHelper.getPurity(fluid);
                tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                        .append(Lang.Jade.waterPurity(WaterPurity.purityComponent(purity))));
                break;
            }
        } catch (Throwable ignored) {
        }
    }
}
