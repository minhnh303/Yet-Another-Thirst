package dev.minhnh.yetanotherthirst.core.item;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class FilterCoreItem extends Item {

    public FilterCoreItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public FilterCoreItem() {
        this(new Properties());
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

    public static double readLifespan(ItemStack stack, double maxL) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return maxL;
        return customData.copyTag().getDoubleOr("Lifespan", maxL);
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
        // ratio 0.0 = fully worn (red), 1.0 = pristine (green); no blue component
        float ratio = (float) Math.max(0.0, Math.min(1.0, readLifespan(stack, maxL) / maxL));
        int r = (int) ((1.0F - ratio) * 255.0F);
        int g = (int) (ratio * 255.0F);
        return (r << 16) | (g << 8);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nonnull Item.TooltipContext context, @Nonnull TooltipDisplay display, @Nonnull Consumer<Component> tooltipAdder, @Nonnull TooltipFlag flag) {
        double maxL = getMaxLifespan(stack);
        if (maxL > 0) {
            int pct = (int) Math.round((readLifespan(stack, maxL) / maxL) * 100);
            tooltipAdder.accept(Lang.Tooltip.filterLifespan(pct + "%"));
            tooltipAdder.accept(Lang.Tooltip.filterMaxPurity(WaterPurity.purityComponent(getMaxPurity(stack))));
        }
    }
}
