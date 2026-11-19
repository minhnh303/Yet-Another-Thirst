package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.modSection;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.bool;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.color;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.decimal;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.enumeration;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.list;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.string;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMPAT;

/**
 * Third-party mod compatibility options. Options marked {@code clientOnly()} are read only by the local client
 * (tooltips, HUD text); compat files are never synced from a server, so they stay editable while connected to one.
 */
public final class CompatOptions {
    public static final ConfigOption<Boolean> APPLE_SKIN_THIRST_TOOLTIP =
            bool(COMPAT, "appleSkinThirstTooltip").clientOnly();
    public static final ConfigOption<Boolean> APPLE_SKIN_THIRST_HUD_PREVIEW =
            bool(COMPAT, "appleSkinThirstHudPreview").clientOnly();

    public static final ConfigOption<String> TOUGH_AS_NAILS_MODE =
            enumeration(COMPAT, "toughAsNailsMode", ThirstConfig.ToughAsNailsMode.values());

    public static final ConfigOption<Boolean> COLD_SWEAT_DEHYDRATION_MODIFIER =
            bool(COMPAT, "coldSweatDehydrationModifier");
    public static final ConfigOption<Boolean> COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS =
            bool(COMPAT, "coldSweatReplacesEnvironmentModifiers");
    public static final ConfigOption<Double> COLD_SWEAT_HOT_BODY_TEMPERATURE =
            decimal(COMPAT, "coldSweatHotBodyTemperature", -150, 150, 0.5);
    public static final ConfigOption<Double> COLD_SWEAT_BURNING_BODY_TEMPERATURE =
            decimal(COMPAT, "coldSweatBurningBodyTemperature", -150, 150, 0.5);
    public static final ConfigOption<Double> COLD_SWEAT_MAX_DEHYDRATION_MODIFIER =
            decimal(COMPAT, "coldSweatMaxDehydrationModifier", 1, 10, 0.05).format(Format.MULTIPLIER);

    public static final ConfigOption<Boolean> VAMPIRISM_VAMPIRE_SUSPENDS_THIRST =
            bool(COMPAT, "vampirismVampireSuspendsThirst");
    public static final ConfigOption<Boolean> SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST =
            bool(COMPAT, "supernaturalVampireSuspendsThirst");

    public static final ConfigOption<Boolean> ENVIRONMENTZ_DEHYDRATION_MODIFIER =
            bool(COMPAT, "environmentzDehydrationModifier");
    public static final ConfigOption<Boolean> ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS =
            bool(COMPAT, "environmentzReplacesEnvironmentModifiers");
    public static final ConfigOption<List<TemperatureTier>> ENVIRONMENTZ_TEMPERATURE_TIERS =
            list(COMPAT, "environmentzTemperatureTiers", ListSchema.temperatureTiers());
    public static final ConfigOption<Boolean> ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT =
            bool(COMPAT, "environmentzShowTemperatureText").clientOnly();
    public static final ConfigOption<String> ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE =
            enumeration(COMPAT, "environmentzTemperatureTextType", "PLAYER", "THERMOMETER", "ENVIRONMENT").clientOnly();
    public static final ConfigOption<String> ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT =
            string(COMPAT, "environmentzTemperatureTextUnit").clientOnly();
    public static final ConfigOption<Integer> ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET =
            integer(COMPAT, "environmentzTemperatureTextXOffset", -512, 512).stepper().clientOnly();
    public static final ConfigOption<Integer> ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET =
            integer(COMPAT, "environmentzTemperatureTextYOffset", -512, 512).stepper().clientOnly();
    public static final ConfigOption<Integer> ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR =
            color(COMPAT, "environmentzTemperatureTextColor").clientOnly();

    private CompatOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("compat", () -> new ItemStack(Items.ENDER_EYE),
                        modSection("appleskin", "appleskin",
                                APPLE_SKIN_THIRST_TOOLTIP,
                                APPLE_SKIN_THIRST_HUD_PREVIEW),
                        modSection("toughasnails", "toughasnails",
                                TOUGH_AS_NAILS_MODE),
                        modSection("cold_sweat", "cold_sweat",
                                COLD_SWEAT_DEHYDRATION_MODIFIER,
                                COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS,
                                COLD_SWEAT_HOT_BODY_TEMPERATURE,
                                COLD_SWEAT_BURNING_BODY_TEMPERATURE,
                                COLD_SWEAT_MAX_DEHYDRATION_MODIFIER),
                        modSection("vampirism", "vampirism",
                                VAMPIRISM_VAMPIRE_SUSPENDS_THIRST),
                        modSection("supernatural", "supernatural",
                                SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST),
                        modSection("environmentz", "environmentz",
                                ENVIRONMENTZ_DEHYDRATION_MODIFIER,
                                ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS,
                                ENVIRONMENTZ_TEMPERATURE_TIERS,
                                ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT,
                                ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE,
                                ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT,
                                ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET,
                                ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET,
                                ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR))
        );
    }
}

