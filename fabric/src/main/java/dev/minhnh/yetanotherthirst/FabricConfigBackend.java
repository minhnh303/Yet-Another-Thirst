package dev.minhnh.yetanotherthirst;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** {@link IConfigBackend} over the JSON files managed by {@link FabricConfig}. */
public final class FabricConfigBackend implements IConfigBackend {

    private static final Gson GSON = new Gson();
    private final Map<ConfigFile, JsonObject> defaults = new EnumMap<>(ConfigFile.class);

    private JsonObject defaultsOf(ConfigFile file) {
        return defaults.computeIfAbsent(file, FabricConfig::defaults);
    }

    @Override
    public boolean has(ConfigFile file, String key) {
        return defaultsOf(file).has(key);
    }

    @Override
    public Object get(ConfigFile file, String key) {
        if (!has(file, key)) {
            throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        }
        JsonObject json = FabricConfig.json(file);
        JsonElement def = defaultsOf(file).get(key);
        return toJava(json.has(key) ? json.get(key) : def, def);
    }

    @Override
    public Object getDefault(ConfigFile file, String key) {
        if (!has(file, key)) {
            throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        }
        JsonElement def = defaultsOf(file).get(key);
        return toJava(def, def);
    }

    @Override
    public void set(ConfigFile file, String key, Object value) {
        if (!has(file, key)) {
            throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        }
        Object converted = convert(defaultsOf(file).get(key), value);
        FabricConfig.json(file).add(key, GSON.toJsonTree(converted));
    }

    /** Throws when a file cannot be written; values are pushed to {@code ThirstConfig} only after every write succeeded. */
    @Override
    public void save(Set<ConfigFile> files) {
        for (ConfigFile file : files) {
            FabricConfig.write(file);
        }
        FabricConfig.sync();
    }

    @Override
    public void reloadItemValues() {
        FabricConfig.reloadThirstValues();
    }

    /**
     * Converts a JSON value to the normalized Java form. Numbers take integer or decimal form from the matching
     * element of {@code template} (the default value), since Gson parses file numbers lazily and their text
     * (e.g. {@code 1e-3}) does not tell the two apart.
     */
    static Object toJava(JsonElement element, JsonElement template) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element instanceof JsonArray array) {
            JsonArray templateArray = template instanceof JsonArray t && !t.isEmpty() ? t : null;
            boolean rows = templateArray != null && templateArray.get(0).isJsonArray();
            List<Object> list = new ArrayList<>(array.size());
            for (int i = 0; i < array.size(); i++) {
                JsonElement childTemplate = templateArray == null ? null
                        : rows ? templateArray.get(0)
                        : i < templateArray.size() ? templateArray.get(i) : null;
                list.add(toJava(array.get(i), childTemplate));
            }
            return list;
        }
        if (element instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isNumber()) return toNumber(primitive, template);
            return primitive.getAsString();
        }
        return element.toString();
    }

    private static Number toNumber(JsonPrimitive primitive, JsonElement template) {
        BigDecimal value = primitive.getAsBigDecimal();
        boolean integral;
        if (template instanceof JsonPrimitive t && t.isNumber()) {
            Number def = t.getAsNumber();
            integral = def instanceof Integer || def instanceof Long || def instanceof Short || def instanceof Byte;
        } else {
            integral = value.stripTrailingZeros().scale() <= 0;
        }
        if (!integral) {
            return value.doubleValue();
        }
        long l = value.longValue();
        return l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE ? (Number) (int) l : (Number) l;
    }

    private static Object convert(JsonElement template, Object value) {
        if (value instanceof Enum<?> e) {
            return e.name();
        }
        if (value instanceof Number number && template instanceof JsonPrimitive primitive && primitive.isNumber()) {
            Number def = primitive.getAsNumber();
            if (def instanceof Integer) return number.intValue();
            if (def instanceof Long) return number.longValue();
            if (def instanceof Double) return number.doubleValue();
            if (def instanceof Float) return number.floatValue();
        }
        if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            for (Object element : list) {
                copy.add(convert(null, element));
            }
            return copy;
        }
        return value;
    }
}

