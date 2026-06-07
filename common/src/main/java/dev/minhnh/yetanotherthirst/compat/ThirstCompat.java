package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
import java.util.Objects;

public final class ThirstCompat {

    private ThirstCompat() {
    }

    public static boolean suspendsThirst(LivingEntity entity) {

        if (isVampire(entity)) {
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

        return isVampire(player);
    }

    public static boolean isVampire(LivingEntity entity) {

        if (!ThirstConfig.COMPAT_VAMPIRISM) {
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
            } catch (ClassNotFoundException ignored) {
                return new MethodHandle[0];
            }
        }

        private static void findCheck(MethodHandles.Lookup lookup, Class<?> helper, Class<?> parameter,
                                      List<MethodHandle> checks) {

            try {
                checks.add(lookup.findStatic(helper, "isVampire", MethodType.methodType(boolean.class, parameter)));
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }
        }
    }
}
