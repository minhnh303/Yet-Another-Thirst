package dev.minhnh.yetanotherthirst.compat;

import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

public final class VampirismCompat {

    private static final MethodHandle IS_VAMPIRE;

    static {
        MethodHandle handle = null;
        try {
            Class<?> cls = Class.forName("de.teamlapen.vampirism.util.Helper");
            Method m = cls.getMethod("isVampire", LivingEntity.class);
            handle = MethodHandles.lookup().unreflect(m);
        } catch (Exception ignored) {}
        IS_VAMPIRE = handle;
    }

    public static boolean isVampire(LivingEntity entity) {
        if (IS_VAMPIRE == null) return false;
        try {
            return (boolean) IS_VAMPIRE.invoke(entity);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private VampirismCompat() {}
}
