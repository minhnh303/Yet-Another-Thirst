package dev.minhnh.yetanotherthirst.core.thirst;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ThirstValues {

    private static final Map<Item, ThirstValue> DRINKS = new HashMap<>();
    private static final Map<Item, ThirstValue> FOODS = new HashMap<>();

    static {
        registerDrink(Items.POTION, 6, 8);
        registerDrink(Items.HONEY_BOTTLE, 3, 4);
        registerDrink(Items.MILK_BUCKET, 2, 4);

        registerFood(Items.APPLE, 2, 3);
        registerFood(Items.GOLDEN_APPLE, 2, 3);
        registerFood(Items.ENCHANTED_GOLDEN_APPLE, 2, 3);
        registerFood(Items.MELON_SLICE, 4, 5);
        registerFood(Items.CARROT, 1, 2);
        registerFood(Items.BEETROOT, 1, 2);
        registerFood(Items.SWEET_BERRIES, 1, 2);
        registerFood(Items.GLOW_BERRIES, 1, 2);
        registerFood(Items.GOLDEN_CARROT, 1, 2);
        registerFood(Items.CHORUS_FRUIT, 1, 2);
        registerFood(Items.MUSHROOM_STEW, 2, 3);
        registerFood(Items.RABBIT_STEW, 2, 3);
        registerFood(Items.BEETROOT_SOUP, 5, 7);
        registerFood(Items.SUSPICIOUS_STEW, 2, 3);
        registerFood(Items.COD, 1, 1);
        registerFood(Items.SALMON, 1, 1);
        registerFood(Items.TROPICAL_FISH, 1, 2);
        registerFood(Items.COOKED_COD, 1, 2);
        registerFood(Items.COOKED_SALMON, 1, 2);
        registerFood(Items.DRIED_KELP, 1, 1);
        registerFood(Items.BREAD, 1, 1);
        registerFood(Items.PUMPKIN_PIE, 1, 2);
        registerFood(Items.COOKIE, 1, 1);
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
}
