package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.effect.QuenchnessEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NeoForgeEffects {

    static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Constants.MOD_ID);

    static final RegistryObject<MobEffect> QUENCHNESS = EFFECTS.register("quenchness", QuenchnessEffect::new);

    private NeoForgeEffects() {
    }
}
