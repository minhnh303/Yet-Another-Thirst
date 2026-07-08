package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.HydrationEffect;
import dev.minhnh.yetanotherthirst.core.effect.ModEffects;
import dev.minhnh.yetanotherthirst.core.effect.ThirstyEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public final class FabricEffects {

    static MobEffect THIRSTY;
    static MobEffect HYDRATION;

    static void register() {

        THIRSTY = reg("thirsty", new ThirstyEffect());
        HYDRATION = reg("hydration", new HydrationEffect());

        bindToCommon();
    }

    private static <T extends MobEffect> T reg(String name, T effect) {

        return Registry.register(BuiltInRegistries.MOB_EFFECT, Constants.asResource(name), effect);
    }

    static void bindToCommon() {

        ModEffects.THIRSTY = () -> BuiltInRegistries.MOB_EFFECT.wrapAsHolder(THIRSTY);
        ModEffects.HYDRATION = () -> BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HYDRATION);
    }

    private FabricEffects() {}
}
