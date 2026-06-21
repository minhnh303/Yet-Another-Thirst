package dev.minhnh.yetanotherthirst.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nonnull;

/**
 * Ensures furnaces check NBT tags (not just item types) when deciding if a new
 * recipe result can stack with items already in the output slot. This prevents
 * water containers of different purities from merging in the output slot.
 *
 * Fabric uses unmodified vanilla code where canBurn is static.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class MixinAbstractFurnaceEntity {

    @Redirect(
            method = "canBurn",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private static boolean yet_another_thirst$canBurnCheckTags(@Nonnull ItemStack outputSlotItem, @Nonnull ItemStack recipeResult) {
        return ItemStack.isSameItemSameTags(outputSlotItem, recipeResult);
    }
}
