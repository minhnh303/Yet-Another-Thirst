package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When any Forge-based fluid handler (including IE) fills a water container from
 * a purity-tagged FluidStack, transfer the purity to the resulting item.
 */
@Mixin(value = FluidBucketWrapper.class, remap = false)
public abstract class MixinFluidBucketWrapper {

    @Shadow @Mutable protected ItemStack container;

    @Inject(method = "fill", at = @At("RETURN"), remap = false)
    private void onFill(FluidStack resource, IFluidHandler.FluidAction action,
                        CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() > 0 && action.execute()
                && FluidPurityHelper.hasPurity(resource)
                && WaterPurity.isWaterFilledContainer(container)) {
            WaterPurity.addPurity(container, FluidPurityHelper.getPurity(resource));
        }
    }

    @Inject(method = "drain(Lnet/minecraftforge/fluids/FluidStack;Lnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("RETURN"), remap = false, cancellable = true)
    private void onDrain(FluidStack resource, IFluidHandler.FluidAction action,
                         CallbackInfoReturnable<FluidStack> cir) {
        FluidStack result = cir.getReturnValue();
        if (result != null && !result.isEmpty() && WaterPurity.hasPurity(container)) {
            FluidPurityHelper.addPurity(result, WaterPurity.getPurity(container));
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "drain(ILnet/minecraftforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/minecraftforge/fluids/FluidStack;", at = @At("RETURN"), remap = false, cancellable = true)
    private void onDrain(int maxDrain, IFluidHandler.FluidAction action,
                         CallbackInfoReturnable<FluidStack> cir) {
        FluidStack result = cir.getReturnValue();
        if (result != null && !result.isEmpty() && WaterPurity.hasPurity(container)) {
            FluidPurityHelper.addPurity(result, WaterPurity.getPurity(container));
            cir.setReturnValue(result);
        }
    }
}
