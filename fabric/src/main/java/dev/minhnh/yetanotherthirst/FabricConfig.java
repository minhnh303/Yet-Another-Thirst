package dev.minhnh.yetanotherthirst;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class FabricConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR_NAME = "yetanotherthirst";

    private static JsonObject commonJson = buildDefaultCommon();
    private static JsonObject itemsJson = buildDefaultItems();
    private static JsonObject compatJson = buildDefaultCompat();
    private static JsonObject clientJson = buildDefaultClient();

    // Keys belonging to each split file
    static final Set<String> ITEMS_KEYS = Set.of(
            "waterBottleStackSize", "drinks", "foods", "itemsBlacklist",
            "filterBaseSpeed", "dirtyWaterDecayMultiplier", "slightlyDirtyWaterDecayMultiplier",
            "acceptableWaterDecayMultiplier", "fabricFilterDurability", "fabricFilterMaxPurity",
            "sandFilterDurability", "sandFilterMaxPurity", "carbonFilterDurability", "carbonFilterMaxPurity",
            "waterBoilerCapacity", "waterBoilerBoilTime", "waterBoilerEnergyConsumption"
    );
    static final Set<String> COMPAT_KEYS = Set.of(
            "appleSkinThirstTooltip", "appleSkinThirstHudPreview", "toughAsNailsMode",
            "coldSweatDehydrationModifier", "coldSweatReplacesEnvironmentModifiers",
            "coldSweatHotBodyTemperature", "coldSweatBurningBodyTemperature",
            "coldSweatMaxDehydrationModifier", "supernaturalVampireSuspendsThirst",
            "vampirismVampireSuspendsThirst", "environmentzDehydrationModifier",
            "environmentzReplacesEnvironmentModifiers", "environmentzTemperatureTiers",
            "environmentzShowTemperatureText", "environmentzTemperatureTextType",
            "environmentzTemperatureTextUnit", "environmentzTemperatureTextXOffset",
            "environmentzTemperatureTextYOffset", "environmentzTemperatureTextColor"
    );

    private FabricConfig() {}

    private static Path newDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR_NAME);
    }

    private static Path oldDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(Constants.MOD_ID);
    }

    private static Path path(IConfigBackend.ConfigFile file) {
        Path dir = newDir();
        return switch (file) {
            case COMMON -> dir.resolve("common.json");
            case ITEMS -> dir.resolve("items.json");
            case COMPAT -> dir.resolve("compat.json");
            case CLIENT -> dir.resolve("client.json");
        };
    }

    public static void load() {
        try {
            Files.createDirectories(newDir());
        } catch (IOException e) {
            Constants.LOG.error("Failed to create config directory", e);
        }

        migrate();

        commonJson = loadOrCreate(path(IConfigBackend.ConfigFile.COMMON), buildDefaultCommon());
        itemsJson = loadOrCreate(path(IConfigBackend.ConfigFile.ITEMS), buildDefaultItems());
        compatJson = loadOrCreate(path(IConfigBackend.ConfigFile.COMPAT), buildDefaultCompat());
        clientJson = loadOrCreate(path(IConfigBackend.ConfigFile.CLIENT), buildDefaultClient());
        sync();
    }

    private static void migrate() {
        Path oldDirPath = oldDir();
        Path oldCommon = oldDirPath.resolve("common.json");
        if (!Files.exists(oldCommon)) return;

        Constants.LOG.info("[YAT] Migrating Fabric config from '{}' to '{}'", Constants.MOD_ID, CONFIG_DIR_NAME);

        try (Reader r = Files.newBufferedReader(oldCommon)) {
            JsonObject old = GSON.fromJson(r, JsonObject.class);

            JsonObject newCommon = buildDefaultCommon();
            JsonObject newItems = buildDefaultItems();
            JsonObject newCompat = buildDefaultCompat();

            for (var entry : old.entrySet()) {
                String key = entry.getKey();
                if (ITEMS_KEYS.contains(key)) {
                    newItems.add(key, entry.getValue());
                } else if (COMPAT_KEYS.contains(key)) {
                    newCompat.add(key, entry.getValue());
                } else {
                    newCommon.add(key, entry.getValue());
                }
            }

            Path commonPath = path(IConfigBackend.ConfigFile.COMMON);
            Path itemsPath = path(IConfigBackend.ConfigFile.ITEMS);
            Path compatPath = path(IConfigBackend.ConfigFile.COMPAT);
            Path clientPath = path(IConfigBackend.ConfigFile.CLIENT);

            if (!Files.exists(commonPath)) writeJson(commonPath, newCommon);
            if (!Files.exists(itemsPath)) writeJson(itemsPath, newItems);
            if (!Files.exists(compatPath)) writeJson(compatPath, newCompat);

            Path oldClient = oldDirPath.resolve("client.json");
            if (Files.exists(oldClient) && !Files.exists(clientPath)) {
                Files.copy(oldClient, clientPath);
            }

            Files.delete(oldCommon);
            if (Files.exists(oldClient)) Files.delete(oldClient);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(oldDirPath)) {
                if (!stream.iterator().hasNext()) Files.delete(oldDirPath);
            }
            Constants.LOG.info("[YAT] Fabric config migration complete.");
        } catch (Exception e) {
            Constants.LOG.error("[YAT] Fabric config migration failed", e);
        }
    }

    /** Best-effort write for load and migration, where a failure only means defaults are not persisted yet. */
    private static void writeJson(Path path, JsonObject obj) {
        try {
            writeAtomically(path, obj);
        } catch (IOException e) {
            Constants.LOG.error("Failed to write config {}", path, e);
        }
    }

    /** Writes to a sibling temp file, then moves it over {@code path}, so a failed write never truncates the config. */
    private static void writeAtomically(Path path, JsonObject obj) throws IOException {
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer w = Files.newBufferedWriter(temp)) {
            GSON.toJson(obj, w);
        }
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static void sync() {
        ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = getBool(commonJson, "extraHydrationConvertsToQuenched", true);
        ThirstConfig.THIRST_DEPLETES_IN_PEACEFUL = getBool(commonJson, "thirstDepletesInPeaceful", false);
        ThirstConfig.DRINK_RAIN_WATER = getBool(commonJson, "drinkRainWater", true);
        ThirstConfig.DEPLETES_WHEN_NAUSEA = getBool(commonJson, "depletesWhenNausea", true);
        ThirstConfig.SPRINT_PREVENTION = getBool(commonJson, "sprintPrevention", true);
        ThirstConfig.SPRINT_THRESHOLD = getInt(commonJson, "sprintThreshold", 6);
        ThirstConfig.DEHYDRATION_HALTS_HEALTH_REGEN = getBool(commonJson, "dehydrationHaltsHealthRegen", true);

        ThirstConfig.setSuspendThirstEffects(getStringList(commonJson, "suspendThirstEffects",
                List.of("tombstone:ghostly_shape")));
        ThirstConfig.setPauseDepletionEffects(getStringList(commonJson, "pauseDepletionEffects",
                List.of("farmersdelight:nourishment", "bakery:stuffed", "farm_and_charm:satiation",
                        "farm_and_charm:sustenance", "farm_and_charm:feast")));
        ThirstConfig.setRegenThirstEffects(getStringList(commonJson, "regenThirstEffects",
                List.of("farmersdelight:nourishment", "farm_and_charm:sustenance")));
        ThirstConfig.REGEN_THIRST_INTERVAL = getInt(commonJson, "regenThirstInterval", 40);
        ThirstConfig.REGEN_THIRST_AMOUNT = getInt(commonJson, "regenThirstAmount", 1);

        ThirstConfig.HYDRATION_EFFECT_THIRST_PER_TICK = getInt(commonJson, "hydrationEffectThirstPerTick", 1);
        ThirstConfig.HYDRATION_EFFECT_QUENCHED_PER_TICK = getInt(commonJson, "hydrationEffectQuenchedPerTick", 1);
        ThirstConfig.THIRSTY_EFFECT_EXHAUSTION_PER_TICK = (float) getDouble(commonJson, "thirstyEffectExhaustionPerTick", 0.005);

        ThirstConfig.DEHYDRATION_DAMAGE = (float) getDouble(commonJson, "dehydrationDamage", 1.0);
        ThirstConfig.DAMAGE_INTERVAL_TICKS = getInt(commonJson, "damageIntervalTicks", 40);
        ThirstConfig.DEHYDRATION_DAMAGE_EASY_LIMIT = (float) getDouble(commonJson, "dehydrationDamageEasyLimit", 10.0);
        ThirstConfig.DEHYDRATION_DAMAGE_NORMAL_LIMIT = (float) getDouble(commonJson, "dehydrationDamageNormalLimit", 0.0);
        ThirstConfig.DEHYDRATION_DAMAGE_HARD_LIMIT = (float) getDouble(commonJson, "dehydrationDamageHardLimit", 0.0);

        ThirstConfig.THIRST_DEPLETION_MODIFIER = (float) getDouble(commonJson, "thirstDepletionModifier", 1.2);
        ThirstConfig.NETHER_THIRST_DEPLETION_MODIFIER = (float) getDouble(commonJson, "netherThirstDepletionModifier", 3.0);
        ThirstConfig.FIRE_RESISTANCE_DEHYDRATION_MODIFIER = (float) getDouble(commonJson, "fireResistanceDehydrationModifier", 0.0);
        ThirstConfig.BIOME_DEHYDRATION_MODIFIER = getBool(commonJson, "biomeDehydrationModifier", true);
        ThirstConfig.FIRE_PROTECTION_DEHYDRATION_MODIFIER = getBool(commonJson, "fireProtectionDehydrationModifier", true);
        ThirstConfig.ENVIRONMENT_MODIFIER_HARSHNESS = (float) getDouble(commonJson, "environmentModifierHarshness", 0.5);

        ThirstConfig.DEFAULT_PURITY = getInt(commonJson, "defaultPurity", 2);
        ThirstConfig.QUENCH_WHEN_DEBUFFED = getBool(commonJson, "quenchWhenDebuffed", true);
        ThirstConfig.DIRTY_NAUSEA_CHANCE = (float) getDouble(commonJson, "dirtyNauseaChance", 1.0);
        ThirstConfig.DIRTY_POISON_CHANCE = (float) getDouble(commonJson, "dirtyPoisonChance", 0.3);
        ThirstConfig.SLIGHTLY_DIRTY_NAUSEA_CHANCE = (float) getDouble(commonJson, "slightlyDirtyNauseaChance", 0.5);
        ThirstConfig.SLIGHTLY_DIRTY_POISON_CHANCE = (float) getDouble(commonJson, "slightlyDirtyPoisonChance", 0.1);
        ThirstConfig.ACCEPTABLE_NAUSEA_CHANCE = (float) getDouble(commonJson, "acceptableNauseaChance", 0.05);
        ThirstConfig.ACCEPTABLE_POISON_CHANCE = (float) getDouble(commonJson, "acceptablePoisonChance", 0.0);
        ThirstConfig.PURIFIED_NAUSEA_CHANCE = (float) getDouble(commonJson, "purifiedNauseaChance", 0.0);
        ThirstConfig.PURIFIED_POISON_CHANCE = (float) getDouble(commonJson, "purifiedPoisonChance", 0.0);

        ThirstConfig.MOUNTAINS_Y = getInt(commonJson, "mountainsY", 100);
        ThirstConfig.CAVES_Y = getInt(commonJson, "cavesY", 48);
        ThirstConfig.RUNNING_WATER_PURIFICATION_AMOUNT = getInt(commonJson, "runningWaterPurificationAmount", 1);

        ThirstConfig.CAN_DRINK_BY_HAND = getBool(commonJson, "canDrinkByHand", false);
        ThirstConfig.HAND_DRINKING_THIRST = getInt(commonJson, "handDrinkingThirst", 3);
        ThirstConfig.HAND_DRINKING_QUENCHED = getInt(commonJson, "handDrinkingQuenched", 2);
        ThirstConfig.DRINK_BOTH_HANDS_NEEDED = getBool(commonJson, "drinkBothHandsNeeded", false);
        ThirstConfig.DEBUG_LOGGING = getBool(commonJson, "debugLogging", false);

        // items
        ThirstConfig.WATER_BOTTLE_STACKSIZE = getInt(itemsJson, "waterBottleStackSize", 64);
        ThirstConfig.FILTER_BASE_SPEED = getInt(itemsJson, "filterBaseSpeed", 1);
        ThirstConfig.DIRTY_WATER_DECAY_MULTIPLIER = (float) getDouble(itemsJson, "dirtyWaterDecayMultiplier", 4.0);
        ThirstConfig.SLIGHTLY_DIRTY_WATER_DECAY_MULTIPLIER = (float) getDouble(itemsJson, "slightlyDirtyWaterDecayMultiplier", 2.0);
        ThirstConfig.ACCEPTABLE_WATER_DECAY_MULTIPLIER = (float) getDouble(itemsJson, "acceptableWaterDecayMultiplier", 1.0);
        ThirstConfig.FABRIC_FILTER_DURABILITY = getInt(itemsJson, "fabricFilterDurability", 10000);
        ThirstConfig.FABRIC_FILTER_MAX_PURITY = getInt(itemsJson, "fabricFilterMaxPurity", 1);
        ThirstConfig.SAND_FILTER_DURABILITY = getInt(itemsJson, "sandFilterDurability", 50000);
        ThirstConfig.SAND_FILTER_MAX_PURITY = getInt(itemsJson, "sandFilterMaxPurity", 2);
        ThirstConfig.CARBON_FILTER_DURABILITY = getInt(itemsJson, "carbonFilterDurability", 250000);
        ThirstConfig.CARBON_FILTER_MAX_PURITY = getInt(itemsJson, "carbonFilterMaxPurity", 3);
        ThirstConfig.WATER_BOILER_CAPACITY = getInt(itemsJson, "waterBoilerCapacity", 2000);
        ThirstConfig.WATER_BOILER_BOIL_TIME = getInt(itemsJson, "waterBoilerBoilTime", 200);
        ThirstConfig.WATER_BOILER_ENERGY_CONSUMPTION = getInt(itemsJson, "waterBoilerEnergyConsumption", 32);

        // compat
        ThirstConfig.APPLESKIN_THIRST_TOOLTIP = getBool(compatJson, "appleSkinThirstTooltip", true);
        ThirstConfig.APPLESKIN_THIRST_HUD_PREVIEW = getBool(compatJson, "appleSkinThirstHudPreview", true);
        ThirstConfig.TOUGH_AS_NAILS_MODE = ThirstConfig.ToughAsNailsMode.valueOf(
                getString(compatJson, "toughAsNailsMode", ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT.name()));
        ThirstConfig.COLD_SWEAT_DEHYDRATION_MODIFIER = getBool(compatJson, "coldSweatDehydrationModifier", true);
        ThirstConfig.COLD_SWEAT_REPLACES_ENVIRONMENT_MODIFIERS = getBool(compatJson, "coldSweatReplacesEnvironmentModifiers", true);
        ThirstConfig.COLD_SWEAT_HOT_BODY_TEMPERATURE = (float) getDouble(compatJson, "coldSweatHotBodyTemperature", 50.0);
        ThirstConfig.COLD_SWEAT_BURNING_BODY_TEMPERATURE = (float) getDouble(compatJson, "coldSweatBurningBodyTemperature", 100.0);
        ThirstConfig.COLD_SWEAT_MAX_DEHYDRATION_MODIFIER = (float) getDouble(compatJson, "coldSweatMaxDehydrationModifier", 1.75);
        ThirstConfig.SUPERNATURAL_VAMPIRE_SUSPENDS_THIRST = getBool(compatJson, "supernaturalVampireSuspendsThirst", true);
        ThirstConfig.VAMPIRISM_VAMPIRE_SUSPENDS_THIRST = getBool(compatJson, "vampirismVampireSuspendsThirst", true);
        ThirstConfig.ENVIRONMENTZ_DEHYDRATION_MODIFIER = getBool(compatJson, "environmentzDehydrationModifier", true);
        ThirstConfig.ENVIRONMENTZ_REPLACES_ENVIRONMENT_MODIFIERS = getBool(compatJson, "environmentzReplacesEnvironmentModifiers", true);
        ThirstConfig.setEnvironmentzTemperatureTiers(
                getTemperatureTiers(compatJson, "environmentzTemperatureTiers", defaultEnvironmentzTemperatureTiers()));
        ThirstConfig.ENVIRONMENTZ_SHOW_TEMPERATURE_TEXT = getBool(compatJson, "environmentzShowTemperatureText", true);
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_TYPE = getString(compatJson, "environmentzTemperatureTextType", "PLAYER");
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_UNIT = getString(compatJson, "environmentzTemperatureTextUnit", "");
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_X_OFFSET = getInt(compatJson, "environmentzTemperatureTextXOffset", 0);
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_Y_OFFSET = getInt(compatJson, "environmentzTemperatureTextYOffset", 0);
        ThirstConfig.ENVIRONMENTZ_TEMPERATURE_TEXT_COLOR = getInt(compatJson, "environmentzTemperatureTextColor", 0xFFFFFF);

        // client
        ThirstConfig.HUD_X_OFFSET = getInt(clientJson, "hudXOffset", 0);
        ThirstConfig.HUD_Y_OFFSET = getInt(clientJson, "hudYOffset", 0);
    }

    public static void reloadThirstValues() {
        Map<Item, ThirstValue> drinks = parseItemValues(getStringListList(itemsJson, "drinks", DefaultItemValues.defaultDrinkValues()));
        Map<Item, ThirstValue> foods = parseItemValues(getStringListList(itemsJson, "foods", DefaultItemValues.defaultFoodValues()));
        Set<Item> blacklist = parseItems(getStringList(itemsJson, "itemsBlacklist",
                List.of("yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2")));

        if (dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL != null) {
            Item tw = dev.minhnh.yetanotherthirst.core.item.ModItems.TERRACOTTA_WATER_BOWL.get();
            Item ww = dev.minhnh.yetanotherthirst.core.item.ModItems.WOODEN_WATER_BOWL.get();
            if (tw != Items.AIR) drinks.putIfAbsent(tw, new ThirstValue(5, 7));
            if (ww != Items.AIR) drinks.putIfAbsent(ww, new ThirstValue(5, 7));
        }

        ThirstValues.replaceConfiguredValues(drinks, foods, blacklist);
    }

    // ── In-game config screen access ──────────────────────────────────────────

    static JsonObject json(IConfigBackend.ConfigFile file) {
        return switch (file) {
            case COMMON -> commonJson;
            case ITEMS -> itemsJson;
            case COMPAT -> compatJson;
            case CLIENT -> clientJson;
        };
    }

    static JsonObject defaults(IConfigBackend.ConfigFile file) {
        return switch (file) {
            case COMMON -> buildDefaultCommon();
            case ITEMS -> buildDefaultItems();
            case COMPAT -> buildDefaultCompat();
            case CLIENT -> buildDefaultClient();
        };
    }

    static void resetToDefaults() {
        commonJson = buildDefaultCommon();
        itemsJson = buildDefaultItems();
        compatJson = buildDefaultCompat();
        clientJson = buildDefaultClient();
        sync();
    }

    /** Writes one file for the config screen; throws so the screen keeps the edits pending when it fails. */
    static void write(IConfigBackend.ConfigFile file) {
        Path path = path(file);
        try {
            writeAtomically(path, json(file));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write config " + path, e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static JsonObject loadOrCreate(Path path, JsonObject defaults) {
        if (Files.exists(path)) {
            try (Reader r = Files.newBufferedReader(path)) {
                JsonObject loaded = GSON.fromJson(r, JsonObject.class);
                if (loaded == null) {
                    loaded = new JsonObject();
                }
                if (addMissingKeys(loaded, defaults)) {
                    writeJson(path, loaded);
                }
                return loaded;
            } catch (Exception e) {
                Constants.LOG.error("Failed to read config {}", path, e);
                backUpUnreadable(path);
            }
        }
        writeJson(path, defaults);
        return defaults;
    }

    /** Keeps an unreadable config (e.g. a JSON syntax error) beside the defaults written in its place, so edits can be recovered. */
    private static void backUpUnreadable(Path path) {
        Path backup = path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis());
        try {
            Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
            Constants.LOG.warn("Unreadable config {} was copied to {} and replaced with defaults", path, backup);
        } catch (IOException e) {
            Constants.LOG.error("Could not back up unreadable config {}", path, e);
        }
    }

    /**
     * Adds any key present in {@code defaults} but missing from {@code target} — e.g. a setting
     * introduced by a mod update — without touching keys the user already has on disk.
     *
     * @return true if at least one key was added.
     */
    private static boolean addMissingKeys(JsonObject target, JsonObject defaults) {
        boolean changed = false;
        for (var entry : defaults.entrySet()) {
            if (!target.has(entry.getKey())) {
                target.add(entry.getKey(), entry.getValue());
                changed = true;
            }
        }
        return changed;
    }

    private static boolean getBool(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }

    private static int getInt(JsonObject obj, String key, int def) {
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }

    private static double getDouble(JsonObject obj, String key, double def) {
        return obj.has(key) ? obj.get(key).getAsDouble() : def;
    }

    private static String getString(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private static List<String> getStringList(JsonObject obj, String key, List<String> def) {
        if (!obj.has(key)) return def;
        List<String> result = new ArrayList<>();
        obj.getAsJsonArray(key).forEach(e -> result.add(e.getAsString()));
        return result;
    }

    private static List<List<Object>> getStringListList(JsonObject obj, String key, List<List<Object>> def) {
        if (!obj.has(key)) return def;
        List<List<Object>> result = new ArrayList<>();
        obj.getAsJsonArray(key).forEach(e -> {
            var arr = e.getAsJsonArray();
            result.add(List.of(arr.get(0).getAsString(), arr.get(1).getAsInt(), arr.get(2).getAsInt()));
        });
        return result;
    }

    private static Map<Item, ThirstValue> parseItemValues(List<List<Object>> entries) {
        Map<Item, ThirstValue> values = new LinkedHashMap<>();
        for (var entry : entries) {
            if (entry.size() < 3) continue;
            String id = (String) entry.get(0);
            ThirstValue value = new ThirstValue(((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).intValue());
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag ->
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(h -> values.put(h.value(), value)));
            } else {
                resolveItem(id).ifPresent(item -> values.put(item, value));
            }
        }
        return values;
    }

    private static Set<Item> parseItems(List<String> entries) {
        Set<Item> items = new HashSet<>();
        for (String id : entries) {
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag ->
                        BuiltInRegistries.ITEM.getTagOrEmpty(tag).forEach(h -> items.add(h.value())));
            } else {
                resolveItem(id).ifPresent(items::add);
            }
        }
        return items;
    }

    private static Optional<Item> resolveItem(String id) {
        try {
            var loc = ResourceLocation.parse(id);
            var item = BuiltInRegistries.ITEM.get(loc);
            if (item != Items.AIR || "minecraft:air".equals(id)) return Optional.of(item);
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<TagKey<Item>> resolveTag(String id) {
        try {
            return Optional.of(TagKey.create(net.minecraft.core.registries.Registries.ITEM, ResourceLocation.parse(id)));
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static JsonArray listOfListsToJson(List<List<Object>> data) {
        JsonArray arr = new JsonArray();
        for (var row : data) {
            JsonArray inner = new JsonArray();
            for (var v : row) inner.add(v instanceof Number n ? n.toString() : v.toString());
            // Fix: re-add as proper types
            JsonArray proper = new JsonArray();
            proper.add(row.get(0).toString());
            proper.add(((Number) row.get(1)).intValue());
            proper.add(((Number) row.get(2)).intValue());
            arr.add(proper);
        }
        return arr;
    }

    private static JsonObject buildDefaultCommon() {
        var obj = new JsonObject();
        obj.addProperty("configVersion", 1);
        obj.addProperty("extraHydrationConvertsToQuenched", true);
        obj.addProperty("thirstDepletesInPeaceful", false);
        obj.addProperty("drinkRainWater", true);
        obj.addProperty("depletesWhenNausea", true);
        obj.addProperty("sprintPrevention", true);
        obj.addProperty("sprintThreshold", 6);
        obj.addProperty("dehydrationHaltsHealthRegen", true);
        obj.add("suspendThirstEffects", stringListToJson(List.of("tombstone:ghostly_shape")));
        obj.add("pauseDepletionEffects", stringListToJson(List.of(
                "farmersdelight:nourishment", "bakery:stuffed",
                "farm_and_charm:satiation", "farm_and_charm:sustenance", "farm_and_charm:feast")));
        obj.add("regenThirstEffects", stringListToJson(List.of("farmersdelight:nourishment", "farm_and_charm:sustenance")));
        obj.addProperty("regenThirstInterval", 40);
        obj.addProperty("regenThirstAmount", 1);
        obj.addProperty("hydrationEffectThirstPerTick", 1);
        obj.addProperty("hydrationEffectQuenchedPerTick", 1);
        obj.addProperty("thirstyEffectExhaustionPerTick", 0.005);
        obj.addProperty("dehydrationDamage", 1.0);
        obj.addProperty("damageIntervalTicks", 40);
        obj.addProperty("dehydrationDamageEasyLimit", 10.0);
        obj.addProperty("dehydrationDamageNormalLimit", 0.0);
        obj.addProperty("dehydrationDamageHardLimit", 0.0);
        obj.addProperty("thirstDepletionModifier", 1.2);
        obj.addProperty("netherThirstDepletionModifier", 3.0);
        obj.addProperty("fireResistanceDehydrationModifier", 0.0);
        obj.addProperty("biomeDehydrationModifier", true);
        obj.addProperty("fireProtectionDehydrationModifier", true);
        obj.addProperty("environmentModifierHarshness", 0.5);
        obj.addProperty("defaultPurity", 2);
        obj.addProperty("quenchWhenDebuffed", true);
        obj.addProperty("dirtyNauseaChance", 1.0);
        obj.addProperty("dirtyPoisonChance", 0.3);
        obj.addProperty("slightlyDirtyNauseaChance", 0.5);
        obj.addProperty("slightlyDirtyPoisonChance", 0.1);
        obj.addProperty("acceptableNauseaChance", 0.05);
        obj.addProperty("acceptablePoisonChance", 0.0);
        obj.addProperty("purifiedNauseaChance", 0.0);
        obj.addProperty("purifiedPoisonChance", 0.0);
        obj.addProperty("mountainsY", 100);
        obj.addProperty("cavesY", 48);
        obj.addProperty("runningWaterPurificationAmount", 1);
        obj.addProperty("canDrinkByHand", false);
        obj.addProperty("handDrinkingThirst", 3);
        obj.addProperty("handDrinkingQuenched", 2);
        obj.addProperty("drinkBothHandsNeeded", false);
        obj.addProperty("debugLogging", false);
        return obj;
    }

    private static JsonObject buildDefaultItems() {
        var obj = new JsonObject();
        obj.addProperty("configVersion", 1);
        obj.addProperty("waterBottleStackSize", 64);
        obj.add("drinks", listOfListsToJson(DefaultItemValues.defaultDrinkValues()));
        obj.add("foods", listOfListsToJson(DefaultItemValues.defaultFoodValues()));
        obj.add("itemsBlacklist", stringListToJson(List.of(
                "yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2")));
        obj.addProperty("filterBaseSpeed", 1);
        obj.addProperty("dirtyWaterDecayMultiplier", 4.0);
        obj.addProperty("slightlyDirtyWaterDecayMultiplier", 2.0);
        obj.addProperty("acceptableWaterDecayMultiplier", 1.0);
        obj.addProperty("fabricFilterDurability", 10000);
        obj.addProperty("fabricFilterMaxPurity", 1);
        obj.addProperty("sandFilterDurability", 50000);
        obj.addProperty("sandFilterMaxPurity", 2);
        obj.addProperty("carbonFilterDurability", 250000);
        obj.addProperty("carbonFilterMaxPurity", 3);
        obj.addProperty("waterBoilerCapacity", 2000);
        obj.addProperty("waterBoilerBoilTime", 200);
        obj.addProperty("waterBoilerEnergyConsumption", 32);
        return obj;
    }

    private static JsonObject buildDefaultCompat() {
        var obj = new JsonObject();
        obj.addProperty("configVersion", 1);
        obj.addProperty("appleSkinThirstTooltip", true);
        obj.addProperty("appleSkinThirstHudPreview", true);
        obj.addProperty("toughAsNailsMode", ThirstConfig.ToughAsNailsMode.AUTO_DISABLE_YAT.name());
        obj.addProperty("coldSweatDehydrationModifier", true);
        obj.addProperty("coldSweatReplacesEnvironmentModifiers", true);
        obj.addProperty("coldSweatHotBodyTemperature", 50.0);
        obj.addProperty("coldSweatBurningBodyTemperature", 100.0);
        obj.addProperty("coldSweatMaxDehydrationModifier", 1.75);
        obj.addProperty("supernaturalVampireSuspendsThirst", true);
        obj.addProperty("vampirismVampireSuspendsThirst", true);
        obj.addProperty("environmentzDehydrationModifier", true);
        obj.addProperty("environmentzReplacesEnvironmentModifiers", true);
        obj.add("environmentzTemperatureTiers", temperatureTiersToJson(defaultEnvironmentzTemperatureTiers()));
        obj.addProperty("environmentzShowTemperatureText", true);
        obj.addProperty("environmentzTemperatureTextType", "PLAYER");
        obj.addProperty("environmentzTemperatureTextUnit", "");
        obj.addProperty("environmentzTemperatureTextXOffset", 0);
        obj.addProperty("environmentzTemperatureTextYOffset", 0);
        obj.addProperty("environmentzTemperatureTextColor", 0xFFFFFF);
        return obj;
    }

    /**
     * Starting point for {@code environmentzTemperatureTiers}, matching the example threshold from
     * the feature request ("playerTemperature >= 20 -> 1.0x"). EnvironmentZ's own tier boundaries
     * are datapack-configurable per server, so these are just a reasonable starting shape — use
     * {@code /thirst query} to read the live body temperature and retune to taste.
     */
    private static List<ThirstConfig.TemperatureTier> defaultEnvironmentzTemperatureTiers() {
        return new ArrayList<>(List.of(
                new ThirstConfig.TemperatureTier(-1000, 0.5F),
                new ThirstConfig.TemperatureTier(-50, 0.85F),
                new ThirstConfig.TemperatureTier(20, 1.0F),
                new ThirstConfig.TemperatureTier(50, 1.5F),
                new ThirstConfig.TemperatureTier(100, 2.5F)
        ));
    }

    private static List<ThirstConfig.TemperatureTier> getTemperatureTiers(
            JsonObject obj, String key, List<ThirstConfig.TemperatureTier> def) {
        if (!obj.has(key)) return def;
        List<ThirstConfig.TemperatureTier> result = new ArrayList<>();
        obj.getAsJsonArray(key).forEach(e -> {
            var arr = e.getAsJsonArray();
            result.add(new ThirstConfig.TemperatureTier(arr.get(0).getAsInt(), (float) arr.get(1).getAsDouble()));
        });
        return result;
    }

    private static JsonArray temperatureTiersToJson(List<ThirstConfig.TemperatureTier> tiers) {
        var arr = new JsonArray();
        for (var tier : tiers) {
            var row = new JsonArray();
            row.add(tier.threshold);
            row.add(Math.round(tier.modifier * 1000.0) / 1000.0);
            arr.add(row);
        }
        return arr;
    }

    private static JsonObject buildDefaultClient() {
        var obj = new JsonObject();
        obj.addProperty("hudXOffset", 0);
        obj.addProperty("hudYOffset", 0);
        return obj;
    }

    private static JsonArray stringListToJson(List<String> list) {
        JsonArray arr = new JsonArray();
        list.forEach(arr::add);
        return arr;
    }
}
