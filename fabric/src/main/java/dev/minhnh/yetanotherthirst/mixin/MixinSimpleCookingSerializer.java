package dev.minhnh.yetanotherthirst.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;

/**
 * Mixin to allow vanilla cooking recipes (smelting, campfire cooking, etc.)
 * to parse a JsonObject for their "result" field on Fabric, supporting NBT tags
 * and custom item counts just like Forge.
 */
@Mixin(SimpleCookingSerializer.class)
public class MixinSimpleCookingSerializer {

    @Redirect(
            method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/GsonHelper;getAsString(Lcom/google/gson/JsonObject;Ljava/lang/String;)Ljava/lang/String;"
            )
    )
    private String yet_another_thirst$redirectResult(JsonObject json, String memberName) {
        if ("result".equals(memberName)) {
            JsonElement resultElement = json.get("result");
            if (resultElement != null && resultElement.isJsonObject()) {
                return GsonHelper.getAsString(resultElement.getAsJsonObject(), "item");
            }
        }
        return GsonHelper.getAsString(json, memberName);
    }

    @ModifyVariable(
            method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/AbstractCookingRecipe;",
            at = @At("STORE"),
            ordinal = 0
    )
    private ItemStack yet_another_thirst$injectNbtIntoResult(ItemStack itemStack, ResourceLocation recipeId, JsonObject json) {
        JsonElement resultElement = json.get("result");
        if (resultElement != null && resultElement.isJsonObject()) {
            JsonObject resultObj = resultElement.getAsJsonObject();
            if (resultObj.has("nbt")) {
                try {
                    JsonElement nbtElement = resultObj.get("nbt");
                    CompoundTag tag;
                    if (nbtElement.isJsonObject()) {
                        tag = TagParser.parseTag(nbtElement.toString());
                    } else {
                        tag = TagParser.parseTag(GsonHelper.convertToString(nbtElement, "nbt"));
                    }
                    itemStack.setTag(tag);
                } catch (Exception e) {
                    // Ignore parsing error
                }
            }
            if (resultObj.has("count")) {
                itemStack.setCount(GsonHelper.getAsInt(resultObj, "count"));
            }
        }
        return itemStack;
    }
}
