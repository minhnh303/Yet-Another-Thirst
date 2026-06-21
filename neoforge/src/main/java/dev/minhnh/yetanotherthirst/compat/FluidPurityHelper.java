package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;

public final class FluidPurityHelper {

    private FluidPurityHelper() {}

    public static boolean hasPurity(FluidStack fluid) {
        return fluid.hasTag() && fluid.getTag().contains("Purity");
    }

    public static int getPurity(FluidStack fluid) {
        if (!hasPurity(fluid)) return ThirstConfig.DEFAULT_PURITY;
        return fluid.getTag().getInt("Purity");
    }

    public static FluidStack addPurity(FluidStack fluid, int purity) {
        CompoundTag tag = fluid.getOrCreateTag();
        tag.putInt("Purity", purity);
        return fluid;
    }
}
