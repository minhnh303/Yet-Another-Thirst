package dev.minhnh.yetanotherthirst.mixin.create;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.block.FilterCoreType;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

/**
 * Implements Create's IHaveGoggleInformation on FilterFrameBlockEntity at runtime.
 * No compile-time Create dependency — the interface is applied by mixin at load time.
 * The method signature matches IHaveGoggleInformation.addToGoggleTooltip.
 */
@Pseudo
@Mixin(value = FilterFrameBlockEntity.class, remap = false)
public abstract class MixinFilterFrameBlockEntity {

    // TANK_SIZE value (1000) mirrors AbstractFilterFrameBlockEntity.TANK_SIZE (protected)
    private static final int TANK_SIZE = 1000;

    @SuppressWarnings("null")
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FilterFrameBlockEntity filter = (FilterFrameBlockEntity) (Object) this;
        tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.goggleFilterTitle()));

        FilterCoreType installedType = filter.getBlockState().getValue(FilterFrameBlock.CORE_TYPE);
        ItemStack core = filter.getFilterCore();

        tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                .append(Lang.Tooltip.goggleFilterInputPurity(
                        filter.getInputAmount(),
                        WaterPurity.purityComponent(filter.getInputPurity()))));
        tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                .append(Lang.Tooltip.goggleFilterOutputPurity(
                        filter.getOutputAmount(),
                        WaterPurity.purityComponent(filter.getOutputPurity()))));

        if (installedType != FilterCoreType.EMPTY && !core.isEmpty()) {
            int maxPurity = FilterCoreItem.getMaxPurity(core);
            double maxLifespan = FilterCoreItem.getMaxLifespan(core);
            double lifespan = FilterCoreItem.readLifespan(core, maxLifespan);
            int lifespanPct = maxLifespan > 0 ? (int) (100.0 * lifespan / maxLifespan) : 0;
            tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                    .append(Lang.Tooltip.filterMaxPurity(WaterPurity.purityComponent(maxPurity))));
            tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                    .append(Lang.Tooltip.filterLifespan(lifespanPct)));
        }
        return true;
    }
}
