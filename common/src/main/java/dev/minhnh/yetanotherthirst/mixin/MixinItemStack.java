package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.PotionItem;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    public void changeWaterBottleStackSize(CallbackInfoReturnable<Integer> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (WaterPurity.isWaterBottle(self)) {
            cir.setReturnValue(ThirstConfig.WATER_BOTTLE_STACKSIZE);
        } else if (self.getItem() instanceof PotionItem) {
            cir.setReturnValue(1);
        }
    }

    @Inject(method = "isSameItemSameTags", at = @At("HEAD"), cancellable = true)
    private static void compareWaterBottlePurityTags(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (ItemStack.isSameItem(first, second) && WaterPurity.isWaterBottle(first) && WaterPurity.isWaterBottle(second)) {
            cir.setReturnValue(WaterPurity.waterBottleTagsMatchForStacking(first, second));
        }
    }
}
