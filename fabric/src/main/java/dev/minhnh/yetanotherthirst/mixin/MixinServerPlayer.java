package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.platform.FabricPlayerDataStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void yat_save(ValueOutput output, CallbackInfo ci) {
        CompoundTag data = FabricPlayerDataStore.get((Player) (Object) this);
        if (!data.isEmpty()) {
            output.store("YetAnotherThirstData", CompoundTag.CODEC, data.copy());
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void yat_load(ValueInput input, CallbackInfo ci) {
        input.read("YetAnotherThirstData", CompoundTag.CODEC)
                .ifPresent(tag -> FabricPlayerDataStore.put((Player) (Object) this, tag));
    }
}
