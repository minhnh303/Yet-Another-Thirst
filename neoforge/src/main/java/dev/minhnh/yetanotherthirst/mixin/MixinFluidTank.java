package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Allows water with different purity levels to merge in any FluidTank.
 * NeoForge's FluidStack.isSameFluidSameComponents() treats water with different
 * CUSTOM_DATA (purity) as incompatible, causing fill() to silently return 0.
 * PurityBlendingTank (FilterFrameBlockEntity) already overrides fill() itself,
 * so this injection never fires for it.
 */
@Mixin(FluidTank.class)
public abstract class MixinFluidTank {

    @Shadow protected FluidStack fluid;
    @Shadow protected int capacity;
    @Shadow public abstract boolean isFluidValid(FluidStack stack);
    @Shadow protected abstract void onContentsChanged();

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true)
    private void yat$onFill(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
        if (fluid.isEmpty() || resource.isEmpty()) return;
        if (!fluid.is(FluidTags.WATER) || !resource.is(FluidTags.WATER)) return;
        if (!fluid.getFluid().isSame(resource.getFluid())) return;

        boolean resourceHasPurity = FluidPurityHelper.hasPurity(resource);
        boolean fluidHasPurity = FluidPurityHelper.hasPurity(fluid);
        int incomingPurity = resourceHasPurity ? FluidPurityHelper.getPurity(resource) : 0;
        int existingPurity = FluidPurityHelper.getPurity(fluid);

        // Identical purity state — isSameFluidSameComponents() will pass and normal fill works
        if (fluidHasPurity == resourceHasPurity && existingPurity == incomingPurity) return;

        if (!isFluidValid(resource)) {
            cir.setReturnValue(0);
            cir.cancel();
            return;
        }

        int space = capacity - fluid.getAmount();
        if (space <= 0) {
            cir.setReturnValue(0);
            cir.cancel();
            return;
        }

        int toFill = Math.min(space, resource.getAmount());
        int blendedPurity = Math.min(existingPurity, incomingPurity);

        if (action.execute()) {
            fluid.setAmount(fluid.getAmount() + toFill);
            FluidPurityHelper.addPurity(fluid, blendedPurity);
            onContentsChanged();
        }

        cir.setReturnValue(toFill);
        cir.cancel();
    }
}
