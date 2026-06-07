package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

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

        if (!ThirstConfig.COMPAT_VAMPIRISM || !ThirstConfig.VAMPIRISM_VAMPIRE_SUSPENDS_THIRST) {
            return false;
        }

        for (MethodHandle check : Vampirism.CHECKS) {
            try {
                if ((boolean) check.invoke(entity)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static boolean isSupernaturalVampire(LivingEntity entity) {

        if (!ThirstConfig.COMPAT_SUPERNATURAL || !ThirstConfig.SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST) {
            return false;
        }

        MethodHandle check = Supernatural.CHECK;
        if (check != null) {
            try {
                if ((boolean) check.invoke(entity)) {
                    return true;
                }
            } catch (Throwable ignored) {
            }
        }

        if (entity instanceof Player player) {
            CompoundTag persistent = Services.PLATFORM.loadPersistentData(player, "PlayerPersisted");
            return persistent.getBoolean("isVampire");
        }
        return false;
    }

    public static boolean usesExternalThirst(LivingEntity entity) {

        return ThirstConfig.COMPAT_TOUGH_AS_NAILS
                && ThirstConfig.TOUGH_AS_NAILS_MODE == ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT;
    }

    public static boolean showsAppleSkinThirstTooltip() {

        return ThirstConfig.COMPAT_APPLESKIN && ThirstConfig.APPLESKIN_THIRST_TOOLTIP;
    }

    public static boolean showsAppleSkinHudPreview() {

        return ThirstConfig.COMPAT_APPLESKIN && ThirstConfig.APPLESKIN_THIRST_HUD_PREVIEW;
    }

    public static boolean coldSweatReplacesEnvironmentModifiers() {

        return ThirstConfig.COMPAT_COLD_SWEAT
                && ThirstConfig.COLD_SWEAT_DEHYDRATION_MODIFIER
                && ThirstConfig.COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS
                && ColdSweat.GET_TEMPERATURE != null;
    }

    public static float coldSweatDehydrationModifier(LivingEntity entity) {

        if (!ThirstConfig.COMPAT_COLD_SWEAT
                || !ThirstConfig.COLD_SWEAT_DEHYDRATION_MODIFIER
                || ColdSweat.GET_TEMPERATURE == null) {
            return 1.0F;
        }

        try {
            double bodyTemperature = (double) ColdSweat.GET_TEMPERATURE.invoke(entity, ColdSweat.BODY_TRAIT);
            float hot = ThirstConfig.COLD_SWEAT_HOT_BODY_TEMPERATURE;
            float burning = ThirstConfig.COLD_SWEAT_BURNING_BODY_TEMPERATURE;
            if (bodyTemperature <= hot || burning <= hot) {
                return 1.0F;
            }

            float progress = (float) ((bodyTemperature - hot) / (burning - hot));
            progress = Math.max(0.0F, Math.min(progress, 1.0F));
            return 1.0F + (ThirstConfig.COLD_SWEAT_MAX_DEHYDRATION_MODIFIER - 1.0F) * progress;
        } catch (Throwable ignored) {
            return 1.0F;
        }
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

    public static ResourceLocation effectId(Registry<MobEffect> effects, MobEffect effect) {

        return effects.getResourceKey(effect).map(ResourceKey::location).orElse(null);
    }

    private static final class Vampirism {

        private static final MethodHandle[] CHECKS = resolveChecks();

        private static MethodHandle[] resolveChecks() {

            try {
                Class<?> helper = Class.forName("de.teamlapen.vampirism.util.Helper", false,
                        ThirstCompat.class.getClassLoader());
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                List<MethodHandle> checks = new ArrayList<>();
                findCheck(lookup, helper, Player.class, checks);
                findCheck(lookup, helper, Entity.class, checks);
                findCheck(lookup, helper, LivingEntity.class, checks);
                return checks.toArray(MethodHandle[]::new);
            } catch (Throwable ignored) {
                return new MethodHandle[0];
            }
        }

        private static void findCheck(MethodHandles.Lookup lookup, Class<?> helper, Class<?> parameter,
                                      List<MethodHandle> checks) {

            try {
                checks.add(lookup.findStatic(helper, "isVampire", MethodType.methodType(boolean.class, parameter)));
            } catch (Throwable ignored) {
            }
        }
    }

    private static final class Supernatural {

        private static final MethodHandle CHECK = resolveCheck();

        private static MethodHandle resolveCheck() {

            try {
                Class<?> manager = Class.forName("net.salju.supernatural.events.SupernaturalManager", false,
                        ThirstCompat.class.getClassLoader());
                return MethodHandles.publicLookup()
                        .findStatic(manager, "isVampire", MethodType.methodType(boolean.class, LivingEntity.class));
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    private static final class ColdSweat {

        private static final MethodHandle GET_TEMPERATURE;
        private static final Object BODY_TRAIT;

        static {
            MethodHandle getTemperature = null;
            Object bodyTrait = null;
            try {
                Class<?> temperature = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature", false,
                        ThirstCompat.class.getClassLoader());
                Class<?> trait = Class.forName("com.momosoftworks.coldsweat.api.util.Trait", false,
                        ThirstCompat.class.getClassLoader());
                @SuppressWarnings({"unchecked", "rawtypes"})
                Object body = Enum.valueOf((Class<? extends Enum>) trait.asSubclass(Enum.class), "BODY");
                getTemperature = MethodHandles.publicLookup()
                        .findStatic(temperature, "get", MethodType.methodType(double.class, LivingEntity.class, trait));
                bodyTrait = body;
            } catch (Throwable ignored) {
            }
            GET_TEMPERATURE = getTemperature;
            BODY_TRAIT = bodyTrait;
        }
    }
}
