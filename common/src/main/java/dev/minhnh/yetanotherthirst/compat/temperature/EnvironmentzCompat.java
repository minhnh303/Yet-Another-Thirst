package dev.minhnh.yetanotherthirst.compat.temperature;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.List;

public final class EnvironmentzCompat {

    private EnvironmentzCompat() {
    }

    public static boolean replacesEnvironmentModifiers() {
        return ThirstConfig.COMPAT_ENVIRONMENTZ
                && ThirstConfig.ENVIRONMENTZ_DEHYDRATION_MODIFIER
                && ThirstConfig.ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS
                && Environmentz.GET_TEMPERATURE_MANAGER != null;
    }

    public static float getDehydrationModifier(LivingEntity entity) {
        if (!ThirstConfig.COMPAT_ENVIRONMENTZ
                || !ThirstConfig.ENVIRONMENTZ_DEHYDRATION_MODIFIER
                || Environmentz.GET_TEMPERATURE_MANAGER == null
                || Environmentz.GET_PLAYER_TEMPERATURE == null) {
            return 1.0F;
        }

        try {
            Object manager = Environmentz.GET_TEMPERATURE_MANAGER.invoke(entity);
            int playerTemperature = (int) Environmentz.GET_PLAYER_TEMPERATURE.invoke(manager);
            return resolveTemperatureTierModifier(playerTemperature);
        } catch (Throwable ignored) {
            return 1.0F;
        }
    }

    public static int getPlayerTemperature(LivingEntity entity) {
        if (!ThirstConfig.COMPAT_ENVIRONMENTZ
                || Environmentz.GET_TEMPERATURE_MANAGER == null
                || Environmentz.GET_PLAYER_TEMPERATURE == null) {
            return 0;
        }
        try {
            Object manager = Environmentz.GET_TEMPERATURE_MANAGER.invoke(entity);
            return (int) Environmentz.GET_PLAYER_TEMPERATURE.invoke(manager);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static int getThermometerTemperature(LivingEntity entity) {
        if (!ThirstConfig.COMPAT_ENVIRONMENTZ
                || Environmentz.GET_TEMPERATURE_MANAGER == null
                || Environmentz.GET_THERMOMETER_TEMPERATURE == null) {
            return 0;
        }
        try {
            Object manager = Environmentz.GET_TEMPERATURE_MANAGER.invoke(entity);
            return (int) Environmentz.GET_THERMOMETER_TEMPERATURE.invoke(manager);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static boolean isThermometerVisible() {
        if (!ThirstConfig.COMPAT_ENVIRONMENTZ
                || Environmentz.CONFIG_FIELD == null
                || Environmentz.SHOW_THERMOMETER_FIELD == null) {
            return true;
        }
        try {
            Object config = Environmentz.CONFIG_FIELD.get(null);
            if (config != null) {
                return Environmentz.SHOW_THERMOMETER_FIELD.getBoolean(config);
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    public static int getThermometerIconX() {
        if (Environmentz.CONFIG_FIELD != null && Environmentz.THERMOMETER_ICON_X_FIELD != null) {
            try {
                Object config = Environmentz.CONFIG_FIELD.get(null);
                if (config != null) {
                    return Environmentz.THERMOMETER_ICON_X_FIELD.getInt(config);
                }
            } catch (Throwable ignored) {
            }
        }
        return 140;
    }

    public static int getThermometerIconY() {
        if (Environmentz.CONFIG_FIELD != null && Environmentz.THERMOMETER_ICON_Y_FIELD != null) {
            try {
                Object config = Environmentz.CONFIG_FIELD.get(null);
                if (config != null) {
                    return Environmentz.THERMOMETER_ICON_Y_FIELD.getInt(config);
                }
            } catch (Throwable ignored) {
            }
        }
        return 32;
    }

    public static Component getDebugInfo(LivingEntity entity) {
        MutableComponent header = Component.literal("[EnvironmentZ]").withStyle(ChatFormatting.GOLD);

        if (Environmentz.GET_TEMPERATURE_MANAGER == null || Environmentz.GET_PLAYER_TEMPERATURE == null) {
            return header.append(Component.literal(" detected but its API could not be resolved (version mismatch?)")
                    .withStyle(ChatFormatting.RED));
        }

        try {
            Object manager = Environmentz.GET_TEMPERATURE_MANAGER.invoke(entity);
            int playerTemperature = (int) Environmentz.GET_PLAYER_TEMPERATURE.invoke(manager);
            int thermometerTemperature = Environmentz.GET_THERMOMETER_TEMPERATURE != null ? (int) Environmentz.GET_THERMOMETER_TEMPERATURE.invoke(manager) : 0;
            float modifier = getDehydrationModifier(entity);
            ChatFormatting modifierColor = modifierColor(modifier);

            MutableComponent message = header;
            message.append(Component.literal("\n- Body Temp: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.valueOf(playerTemperature)).withStyle(modifierColor));

            if (thermometerTemperature != 0) {
                message.append(Component.literal(" | Ambient Temp: ").withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(String.valueOf(thermometerTemperature)).withStyle(ChatFormatting.YELLOW));
            }

            int displayedTemp = ("THERMOMETER".equalsIgnoreCase(ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE)
                    || "ENVIRONMENT".equalsIgnoreCase(ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE))
                    ? thermometerTemperature
                    : playerTemperature;
            String unit = ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT != null ? ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT : "";

            message.append(Component.literal("\n- HUD Display: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE + " (" + displayedTemp + unit + ")").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal("\n- Tiers: [").withStyle(ChatFormatting.GRAY))
                    .append(temperatureTiersComponent(playerTemperature))
                    .append(Component.literal("]").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("\n- Dehydration Modifier: ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(String.format("%.3fx", modifier)).withStyle(modifierColor));

            if (!ThirstConfig.ENVIRONMENTZ_DEHYDRATION_MODIFIER) {
                message.append(Component.literal(" (integration disabled in config)")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
            return message;
        } catch (Throwable t) {
            return header.append(Component.literal(" read failed: " + t).withStyle(ChatFormatting.RED));
        }
    }

    private static float resolveTemperatureTierModifier(int playerTemperature) {
        float modifier = 1.0F;
        for (ThirstConfig.TemperatureTier tier : ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS) {
            if (playerTemperature < tier.threshold) {
                break;
            }
            modifier = tier.modifier;
        }
        return modifier;
    }

    private static ChatFormatting modifierColor(float modifier) {
        if (modifier > 1.0F) {
            return ChatFormatting.RED;
        }
        if (modifier < 1.0F) {
            return ChatFormatting.AQUA;
        }
        return ChatFormatting.WHITE;
    }

    private static MutableComponent temperatureTiersComponent(int playerTemperature) {
        List<ThirstConfig.TemperatureTier> tiers = ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TIERS;
        if (tiers.isEmpty()) {
            return Component.literal("none configured").withStyle(ChatFormatting.DARK_GRAY);
        }

        ThirstConfig.TemperatureTier active = null;
        for (ThirstConfig.TemperatureTier tier : tiers) {
            if (playerTemperature < tier.threshold) {
                break;
            }
            active = tier;
        }

        MutableComponent result = Component.empty();
        for (int i = 0; i < tiers.size(); i++) {
            ThirstConfig.TemperatureTier tier = tiers.get(i);
            boolean isActive = tier == active;
            MutableComponent entry = Component.literal(">=" + tier.threshold + " -> " + tier.modifier + "x")
                    .withStyle(isActive ? ChatFormatting.YELLOW : ChatFormatting.WHITE);
            if (isActive) {
                entry = entry.withStyle(ChatFormatting.UNDERLINE);
            }
            result.append(entry);
            if (i < tiers.size() - 1) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
        }
        return result;
    }

    private static final class Environmentz {
        private static final MethodHandle GET_TEMPERATURE_MANAGER;
        private static final MethodHandle GET_PLAYER_TEMPERATURE;
        private static final MethodHandle GET_THERMOMETER_TEMPERATURE;
        private static final Field CONFIG_FIELD;
        private static final Field THERMOMETER_ICON_X_FIELD;
        private static final Field THERMOMETER_ICON_Y_FIELD;
        private static final Field SHOW_THERMOMETER_FIELD;

        static {
            MethodHandle getManager = null;
            MethodHandle getPlayerTemperature = null;
            MethodHandle getThermometerTemperature = null;
            Field configField = null;
            Field txField = null;
            Field tyField = null;
            Field showThermometerField = null;
            try {
                ClassLoader classLoader = EnvironmentzCompat.class.getClassLoader();
                Class<?> access = Class.forName("net.environmentz.access.TemperatureManagerAccess", false, classLoader);
                Class<?> manager = Class.forName("net.environmentz.temperature.TemperatureManager", false, classLoader);
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                getManager = lookup.findVirtual(access, "getTemperatureManager", MethodType.methodType(manager));
                getPlayerTemperature = lookup.findVirtual(manager, "getPlayerTemperature", MethodType.methodType(int.class));
                try {
                    getThermometerTemperature = lookup.findVirtual(manager, "getThermometerTemperature", MethodType.methodType(int.class));
                } catch (Throwable ignored) {
                }

                try {
                    Class<?> configInit = Class.forName("net.environmentz.init.ConfigInit", false, classLoader);
                    configField = configInit.getField("CONFIG");
                    Object configObj = configField.get(null);
                    if (configObj != null) {
                        Class<?> configClass = configObj.getClass();
                        txField = configClass.getField("thermometerIconX");
                        tyField = configClass.getField("thermometerIconY");
                        showThermometerField = configClass.getField("showThermometer");
                    }
                } catch (Throwable ignored) {
                }
            } catch (Throwable ignored) {
            }
            GET_TEMPERATURE_MANAGER = getManager;
            GET_PLAYER_TEMPERATURE = getPlayerTemperature;
            GET_THERMOMETER_TEMPERATURE = getThermometerTemperature;
            CONFIG_FIELD = configField;
            THERMOMETER_ICON_X_FIELD = txField;
            THERMOMETER_ICON_Y_FIELD = tyField;
            SHOW_THERMOMETER_FIELD = showThermometerField;
        }
    }
}

