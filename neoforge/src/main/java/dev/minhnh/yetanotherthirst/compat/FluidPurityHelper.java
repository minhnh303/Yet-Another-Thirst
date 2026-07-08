package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;

public final class FluidPurityHelper {

    private FluidPurityHelper() {}

    public static boolean hasPurity(FluidStack fluid) {
        if (fluid.isEmpty()) return false;
        CustomData data = fluid.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().contains("Purity");
    }

    public static int getPurity(FluidStack fluid) {
        if (!hasPurity(fluid)) return ThirstConfig.DEFAULT_PURITY;
        CustomData data = fluid.get(DataComponents.CUSTOM_DATA);
        if (data == null) return ThirstConfig.DEFAULT_PURITY;
        return data.copyTag().getInt("Purity");
    }

    public static FluidStack addPurity(FluidStack fluid, int purity) {
        CustomData existing = fluid.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = existing.copyTag();
        tag.putInt("Purity", purity);
        fluid.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return fluid;
    }
}
