package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstValue;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ThirstValuesTest {

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    @DisplayName("Default drinks contain potion, honey bottle, and milk bucket")
    void testDefaultDrinks() {
        ThirstValues.resetToDefaults();

        Optional<ThirstValue> potion = ThirstValues.get(new ItemStack(Items.POTION));
        assertTrue(potion.isPresent());
        assertEquals(6, potion.get().thirst());
        assertEquals(8, potion.get().quenched());

        Optional<ThirstValue> honey = ThirstValues.get(new ItemStack(Items.HONEY_BOTTLE));
        assertTrue(honey.isPresent());
        assertEquals(3, honey.get().thirst());
        assertEquals(4, honey.get().quenched());

        Optional<ThirstValue> milk = ThirstValues.get(new ItemStack(Items.MILK_BUCKET));
        assertTrue(milk.isPresent());
        assertEquals(2, milk.get().thirst());
        assertEquals(4, milk.get().quenched());
    }

    @Test
    @DisplayName("Default foods contain melon slice and soups")
    void testDefaultFoods() {
        ThirstValues.resetToDefaults();

        Optional<ThirstValue> melon = ThirstValues.get(new ItemStack(Items.MELON_SLICE));
        assertTrue(melon.isPresent());
        assertEquals(4, melon.get().thirst());
        assertEquals(5, melon.get().quenched());

        Optional<ThirstValue> soup = ThirstValues.get(new ItemStack(Items.BEETROOT_SOUP));
        assertTrue(soup.isPresent());
        assertEquals(5, soup.get().thirst());
        assertEquals(7, soup.get().quenched());
    }

    @Test
    @DisplayName("Custom registration adds new drink and food values")
    void testCustomRegistration() {
        ThirstValues.resetToDefaults();

        ThirstValues.registerDrink(Items.DIAMOND, 10, 10);
        Optional<ThirstValue> customDrink = ThirstValues.get(new ItemStack(Items.DIAMOND));
        assertTrue(customDrink.isPresent());
        assertEquals(10, customDrink.get().thirst());
        assertEquals(10, customDrink.get().quenched());

        ThirstValues.resetToDefaults();
    }

    @Test
    @DisplayName("Replacing configured values respects blacklists")
    void testReplaceConfiguredValues() {
        ThirstValues.resetToDefaults();

        Map<net.minecraft.world.item.Item, ThirstValue> drinks = Map.of(Items.POTION, new ThirstValue(10, 10));
        Map<net.minecraft.world.item.Item, ThirstValue> foods = Map.of(Items.APPLE, new ThirstValue(5, 5));
        Set<net.minecraft.world.item.Item> blacklist = Set.of(Items.POTION);

        ThirstValues.replaceConfiguredValues(drinks, foods, blacklist);

        // Potion was blacklisted
        assertFalse(ThirstValues.get(new ItemStack(Items.POTION)).isPresent());

        // Apple was not blacklisted
        Optional<ThirstValue> apple = ThirstValues.get(new ItemStack(Items.APPLE));
        assertTrue(apple.isPresent());
        assertEquals(5, apple.get().thirst());

        ThirstValues.resetToDefaults();
    }
}

