package dev.minhnh.yetanotherthirst.core.effect;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class ThirstyEffect extends MobEffect {

    public ThirstyEffect() {

        super(MobEffectCategory.HARMFUL, 0xb15b2a);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {

        if (entity instanceof ServerPlayer player
                && !ThirstCompat.usesExternalThirst(player)
                && !ThirstCompat.suspendsThirst(player)
                && !ThirstCompat.pausesDepletion(player)) {
            ThirstState state = ThirstStorage.get(player);
            if (state.isEnabled()) {
                state.addExhaustion(ThirstConfig.THIRSTY_EFFECT_EXHAUSTION_PER_TICK * (amplifier + 1));
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {

        return true;
    }
}
