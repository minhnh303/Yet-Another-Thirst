package dev.minhnh.yetanotherthirst.core.thirst;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffects;

public final class ThirstTicker {

    private static final ResourceKey<DamageType> DEHYDRATE_KEY = ResourceKey.create(
            Registries.DAMAGE_TYPE, Constants.asResource("dehydrate"));

    private ThirstTicker() {
    }

    public static void tick(ServerPlayer player) {

        if (player.getAbilities().invulnerable) {
            return;
        }

        // Tombstone: skip tick entirely while player has the ghostly shape (death chest ghost form)
        if (ThirstConfig.COMPAT_TOMBSTONE && player.getActiveEffects().stream()
                .anyMatch(e -> e.getEffect().getDescriptionId().contains("ghostly_shape"))) {
            return;
        }

        ThirstState state = ThirstStorage.get(player);
        if (!state.isEnabled()) {
            if (state.consumeInitialSync()) {
                ThirstStorage.sync(player);
            }
            return;
        }

        Difficulty difficulty = player.level().getDifficulty();
        if (ThirstConfig.DEPLETES_WHEN_NAUSEA && player.hasEffect(MobEffects.CONFUSION)) {
            state.addExhaustion(0.06F * exhaustionModifier(player));
        }

        if (player.hasEffect(MobEffects.HUNGER)) {
            int amplifier = player.getEffect(MobEffects.HUNGER).getAmplifier() + 1;
            state.addExhaustion(-0.005F * amplifier * exhaustionModifier(player));
        }

        if (!player.isPassenger() && !isExhaustionExempt(player)) {
            updateFoodExhaustion(player, state);
        }

        while (state.getExhaustion() > ThirstConfig.EXHAUSTION_LIMIT) {
            if (state.getQuenched() > 0 || difficulty != Difficulty.PEACEFUL || ThirstConfig.THIRST_DEPLETES_IN_PEACEFUL) {
                state.depleteOneLevel();
            } else {
                state.setExhaustion(ThirstConfig.EXHAUSTION_LIMIT);
                break;
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

        damageIfDehydrated(player, state, difficulty);
    }

    private static boolean isExhaustionExempt(ServerPlayer player) {
        if (ThirstConfig.COMPAT_FARMERS_DELIGHT && player.getActiveEffects().stream()
                .anyMatch(e -> "effect.farmersdelight.nourishment".equals(e.getEffect().getDescriptionId()))) {
            return true;
        }
        if (ThirstConfig.COMPAT_LETS_DO_BAKERY && player.getActiveEffects().stream()
                .anyMatch(e -> e.getEffect().getDescriptionId().contains("stuffed"))) {
            return true;
        }
        if (ThirstConfig.COMPAT_LETS_DO_BREWERY && player.getActiveEffects().stream()
                .anyMatch(e -> e.getEffect().getDescriptionId().contains("saturated"))) {
            return true;
        }
        return false;
    }

    private static void updateFoodExhaustion(ServerPlayer player, ThirstState state) {

        float hungerExhaustion = player.getFoodData().getExhaustionLevel();
        float previous = state.getPreviousFoodExhaustion();
        float normalized = hungerExhaustion < previous ? hungerExhaustion + ThirstConfig.EXHAUSTION_LIMIT : hungerExhaustion;
        float delta = normalized - previous;
        if (delta > 0.0F) {
            state.addExhaustion(delta * exhaustionModifier(player));
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

        if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 0.0F && difficulty == Difficulty.NORMAL) {
            player.hurt(dehydrateSource(player), 1.0F);
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

        float modifier = player.level().dimensionType().ultraWarm()
                ? ThirstConfig.NETHER_THIRST_DEPLETION_MODIFIER
                : ThirstConfig.THIRST_DEPLETION_MODIFIER;

        if (player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            modifier *= ThirstConfig.FIRE_RESISTANCE_DEHYDRATION_MODIFIER;
        }
        return modifier;
    }
}
