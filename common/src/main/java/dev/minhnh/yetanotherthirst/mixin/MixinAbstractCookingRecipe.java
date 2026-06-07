package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Injects the default water purity tag into untagged water containers during
 * recipe matching, allowing players to cook/boil untreated water.
 */
@Mixin(AbstractCookingRecipe.class)
public class MixinAbstractCookingRecipe {

    @ModifyArg(
            method = "matches",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    public ItemStack yet_another_thirst$injectDefaultPurity(ItemStack itemStack) {
        if (WaterPurity.isWaterFilledContainer(itemStack)) {
            CompoundTag tag = itemStack.getTag();
            if (tag == null || !tag.contains("Purity")) {
                ItemStack matched = itemStack.copy();
                matched.getOrCreateTag().putInt("Purity", ThirstConfig.DEFAULT_PURITY);
                return matched;
            }
        }
        return itemStack;
    }
}
