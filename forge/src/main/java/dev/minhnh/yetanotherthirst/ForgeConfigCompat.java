package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ForgeConfigCompat {

    static final ForgeConfigSpec SPEC;

    @SuppressWarnings("unused")
    private static final ForgeConfigSpec.IntValue CONFIG_VERSION;
    private static final ForgeConfigSpec.BooleanValue APPLESKIN_THIRST_TOOLTIP;
    private static final ForgeConfigSpec.BooleanValue APPLESKIN_THIRST_HUD_PREVIEW;
    private static final ForgeConfigSpec.EnumValue<ThirstConfig.ToughAsNailsMode> TOUGH_AS_NAILS_MODE;
    private static final ForgeConfigSpec.BooleanValue COLD_SWEAT_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS;
    private static final ForgeConfigSpec.DoubleValue COLD_SWEAT_HOT_BODY_TEMPERATURE;
    private static final ForgeConfigSpec.DoubleValue COLD_SWEAT_BURNING_BODY_TEMPERATURE;
    private static final ForgeConfigSpec.DoubleValue COLD_SWEAT_MAX_DEHYDRATION_MODIFIER;
    private static final ForgeConfigSpec.BooleanValue SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST;
    private static final ForgeConfigSpec.BooleanValue VAMPIRISM_VAMPIRE_SUSPENDS_THIRST;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        CONFIG_VERSION = builder
                .comment("Config file version — do not modify")
                .defineInRange("configVersion", 1, 1, Integer.MAX_VALUE);

        builder.push("compatibility");
        APPLESKIN_THIRST_TOOLTIP = builder
                .comment("When AppleSkin is loaded, add YAT thirst and quenched values to item tooltips")
                .define("appleSkinThirstTooltip", true);
        APPLESKIN_THIRST_HUD_PREVIEW = builder
                .comment("When AppleSkin is loaded, preview held item thirst restoration on the thirst HUD")
                .define("appleSkinThirstHudPreview", true);
        TOUGH_AS_NAILS_MODE = builder
                .comment("Tough As Nails handling mode",
                        "OFF: ignore Tough As Nails",
                        "AUTO_DISABLE_YAT: hide and stop YAT thirst while Tough As Nails is loaded",
                        "ITEMS_ONLY: keep YAT thirst active and use configured TAN drink item values",
                        "FORCE_YAT: keep YAT thirst active even when Tough As Nails is loaded")
                .defineEnum("toughAsNailsMode", ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT);
        COLD_SWEAT_DEHYDRATION_MODIFIER = builder
                .comment("Use Cold Sweat body temperature to increase dehydration when the player is hot")
                .define("coldSweatDehydrationModifier", true);
        COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS = builder
                .comment("When Cold Sweat is loaded, replace YAT biome/Nether temperature modifiers with Cold Sweat body temperature")
                .define("coldSweatReplacesEnvironmentModifiers", true);
        COLD_SWEAT_HOT_BODY_TEMPERATURE = builder
                .comment("Cold Sweat body temperature where extra dehydration starts")
                .defineInRange("coldSweatHotBodyTemperature", 50.0, -150.0, 150.0);
        COLD_SWEAT_BURNING_BODY_TEMPERATURE = builder
                .comment("Cold Sweat body temperature where extra dehydration reaches the configured maximum")
                .defineInRange("coldSweatBurningBodyTemperature", 100.0, -150.0, 150.0);
        COLD_SWEAT_MAX_DEHYDRATION_MODIFIER = builder
                .comment("Maximum dehydration multiplier applied at or above coldSweatBurningBodyTemperature")
                .defineInRange("coldSweatMaxDehydrationModifier", 1.75, 1.0, 10.0);
        SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST = builder
                .comment("Suspend YAT thirst and hide the thirst HUD for Supernatural vampires")
                .define("supernaturalVampireSuspendsThirst", true);
        VAMPIRISM_VAMPIRE_SUSPENDS_THIRST = builder
                .comment("Suspend YAT thirst and hide the thirst HUD for Vampirism vampires")
                .define("vampirismVampireSuspendsThirst", true);
        builder.pop();

        SPEC = builder.build();
    }

    static void sync() {
        ThirstConfig.APPLESKIN_THIRST_TOOLTIP = APPLESKIN_THIRST_TOOLTIP.get();
        ThirstConfig.APPLESKIN_THIRST_HUD_PREVIEW = APPLESKIN_THIRST_HUD_PREVIEW.get();
        ThirstConfig.TOUGH_AS_NAILS_MODE = TOUGH_AS_NAILS_MODE.get();
        ThirstConfig.COLD_SWEAT_DEHYDRATION_MODIFIER = COLD_SWEAT_DEHYDRATION_MODIFIER.get();
        ThirstConfig.COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS = COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS.get();
        ThirstConfig.COLD_SWEAT_HOT_BODY_TEMPERATURE = COLD_SWEAT_HOT_BODY_TEMPERATURE.get().floatValue();
        ThirstConfig.COLD_SWEAT_BURNING_BODY_TEMPERATURE = COLD_SWEAT_BURNING_BODY_TEMPERATURE.get().floatValue();
        ThirstConfig.COLD_SWEAT_MAX_DEHYDRATION_MODIFIER = COLD_SWEAT_MAX_DEHYDRATION_MODIFIER.get().floatValue();
        ThirstConfig.SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST = SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST.get();
        ThirstConfig.VAMPIRISM_VAMPIRE_SUSPENDS_THIRST = VAMPIRISM_VAMPIRE_SUSPENDS_THIRST.get();
    }

    private ForgeConfigCompat() {}
}
