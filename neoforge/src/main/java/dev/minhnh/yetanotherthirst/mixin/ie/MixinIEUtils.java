package dev.minhnh.yetanotherthirst.mixin.ie;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
            if (output != null && !output.isEmpty() && output.is(FluidTags.WATER)) {
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
