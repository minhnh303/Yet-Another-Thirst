package dev.minhnh.yetanotherthirst.core.thirst;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ThirstValues {

    private static final Map<Item, ThirstValue> DRINKS = new HashMap<>();
    private static final Map<Item, ThirstValue> FOODS = new HashMap<>();

    static {
        resetToDefaults();
    }

    private ThirstValues() {
    }

    public static Optional<ThirstValue> get(ItemStack stack) {

        Item item = stack.getItem();
        ThirstValue drink = DRINKS.get(item);
        if (drink != null) {
            return Optional.of(drink);
        }
        return Optional.ofNullable(FOODS.get(item));
    }

    public static void registerDrink(Item item, int thirst, int quenched) {

        DRINKS.putIfAbsent(item, new ThirstValue(thirst, quenched));
    }

    public static void registerFood(Item item, int thirst, int quenched) {

        FOODS.putIfAbsent(item, new ThirstValue(thirst, quenched));
    }

    public static void replaceConfiguredValues(Map<Item, ThirstValue> drinks, Map<Item, ThirstValue> foods, Set<Item> blacklist) {

        DRINKS.clear();
        FOODS.clear();

        drinks.forEach((item, value) -> {
            if (!blacklist.contains(item)) {
                DRINKS.put(item, value);
            }
        });
        foods.forEach((item, value) -> {
            if (!blacklist.contains(item)) {
                FOODS.put(item, value);
            }
        });
    }

    public static void resetToDefaults() {

        DRINKS.clear();
        FOODS.clear();

        defaultDrinks().forEach((item, value) -> DRINKS.put(item, value));
        defaultFoods().forEach((item, value) -> FOODS.put(item, value));
    }

    public static Map<Item, ThirstValue> defaultDrinks() {

        Map<Item, ThirstValue> values = new LinkedHashMap<>();
        values.put(Items.POTION, new ThirstValue(6, 8));
        values.put(Items.HONEY_BOTTLE, new ThirstValue(3, 4));
        values.put(Items.MILK_BUCKET, new ThirstValue(2, 4));
        return values;
    }

    public static Map<Item, ThirstValue> defaultFoods() {

        Map<Item, ThirstValue> values = new LinkedHashMap<>();
        values.put(Items.APPLE, new ThirstValue(2, 3));
        values.put(Items.GOLDEN_APPLE, new ThirstValue(2, 3));
        values.put(Items.ENCHANTED_GOLDEN_APPLE, new ThirstValue(2, 3));
        values.put(Items.MELON_SLICE, new ThirstValue(4, 5));
        values.put(Items.CARROT, new ThirstValue(1, 2));
        values.put(Items.BEETROOT, new ThirstValue(1, 2));
        values.put(Items.SWEET_BERRIES, new ThirstValue(1, 2));
        values.put(Items.GLOW_BERRIES, new ThirstValue(1, 2));
        values.put(Items.GOLDEN_CARROT, new ThirstValue(1, 2));
        values.put(Items.CHORUS_FRUIT, new ThirstValue(1, 2));
        values.put(Items.MUSHROOM_STEW, new ThirstValue(2, 3));
        values.put(Items.RABBIT_STEW, new ThirstValue(2, 3));
        values.put(Items.BEETROOT_SOUP, new ThirstValue(5, 7));
        values.put(Items.SUSPICIOUS_STEW, new ThirstValue(2, 3));
        values.put(Items.COD, new ThirstValue(1, 1));
        values.put(Items.SALMON, new ThirstValue(1, 1));
        values.put(Items.TROPICAL_FISH, new ThirstValue(1, 2));
        values.put(Items.COOKED_COD, new ThirstValue(1, 2));
        values.put(Items.COOKED_SALMON, new ThirstValue(1, 2));
        values.put(Items.DRIED_KELP, new ThirstValue(1, 1));
        values.put(Items.BREAD, new ThirstValue(1, 1));
        values.put(Items.PUMPKIN_PIE, new ThirstValue(1, 2));
        values.put(Items.COOKIE, new ThirstValue(1, 1));
        return values;
    }
}
