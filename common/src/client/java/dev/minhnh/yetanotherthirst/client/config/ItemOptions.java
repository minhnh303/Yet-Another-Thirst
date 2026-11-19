package dev.minhnh.yetanotherthirst.client.config;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.section;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.list;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.ITEMS;

/** Stack size, drink/food hydration values, and item blacklist options. */
public final class ItemOptions {
    public static final ConfigOption<Integer> WATER_BOTTLE_STACK_SIZE =
            integer(ITEMS, "waterBottleStackSize", 1, 64).rejoin();
    public static final ConfigOption<List<ItemValue>> DRINKS =
            list(ITEMS, "drinks", ListSchema.itemValues());
    public static final ConfigOption<List<ItemValue>> FOODS =
            list(ITEMS, "foods", ListSchema.itemValues());
    public static final ConfigOption<List<String>> ITEMS_BLACKLIST =
            list(ITEMS, "itemsBlacklist", ListSchema.items());

    private ItemOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("items", () -> new ItemStack(Items.MELON_SLICE),
                        section("stack", WATER_BOTTLE_STACK_SIZE),
                        section("item_values", DRINKS, FOODS, ITEMS_BLACKLIST))
        );
    }
}

