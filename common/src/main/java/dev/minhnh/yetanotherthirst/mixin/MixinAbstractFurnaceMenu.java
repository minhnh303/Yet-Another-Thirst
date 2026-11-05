package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFurnaceMenu.class)
public abstract class MixinAbstractFurnaceMenu {

    @Inject(method = "canSmelt", at = @At("RETURN"), cancellable = true)
    private void yet_another_thirst$filterWaterSmeltingInput(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }
        if (stack.is(Items.POTION) && !WaterPurity.isWaterBottle(stack)) {
            cir.setReturnValue(false);
            return;
        }
        if (WaterPurity.isWaterFilledContainer(stack) && WaterPurity.getPurity(stack) >= WaterPurity.MAX_PURITY) {
            cir.setReturnValue(false);
        }
    }
}

