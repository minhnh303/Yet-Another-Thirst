package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.compat.gui.AppleSkinCompat;
import dev.minhnh.yetanotherthirst.compat.status.SupernaturalCompat;
import dev.minhnh.yetanotherthirst.compat.status.VampirismCompat;
import dev.minhnh.yetanotherthirst.compat.temperature.ColdSweatCompat;
import dev.minhnh.yetanotherthirst.compat.temperature.EnvironmentzCompat;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Compatibility facade for third-party mod integrations.
 * Delegates to modular domain-specific providers while preserving a stable public API.
 */
public final class ThirstCompat {

    private ThirstCompat() {
    }

    public static boolean suspendsThirst(LivingEntity entity) {
        if (isVampire(entity) || isSupernaturalVampire(entity)) {
            return true;
        }

        if (!ThirstConfig.SUSPENDED_THIRST_EFFECTS.isEmpty()) {
            Registry<MobEffect> effects = entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT);
            for (var effectInstance : entity.getActiveEffects()) {
                ResourceLocation effectId = effectId(effects, effectInstance.getEffect());
                if (effectId != null) {
                    for (var condition : ThirstConfig.SUSPENDED_THIRST_EFFECTS) {
                        if (condition.effectId.equals(effectId) && condition.matches(effectInstance.getAmplifier())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean pausesDepletion(LivingEntity entity) {
        return hasFoodPauseEffect(entity);
    }

    public static boolean hidesThirstHud(Player player) {
        return isVampire(player) || isSupernaturalVampire(player) || usesExternalThirst(player);
    }

    public static boolean isVampire(LivingEntity entity) {
        return VampirismCompat.isVampire(entity);
    }

    public static boolean isSupernaturalVampire(LivingEntity entity) {
        return SupernaturalCompat.isVampire(entity);
    }

    public static boolean usesExternalThirst(LivingEntity entity) {
        return ThirstConfig.COMPAT_TOUGH_AS_NAILS
                && ThirstConfig.TOUGH_AS_NAILS_MODE == ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT;
    }

    public static boolean showsAppleSkinThirstTooltip() {
        return AppleSkinCompat.showsThirstTooltip();
    }

    public static boolean showsAppleSkinHudPreview() {
        return AppleSkinCompat.showsHudPreview();
    }

    /**
     * Cold Sweat and EnvironmentZ each simulate a body-temperature system.
     * Cold Sweat takes precedence when both are active.
     */
    public static float externalTemperatureDehydrationModifier(LivingEntity entity) {
        if (ThirstConfig.COMPAT_COLD_SWEAT) {
            return coldSweatDehydrationModifier(entity);
        }
        if (ThirstConfig.COMPAT_ENVIRONMENTZ) {
            return environmentzDehydrationModifier(entity);
        }
        return 1.0F;
    }

    public static boolean replacesEnvironmentModifiers() {
        if (ThirstConfig.COMPAT_COLD_SWEAT) {
            return coldSweatReplacesEnvironmentModifiers();
        }
        if (ThirstConfig.COMPAT_ENVIRONMENTZ) {
            return environmentzReplacesEnvironmentModifiers();
        }
        return false;
    }

    public static boolean coldSweatReplacesEnvironmentModifiers() {
        return ColdSweatCompat.replacesEnvironmentModifiers();
    }

    public static float coldSweatDehydrationModifier(LivingEntity entity) {
        return ColdSweatCompat.getDehydrationModifier(entity);
    }

    public static boolean environmentzReplacesEnvironmentModifiers() {
        return EnvironmentzCompat.replacesEnvironmentModifiers();
    }

    public static float environmentzDehydrationModifier(LivingEntity entity) {
        return EnvironmentzCompat.getDehydrationModifier(entity);
    }

    public static Component environmentzDebugInfo(LivingEntity entity) {
        return EnvironmentzCompat.getDebugInfo(entity);
    }

    public static int getEnvironmentzPlayerTemperature(LivingEntity entity) {
        return EnvironmentzCompat.getPlayerTemperature(entity);
    }

    public static int getEnvironmentzThermometerTemperature(LivingEntity entity) {
        return EnvironmentzCompat.getThermometerTemperature(entity);
    }

    public static boolean isEnvironmentzThermometerVisible() {
        return EnvironmentzCompat.isThermometerVisible();
    }

    public static int getEnvironmentzThermometerIconX() {
        return EnvironmentzCompat.getThermometerIconX();
    }

    public static int getEnvironmentzThermometerIconY() {
        return EnvironmentzCompat.getThermometerIconY();
    }

    public static ResourceLocation effectId(Registry<MobEffect> effects, MobEffect effect) {
        return effects.getResourceKey(effect).map(ResourceKey::location).orElse(null);
    }

    private static boolean hasFoodPauseEffect(LivingEntity entity) {
        if (ThirstConfig.PAUSE_DEPLETION_EFFECTS.isEmpty()) {
            return false;
        }

        Registry<MobEffect> effects = entity.level().registryAccess().registryOrThrow(Registries.MOB_EFFECT);
        for (var effectInstance : entity.getActiveEffects()) {
            ResourceLocation effectId = effectId(effects, effectInstance.getEffect());
            if (effectId != null) {
                for (var condition : ThirstConfig.PAUSE_DEPLETION_EFFECTS) {
                    if (condition.effectId.equals(effectId) && condition.matches(effectInstance.getAmplifier())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
