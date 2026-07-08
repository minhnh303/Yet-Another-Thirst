package dev.minhnh.yetanotherthirst.core.item;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nonnull;
import java.util.List;

public class FilterCoreItem extends Item {

    public FilterCoreItem(Properties properties) {
        super(properties);
    }

    public static double getMaxLifespan(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.FABRIC_FILTER_CORE.get()) return ThirstConfig.FABRIC_FILTER_DURABILITY;
        if (item == ModItems.SAND_FILTER_CORE.get()) return ThirstConfig.SAND_FILTER_DURABILITY;
        if (item == ModItems.CARBON_FILTER_CORE.get()) return ThirstConfig.CARBON_FILTER_DURABILITY;
        return 0;
    }

    public static int getMaxPurity(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.FABRIC_FILTER_CORE.get()) return ThirstConfig.FABRIC_FILTER_MAX_PURITY;
        if (item == ModItems.SAND_FILTER_CORE.get()) return ThirstConfig.SAND_FILTER_MAX_PURITY;
        if (item == ModItems.CARBON_FILTER_CORE.get()) return ThirstConfig.CARBON_FILTER_MAX_PURITY;
        return 0;
    }

    /** Read current lifespan from DataComponents custom data. Falls back to maxL if unset. */
    public static double readLifespan(ItemStack stack, double maxL) {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return data.contains("Lifespan") ? data.getDouble("Lifespan") : maxL;
    }

    /** Write updated lifespan into DataComponents custom data. */
    public static void writeLifespan(ItemStack stack, double lifespan) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putDouble("Lifespan", lifespan));
    }

    public static MutableComponent getFilterMaterialName(Item item) {
        if (item == ModItems.FABRIC_FILTER_CORE.get()) return Lang.FilterMaterial.fabric();
        if (item == ModItems.SAND_FILTER_CORE.get()) return Lang.FilterMaterial.sand();
        if (item == ModItems.CARBON_FILTER_CORE.get()) return Lang.FilterMaterial.carbon();
        if (item == ModItems.CLOGGED_FABRIC_FILTER.get()) return Lang.FilterMaterial.fabricClogged();
        if (item == ModItems.CLOGGED_SAND_FILTER.get()) return Lang.FilterMaterial.sandClogged();
        if (item == ModItems.CLOGGED_CARBON_FILTER.get()) return Lang.FilterMaterial.carbonClogged();
        return Component.empty();
    }

    @Override
    public boolean isBarVisible(@Nonnull ItemStack stack) {
        if (stack.getCount() > 1) return false;
        double maxL = getMaxLifespan(stack);
        if (maxL <= 0) return false;
        return readLifespan(stack, maxL) < maxL;
    }

    @Override
    public int getBarWidth(@Nonnull ItemStack stack) {
        double maxL = getMaxLifespan(stack);
        if (maxL <= 0) return 0;
        return (int) Math.round(13.0 * readLifespan(stack, maxL) / maxL);
    }

    @Override
    public int getBarColor(@Nonnull ItemStack stack) {
        double maxL = getMaxLifespan(stack);
        if (maxL <= 0) return 0xFFFFFF;
        double ratio = readLifespan(stack, maxL) / maxL;
        // red (0xFF0000) when worn, green (0x00FF00) when fresh
        int r = (int) Math.round(255 * (1.0 - ratio));
        int g = (int) Math.round(255 * ratio);
        return (r << 16) | (g << 8);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, Item.TooltipContext context, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        double maxL = getMaxLifespan(stack);
        if (maxL > 0) {
            int pct = (int) Math.round((readLifespan(stack, maxL) / maxL) * 100);
            tooltip.add(Lang.Tooltip.filterLifespan(pct + "%"));
            tooltip.add(Lang.Tooltip.filterMaxPurity(WaterPurity.purityComponent(getMaxPurity(stack))));
        }
    }
}
