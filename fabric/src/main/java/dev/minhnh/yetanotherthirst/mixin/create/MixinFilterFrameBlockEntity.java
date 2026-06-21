package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.block.FilterCoreType;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = FilterFrameBlockEntity.class, remap = false)
public class MixinFilterFrameBlockEntity implements IHaveGoggleInformation {

    @SuppressWarnings("null")
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FilterFrameBlockEntity filter = (FilterFrameBlockEntity) (Object) this;
        tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.goggleFilterTitle()));

        FilterCoreType installedType = filter.getBlockState().getValue(FilterFrameBlock.CORE_TYPE);
        ItemStack core = installedType != FilterCoreType.EMPTY ? filter.getFilterCore() : ItemStack.EMPTY;

        int inputAmount = filter.getInputAmount();
        if (inputAmount > 0) {
            tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(
                    Lang.Tooltip.goggleFilterInputPurity(inputAmount, WaterPurity.purityComponent(filter.getInputPurity()))));
        } else {
            tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.goggleFilterInputEmpty()));
        }

        int outputAmount = filter.getOutputAmount();
        if (outputAmount > 0) {
            tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(
                    Lang.Tooltip.goggleFilterOutputPurity(outputAmount, WaterPurity.purityComponent(filter.getOutputPurity()))));
        } else {
            boolean isFiltering = false;
            int expectedPurity = 0;
            if (inputAmount > 0 && !core.isEmpty()) {
                double maxL = FilterCoreItem.getMaxLifespan(core);
                double remL = FilterCoreItem.readLifespan(core, maxL);
                if (maxL > 0 && remL > 0) {
                    int maxP = FilterCoreItem.getMaxPurity(core);
                    int p1 = filter.getInputPurity();
                    if (maxP > 0 && p1 < maxP) {
                        isFiltering = true;
                        expectedPurity = Math.min(p1 + 1, maxP);
                    }
                }
            }
            tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(
                    isFiltering
                            ? Lang.Tooltip.goggleFilterOutputEmptyPurity(WaterPurity.purityComponent(expectedPurity))
                            : Lang.Tooltip.goggleFilterOutputEmpty()));
        }

        if (!core.isEmpty()) {
            Component name = FilterCoreItem.getFilterMaterialName(core.getItem());
            double maxL = FilterCoreItem.getMaxLifespan(core);
            if (maxL > 0) {
                int pct = (int) Math.round((FilterCoreItem.readLifespan(core, maxL) / maxL) * 100);
                tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.filterCoreStatus(name, pct + "%")));
            } else {
                tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.filterCoreStatusNoDurability(name)));
            }
        } else {
            tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.filterEmpty()));
        }
        return true;
    }
}
