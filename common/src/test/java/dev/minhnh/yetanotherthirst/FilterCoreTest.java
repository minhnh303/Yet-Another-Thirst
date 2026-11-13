package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.block.AbstractFilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.block.FilterCoreType;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FilterCoreTest {

    @BeforeAll
    static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        ModItems.FABRIC_FILTER_CORE = () -> Items.PAPER;
        ModItems.SAND_FILTER_CORE = () -> Items.SAND;
        ModItems.CARBON_FILTER_CORE = () -> Items.COAL;
        ModItems.CLOGGED_FABRIC_FILTER = () -> Items.DEAD_BUSH;
        ModItems.CLOGGED_SAND_FILTER = () -> Items.GRAVEL;
        ModItems.CLOGGED_CARBON_FILTER = () -> Items.CHARCOAL;
    }

    @Test
    @DisplayName("Filter core identification and clogged checks")
    void testFilterCoreIdentification() {
        ItemStack fabricCore = new ItemStack(ModItems.FABRIC_FILTER_CORE.get());
        ItemStack sandCore = new ItemStack(ModItems.SAND_FILTER_CORE.get());
        ItemStack carbonCore = new ItemStack(ModItems.CARBON_FILTER_CORE.get());
        ItemStack cloggedFabric = new ItemStack(ModItems.CLOGGED_FABRIC_FILTER.get());
        ItemStack ironIngot = new ItemStack(Items.IRON_INGOT);

        assertTrue(AbstractFilterFrameBlockEntity.isFilterCore(fabricCore));
        assertTrue(AbstractFilterFrameBlockEntity.isFilterCore(sandCore));
        assertTrue(AbstractFilterFrameBlockEntity.isFilterCore(carbonCore));
        assertTrue(AbstractFilterFrameBlockEntity.isFilterCore(cloggedFabric));
        assertFalse(AbstractFilterFrameBlockEntity.isFilterCore(ironIngot));

        assertFalse(AbstractFilterFrameBlockEntity.isClogged(fabricCore));
        assertTrue(AbstractFilterFrameBlockEntity.isClogged(cloggedFabric));
    }

    @Test
    @DisplayName("Core types map correctly from ItemStack")
    void testCoreTypes() {
        assertEquals(FilterCoreType.EMPTY, AbstractFilterFrameBlockEntity.getCoreTypeFromStack(ItemStack.EMPTY));
        assertEquals(FilterCoreType.FABRIC, AbstractFilterFrameBlockEntity.getCoreTypeFromStack(new ItemStack(ModItems.FABRIC_FILTER_CORE.get())));
        assertEquals(FilterCoreType.SAND, AbstractFilterFrameBlockEntity.getCoreTypeFromStack(new ItemStack(ModItems.SAND_FILTER_CORE.get())));
        assertEquals(FilterCoreType.CARBON, AbstractFilterFrameBlockEntity.getCoreTypeFromStack(new ItemStack(ModItems.CARBON_FILTER_CORE.get())));
        assertEquals(FilterCoreType.CLOGGED_FABRIC, AbstractFilterFrameBlockEntity.getCoreTypeFromStack(new ItemStack(ModItems.CLOGGED_FABRIC_FILTER.get())));
    }

    @Test
    @DisplayName("Max lifespan and max purity configuration per core type")
    void testLifespanAndMaxPurity() {
        ItemStack fabricCore = new ItemStack(ModItems.FABRIC_FILTER_CORE.get());
        assertEquals(ThirstConfig.FABRIC_FILTER_DURABILITY, FilterCoreItem.getMaxLifespan(fabricCore));
        assertEquals(ThirstConfig.FABRIC_FILTER_MAX_PURITY, FilterCoreItem.getMaxPurity(fabricCore));

        ItemStack sandCore = new ItemStack(ModItems.SAND_FILTER_CORE.get());
        assertEquals(ThirstConfig.SAND_FILTER_DURABILITY, FilterCoreItem.getMaxLifespan(sandCore));
        assertEquals(ThirstConfig.SAND_FILTER_MAX_PURITY, FilterCoreItem.getMaxPurity(sandCore));

        ItemStack carbonCore = new ItemStack(ModItems.CARBON_FILTER_CORE.get());
        assertEquals(ThirstConfig.CARBON_FILTER_DURABILITY, FilterCoreItem.getMaxLifespan(carbonCore));
        assertEquals(ThirstConfig.CARBON_FILTER_MAX_PURITY, FilterCoreItem.getMaxPurity(carbonCore));

        ItemStack unknownItem = new ItemStack(Items.STICK);
        assertEquals(0, FilterCoreItem.getMaxLifespan(unknownItem));
        assertEquals(0, FilterCoreItem.getMaxPurity(unknownItem));
    }

    @Test
    @DisplayName("Lifespan reading from DataComponents custom data")
    void testLifespanNbt() {
        ItemStack fabricCore = new ItemStack(ModItems.FABRIC_FILTER_CORE.get());
        double maxL = FilterCoreItem.getMaxLifespan(fabricCore);

        // Pristine item without tag has full lifespan
        assertEquals(maxL, FilterCoreItem.readLifespan(fabricCore, maxL));

        // Damaged item with Lifespan tag
        FilterCoreItem.writeLifespan(fabricCore, 123.45);
        assertEquals(123.45, FilterCoreItem.readLifespan(fabricCore, maxL));
    }
}

