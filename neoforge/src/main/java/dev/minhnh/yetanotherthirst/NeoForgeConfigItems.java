package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.*;

public final class NeoForgeConfigItems {

    static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue WATER_BOTTLE_STACKSIZE;
    private static final ModConfigSpec.ConfigValue<List<? extends List<?>>> DRINK_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends List<?>>> FOOD_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ITEMS_BLACKLIST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("items");
        WATER_BOTTLE_STACKSIZE = builder
                .comment("Maximum stack size for water bottles (1-64)")
                .defineInRange("waterBottleStackSize", 64, 1, 64);
        DRINK_VALUES = builder
                .comment("Items that restore thirst when drunk. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("drinks", DefaultItemValues.defaultDrinkValues(), NeoForgeConfigItems::validItemValueEntry);
        FOOD_VALUES = builder
                .comment("Items that restore thirst when eaten. Format: [[\"item-id\", thirst, quenched], [\"#item-tag\", thirst, quenched]]")
                .defineList("foods", DefaultItemValues.defaultFoodValues(), NeoForgeConfigItems::validItemValueEntry);
        ITEMS_BLACKLIST = builder
                .comment("Items excluded from thirst restoration even if present in drink or food values")
                .defineList("itemsBlacklist", List.of("yet_another_thirst:example_item_1", "yet_another_thirst:example_item_2"), value -> value instanceof String);
        builder.pop();

        SPEC = builder.build();
    }

    static void sync() {
        ThirstConfig.WATER_BOTTLE_STACKSIZE = WATER_BOTTLE_STACKSIZE.get();
    }

    static void reloadThirstValues() {
        Map<Item, ThirstValue> drinks = parseItemValues(DRINK_VALUES.get());
        Map<Item, ThirstValue> foods = parseItemValues(FOOD_VALUES.get());
        Set<Item> blacklist = parseItems(ITEMS_BLACKLIST.get());

        if (ModItems.TERRACOTTA_WATER_BOWL != null) {
            Item terracotta = ModItems.TERRACOTTA_WATER_BOWL.get();
            if (terracotta != null && terracotta != Items.AIR) {
                drinks.putIfAbsent(terracotta, new ThirstValue(5, 7));
            }
        }
        if (ModItems.WOODEN_WATER_BOWL != null) {
            Item wooden = ModItems.WOODEN_WATER_BOWL.get();
            if (wooden != null && wooden != Items.AIR) {
                drinks.putIfAbsent(wooden, new ThirstValue(5, 7));
            }
        }

        ThirstValues.replaceConfiguredValues(drinks, foods, blacklist);
    }

    private static boolean validItemValueEntry(Object value) {
        if (!(value instanceof List<?> entry) || entry.size() < 3) {
            return false;
        }
        return entry.get(0) instanceof String && entry.get(1) instanceof Number && entry.get(2) instanceof Number;
    }

    private static Map<Item, ThirstValue> parseItemValues(List<? extends List<?>> entries) {
        Map<Item, ThirstValue> values = new LinkedHashMap<>();
        for (List<?> entry : entries) {
            if (!validItemValueEntry(entry)) continue;
            String id = (String) entry.get(0);
            ThirstValue value = new ThirstValue(((Number) entry.get(1)).intValue(), ((Number) entry.get(2)).intValue());
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag -> tag.forEach(item -> values.put(item, value)));
            } else {
                resolveItem(id).ifPresent(item -> values.put(item, value));
            }
        }
        return values;
    }

    private static Set<Item> parseItems(List<? extends String> entries) {
        Set<Item> items = new HashSet<>();
        for (String id : entries) {
            if (id.startsWith("#")) {
                resolveTag(id.substring(1)).ifPresent(tag -> tag.forEach(items::add));
            } else {
                resolveItem(id).ifPresent(items::add);
            }
        }
        return items;
    }

    private static Optional<Item> resolveItem(String id) {
        try {
            ResourceLocation location = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(location);
            if (item != null && (item != Items.AIR || "minecraft:air".equals(id))) {
                return Optional.of(item);
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    private static Optional<List<Item>> resolveTag(String id) {
        try {
            TagKey<Item> key = TagKey.create(Registries.ITEM, ResourceLocation.parse(id));
            return BuiltInRegistries.ITEM.getTag(key)
                    .map(tag -> tag.stream().map(Holder::value).toList());
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private NeoForgeConfigItems() {}
}
