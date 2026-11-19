package dev.minhnh.yetanotherthirst.core.thirst;

public final class ThirstConfig {

    public static final int MAX_THIRST = 20;
    public static final int DEFAULT_THIRST = 20;
    public static final int DEFAULT_QUENCHED = 5;
    public static final float EXHAUSTION_LIMIT = 4.0F;
    public static final int SYNC_INTERVAL_TICKS = 10;
    public static int DAMAGE_INTERVAL_TICKS = 40;
    public static float DEHYDRATION_DAMAGE = 1.0F;
    public static float DEHYDRATION_DAMAGE_EASY_LIMIT = 10.0F;
    public static float DEHYDRATION_DAMAGE_NORMAL_LIMIT = 0.0F;
    public static float DEHYDRATION_DAMAGE_HARD_LIMIT = 0.0F;

    public static boolean EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = true;
    public static boolean THIRST_DEPLETES_IN_PEACEFUL = false;
    public static boolean DRINK_RAIN_WATER = true;
    public static boolean DEPLETES_WHEN_NAUSEA = true;
    public static boolean SPRINT_PREVENTION = true;
    public static int SPRINT_THRESHOLD = 6;
    public static boolean DEHYDRATION_HALTS_HEALTH_REGEN = true;

    public static float THIRST_DEPLETION_MODIFIER = 1.2F;
    public static float NETHER_THIRST_DEPLETION_MODIFIER = 3.0F;
    public static float FIRE_RESISTANCE_DEHYDRATION_MODIFIER = 0.0F;
    public static boolean BIOME_DEHYDRATION_MODIFIER = true;
    public static boolean FIRE_PROTECTION_DEHYDRATION_MODIFIER = true;
    public static float ENVIRONMENT_MODIFIER_HARSHNESS = 0.5F;

    // Purity system
    public static int DEFAULT_PURITY = 2;
    public static boolean QUENCH_WHEN_DEBUFFED = true;
    public static float DIRTY_NAUSEA_CHANCE = 1.0F;
    public static float DIRTY_POISON_CHANCE = 0.3F;
    public static float SLIGHTLY_DIRTY_NAUSEA_CHANCE = 0.5F;
    public static float SLIGHTLY_DIRTY_POISON_CHANCE = 0.1F;
    public static float ACCEPTABLE_NAUSEA_CHANCE = 0.05F;
    public static float ACCEPTABLE_POISON_CHANCE = 0.0F;
    public static float PURIFIED_NAUSEA_CHANCE = 0.0F;
    public static float PURIFIED_POISON_CHANCE = 0.0F;

    // World purity thresholds
    public static int MOUNTAINS_Y = 100;
    public static int CAVES_Y = 48;
    public static int RUNNING_WATER_PURIFICATION_AMOUNT = 1;

    // Modular Filter System Config
    public static int FILTER_BASE_SPEED = 1;
    public static float DIRTY_WATER_DECAY_MULTIPLIER = 4.0F;
    public static float SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = 2.0F;
    public static float ACCEPTABLE_WATER_DECAY_MULTIPLIER = 1.0F;

    public static int FABRIC_FILTER_DURABILITY = 10000;
    public static int FABRIC_FILTER_MAX_PURITY = 1;

    public static int SAND_FILTER_DURABILITY = 50000;
    public static int SAND_FILTER_MAX_PURITY = 2;

    public static int CARBON_FILTER_DURABILITY = 250000;
    public static int CARBON_FILTER_MAX_PURITY = 3;

    // Water Boiler Config
    public static int WATER_BOILER_CAPACITY = 2000;
    public static int WATER_BOILER_BOIL_TIME = 200;
    public static int WATER_BOILER_ENERGY_CONSUMPTION = 32;

    // Hand drinking
    public static boolean CAN_DRINK_BY_HAND = false;
    public static int HAND_DRINKING_THIRST = 3;
    public static int HAND_DRINKING_QUENCHED = 2;
    public static boolean DRINK_BOTH_HANDS_NEEDED = false;

    // Items
    public static int WATER_BOTTLE_STACKSIZE = 64;

    // Client-side only — set by the loader's client config
    public static int HUD_X_OFFSET = 0;
    public static int HUD_Y_OFFSET = 0;

    // Mod compatibility flags (set by loader during common setup)
    public static boolean COMPAT_TOMBSTONE = false;
    public static boolean COMPAT_VAMPIRISM = false;
    public static boolean COMPAT_FARMERS_DELIGHT = false;
    public static boolean COMPAT_LETS_DO_BAKERY = false;
    public static boolean COMPAT_LETS_DO_BREWERY = false;
    public static boolean COMPAT_LETS_DO_FARM_AND_CHARM = false;
    public static boolean COMPAT_APPLESKIN = false;
    public static boolean COMPAT_JADE = false;
    public static boolean COMPAT_TOUGH_AS_NAILS = false;
    public static boolean COMPAT_COLD_SWEAT = false;
    public static boolean COMPAT_SUPERNATURAL = false;
    public static boolean COMPAT_IE_HEATER = false;
    public static boolean COMPAT_ENVIRONMENTZ = false;

    // Mod compatibility behavior
    public static boolean APPLESKIN_THIRST_TOOLTIP = true;
    public static boolean APPLESKIN_THIRST_HUD_PREVIEW = true;
    public static ToughAsNailsMode TOUGH_AS_NAILS_MODE = ToughAsNailsMode.AUTO_DISABLE_YAT;
    public static boolean COLD_SWEAT_DEHYDRATION_MODIFIER = true;
    public static boolean COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS = true;
    public static float COLD_SWEAT_HOT_BODY_TEMPERATURE = 50.0F;
    public static float COLD_SWEAT_BURNING_BODY_TEMPERATURE = 100.0F;
    public static float COLD_SWEAT_MAX_DEHYDRATION_MODIFIER = 1.75F;
    public static boolean ENVIRONMENTZ_DEHYDRATION_MODIFIER = true;
    public static boolean ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS = true;
    public static boolean ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT = true;
    public static String ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE = "PLAYER";
    public static String ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT = "";
    public static int ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET = 0;
    public static int ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET = 0;
    public static int ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR = 0xFFFFFF;

    /**
     * User-defined EnvironmentZ {@code playerTemperature} -> dehydration modifier tiers, e.g.
     * "playerTemperature >= 20 applies 1.0x". Always kept sorted ascending by threshold; the
     * modifier used is the one for the highest threshold at or below the player's current body
     * temperature. Temperatures below every configured threshold get the neutral 1.0x modifier.
     */
    public static java.util.List<TemperatureTier> ENVIRONMENTZ_TEMPERATURE_TIERS = defaultEnvironmentzTemperatureTiers();

    public static java.util.List<TemperatureTier> defaultEnvironmentzTemperatureTiers() {
        return new java.util.ArrayList<>(java.util.List.of(
                new TemperatureTier(-1000, 0.5F),
                new TemperatureTier(-50, 0.85F),
                new TemperatureTier(20, 1.0F),
                new TemperatureTier(50, 1.5F),
                new TemperatureTier(100, 2.5F)
        ));
    }

    public static void setEnvironmentzTemperatureTiers(java.util.List<TemperatureTier> tiers) {
        java.util.List<TemperatureTier> sorted = new java.util.ArrayList<>(tiers);
        sorted.sort(java.util.Comparator.comparingInt(tier -> tier.threshold));
        ENVIRONMENTZ_TEMPERATURE_TIERS = sorted;
    }

    /**
     * Returns the tier with the highest threshold not above {@code temperature}, or null if none.
     * Independent of list order so in-progress GUI edits (unsorted until save) still resolve correctly.
     */
    public static TemperatureTier findActiveTemperatureTier(int temperature) {
        TemperatureTier active = null;
        for (TemperatureTier tier : ENVIRONMENTZ_TEMPERATURE_TIERS) {
            if (temperature >= tier.threshold && (active == null || tier.threshold >= active.threshold)) {
                active = tier;
            }
        }
        return active;
    }

    public static final class TemperatureTier {
        public final int threshold;
        public final float modifier;

        public TemperatureTier(int threshold, float modifier) {
            this.threshold = threshold;
            this.modifier = modifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TemperatureTier that)) return false;
            return threshold == that.threshold && Float.compare(that.modifier, modifier) == 0;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(threshold, modifier);
        }
    }
    public static boolean SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST = true;
    public static boolean VAMPIRISM_VAMPIRE_SUSPENDS_THIRST = true;

    // Configured effects that suspend thirst
    public static java.util.List<String> SUSPENDED_THIRST_EFFECTS_RAW = new java.util.ArrayList<>(defaultSuspendThirstEffects());
    public static java.util.List<EffectCondition> SUSPENDED_THIRST_EFFECTS = new java.util.ArrayList<>();

    // Configured effects that pause thirst depletion
    public static java.util.List<String> PAUSE_DEPLETION_EFFECTS_RAW = new java.util.ArrayList<>(defaultPauseDepletionEffects());
    public static java.util.List<EffectCondition> PAUSE_DEPLETION_EFFECTS = new java.util.ArrayList<>();

    public static java.util.List<String> defaultSuspendThirstEffects() {
        return java.util.List.of("tombstone:ghostly_shape");
    }

    public static java.util.List<String> defaultPauseDepletionEffects() {
        return java.util.List.of(
                "farmersdelight:nourishment",
                "bakery:stuffed",
                "farm_and_charm:satiation",
                "farm_and_charm:sustenance",
                "farm_and_charm:feast"
        );
    }

    public static java.util.List<String> defaultRegenThirstEffects() {
        return java.util.List.of("farmersdelight:nourishment", "farm_and_charm:sustenance");
    }

    public static void setPauseDepletionEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        PAUSE_DEPLETION_EFFECTS_RAW = list;
        PAUSE_DEPLETION_EFFECTS = conditions;
    }

    // Configured effects that regenerate thirst
    public static java.util.List<String> REGEN_THIRST_EFFECTS_RAW = new java.util.ArrayList<>(defaultRegenThirstEffects());
    public static java.util.List<EffectCondition> REGEN_THIRST_EFFECTS = new java.util.ArrayList<>();
    public static int REGEN_THIRST_INTERVAL = 40;
    public static int REGEN_THIRST_AMOUNT = 1;
    public static int HYDRATION_EFFECT_THIRST_PER_TICK = 1;
    public static int HYDRATION_EFFECT_QUENCHED_PER_TICK = 1;
    public static float THIRSTY_EFFECT_EXHAUSTION_PER_TICK = 0.005F;

    // Configured item values & blacklist (used by Config GUI and loader config persistence)
    public static java.util.Map<String, ThirstValue> CONFIGURED_DRINKS = defaultDrinksMap();
    public static java.util.Map<String, ThirstValue> CONFIGURED_FOODS = defaultFoodsMap();
    public static java.util.Set<String> CONFIGURED_BLACKLIST = defaultBlacklistSet();

    static {
        setSuspendThirstEffects(defaultSuspendThirstEffects());
        setPauseDepletionEffects(defaultPauseDepletionEffects());
        setRegenThirstEffects(defaultRegenThirstEffects());
    }

    public static java.util.Map<String, ThirstValue> defaultDrinksMap() {
        return parseRawItemMap(dev.minhnh.yetanotherthirst.DefaultItemValues.defaultDrinkValues());
    }

    public static java.util.Map<String, ThirstValue> defaultFoodsMap() {
        return parseRawItemMap(dev.minhnh.yetanotherthirst.DefaultItemValues.defaultFoodValues());
    }

    public static java.util.Set<String> defaultBlacklistSet() {
        return new java.util.LinkedHashSet<>(java.util.List.of(
                "yet_another_thirst:example_item_1",
                "yet_another_thirst:example_item_2"
        ));
    }

    public static java.util.Map<String, ThirstValue> parseRawItemMap(java.util.List<? extends java.util.List<?>> entries) {
        java.util.Map<String, ThirstValue> map = new java.util.LinkedHashMap<>();
        for (java.util.List<?> row : entries) {
            if (row.size() >= 3 && row.get(0) instanceof String id
                    && row.get(1) instanceof Number t && row.get(2) instanceof Number q) {
                // A repeated id moves to its last position so later rows (e.g. tags) are applied in file order.
                map.remove(id);
                map.put(id, new ThirstValue(t.intValue(), q.intValue()));
            }
        }
        return map;
    }

    public static java.util.List<java.util.List<Object>> toRawItemList(java.util.Map<String, ThirstValue> map) {
        java.util.List<java.util.List<Object>> list = new java.util.ArrayList<>();
        for (var entry : map.entrySet()) {
            list.add(java.util.List.of(entry.getKey(), entry.getValue().thirst(), entry.getValue().quenched()));
        }
        return list;
    }

    public static void setRegenThirstEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        REGEN_THIRST_EFFECTS_RAW = list;
        REGEN_THIRST_EFFECTS = conditions;
    }

    public static void setSuspendThirstEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        SUSPENDED_THIRST_EFFECTS_RAW = list;
        SUSPENDED_THIRST_EFFECTS = conditions;
    }

    public static Snapshot takeSnapshot() {
        return new Snapshot();
    }

    public static void restoreSnapshot(Snapshot s) {
        s.restore();
    }

    public static final class Snapshot {
        private final int damageIntervalTicks = DAMAGE_INTERVAL_TICKS;
        private final float dehydrationDamage = DEHYDRATION_DAMAGE;
        private final float dehydrationDamageEasyLimit = DEHYDRATION_DAMAGE_EASY_LIMIT;
        private final float dehydrationDamageNormalLimit = DEHYDRATION_DAMAGE_NORMAL_LIMIT;
        private final float dehydrationDamageHardLimit = DEHYDRATION_DAMAGE_HARD_LIMIT;
        private final boolean extraHydrationConvertsToQuenched = EXTRA_HYDRATION_CONVERTS_TO_QUENCHED;
        private final boolean thirstDepletesInPeaceful = THIRST_DEPLETES_IN_PEACEFUL;
        private final boolean drinkRainWater = DRINK_RAIN_WATER;
        private final boolean depletesWhenNausea = DEPLETES_WHEN_NAUSEA;
        private final boolean sprintPrevention = SPRINT_PREVENTION;
        private final int sprintThreshold = SPRINT_THRESHOLD;
        private final boolean dehydrationHaltsHealthRegen = DEHYDRATION_HALTS_HEALTH_REGEN;
        private final float thirstDepletionModifier = THIRST_DEPLETION_MODIFIER;
        private final float netherThirstDepletionModifier = NETHER_THIRST_DEPLETION_MODIFIER;
        private final float fireResistanceDehydrationModifier = FIRE_RESISTANCE_DEHYDRATION_MODIFIER;
        private final boolean biomeDehydrationModifier = BIOME_DEHYDRATION_MODIFIER;
        private final boolean fireProtectionDehydrationModifier = FIRE_PROTECTION_DEHYDRATION_MODIFIER;
        private final float environmentModifierHarshness = ENVIRONMENT_MODIFIER_HARSHNESS;
        private final int defaultPurity = DEFAULT_PURITY;
        private final boolean quenchWhenDebuffed = QUENCH_WHEN_DEBUFFED;
        private final float dirtyNauseaChance = DIRTY_NAUSEA_CHANCE;
        private final float dirtyPoisonChance = DIRTY_POISON_CHANCE;
        private final float slightlyDirtyNauseaChance = SLIGHTLY_DIRTY_NAUSEA_CHANCE;
        private final float slightlyDirtyPoisonChance = SLIGHTLY_DIRTY_POISON_CHANCE;
        private final float acceptableNauseaChance = ACCEPTABLE_NAUSEA_CHANCE;
        private final float acceptablePoisonChance = ACCEPTABLE_POISON_CHANCE;
        private final float purifiedNauseaChance = PURIFIED_NAUSEA_CHANCE;
        private final float purifiedPoisonChance = PURIFIED_POISON_CHANCE;
        private final int mountainsY = MOUNTAINS_Y;
        private final int cavesY = CAVES_Y;
        private final int runningWaterPurificationAmount = RUNNING_WATER_PURIFICATION_AMOUNT;
        private final int filterBaseSpeed = FILTER_BASE_SPEED;
        private final float dirtyWaterDecayMultiplier = DIRTY_WATER_DECAY_MULTIPLIER;
        private final float slightlyDirtyWaterDecayMultiplier = SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER;
        private final float acceptableWaterDecayMultiplier = ACCEPTABLE_WATER_DECAY_MULTIPLIER;
        private final int fabricFilterDurability = FABRIC_FILTER_DURABILITY;
        private final int fabricFilterMaxPurity = FABRIC_FILTER_MAX_PURITY;
        private final int sandFilterDurability = SAND_FILTER_DURABILITY;
        private final int sandFilterMaxPurity = SAND_FILTER_MAX_PURITY;
        private final int carbonFilterDurability = CARBON_FILTER_DURABILITY;
        private final int carbonFilterMaxPurity = CARBON_FILTER_MAX_PURITY;
        private final int waterBoilerCapacity = WATER_BOILER_CAPACITY;
        private final int waterBoilerBoilTime = WATER_BOILER_BOIL_TIME;
        private final int waterBoilerEnergyConsumption = WATER_BOILER_ENERGY_CONSUMPTION;
        private final boolean canDrinkByHand = CAN_DRINK_BY_HAND;
        private final int handDrinkingThirst = HAND_DRINKING_THIRST;
        private final int handDrinkingQuenched = HAND_DRINKING_QUENCHED;
        private final boolean drinkBothHandsNeeded = DRINK_BOTH_HANDS_NEEDED;
        private final int waterBottleStackSize = WATER_BOTTLE_STACKSIZE;
        private final int hudXOffset = HUD_X_OFFSET;
        private final int hudYOffset = HUD_Y_OFFSET;
        private final boolean appleSkinThirstTooltip = APPLESKIN_THIRST_TOOLTIP;
        private final boolean appleSkinThirstHudPreview = APPLESKIN_THIRST_HUD_PREVIEW;
        private final ToughAsNailsMode toughAsNailsMode = TOUGH_AS_NAILS_MODE;
        private final boolean coldSweatDehydrationModifier = COLD_SWEAT_DEHYDRATION_MODIFIER;
        private final boolean coldSweatReplacesEnvironmentModifiers = COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS;
        private final float coldSweatHotBodyTemperature = COLD_SWEAT_HOT_BODY_TEMPERATURE;
        private final float coldSweatBurningBodyTemperature = COLD_SWEAT_BURNING_BODY_TEMPERATURE;
        private final float coldSweatMaxDehydrationModifier = COLD_SWEAT_MAX_DEHYDRATION_MODIFIER;
        private final boolean environmentzDehydrationModifier = ENVIRONMENTZ_DEHYDRATION_MODIFIER;
        private final boolean environmentzReplacesEnvironmentModifiers = ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS;
        private final boolean environmentzShowTemperatureText = ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT;
        private final String environmentzTemperatureTextType = ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE;
        private final String environmentzTemperatureTextUnit = ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT;
        private final int environmentzTemperatureTextXOffset = ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET;
        private final int environmentzTemperatureTextYOffset = ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET;
        private final int environmentzTemperatureTextColor = ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR;
        private final java.util.List<TemperatureTier> environmentzTemperatureTiers = new java.util.ArrayList<>(ENVIRONMENTZ_TEMPERATURE_TIERS);
        private final boolean supernaturalVampireSuspendsThirst = SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST;
        private final boolean vampirismVampireSuspendsThirst = VAMPIRISM_VAMPIRE_SUSPENDS_THIRST;
        private final java.util.List<String> suspendedThirstEffectsRaw = new java.util.ArrayList<>(SUSPENDED_THIRST_EFFECTS_RAW);
        private final java.util.List<String> pauseDepletionEffectsRaw = new java.util.ArrayList<>(PAUSE_DEPLETION_EFFECTS_RAW);
        private final java.util.List<String> regenThirstEffectsRaw = new java.util.ArrayList<>(REGEN_THIRST_EFFECTS_RAW);
        private final int regenThirstInterval = REGEN_THIRST_INTERVAL;
        private final int regenThirstAmount = REGEN_THIRST_AMOUNT;
        private final int hydrationEffectThirstPerTick = HYDRATION_EFFECT_THIRST_PER_TICK;
        private final int hydrationEffectQuenchedPerTick = HYDRATION_EFFECT_QUENCHED_PER_TICK;
        private final float thirstyEffectExhaustionPerTick = THIRSTY_EFFECT_EXHAUSTION_PER_TICK;
        private final java.util.Map<String, ThirstValue> configuredDrinks = new java.util.LinkedHashMap<>(CONFIGURED_DRINKS);
        private final java.util.Map<String, ThirstValue> configuredFoods = new java.util.LinkedHashMap<>(CONFIGURED_FOODS);
        private final java.util.Set<String> configuredBlacklist = new java.util.LinkedHashSet<>(CONFIGURED_BLACKLIST);

        void restore() {
            DAMAGE_INTERVAL_TICKS = damageIntervalTicks;
            DEHYDRATION_DAMAGE = dehydrationDamage;
            DEHYDRATION_DAMAGE_EASY_LIMIT = dehydrationDamageEasyLimit;
            DEHYDRATION_DAMAGE_NORMAL_LIMIT = dehydrationDamageNormalLimit;
            DEHYDRATION_DAMAGE_HARD_LIMIT = dehydrationDamageHardLimit;
            EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = extraHydrationConvertsToQuenched;
            THIRST_DEPLETES_IN_PEACEFUL = thirstDepletesInPeaceful;
            DRINK_RAIN_WATER = drinkRainWater;
            DEPLETES_WHEN_NAUSEA = depletesWhenNausea;
            SPRINT_PREVENTION = sprintPrevention;
            SPRINT_THRESHOLD = sprintThreshold;
            DEHYDRATION_HALTS_HEALTH_REGEN = dehydrationHaltsHealthRegen;
            THIRST_DEPLETION_MODIFIER = thirstDepletionModifier;
            NETHER_THIRST_DEPLETION_MODIFIER = netherThirstDepletionModifier;
            FIRE_RESISTANCE_DEHYDRATION_MODIFIER = fireResistanceDehydrationModifier;
            BIOME_DEHYDRATION_MODIFIER = biomeDehydrationModifier;
            FIRE_PROTECTION_DEHYDRATION_MODIFIER = fireProtectionDehydrationModifier;
            ENVIRONMENT_MODIFIER_HARSHNESS = environmentModifierHarshness;
            DEFAULT_PURITY = defaultPurity;
            QUENCH_WHEN_DEBUFFED = quenchWhenDebuffed;
            DIRTY_NAUSEA_CHANCE = dirtyNauseaChance;
            DIRTY_POISON_CHANCE = dirtyPoisonChance;
            SLIGHTLY_DIRTY_NAUSEA_CHANCE = slightlyDirtyNauseaChance;
            SLIGHTLY_DIRTY_POISON_CHANCE = slightlyDirtyPoisonChance;
            ACCEPTABLE_NAUSEA_CHANCE = acceptableNauseaChance;
            ACCEPTABLE_POISON_CHANCE = acceptablePoisonChance;
            PURIFIED_NAUSEA_CHANCE = purifiedNauseaChance;
            PURIFIED_POISON_CHANCE = purifiedPoisonChance;
            MOUNTAINS_Y = mountainsY;
            CAVES_Y = cavesY;
            RUNNING_WATER_PURIFICATION_AMOUNT = runningWaterPurificationAmount;
            FILTER_BASE_SPEED = filterBaseSpeed;
            DIRTY_WATER_DECAY_MULTIPLIER = dirtyWaterDecayMultiplier;
            SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = slightlyDirtyWaterDecayMultiplier;
            ACCEPTABLE_WATER_DECAY_MULTIPLIER = acceptableWaterDecayMultiplier;
            FABRIC_FILTER_DURABILITY = fabricFilterDurability;
            FABRIC_FILTER_MAX_PURITY = fabricFilterMaxPurity;
            SAND_FILTER_DURABILITY = sandFilterDurability;
            SAND_FILTER_MAX_PURITY = sandFilterMaxPurity;
            CARBON_FILTER_DURABILITY = carbonFilterDurability;
            CARBON_FILTER_MAX_PURITY = carbonFilterMaxPurity;
            WATER_BOILER_CAPACITY = waterBoilerCapacity;
            WATER_BOILER_BOIL_TIME = waterBoilerBoilTime;
            WATER_BOILER_ENERGY_CONSUMPTION = waterBoilerEnergyConsumption;
            CAN_DRINK_BY_HAND = canDrinkByHand;
            HAND_DRINKING_THIRST = handDrinkingThirst;
            HAND_DRINKING_QUENCHED = handDrinkingQuenched;
            DRINK_BOTH_HANDS_NEEDED = drinkBothHandsNeeded;
            WATER_BOTTLE_STACKSIZE = waterBottleStackSize;
            HUD_X_OFFSET = hudXOffset;
            HUD_Y_OFFSET = hudYOffset;
            APPLESKIN_THIRST_TOOLTIP = appleSkinThirstTooltip;
            APPLESKIN_THIRST_HUD_PREVIEW = appleSkinThirstHudPreview;
            TOUGH_AS_NAILS_MODE = toughAsNailsMode;
            COLD_SWEAT_DEHYDRATION_MODIFIER = coldSweatDehydrationModifier;
            COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS = coldSweatReplacesEnvironmentModifiers;
            COLD_SWEAT_HOT_BODY_TEMPERATURE = coldSweatHotBodyTemperature;
            COLD_SWEAT_BURNING_BODY_TEMPERATURE = coldSweatBurningBodyTemperature;
            COLD_SWEAT_MAX_DEHYDRATION_MODIFIER = coldSweatMaxDehydrationModifier;
            ENVIRONMENTZ_DEHYDRATION_MODIFIER = environmentzDehydrationModifier;
            ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS = environmentzReplacesEnvironmentModifiers;
            ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT = environmentzShowTemperatureText;
            ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE = environmentzTemperatureTextType;
            ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT = environmentzTemperatureTextUnit;
            ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET = environmentzTemperatureTextXOffset;
            ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET = environmentzTemperatureTextYOffset;
            ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR = environmentzTemperatureTextColor;
            setEnvironmentzTemperatureTiers(environmentzTemperatureTiers);
            SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST = supernaturalVampireSuspendsThirst;
            VAMPIRISM_VAMPIRE_SUSPENDS_THIRST = vampirismVampireSuspendsThirst;
            setSuspendThirstEffects(suspendedThirstEffectsRaw);
            setPauseDepletionEffects(pauseDepletionEffectsRaw);
            setRegenThirstEffects(regenThirstEffectsRaw);
            REGEN_THIRST_INTERVAL = regenThirstInterval;
            REGEN_THIRST_AMOUNT = regenThirstAmount;
            HYDRATION_EFFECT_THIRST_PER_TICK = hydrationEffectThirstPerTick;
            HYDRATION_EFFECT_QUENCHED_PER_TICK = hydrationEffectQuenchedPerTick;
            THIRSTY_EFFECT_EXHAUSTION_PER_TICK = thirstyEffectExhaustionPerTick;
            CONFIGURED_DRINKS = new java.util.LinkedHashMap<>(configuredDrinks);
            CONFIGURED_FOODS = new java.util.LinkedHashMap<>(configuredFoods);
            CONFIGURED_BLACKLIST = new java.util.LinkedHashSet<>(configuredBlacklist);
        }
    }

    public static class EffectCondition {
        public final net.minecraft.resources.ResourceLocation effectId;
        public final String operator;
        public final int amplifier;

        public EffectCondition(net.minecraft.resources.ResourceLocation effectId, String operator, int amplifier) {
            this.effectId = effectId;
            this.operator = operator;
            this.amplifier = amplifier;
        }

        public static EffectCondition parse(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }
            input = input.trim();
            String operator = null;
            int opIndex = -1;
            // Longest operators first to prevent "=" from matching inside ">=", "<=", "==", or "!="
            String[] operators = {">=", "<=", "==", "!=", ">", "<", "="};
            for (String op : operators) {
                int idx = input.indexOf(op);
                if (idx != -1) {
                    operator = op;
                    opIndex = idx;
                    break;
                }
            }

            net.minecraft.resources.ResourceLocation effectId;
            int amplifier = 0;
            if (operator != null) {
                String idPart = input.substring(0, opIndex).trim();
                String valPart = input.substring(opIndex + operator.length()).trim();
                try {
                    effectId = new net.minecraft.resources.ResourceLocation(idPart);
                } catch (Exception e) {
                    effectId = null;
                }
                try {
                    amplifier = Integer.parseInt(valPart);
                } catch (NumberFormatException e) {
                    amplifier = 0;
                }
            } else {
                try {
                    effectId = new net.minecraft.resources.ResourceLocation(input);
                } catch (Exception e) {
                    effectId = null;
                }
                operator = ">=";
                amplifier = 0;
            }

            if (effectId == null) {
                return null;
            }
            return new EffectCondition(effectId, operator, amplifier);
        }

        public boolean matches(int activeAmplifier) {
            if (amplifier < 0) {
                return true;
            }
            switch (operator) {
                case ">=":
                    return activeAmplifier >= amplifier;
                case ">":
                    return activeAmplifier > amplifier;
                case "<=":
                    return activeAmplifier <= amplifier;
                case "<":
                    return activeAmplifier < amplifier;
                case "==":
                case "=":
                    return activeAmplifier == amplifier;
                case "!=":
                    return activeAmplifier != amplifier;
                default:
                    return activeAmplifier >= amplifier;
            }
        }
    }

    public enum ToughAsNailsMode {
        OFF,
        AUTO_DISABLE_YAT,
        ITEMS_ONLY,
        FORCE_YAT
    }

    private ThirstConfig() {
    }
}
