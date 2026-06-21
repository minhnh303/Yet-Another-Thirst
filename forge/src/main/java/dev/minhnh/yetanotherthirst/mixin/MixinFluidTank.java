package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FluidTank.class, remap = false)
public abstract class MixinFluidTank {

    @Shadow protected FluidStack fluid;
    @Shadow protected int capacity;
    @Shadow public abstract boolean isFluidValid(FluidStack stack);
    @Shadow protected abstract void onContentsChanged();

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true, remap = false)
    private void onFill(FluidStack resource, IFluidHandler.FluidAction action, CallbackInfoReturnable<Integer> cir) {
        if (resource == null || resource.isEmpty() || !isFluidValid(resource)) {
            cir.setReturnValue(0);
            return;
        }

        if (fluid == null || fluid.isEmpty()) {
            return;
        }

        if (fluid.getFluid() != resource.getFluid()) {
            return;
        }

        CompoundTag tag1 = fluid.getTag();
        CompoundTag tag2 = resource.getTag();
        boolean tagsMatch = true;
        if (tag1 != null || tag2 != null) {
            CompoundTag copy1 = tag1 != null ? tag1.copy() : new CompoundTag();
            CompoundTag copy2 = tag2 != null ? tag2.copy() : new CompoundTag();
            copy1.remove("Purity");
            copy2.remove("Purity");
            if (!copy1.equals(copy2)) {
                tagsMatch = false;
            }
        }

        if (!tagsMatch) {
            return;
        }

        int currentAmount = fluid.getAmount();
        int space = capacity - currentAmount;
        if (space <= 0) {
            cir.setReturnValue(0);
            return;
        }

        int toFill = Math.min(space, resource.getAmount());
        if (action.execute()) {
            int p1 = FluidPurityHelper.getPurity(fluid);
            int p2 = FluidPurityHelper.getPurity(resource);
            int blendedPurity = Math.min(p1, p2);
            fluid.setAmount(currentAmount + toFill);
            FluidPurityHelper.addPurity(fluid, blendedPurity);
            onContentsChanged();
        }
        cir.setReturnValue(toFill);
    }
}
