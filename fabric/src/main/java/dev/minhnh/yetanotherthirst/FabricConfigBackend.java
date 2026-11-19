package dev.minhnh.yetanotherthirst;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.minhnh.yetanotherthirst.platform.services.IConfigBackend;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** {@link IConfigBackend} over Fabric JSON config files. */
public final class FabricConfigBackend implements IConfigBackend {
    private static final Gson GSON = new Gson();
    private final Map<ConfigFile, JsonObject> defaults = new EnumMap<>(ConfigFile.class);

    private JsonObject defaultsOf(ConfigFile file) {
        return defaults.computeIfAbsent(file, FabricConfig::defaults);
    }

    @Override
    public boolean has(ConfigFile file, String key) {
        if (file == ConfigFile.ITEMS && key.startsWith("waterBoiler")) return false;
        return defaultsOf(file).has(key);
    }

    @Override
    public Object get(ConfigFile file, String key) {
        requireKey(file, key);
        synchronized (FabricConfig.class) {
            JsonObject json = FabricConfig.json(file);
            return toJava(json.has(key) ? json.get(key) : defaultsOf(file).get(key));
        }
    }

    @Override
    public Object getDefault(ConfigFile file, String key) {
        requireKey(file, key);
        return toJava(defaultsOf(file).get(key));
    }

    @Override
    public void set(ConfigFile file, String key, Object value) {
        requireKey(file, key);
        JsonElement element = GSON.toJsonTree(value instanceof Enum<?> entry ? entry.name() : value);
        synchronized (FabricConfig.class) {
            FabricConfig.json(file).add(key, element);
        }
    }

    private void requireKey(ConfigFile file, String key) {
        if (!has(file, key)) {
            throw new IllegalArgumentException("Unknown config key " + file + "/" + key);
        }
    }

    @Override
    public void save(Set<ConfigFile> files) {
        files.forEach(FabricConfig::write);
    }

    @Override
    public void apply(Set<ConfigFile> files) {
        FabricConfig.sync();
        if (files.contains(ConfigFile.ITEMS)) {
            FabricConfig.reloadThirstValues();
        }
    }

    private static Object toJava(JsonElement element) {
        if (element == null || element.isJsonNull()) return null;
        if (element instanceof JsonArray array) {
            List<Object> result = new ArrayList<>(array.size());
            array.forEach(value -> result.add(toJava(value)));
            return result;
        }
        if (element instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isNumber()) {
                String raw = primitive.getAsString();
                if (!raw.contains(".") && !raw.contains("e") && !raw.contains("E")) {
                    long value = primitive.getAsLong();
                    if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                        return (int) value;
                    }
                    return value;
                }
                return primitive.getAsDouble();
            }
            return primitive.getAsString();
        }
        return element.toString();
    }
}
