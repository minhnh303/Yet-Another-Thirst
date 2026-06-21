package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class FabricItemFinishMixin {

    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    private void onFinishUsingItem(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        ItemStack stack = entity.getUseItem();
        if (!stack.isEmpty()) {
            ThirstEvents.onItemFinished(entity, stack);
        }
    }
}
