package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.client.config.ConfigCategory.Section;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Format;
import dev.minhnh.yetanotherthirst.client.config.ConfigOption.Type;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.CLIENT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMMON;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.COMPAT;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;

/**
 * Registry of every option the config screen edits, organized by category. Ranges mirror the Forge/NeoForge
 * specs so the screen can never write a value those loaders would reject; keys a loader does not define
 * (e.g. EnvironmentZ on Forge) are hidden at runtime through {@code IConfigBackend#has}.
 */
public final class ConfigOptions {

    private static final List<ConfigCategory> REGISTERED_CATEGORIES = new ArrayList<>();
    private static final Map<String, ConfigOption<?>> BY_KEY = new LinkedHashMap<>();

    public static final List<ConfigCategory> CATEGORIES = Collections.unmodifiableList(REGISTERED_CATEGORIES);

    static {
        registerGeneral();
        registerDepletion();
        registerDehydration();
        registerEffects();
        registerDrinking();
        registerPurity();
        registerMachines();
        registerItems();
        registerCompat();
        registerHud();
    }

    private ConfigOptions() {
    }

    public static void register(ConfigCategory category) {
        REGISTERED_CATEGORIES.add(category);
        for (ConfigOption<?> option : category.options()) {
            BY_KEY.put(option.key, option);
        }
    }

    public static ConfigOption<?> byKey(String key) {
        return BY_KEY.get(key);
    }

    private static void registerGeneral() {
        register(category("general", () -> PotionContents.createItemStack(Items.POTION, Potions.WATER),
                section("basics",
                        bool(COMMON, "extraHydrationConvertsToQuenched"),
                        bool(COMMON, "thirstDepletesInPeaceful"),
                        bool(COMMON, "depletesWhenNausea"),
                        bool(COMMON, "dehydrationHaltsHealthRegen")),
                section("sprint",
                        bool(COMMON, "sprintPrevention"),
                        integer(COMMON, "sprintThreshold", 0, 20).format(Format.DROPLETS)),
                section("debug",
                        bool(COMMON, "debugLogging"))));
    }

    private static void registerDepletion() {
        register(category("depletion", () -> new ItemStack(Items.BLAZE_POWDER),
                section("rates",
                        decimal(COMMON, "thirstDepletionModifier", 0, 10, 0.05).format(Format.MULTIPLIER),
                        decimal(COMMON, "netherThirstDepletionModifier", 1, 10, 0.05).format(Format.MULTIPLIER)),
                section("environment",
                        bool(COMMON, "biomeDehydrationModifier"),
                        decimal(COMMON, "environmentModifierHarshness", 0, 1, 0.05).format(Format.PERCENT),
                        bool(COMMON, "fireProtectionDehydrationModifier"),
                        decimal(COMMON, "fireResistanceDehydrationModifier", 0, 1, 0.05).format(Format.MULTIPLIER))));
    }

    private static void registerDehydration() {
        register(category("dehydration", () -> new ItemStack(Items.DEAD_BUSH),
                section("damage",
                        decimal(COMMON, "dehydrationDamage", 0, 100, 0.5).format(Format.HEARTS).field(),
                        integer(COMMON, "damageIntervalTicks", 1, 72000).format(Format.TICKS).field()),
                section("health_floor",
                        decimal(COMMON, "dehydrationDamageEasyLimit", 0, 20, 0.5).format(Format.HEARTS),
                        decimal(COMMON, "dehydrationDamageNormalLimit", 0, 20, 0.5).format(Format.HEARTS),
                        decimal(COMMON, "dehydrationDamageHardLimit", 0, 20, 0.5).format(Format.HEARTS))));
    }

    private static void registerEffects() {
        register(category("effects", () -> new ItemStack(Items.BREWING_STAND),
                section("potency",
                        integer(COMMON, "hydrationEffectThirstPerTick", 0, 20),
                        integer(COMMON, "hydrationEffectQuenchedPerTick", 0, 20),
                        decimal(COMMON, "thirstyEffectExhaustionPerTick", 0, 4, 0.005).field()),
                section("effect_rules",
                        list(COMMON, "suspendThirstEffects", ListSchema.effects()),
                        list(COMMON, "pauseDepletionEffects", ListSchema.effects()),
                        list(COMMON, "regenThirstEffects", ListSchema.effects())),
                section("regen",
                        integer(COMMON, "regenThirstInterval", 1, 72000).format(Format.TICKS).field(),
                        integer(COMMON, "regenThirstAmount", 1, 20))));
    }

    private static void registerDrinking() {
        register(category("drinking", () -> new ItemStack(Items.WATER_BUCKET),
                section("rain",
                        bool(COMMON, "drinkRainWater")),
                section("hand",
                        bool(COMMON, "canDrinkByHand"),
                        integer(COMMON, "handDrinkingThirst", 0, 20),
                        integer(COMMON, "handDrinkingQuenched", 0, 20),
                        bool(COMMON, "drinkBothHandsNeeded"))));
    }

    private static void registerPurity() {
        register(category("purity", () -> new ItemStack(Items.CAULDRON),
                section("water",
                        integer(COMMON, "defaultPurity", 0, 3).format(Format.PURITY),
                        bool(COMMON, "quenchWhenDebuffed")),
                section("sickness_dirty",
                        chance("dirtyNauseaChance"), chance("dirtyPoisonChance")),
                section("sickness_slightly_dirty",
                        chance("slightlyDirtyNauseaChance"), chance("slightlyDirtyPoisonChance")),
                section("sickness_acceptable",
                        chance("acceptableNauseaChance"), chance("acceptablePoisonChance")),
                section("sickness_purified",
                        chance("purifiedNauseaChance"), chance("purifiedPoisonChance")),
                section("sources",
                        integer(COMMON, "mountainsY", -64, 320).format(Format.BLOCK_Y),
                        integer(COMMON, "cavesY", -64, 320).format(Format.BLOCK_Y),
                        integer(COMMON, "runningWaterPurificationAmount", 0, 3))));
    }

    private static void registerMachines() {
        register(category("machines", () -> new ItemStack(Items.HOPPER),
                section("filter",
                        integer(ITEMS, "filterBaseSpeed", 1, 1000).field(),
                        decimal(ITEMS, "dirtyWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field(),
                        decimal(ITEMS, "slightlyDirtyWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field(),
                        decimal(ITEMS, "acceptableWaterDecayMultiplier", 0, 100, 0.1).format(Format.MULTIPLIER).field()),
                section("core_fabric",
                        integer(ITEMS, "fabricFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field(),
                        integer(ITEMS, "fabricFilterMaxPurity", 0, 3).format(Format.PURITY)),
                section("core_sand",
                        integer(ITEMS, "sandFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field(),
                        integer(ITEMS, "sandFilterMaxPurity", 0, 3).format(Format.PURITY)),
                section("core_carbon",
                        integer(ITEMS, "carbonFilterDurability", 1, 10_000_000).format(Format.MILLIBUCKETS).field(),
                        integer(ITEMS, "carbonFilterMaxPurity", 0, 3).format(Format.PURITY)),
                section("boiler",
                        integer(ITEMS, "waterBoilerCapacity", 1000, 1_000_000).format(Format.MILLIBUCKETS).field(),
                        integer(ITEMS, "waterBoilerBoilTime", 1, 72000).format(Format.TICKS).field(),
                        integer(ITEMS, "waterBoilerEnergyConsumption", 1, 100_000).field())));
    }

    private static void registerItems() {
        register(category("items", () -> new ItemStack(Items.MELON_SLICE),
                section("stack",
                        integer(ITEMS, "waterBottleStackSize", 1, 64).rejoin()),
                section("item_values",
                        list(ITEMS, "drinks", ListSchema.itemValues()),
                        list(ITEMS, "foods", ListSchema.itemValues()),
                        list(ITEMS, "itemsBlacklist", ListSchema.items()))));
    }

    private static void registerCompat() {
        register(category("compat", () -> new ItemStack(Items.ENDER_EYE),
                modSection("appleskin", "appleskin",
                        bool(COMPAT, "appleSkinThirstTooltip").clientOnly(),
                        bool(COMPAT, "appleSkinThirstHudPreview").clientOnly()),
                modSection("toughasnails", "toughasnails",
                        enumeration(COMPAT, "toughAsNailsMode", ThirstConfig.ToughAsNailsMode.values())),
                modSection("cold_sweat", "cold_sweat",
                        bool(COMPAT, "coldSweatDehydrationModifier"),
                        bool(COMPAT, "coldSweatReplacesEnvironmentModifiers"),
                        decimal(COMPAT, "coldSweatHotBodyTemperature", -150, 150, 0.5),
                        decimal(COMPAT, "coldSweatBurningBodyTemperature", -150, 150, 0.5),
                        decimal(COMPAT, "coldSweatMaxDehydrationModifier", 1, 10, 0.05).format(Format.MULTIPLIER)),
                modSection("vampirism", "vampirism",
                        bool(COMPAT, "vampirismVampireSuspendsThirst")),
                modSection("supernatural", "supernatural",
                        bool(COMPAT, "supernaturalVampireSuspendsThirst")),
                modSection("environmentz", "environmentz",
                        bool(COMPAT, "environmentzDehydrationModifier"),
                        bool(COMPAT, "environmentzReplacesEnvironmentModifiers"),
                        list(COMPAT, "environmentzTemperatureTiers", ListSchema.temperatureTiers()),
                        bool(COMPAT, "environmentzShowTemperatureText").clientOnly(),
                        enumeration(COMPAT, "environmentzTemperatureTextType", "PLAYER", "THERMOMETER", "ENVIRONMENT").clientOnly(),
                        string(COMPAT, "environmentzTemperatureTextUnit").clientOnly(),
                        integer(COMPAT, "environmentzTemperatureTextXOffset", -512, 512).stepper().clientOnly(),
                        integer(COMPAT, "environmentzTemperatureTextYOffset", -512, 512).stepper().clientOnly(),
                        color(COMPAT, "environmentzTemperatureTextColor").clientOnly())));
    }

    private static void registerHud() {
        register(category("hud", () -> new ItemStack(Items.SPYGLASS),
                new Section("hud_position", null, true, List.of(
                        integer(CLIENT, "hudXOffset", -512, 512).stepper(),
                        integer(CLIENT, "hudYOffset", -512, 512).stepper()))));
    }

    private static ConfigCategory category(String id, Supplier<ItemStack> icon, Section... sections) {
        return new ConfigCategory(id, icon, List.of(sections));
    }

    private static Section section(String id, ConfigOption<?>... options) {
        return new Section(id, null, false, List.of(options));
    }

    private static Section modSection(String id, String modId, ConfigOption<?>... options) {
        for (ConfigOption<?> option : options) {
            option.requiredMod = modId;
        }
        return new Section(id, modId, false, List.of(options));
    }

    private static ConfigOption<Boolean> bool(ConfigFile file, String key) {
        return new ConfigOption<>(file, key, Type.BOOL);
    }

    private static ConfigOption<String> string(ConfigFile file, String key) {
        return new ConfigOption<>(file, key, Type.STRING);
    }

    private static ConfigOption<Integer> color(ConfigFile file, String key) {
        ConfigOption<Integer> option = new ConfigOption<>(file, key, Type.COLOR);
        option.max = 0xFFFFFF;
        return option;
    }

    private static ConfigOption<Integer> integer(ConfigFile file, String key, int min, int max) {
        ConfigOption<Integer> option = new ConfigOption<>(file, key, Type.INT);
        option.min = min;
        option.max = max;
        return option;
    }

    private static ConfigOption<Double> decimal(ConfigFile file, String key, double min, double max, double step) {
        ConfigOption<Double> option = new ConfigOption<>(file, key, Type.DECIMAL);
        option.min = min;
        option.max = max;
        option.step = step;
        return option;
    }

    private static ConfigOption<Double> chance(String key) {
        return decimal(COMMON, key, 0, 1, 0.01).format(Format.PERCENT);
    }

    private static ConfigOption<String> enumeration(ConfigFile file, String key, Enum<?>... values) {
        return enumeration(file, key, Arrays.stream(values).map(Enum::name).toArray(String[]::new));
    }

    private static ConfigOption<String> enumeration(ConfigFile file, String key, String... values) {
        ConfigOption<String> option = new ConfigOption<>(file, key, Type.ENUM);
        option.enumValues = List.of(values);
        return option;
    }

    private static <T> ConfigOption<List<T>> list(ConfigFile file, String key, ListSchema<T> schema) {
        ConfigOption<List<T>> option = new ConfigOption<>(file, key, Type.LIST);
        option.schema = schema;
        return option;
    }
}
