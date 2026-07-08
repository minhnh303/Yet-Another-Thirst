package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.block.AbstractFilterFrameBlock;
import dev.minhnh.yetanotherthirst.core.block.FilterCoreType;
import dev.minhnh.yetanotherthirst.core.block.FilterFrameBlockEntity;
import dev.minhnh.yetanotherthirst.core.item.FilterCoreItem;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

/**
 * Injects IHaveGoggleInformation into FilterFrameBlockEntity so Create's goggles
 * can display filter status. Loaded only when Create is present (required:false config).
 */
@Pseudo
@Mixin(value = FilterFrameBlockEntity.class, remap = false)
public abstract class MixinFilterFrameBlockEntity implements IHaveGoggleInformation {

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        FilterFrameBlockEntity filter = (FilterFrameBlockEntity) (Object) this;
        tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Tooltip.goggleFilterTitle()));

        FilterCoreType installedType = filter.getBlockState().getValue(AbstractFilterFrameBlock.CORE_TYPE);
        ItemStack core = filter.getFilterCore();

        int inputAmount = filter.getInputTank().getFluidAmount();
        int outputAmount = filter.getOutputTank().getFluidAmount();

        Component inputPurityComp = inputAmount > 0
                ? WaterPurity.purityComponent(FluidPurityHelper.getPurity(filter.getInputTank().getFluidInTank(0)))
                : Component.literal("—");
        Component outputPurityComp = outputAmount > 0
                ? WaterPurity.purityComponent(FluidPurityHelper.getPurity(filter.getOutputTank().getFluidInTank(0)))
                : Component.literal("—");

        tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                .append(Lang.Tooltip.goggleFilterInputPurity(inputAmount, inputPurityComp)));
        tooltip.add(Component.literal(Lang.PREFIX_SPACE)
                .append(Lang.Tooltip.goggleFilterOutputPurity(outputAmount, outputPurityComp)));

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
