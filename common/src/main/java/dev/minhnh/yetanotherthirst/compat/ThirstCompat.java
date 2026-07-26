package dev.minhnh.yetanotherthirst.compat;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.Services;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
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

    /**
     * Cold Sweat and EnvironmentZ each simulate a full body-temperature system on their own;
     * stacking both would double-count heat/cold instead of deferring to a single source of truth.
     * They only ever ship for disjoint loaders, but a mixed-loader compatibility layer (e.g. Sinytra
     * Connector) could still load both at once, so precedence is resolved explicitly here rather
     * than relying on that separation. Cold Sweat — the longer-established integration — wins if
     * both happen to be loaded.
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

    /** Same precedence as {@link #externalTemperatureDehydrationModifier}. */
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

    public static boolean environmentzReplacesEnvironmentModifiers() {

        return ThirstConfig.COMPAT_ENVIRONMENTZ
                && ThirstConfig.ENVIRONMENTZ_DEHYDRATION_MODIFIER
                && ThirstConfig.ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS
                && Environmentz.GET_TEMPERATURE_MANAGER != null;
    }

    public static float environmentzDehydrationModifier(LivingEntity entity) {

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

    /**
     * {@link ThirstConfig#ENVIRONMENTZ_TEMPERATURE_TIERS} is sorted ascending by threshold, so the
     * applicable modifier is the one for the highest threshold at or below {@code playerTemperature};
     * later (higher) thresholds can only fail to match once an earlier one already has, so the scan
     * can stop at the first miss. Temperatures below every configured threshold stay neutral (1.0x).
     */
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

    /**
     * Colored, tier-highlighted snapshot of EnvironmentZ's raw player temperature, the user-configured
     * tier list it was compared against, and the resulting dehydration modifier — for {@code /thirst
     * query} to surface without needing to cross-reference EnvironmentZ's own debug log. Modifier and
     * temperature are colored red/aqua/white to match whether dehydration is currently sped up, slowed
     * down, or unaffected; the tier list highlights whichever entry is presently in effect.
     */
    public static Component environmentzDebugInfo(LivingEntity entity) {

        MutableComponent header = Component.literal("[EnvironmentZ]").withStyle(ChatFormatting.GOLD);

        if (Environmentz.GET_TEMPERATURE_MANAGER == null || Environmentz.GET_PLAYER_TEMPERATURE == null) {
            return header.append(Component.literal(" detected but its API could not be resolved (version mismatch?)")
                    .withStyle(ChatFormatting.RED));
        }

        try {
            Object manager = Environmentz.GET_TEMPERATURE_MANAGER.invoke(entity);
            int playerTemperature = (int) Environmentz.GET_PLAYER_TEMPERATURE.invoke(manager);
            int thermometerTemperature = Environmentz.GET_THERMOMETER_TEMPERATURE != null ? (int) Environmentz.GET_THERMOMETER_TEMPERATURE.invoke(manager) : 0;
            float modifier = environmentzDehydrationModifier(entity);
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

    /**
     * EnvironmentZ exposes its per-player temperature state through a mixin-injected interface
     * ({@code TemperatureManagerAccess}) rather than a dedicated API jar, so the manager and
     * {@code getPlayerTemperature()} are resolved reflectively instead of via a compile-time
     * dependency. The hot/cold thresholds themselves are NOT read from EnvironmentZ — dehydration
     * scaling is driven entirely by {@link ThirstConfig#ENVIRONMENTZ_TEMPERATURE_TIERS}, a
     * user-configured list of raw {@code playerTemperature} cutoffs, so tuning doesn't depend on
     * EnvironmentZ's own (datapack-configurable) tier boundaries.
     *
     * <p>Note: {@code TemperatureManager.isHotEnvAffected()}/{@code isColdEnvAffected()} are NOT "is
     * the player currently hot/cold" state — both default to {@code true} and only ever flip to
     * indicate a player is immune to that side entirely. {@code getPlayerTemperature()} is the
     * correct signal, exactly as EnvironmentZ's own HUD/debuff logic uses it.
     */
    public static int getEnvironmentzPlayerTemperature(LivingEntity entity) {
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

    public static int getEnvironmentzThermometerTemperature(LivingEntity entity) {
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

    public static boolean isEnvironmentzThermometerVisible() {
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

    public static int getEnvironmentzThermometerIconX() {
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

    public static int getEnvironmentzThermometerIconY() {
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
                ClassLoader classLoader = ThirstCompat.class.getClassLoader();
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
