package dev.minhnh.yetanotherthirst.core.effect;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class HydrationEffect extends MobEffect {

    public HydrationEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x21b1ff);
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player && !ThirstCompat.usesExternalThirst(player)) {
            ThirstState state = ThirstStorage.get(player);
            if (!state.isEnabled()) {
                return true;
            }

            int thirst = state.getThirst();
            int quenched = state.getQuenched();
            int amp = amplifier + 1;
            state.drink(
                    ThirstConfig.HYDRATION_EFFECT_THIRST_PER_TICK * amp,
                    ThirstConfig.HYDRATION_EFFECT_QUENCHED_PER_TICK * amp);
            if (state.getThirst() != thirst || state.getQuenched() != quenched) {
                ThirstStorage.sync(player);
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
