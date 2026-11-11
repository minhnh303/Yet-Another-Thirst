package dev.minhnh.yetanotherthirst.compat.temperature;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class ColdSweatCompat {

    private ColdSweatCompat() {
    }

    public static boolean replacesEnvironmentModifiers() {
        return ThirstConfig.COMPAT_COLD_SWEAT
                && ThirstConfig.COLD_SWEAT_DEHYDRATION_MODIFIER
                && ThirstConfig.COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS
                && ColdSweat.GET_TEMPERATURE != null;
    }

    public static float getDehydrationModifier(LivingEntity entity) {
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

    private static final class ColdSweat {

        private static final MethodHandle GET_TEMPERATURE;
        private static final Object BODY_TRAIT;

        static {
            MethodHandle getTemperature = null;
            Object bodyTrait = null;
            try {
                ClassLoader classLoader = ColdSweatCompat.class.getClassLoader();
                Class<?> temperature = Class.forName("com.momosoftworks.coldsweat.api.util.Temperature", false, classLoader);
                Class<?> trait = Class.forName("com.momosoftworks.coldsweat.api.util.Trait", false, classLoader);
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

