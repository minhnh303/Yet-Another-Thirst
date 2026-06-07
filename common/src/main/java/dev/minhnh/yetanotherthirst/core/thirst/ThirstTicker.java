package dev.minhnh.yetanotherthirst.core.thirst;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.biome.Biome;

public final class ThirstTicker {

    private static final ResourceKey<DamageType> DEHYDRATE_KEY = ResourceKey.create(
            Registries.DAMAGE_TYPE, Constants.asResource("dehydrate"));

    private ThirstTicker() {
    }

    public static void tick(ServerPlayer player) {

        if (player.getAbilities().invulnerable) {
            return;
        }

        ThirstState state = ThirstStorage.get(player);
        if (ThirstCompat.usesExternalThirst(player)) {
            state.resetDamageTimer();
            if (state.consumeInitialSync()) {
                ThirstStorage.sync(player);
            }
            return;
        }
        if (!state.isEnabled()) {
            if (state.consumeInitialSync()) {
                ThirstStorage.sync(player);
            }
            return;
        }

        if (ThirstCompat.suspendsThirst(player)) {
            state.resetDamageTimer();
            return;
        }

        Difficulty difficulty = player.level().getDifficulty();
        boolean depletionPaused = ThirstCompat.pausesDepletion(player);

        if (!depletionPaused && ThirstConfig.DEPLETES_WHEN_NAUSEA && player.hasEffect(MobEffects.CONFUSION)) {
            state.addExhaustion(0.06F * exhaustionModifier(player));
        }

        if (!depletionPaused && player.hasEffect(MobEffects.HUNGER)) {
            int amplifier = player.getEffect(MobEffects.HUNGER).getAmplifier() + 1;
            state.addExhaustion(-0.005F * amplifier * exhaustionModifier(player));
        }

        if (!depletionPaused && !player.isPassenger()) {
            updateFoodExhaustion(player, state);
        }

        if (state.getExhaustion() > ThirstConfig.EXHAUSTION_LIMIT) {
            if (!depletionPaused
                    && (state.getQuenched() > 0 || difficulty != Difficulty.PEACEFUL
                    || ThirstConfig.THIRST_DEPLETES_IN_PEACEFUL)) {
                state.depleteOneLevel();
            } else {
                state.setExhaustion(ThirstConfig.EXHAUSTION_LIMIT);
            }
        }

        if (state.nextSyncTimer() > ThirstConfig.SYNC_INTERVAL_TICKS) {
            if (difficulty == Difficulty.PEACEFUL && !ThirstConfig.THIRST_DEPLETES_IN_PEACEFUL) {
                state.setThirst(state.getThirst() + 1);
            }
            drinkRain(player, state);
            state.resetSyncTimer();
            ThirstStorage.sync(player);
        }

        if (ThirstConfig.SPRINT_PREVENTION && state.getThirst() <= ThirstConfig.SPRINT_THRESHOLD) {
            player.setSprinting(false);
        }

        if (!ThirstConfig.REGEN_THIRST_EFFECTS.isEmpty() && player.tickCount % ThirstConfig.REGEN_THIRST_INTERVAL == 0) {
            net.minecraft.core.Registry<net.minecraft.world.effect.MobEffect> effectsRegistry = player.level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.MOB_EFFECT);
            for (var effectInstance : player.getActiveEffects()) {
                net.minecraft.resources.ResourceLocation effectId = ThirstCompat.effectId(effectsRegistry, effectInstance.getEffect());
                if (effectId != null) {
                    for (var condition : ThirstConfig.REGEN_THIRST_EFFECTS) {
                        if (condition.effectId.equals(effectId) && condition.matches(effectInstance.getAmplifier())) {
                            if (state.getThirst() < ThirstConfig.MAX_THIRST) {
                                state.setThirst(state.getThirst() + ThirstConfig.REGEN_THIRST_AMOUNT);
                                ThirstStorage.sync(player);
                            }
                            break;
                        }
                    }
                }
            }
        }

        damageIfDehydrated(player, state, difficulty);
    }

    private static void updateFoodExhaustion(ServerPlayer player, ThirstState state) {

        float hungerExhaustion = player.getFoodData().getExhaustionLevel();
        float previous = state.getPreviousFoodExhaustion();
        float normalized = hungerExhaustion < previous
                ? (state.isExhaustionRecalculate() ? hungerExhaustion + ThirstConfig.EXHAUSTION_LIMIT : hungerExhaustion)
                : hungerExhaustion;

        if (state.isExhaustionRecalculate()) {
            state.setExhaustionRecalculate(false);
        }

        float delta = normalized - previous;
        if (delta > 0.0F) {
            state.addExhaustion(delta * exhaustionModifier(player));
        }

        if (state.isJustHealed()) {
            state.setJustHealed(false);
        }

        state.setPreviousFoodExhaustion(hungerExhaustion);
    }

    private static void drinkRain(ServerPlayer player, ThirstState state) {

        if (!ThirstConfig.DRINK_RAIN_WATER) {
            return;
        }

        float angle = Mth.wrapDegrees(player.getXRot());
        if (angle <= -80.0F && player.level().isRainingAt(player.blockPosition().above())) {
            state.setThirst(state.getThirst() + 1);
            state.setQuenched(state.getQuenched() + 1);
        }
    }

    private static void damageIfDehydrated(ServerPlayer player, ThirstState state, Difficulty difficulty) {

        if (state.getThirst() > 0) {
            state.resetDamageTimer();
            return;
        }

        if (state.nextDamageTimer() < ThirstConfig.DAMAGE_INTERVAL_TICKS) {
            return;
        }

        float limit = ThirstConfig.DEHYDRATION_DAMAGE_EASY_LIMIT;
        if (difficulty == Difficulty.NORMAL) {
            limit = ThirstConfig.DEHYDRATION_DAMAGE_NORMAL_LIMIT;
        } else if (difficulty == Difficulty.HARD) {
            limit = ThirstConfig.DEHYDRATION_DAMAGE_HARD_LIMIT;
        }

        if (player.getHealth() > limit) {
            player.hurt(dehydrateSource(player), ThirstConfig.DEHYDRATION_DAMAGE);
        }
        state.resetDamageTimer();
    }

    private static DamageSource dehydrateSource(ServerPlayer player) {

        try {
            return new DamageSource(player.level().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE)
                    .getHolderOrThrow(DEHYDRATE_KEY));
        } catch (Exception e) {
            return player.damageSources().starve();
        }
    }

    private static float exhaustionModifier(ServerPlayer player) {

        float modifier = biomeModifier(player) * fireProtectionModifier(player) * ThirstCompat.coldSweatDehydrationModifier(player);

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            modifier *= ThirstConfig.FIRE_RESISTANCE_DEHYDRATION_MODIFIER;
        }
        return modifier;
    }

    private static float biomeModifier(ServerPlayer player) {

        if (ThirstCompat.coldSweatReplacesEnvironmentModifiers()) {
            return ThirstConfig.THIRST_DEPLETION_MODIFIER;
        }

        if (player.level().dimensionType().ultraWarm()) {
            return ThirstConfig.NETHER_THIRST_DEPLETION_MODIFIER;
        }

        if (!ThirstConfig.BIOME_DEHYDRATION_MODIFIER) {
            return ThirstConfig.THIRST_DEPLETION_MODIFIER;
        }

        Biome biome = player.level().getBiome(player.getOnPos()).value();

        float humidity = biome.getPrecipitationAt(player.getOnPos()) == Biome.Precipitation.NONE ? 1.1F : 1.2F;

        float baseTemp = biome.getBaseTemperature();
        float tempFactor;
        if (baseTemp >= 0.7F) {
            tempFactor = 1.0F + (baseTemp - 0.7F) * 0.5F / 1.3F;
        } else {
            tempFactor = 1.0F - (0.7F - baseTemp) * 0.5F / 1.2F;
        }
        tempFactor = Mth.clamp(tempFactor, 0.3F, 2.0F);

        float modifier = ThirstConfig.THIRST_DEPLETION_MODIFIER * (tempFactor / humidity);
        if (modifier < 1.0F) {
            float offset = (1.0F - modifier) * ThirstConfig.ENVIRONMENT_MODIFIER_HARSHNESS;
            modifier = 1.0F - offset;
        }
        return modifier;
    }

    private static float fireProtectionModifier(ServerPlayer player) {

        if (!ThirstConfig.FIRE_PROTECTION_DEHYDRATION_MODIFIER) {
            return 1.0F;
        }

        int levels = (int) EnchantmentHelper.getDamageProtection(player.serverLevel(), player, player.damageSources().onFire()) / 2;
        levels = Math.min(levels, 12);
        return 1.0F - levels * 0.0625F * 0.75F;
    }
}
