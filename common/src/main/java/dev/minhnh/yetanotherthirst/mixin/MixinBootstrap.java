package dev.minhnh.yetanotherthirst.mixin;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces vanilla CauldronInteraction.WATER entries for glass bottle and bucket
 * so that the purity stored in the cauldron's BLOCK_PURITY block state is
 * transferred to the filled item when the player takes water out.
 */
@Mixin(Bootstrap.class)
public class MixinBootstrap {

    @Inject(
            method = "bootStrap",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/cauldron/CauldronInteraction;bootStrap()V",
                    shift = At.Shift.AFTER
            )
    )
    private static void injectCauldronPurityInteractions(CallbackInfo ci) {

        // ── Glass bottle from full cauldron ───────────────────────────────────
        CauldronInteraction.WATER.remove(Items.GLASS_BOTTLE);
        CauldronInteraction.WATER.put(Items.GLASS_BOTTLE, (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                WaterPurity.addPurity(result, level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // ── Bucket from full cauldron ─────────────────────────────────────────
        CauldronInteraction.WATER.remove(Items.BUCKET);
        CauldronInteraction.WATER.put(Items.BUCKET, (blockState, level, pos, player, hand, itemStack) -> {
            if (!blockState.getValue(LayeredCauldronBlock.LEVEL).equals(3)) return InteractionResult.PASS;
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = WaterPurity.addPurity(new ItemStack(Items.WATER_BUCKET), level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });
    }
}
