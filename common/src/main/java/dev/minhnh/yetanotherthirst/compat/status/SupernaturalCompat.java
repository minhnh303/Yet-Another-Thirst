package dev.minhnh.yetanotherthirst.compat.status;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class SupernaturalCompat {

    private SupernaturalCompat() {
    }

    public static boolean isVampire(LivingEntity entity) {
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

    private static final class Supernatural {

        private static final MethodHandle CHECK = resolveCheck();

        private static MethodHandle resolveCheck() {
            try {
                Class<?> manager = Class.forName("net.salju.supernatural.events.SupernaturalManager", false,
                        SupernaturalCompat.class.getClassLoader());
                return MethodHandles.publicLookup()
                        .findStatic(manager, "isVampire", MethodType.methodType(boolean.class, LivingEntity.class));
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}

