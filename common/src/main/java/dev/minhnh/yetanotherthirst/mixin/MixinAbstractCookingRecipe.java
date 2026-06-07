package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects the default water purity tag into untagged water containers during
 * recipe matching, allowing players to cook/boil untreated water.
 */
@Mixin(AbstractCookingRecipe.class)
public abstract class MixinAbstractCookingRecipe {

    @Shadow @Final protected Ingredient ingredient;
    @Shadow @Final protected ItemStack result;

    @Inject(method = "matches(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/world/level/Level;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void yet_another_thirst$matchWaterPurityRecipe(SingleRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        ItemStack inputItem = input.item();
        if (!WaterPurity.isWaterFilledContainer(inputItem)) {
            return;
        }

        for (ItemStack ingredientItem : ingredient.getItems()) {
            if (WaterPurity.isSameWaterFilledContainer(inputItem, ingredientItem)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SingleRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"), remap = false)
    private void yet_another_thirst$prepareWaterPotionResult(SingleRecipeInput input, HolderLookup.Provider provider, CallbackInfoReturnable<ItemStack> cir) {
        WaterPurity.prepareWaterPotion(cir.getReturnValue());
    }
}
