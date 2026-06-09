package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.HydrationEffect;
import dev.minhnh.yetanotherthirst.core.effect.ModEffects;
import dev.minhnh.yetanotherthirst.core.effect.ThirstyEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeEffects {

    static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Constants.MOD_ID);

    static final DeferredHolder<MobEffect, ThirstyEffect> THIRSTY = EFFECTS.register("thirsty", ThirstyEffect::new);
    static final DeferredHolder<MobEffect, HydrationEffect> HYDRATION = EFFECTS.register("hydration", HydrationEffect::new);

    /** Binds NeoForge RegistryObjects into the loader-agnostic ModEffects suppliers. */
    static void bindToCommon() {
        ModEffects.THIRSTY = () -> THIRSTY;
        ModEffects.HYDRATION = () -> HYDRATION;
    }

    private NeoForgeEffects() {
    }
}
