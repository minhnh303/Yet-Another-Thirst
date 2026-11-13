package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaterPurityTest {

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        if (!Items.POTION.builtInRegistryHolder().areComponentsBound()) {
            BuiltInRegistries.DATA_COMPONENT_INITIALIZERS
                    .build(VanillaRegistries.createLookup())
                    .forEach(DataComponentInitializers.PendingComponents::apply);
        }
        WaterPurity.init();
    }

    private static ItemStack createWaterBottle() {
        return PotionContents.createItemStack(Items.POTION, Potions.WATER);
    }

    @Test
    @DisplayName("Purity components have correct translation keys and colors")
    void testPurityComponents() {
        Component dirty = WaterPurity.purityComponent(0);
        assertTrue(dirty.getContents() instanceof TranslatableContents tc && tc.getKey().equals("yet_another_thirst.purity.dirty"));
        assertEquals(TextColor.fromRgb(0xa84825), dirty.getStyle().getColor());

        Component slightlyDirty = WaterPurity.purityComponent(1);
        assertTrue(slightlyDirty.getContents() instanceof TranslatableContents tc && tc.getKey().equals("yet_another_thirst.purity.slightly_dirty"));
        assertEquals(TextColor.fromRgb(0x796c71), slightlyDirty.getStyle().getColor());

        Component acceptable = WaterPurity.purityComponent(2);
        assertTrue(acceptable.getContents() instanceof TranslatableContents tc && tc.getKey().equals("yet_another_thirst.purity.acceptable"));
        assertEquals(TextColor.fromRgb(0x5d829d), acceptable.getStyle().getColor());

        Component purified = WaterPurity.purityComponent(3);
        assertTrue(purified.getContents() instanceof TranslatableContents tc && tc.getKey().equals("yet_another_thirst.purity.purified"));
        assertEquals(TextColor.fromRgb(0x21b1ff), purified.getStyle().getColor());
    }

    @Test
    @DisplayName("Water bottle purity tag operations")
    void testPurityTags() {
        ItemStack bottle = createWaterBottle();
        assertTrue(WaterPurity.isWaterBottle(bottle));
        assertTrue(WaterPurity.isWaterFilledContainer(bottle));

        // Before adding purity tag
        assertFalse(WaterPurity.hasPurity(bottle));
        assertEquals(ThirstConfig.DEFAULT_PURITY, WaterPurity.getPurity(bottle));

        // Add purity
        WaterPurity.addPurity(bottle, 2);
        assertTrue(WaterPurity.hasPurity(bottle));
        assertEquals(2, WaterPurity.getPurity(bottle));

        // Verify items without purity get default when verified
        ItemStack unverifiedBottle = createWaterBottle();
        WaterPurity.verifyItemStackPurity(unverifiedBottle);
        assertTrue(WaterPurity.hasPurity(unverifiedBottle));
        assertEquals(ThirstConfig.DEFAULT_PURITY, WaterPurity.getPurity(unverifiedBottle));
    }

    @Test
    @DisplayName("Stacking logic checks purity tag equality")
    void testWaterBottleStacking() {
        ItemStack b1 = createWaterBottle();
        ItemStack b2 = createWaterBottle();
        WaterPurity.addPurity(b1, 1);
        WaterPurity.addPurity(b2, 1);
        assertTrue(WaterPurity.waterBottleTagsMatchForStacking(b1, b2));

        ItemStack b3 = createWaterBottle();
        WaterPurity.addPurity(b3, 3);
        assertFalse(WaterPurity.waterBottleTagsMatchForStacking(b1, b3));

        ItemStack apple = new ItemStack(Items.APPLE);
        assertFalse(WaterPurity.waterBottleTagsMatchForStacking(b1, apple));
    }

    @Test
    @DisplayName("Tooltip appends purity only for water containers with purity tag")
    void testAppendTooltip() {
        ItemStack bottle = createWaterBottle();
        List<Component> tooltip = new ArrayList<>();

        // Without tag: no tooltip added
        WaterPurity.appendTooltip(bottle, tooltip);
        assertTrue(tooltip.isEmpty());

        // With tag: tooltip added
        WaterPurity.addPurity(bottle, 3);
        WaterPurity.appendTooltip(bottle, tooltip);
        assertEquals(1, tooltip.size());

        // Non-water container: no tooltip added
        List<Component> appleTooltip = new ArrayList<>();
        WaterPurity.appendTooltip(new ItemStack(Items.APPLE), appleTooltip);
        assertTrue(appleTooltip.isEmpty());
    }
}
