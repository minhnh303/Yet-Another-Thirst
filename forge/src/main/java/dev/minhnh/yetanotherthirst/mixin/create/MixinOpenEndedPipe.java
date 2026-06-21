package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When Create's open-ended pipe picks up water from the world,
 * tag the FluidStack with the purity level derived from the block position.
 */
@Pseudo
@Mixin(value = OpenEndedPipe.class, remap = false)
public class MixinOpenEndedPipe {

    @Unique
    private int yet_another_thirst$cachedPurity = -1;

    @Inject(method = "removeFluidFromSpace", at = @At("HEAD"), remap = false)
    private void preRemoveFluidFromSpace(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        OpenEndedPipe pipe = (OpenEndedPipe) (Object) this;
        if (pipe.getWorld() != null && pipe.getOutputPos() != null) {
            this.yet_another_thirst$cachedPurity = WaterPurity.getBlockPurity(pipe.getWorld(), pipe.getOutputPos());
        } else {
            this.yet_another_thirst$cachedPurity = -1;
        }
    }

    @Inject(method = "removeFluidFromSpace", at = @At("RETURN"), cancellable = true, remap = false)
    private void removeFluidFromSpace(boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        OpenEndedPipe pipe = (OpenEndedPipe) (Object) this;
        FluidStack returned = cir.getReturnValue();
        if (returned != null && !returned.isEmpty() && FluidHelper.isWater(returned.getFluid())) {
            int purity = this.yet_another_thirst$cachedPurity != -1 ? this.yet_another_thirst$cachedPurity : WaterPurity.getBlockPurity(pipe.getWorld(), pipe.getOutputPos());
            FluidPurityHelper.addPurity(returned, purity);
            cir.setReturnValue(returned);
        }
    }
}
