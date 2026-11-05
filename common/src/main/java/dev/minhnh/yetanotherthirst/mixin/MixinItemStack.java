package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements ItemInstance {

    @Override
    public int getMaxStackSize() {
        ItemStack self = (ItemStack) (Object) this;
        if (WaterPurity.isWaterBottle(self)) {
            return ThirstConfig.WATER_BOTTLE_STACKSIZE;
        } else if (self.getItem() instanceof PotionItem) {
            return 1;
        }
        return this.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    @Inject(method = "isSameItemSameComponents", at = @At("HEAD"), cancellable = true)
    private static void compareWaterBottlePurityComponents(ItemStack first, ItemStack second, CallbackInfoReturnable<Boolean> cir) {
        if (ItemStack.isSameItem(first, second) && WaterPurity.isWaterBottle(first) && WaterPurity.isWaterBottle(second)) {
            cir.setReturnValue(WaterPurity.waterBottleTagsMatchForStacking(first, second));
        }
    }
}
