package dev.minhnh.yetanotherthirst.compat.status;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

public final class VampirismCompat {

    private VampirismCompat() {
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

    private static final class Vampirism {

        private static final MethodHandle[] CHECKS = resolveChecks();

        private static MethodHandle[] resolveChecks() {
            try {
                Class<?> helper = Class.forName("de.teamlapen.vampirism.util.Helper", false,
                        VampirismCompat.class.getClassLoader());
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
}

