package dev.minhnh.yetanotherthirst.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {

    @Inject(
            method = "apply",
            at = @At("HEAD")
    )
    private void yet_another_thirst$filterRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        map.entrySet().removeIf(entry -> {
            if (entry.getValue().isJsonObject()) {
                JsonObject json = entry.getValue().getAsJsonObject();
                if (json.has("fabric:load_conditions")) {
                    return !ResourceConditions.objectMatchesConditions(json);
                }
            }
            return false;
        });
    }
}
