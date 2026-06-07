package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.QuenchnessEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NeoForgeEffects {

    static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, Constants.MOD_ID);

    static final DeferredHolder<MobEffect, QuenchnessEffect> QUENCHNESS = EFFECTS.register("quenchness", QuenchnessEffect::new);

    private NeoForgeEffects() {
    }
}
