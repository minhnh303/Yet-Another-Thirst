package dev.minhnh.yetanotherthirst.mixin.ie;

import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "blusunrize.immersiveengineering.common.blocks.metal.FluidPumpBlockEntity", remap = false)
public abstract class MixinFluidPumpBlockEntity {

    @Redirect(
            method = "gatherInfiniteFluidFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;fill(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)I"
            ),
            remap = false
    )
    private int onGatherInfiniteFluidFill(FluidTank instance, FluidStack resource, IFluidHandler.FluidAction action, Direction gatherFrom) {
        if (resource != null && !resource.isEmpty() && resource.is(FluidTags.WATER)) {
            BlockEntity be = (BlockEntity) (Object) this;
            BlockPos pos = be.getBlockPos().relative(gatherFrom);
            FluidPurityHelper.addPurity(resource, WaterPurity.getBlockPurity(be.getLevel(), pos));
        }
        return instance.fill(resource, action);
    }
}
