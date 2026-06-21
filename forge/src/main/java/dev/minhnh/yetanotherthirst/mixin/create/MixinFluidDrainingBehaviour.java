package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.fluids.transfer.FluidDrainingBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When Create drains water from the world into its fluid system,
 * tag the resulting FluidStack with the world-position purity level.
 */
@Pseudo
@Mixin(value = FluidDrainingBehaviour.class, remap = false)
public abstract class MixinFluidDrainingBehaviour {

    @Inject(method = "getDrainableFluid", at = @At("RETURN"), remap = false, cancellable = true)
    public void getDrainableFluid(BlockPos rootPos, CallbackInfoReturnable<FluidStack> cir) {
        FluidDrainingBehaviour behaviour = (FluidDrainingBehaviour) (Object) this;
        FluidStack output = cir.getReturnValue();
        if (FluidHelper.isWater(output.getFluid())) {
            FluidPurityHelper.addPurity(output, WaterPurity.getBlockPurity(behaviour.getWorld(), rootPos));
            cir.setReturnValue(output);
        }
    }
}
