package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SingleItemRecipe.class)
public abstract class MixinSingleItemRecipe {

    @Shadow
    protected abstract ItemStackTemplate result();

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void yet_another_thirst$matchWaterBoilingPurity(SingleRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            return;
        }

        ItemStackTemplate template = result();
        CustomData customData = template.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();
        if (!tag.contains("Purity")) {
            return;
        }

        int targetPurity = tag.getIntOr("Purity", -1);
        if (targetPurity < 0) {
            return;
        }

        ItemStack inStack = input.item();
        if (template.item().value() == Items.POTION) {
            if (!WaterPurity.isWaterBottle(inStack)) {
                cir.setReturnValue(false);
                return;
            }
        } else if (!WaterPurity.isWaterFilledContainer(inStack)) {
            cir.setReturnValue(false);
            return;
        }

        int inPurity = WaterPurity.getPurity(inStack);
        if (inPurity >= WaterPurity.MAX_PURITY) {
            cir.setReturnValue(false);
            return;
        }

        SingleItemRecipe self = (SingleItemRecipe) (Object) this;
        if (self instanceof CampfireCookingRecipe) {
            if (targetPurity != inPurity + 1) {
                cir.setReturnValue(false);
            }
        } else if (self instanceof SmeltingRecipe) {
            if (targetPurity == 2) {
                if (inPurity > 1) {
                    cir.setReturnValue(false);
                }
            } else if (targetPurity == 3) {
                if (inPurity != 2) {
                    cir.setReturnValue(false);
                }
            } else {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "assemble", at = @At("RETURN"))
    private void yet_another_thirst$preserveComponentsOnAssemble(SingleRecipeInput input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (WaterPurity.isWaterFilledContainer(result) && WaterPurity.hasPurity(result)) {
            ItemStack in = input.item();
            if (in.has(DataComponents.CUSTOM_NAME) && !result.has(DataComponents.CUSTOM_NAME)) {
                result.set(DataComponents.CUSTOM_NAME, in.get(DataComponents.CUSTOM_NAME));
            }
        }
    }
}

