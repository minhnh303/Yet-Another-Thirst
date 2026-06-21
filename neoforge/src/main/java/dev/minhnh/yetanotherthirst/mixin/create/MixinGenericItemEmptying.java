package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When Create empties a water container into a fluid tank,
 * transfer the item's purity to the resulting FluidStack.
 */
@Pseudo
@Mixin(value = GenericItemEmptying.class, remap = false)
public class MixinGenericItemEmptying {

    @Inject(method = "emptyItem", at = @At("RETURN"), cancellable = true, remap = false)
    private static void emptyItem(Level world, ItemStack stack, boolean simulate,
                                  CallbackInfoReturnable<Pair<FluidStack, ItemStack>> cir) {
        Pair<FluidStack, ItemStack> output = cir.getReturnValue();
        if (WaterPurity.hasPurity(stack)) {
            FluidStack fluidStack = output.getFirst();
            if (fluidStack.isEmpty()) return;
            FluidPurityHelper.addPurity(fluidStack, WaterPurity.getPurity(stack));
            cir.setReturnValue(Pair.of(fluidStack, output.getSecond()));
        }
    }
}
