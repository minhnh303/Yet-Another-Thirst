package dev.minhnh.yetanotherthirst.mixin.ie;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.common.util.inventory.MultiFluidTank", remap = false)
public abstract class MixinMultiFluidTank {

    @Shadow public List<FluidStack> fluids;
    @Shadow public abstract int getCapacity();
    @Shadow public abstract boolean isFluidValid(FluidStack stack);

    @Inject(
            method = "fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I",
            at = @At("HEAD"), cancellable = true, require = 0, remap = false
    )
    private void yat$onFill(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
        if (fluids == null || fluids.isEmpty() || resource.isEmpty()) return;
        if (!resource.is(FluidTags.WATER)) return;

        for (FluidStack existing : fluids) {
            if (!existing.is(FluidTags.WATER)) continue;
            if (!existing.getFluid().isSame(resource.getFluid())) continue;

            int existingPurity = FluidPurityHelper.getPurity(existing);
            int incomingPurity = FluidPurityHelper.getPurity(resource);
            boolean existingHasPurity = FluidPurityHelper.hasPurity(existing);
            boolean resourceHasPurity = FluidPurityHelper.hasPurity(resource);

            // Identical purity state — isSameFluidSameComponents() will pass, let normal fill handle it
            if (existingHasPurity == resourceHasPurity && existingPurity == incomingPurity) return;

            if (!isFluidValid(resource)) {
                cir.setReturnValue(0);
                cir.cancel();
                return;
            }

            int totalFilled = fluids.stream().mapToInt(FluidStack::getAmount).sum();
            int space = getCapacity() - totalFilled;
            if (space <= 0) {
                cir.setReturnValue(0);
                cir.cancel();
                return;
            }

            int toFill = Math.min(space, resource.getAmount());
            int blendedPurity = Math.min(existingPurity, incomingPurity);

            if (action.execute()) {
                existing.setAmount(existing.getAmount() + toFill);
                FluidPurityHelper.addPurity(existing, blendedPurity);
            }

            cir.setReturnValue(toFill);
            cir.cancel();
            return;
        }
    }
}
