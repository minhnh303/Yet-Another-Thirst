package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.HydrationEffect;
import dev.minhnh.yetanotherthirst.core.effect.ModEffects;
import dev.minhnh.yetanotherthirst.core.effect.ThirstyEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NeoForgeEffects {

    static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.MOD_ID);

    static final RegistryObject<MobEffect> THIRSTY = EFFECTS.register("thirsty", ThirstyEffect::new);
    static final RegistryObject<MobEffect> HYDRATION = EFFECTS.register("hydration", HydrationEffect::new);

    /** Binds NeoForge RegistryObjects into the loader-agnostic ModEffects suppliers. */
    static void bindToCommon() {
        ModEffects.THIRSTY = THIRSTY;
        ModEffects.HYDRATION = HYDRATION;
    }

    private NeoForgeEffects() {
    }
}
