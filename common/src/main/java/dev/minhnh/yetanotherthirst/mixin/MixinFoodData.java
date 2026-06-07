package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class MixinFoodData {

    @Shadow
    public abstract void addExhaustion(float p_38704_);

    @Shadow
    private float exhaustionLevel;

    @Unique
    private int yet_another_thirst$dehydratedHealTimer = 0;

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 0),
            remap = false
    )
    private void yet_another_thirst$healWithSaturation(Player player, float amount) {
        ThirstState thirstData = ThirstStorage.get(player);
        FoodData foodData = player.getFoodData();

        float f = Math.min(foodData.getSaturationLevel(), 6.0F);

        boolean shouldHeal = !ThirstConfig.DEHYDRATION_HALTS_HEALTH_REGEN || thirstData.getThirst() >= 20;

        if (shouldHeal) {
            player.heal(f / 6.0F);
            thirstData.setJustHealed(true);
            return;
        }

        yet_another_thirst$dehydratedHealTimer++;
        if (yet_another_thirst$dehydratedHealTimer >= 8 && thirstData.getThirst() > 18) {
            player.heal(f / 6.0F);
            thirstData.setJustHealed(true);
            yet_another_thirst$dehydratedHealTimer = 0;
            return;
        }

        this.addExhaustion(-f);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;heal(F)V", ordinal = 1),
            remap = false
    )
    private void yet_another_thirst$healWithHunger(Player player, float amount) {
        ThirstState thirstData = ThirstStorage.get(player);
        boolean shouldHeal = !ThirstConfig.DEHYDRATION_HALTS_HEALTH_REGEN || thirstData.getThirst() > 18;

        if (shouldHeal) {
            player.heal(1.0F);
            thirstData.setJustHealed(true);
        } else {
            this.addExhaustion(-6.0F);
        }
    }

    @Inject(
            method = "tick",
            at = @At(value = "HEAD"),
            remap = false
    )
    private void yet_another_thirst$dealWithExhaustionBySaturation(Player player, CallbackInfo ci) {
        if (exhaustionLevel > 4.0F) {
            ThirstStorage.get(player).setExhaustionRecalculate(true);
        }
    }
}
