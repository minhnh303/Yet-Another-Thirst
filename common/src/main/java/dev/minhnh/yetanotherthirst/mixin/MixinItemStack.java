package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow public abstract Item getItem();

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true, remap = false)
    public void changeWaterBottleStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (getItem() == Items.POTION && WaterPurity.isWaterBottle(self)) {
            cir.setReturnValue(ThirstConfig.WATER_BOTTLE_STACKSIZE);
        }
    }

    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true, remap = false)
    private static void compareWaterContainerComponents(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (WaterPurity.isWaterFilledContainer(first) && WaterPurity.isWaterFilledContainer(second)) {
            cir.setReturnValue(WaterPurity.isSameWaterFilledContainer(first, second));
        }
    }
}
