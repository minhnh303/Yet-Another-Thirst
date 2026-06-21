package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.platform.FabricPlayerDataStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void yat_save(CompoundTag compound, CallbackInfo ci) {

        CompoundTag data = FabricPlayerDataStore.get((Player) (Object) this);
        if (!data.isEmpty()) {
            compound.put("YetAnotherThirstData", data.copy());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void yat_load(CompoundTag compound, CallbackInfo ci) {

        if (compound.contains("YetAnotherThirstData")) {
            FabricPlayerDataStore.put((Player) (Object) this, compound.getCompound("YetAnotherThirstData"));
        }
    }
}
