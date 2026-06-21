package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * When Create fills a water container from a fluid tank,
 * transfer the FluidStack's purity to the resulting item.
 */
@Pseudo
@Mixin(value = GenericItemFilling.class, remap = false)
public class MixinGenericItemFilling {

    @Inject(method = "fillItem", at = @At("RETURN"), cancellable = true, remap = false)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack,
                                 FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = cir.getReturnValue();
        if (FluidPurityHelper.hasPurity(availableFluid) && WaterPurity.isWaterFilledContainer(output)) {
            WaterPurity.addPurity(output, FluidPurityHelper.getPurity(availableFluid));
            cir.setReturnValue(output);
        }
    }
}
