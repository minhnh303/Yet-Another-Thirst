package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.HydrationEffect;
import dev.minhnh.yetanotherthirst.core.effect.ModEffects;
import dev.minhnh.yetanotherthirst.core.effect.ThirstyEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class FabricEffects {

    static Holder<MobEffect> THIRSTY;
    static Holder<MobEffect> HYDRATION;

    static void register() {

        THIRSTY = reg("thirsty", new ThirstyEffect());
        HYDRATION = reg("hydration", new HydrationEffect());

        bindToCommon();
    }

    private static <T extends MobEffect> Holder<T> reg(String name, T effect) {

        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Constants.asResource(name), effect);
    }

    static void bindToCommon() {

        ModEffects.THIRSTY = () -> THIRSTY;
        ModEffects.HYDRATION = () -> HYDRATION;
    }

    private FabricEffects() {}
}
