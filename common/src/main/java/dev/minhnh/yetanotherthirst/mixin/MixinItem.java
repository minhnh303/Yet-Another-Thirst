package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItem {

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    public void changePotionMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof PotionItem) {
            cir.setReturnValue(ThirstConfig.WATER_BOTTLE_STACKSIZE);
        }
    }

    @Inject(
            method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    public void changePotionMaxStackSizeWithStack(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if ((Object) this instanceof PotionItem) {
            if (WaterPurity.isWaterBottle(stack)) {
                cir.setReturnValue(ThirstConfig.WATER_BOTTLE_STACKSIZE);
            } else {
                cir.setReturnValue(1);
            }
        }
    }
}
