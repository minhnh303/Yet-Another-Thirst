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

    /**
     * Immutable snapshots swapped atomically: the client (tooltips/HUD) and the integrated server
     * read these while a config reload may run on another thread.
     */
    private static volatile Map<Item, ThirstValue> drinks = Map.of();
    private static volatile Map<Item, ThirstValue> foods = Map.of();

    static {
        resetToDefaults();
    }

    private ThirstValues() {
    }

    public static Optional<ThirstValue> get(ItemStack stack) {
        Item item = stack.getItem();
        ThirstValue drink = drinks.get(item);
        if (drink != null) {
            return Optional.of(drink);
        }
        return Optional.ofNullable(foods.get(item));
    }

    public static synchronized void registerDrink(Item item, int thirst, int quenched) {
        if (drinks.containsKey(item)) return;
        Map<Item, ThirstValue> next = new HashMap<>(drinks);
        next.put(item, new ThirstValue(thirst, quenched));
        drinks = Map.copyOf(next);
    }

    public static synchronized void registerFood(Item item, int thirst, int quenched) {
        if (foods.containsKey(item)) return;
        Map<Item, ThirstValue> next = new HashMap<>(foods);
        next.put(item, new ThirstValue(thirst, quenched));
        foods = Map.copyOf(next);
    }

    public static synchronized void replaceConfiguredValues(Map<Item, ThirstValue> drinks, Map<Item, ThirstValue> foods, Set<Item> blacklist) {
        ThirstValues.drinks = withoutBlacklisted(drinks, blacklist);
        ThirstValues.foods = withoutBlacklisted(foods, blacklist);
    }

    private static Map<Item, ThirstValue> withoutBlacklisted(Map<Item, ThirstValue> values, Set<Item> blacklist) {
        Map<Item, ThirstValue> result = new HashMap<>(values.size());
        values.forEach((item, value) -> {
            if (!blacklist.contains(item)) {
                result.put(item, value);
            }
        });
        return Map.copyOf(result);
    }

    public static synchronized void resetToDefaults() {
        drinks = Map.copyOf(defaultDrinks());
        foods = Map.copyOf(defaultFoods());
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
