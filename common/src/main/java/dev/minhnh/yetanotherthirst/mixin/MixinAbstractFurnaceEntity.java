package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Ensures furnaces check NBT tags (not just item types) when deciding if a new
 * recipe result can stack with items already in the output slot. This prevents
 * water containers of different purities from merging in the output slot.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class MixinAbstractFurnaceEntity {

    @Redirect(
            method = {"canBurn", "burn"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItemSameComponents(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            remap = false
    )
    private boolean yet_another_thirst$canBurnCheckComponents(ItemStack outputSlotItem, ItemStack recipeResult) {
        if (WaterPurity.isWaterFilledContainer(outputSlotItem) || WaterPurity.isWaterFilledContainer(recipeResult)) {
            return WaterPurity.isSameWaterFilledContainer(outputSlotItem, recipeResult);
        }

        return ItemStack.isSameItemSameComponents(outputSlotItem, recipeResult);
    }
}
