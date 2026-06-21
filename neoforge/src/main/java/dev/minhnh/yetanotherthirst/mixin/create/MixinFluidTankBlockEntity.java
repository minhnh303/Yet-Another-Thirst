package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(value = FluidTankBlockEntity.class, remap = false)
public class MixinFluidTankBlockEntity {

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), remap = false)
    private void onAddToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        FluidTankBlockEntity tank = (FluidTankBlockEntity) (Object) this;
        FluidTankBlockEntity controller = tank.getControllerBE();
        if (controller == null) {
            return;
        }
        net.minecraftforge.fluids.capability.templates.FluidTank tankInventory = controller.getTankInventory();
        if (tankInventory == null) {
            return;
        }
        FluidStack fluidStack = tankInventory.getFluid();
        if (fluidStack == null || fluidStack.isEmpty()) {
            return;
        }
        if (FluidHelper.isWater(fluidStack.getFluid())) {
            int purity = FluidPurityHelper.getPurity(fluidStack);
            tooltip.add(Lang.Jade.waterPurity(WaterPurity.purityComponent(purity)));
        }
    }
}
