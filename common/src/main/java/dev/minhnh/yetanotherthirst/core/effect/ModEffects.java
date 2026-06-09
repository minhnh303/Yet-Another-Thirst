package dev.minhnh.yetanotherthirst.core.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

import java.util.function.Supplier;

/** Loader-agnostic effect references. Populated by each loader's effect registration. */
public final class ModEffects {

    public static Supplier<Holder<MobEffect>> HYDRATION;
    public static Supplier<Holder<MobEffect>> THIRSTY;

    private ModEffects() {}
}
