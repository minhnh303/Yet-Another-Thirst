package dev.minhnh.yetanotherthirst.core.effect;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class QuenchnessEffect extends MobEffect {

    public QuenchnessEffect() {

        super(MobEffectCategory.BENEFICIAL, 0x21b1ff);
    }

    @Override
    public boolean isInstantenous() {

        return true;
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, @Nullable LivingEntity entity, int amplifier, double health) {

        drink(entity, amplifier);
    }

    @Override
    public boolean applyEffectTick(@Nullable LivingEntity entity, int amplifier) {

        drink(entity, amplifier);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {

        return duration >= 1;
    }

    private static void drink(LivingEntity entity, int amplifier) {

        if (entity instanceof ServerPlayer player) {
            int amount = amplifier + 1;
            ThirstStorage.get(player).drink(amount, amount);
            ThirstStorage.sync(player);
        }
    }
}
