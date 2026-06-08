package dev.minhnh.yetanotherthirst.core.effect;

import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

/** Loader-agnostic effect references. Populated by each loader's effect registration. */
public final class ModEffects {

    public static Supplier<MobEffect> HYDRATION;
    public static Supplier<MobEffect> THIRSTY;

    private ModEffects() {}
}
