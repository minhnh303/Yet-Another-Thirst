package dev.minhnh.yetanotherthirst.mixin.create;

import dev.minhnh.yetanotherthirst.Lang;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.List;

@Pseudo
@Mixin(targets = "com.simibubi.create.content.fluids.tank.FluidTankBlockEntity", remap = false)
public class MixinFluidTankBlockEntity {

    @SuppressWarnings("null")
    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), remap = false)
    private void onAddToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
        try {
            Object controller = this.getClass().getMethod("getControllerBE").invoke(this);
            if (!(controller instanceof SidedStorageBlockEntity storageEntity)) return;

            Storage<FluidVariant> storage = storageEntity.getFluidStorage(null);
            if (storage == null) return;

            Iterator<StorageView<FluidVariant>> it = storage.iterator();
            while (it.hasNext()) {
                StorageView<FluidVariant> view = it.next();
                if (view.isResourceBlank() || view.getAmount() == 0) continue;
                FluidVariant variant = view.getResource();
                if (!variant.isOf(Fluids.WATER)) continue;

                int purity = ThirstConfig.DEFAULT_PURITY;
                CustomData customData = variant.getComponents().get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).orElse(null);
                CompoundTag nbt = customData != null ? customData.copyTag() : null;
                if (nbt != null && nbt.contains("Purity")) {
                    purity = nbt.getInt("Purity");
                }
                tooltip.add(Component.literal(Lang.PREFIX_SPACE).append(Lang.Jade.waterPurity(WaterPurity.purityComponent(purity))));
                break;
            }
        } catch (Throwable ignored) {
        }
    }
}
