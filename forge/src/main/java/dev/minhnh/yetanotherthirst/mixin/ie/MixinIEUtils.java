package dev.minhnh.yetanotherthirst.mixin.ie;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks into IE's central fluid-block drain utility so that any water picked up
 * by an IE pump is tagged with the position-derived contamination level.
 * Target: blusunrize.immersiveengineering.common.util.Utils.drainFluidBlock
 */
@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.common.util.Utils", remap = false)
public class MixinIEUtils {

    private static final ThreadLocal<Integer> PURITY_CACHE = new ThreadLocal<>();

    @Inject(method = "drainFluidBlock", at = @At("HEAD"), require = 0, remap = false)
    private static void onDrainFluidBlockHead(Level world, BlockPos pos, FluidAction action,
                                              CallbackInfoReturnable<FluidStack> cir) {
        PURITY_CACHE.set(WaterPurity.getBlockPurity(world, pos));
    }

    @Inject(method = "drainFluidBlock", at = @At("RETURN"), require = 0, remap = false,
            cancellable = true)
    private static void onDrainFluidBlock(Level world, BlockPos pos, FluidAction action,
                                          CallbackInfoReturnable<FluidStack> cir) {
        try {
            FluidStack output = cir.getReturnValue();
            if (output != null && !output.isEmpty() && output.getFluid().is(FluidTags.WATER)) {
                Integer cachedPurity = PURITY_CACHE.get();
                int purity = (cachedPurity != null) ? cachedPurity : WaterPurity.getBlockPurity(world, pos);
                FluidPurityHelper.addPurity(output, purity);
                cir.setReturnValue(output);
            }
        } finally {
            PURITY_CACHE.remove();
        }
    }
}
