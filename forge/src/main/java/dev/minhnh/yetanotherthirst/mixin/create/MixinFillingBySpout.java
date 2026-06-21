package dev.minhnh.yetanotherthirst.mixin.create;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.spout.FillingBySpout;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import dev.minhnh.yetanotherthirst.compat.FluidPurityHelper;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

/**
 * When Create's spout fills an item, preserve purity from the source FluidStack
 * through both recipe-based and generic filling paths.
 */
@Pseudo
@Mixin(value = FillingBySpout.class, remap = false)
public class MixinFillingBySpout {

    @Final
    @Shadow
    private static RecipeWrapper WRAPPER;

    @Inject(method = "fillItem", at = @At("HEAD"), cancellable = true, remap = false)
    private static void fillItem(Level world, int requiredAmount, ItemStack stack,
                                 FluidStack availableFluid, CallbackInfoReturnable<ItemStack> cir) {
        if (!FluidPurityHelper.hasPurity(availableFluid)) return;

        int purity = FluidPurityHelper.getPurity(availableFluid);
        FluidStack toFill = availableFluid.copy();
        toFill.setAmount(requiredAmount);
        WRAPPER.setItem(0, stack);

        FillingRecipe fillingRecipe = SequencedAssemblyRecipe
                .getRecipe(world, WRAPPER, AllRecipeTypes.FILLING.getType(), FillingRecipe.class)
                .filter(fr -> fr.getRequiredFluid().test(toFill))
                .orElseGet(() -> {
                    Iterator<Recipe<RecipeWrapper>> it = world.getRecipeManager()
                            .getRecipesFor(AllRecipeTypes.FILLING.getType(), WRAPPER, world).iterator();
                    FillingRecipe fr;
                    FluidIngredient requiredFluid;
                    do {
                        if (!it.hasNext()) return null;
                        fr = (FillingRecipe) it.next();
                        requiredFluid = fr.getRequiredFluid();
                    } while (!requiredFluid.test(toFill));
                    return fr;
                });

        if (fillingRecipe != null) {
            List<ItemStack> results = fillingRecipe.rollResults();
            availableFluid.shrink(requiredAmount);
            stack.shrink(1);
            ItemStack result = results.isEmpty() ? ItemStack.EMPTY : results.get(0);
            cir.setReturnValue(WaterPurity.isWaterFilledContainer(result)
                    ? WaterPurity.addPurity(result, purity) : result);
        } else {
            cir.setReturnValue(GenericItemFilling.fillItem(world, requiredAmount, stack, availableFluid));
        }
    }
}
