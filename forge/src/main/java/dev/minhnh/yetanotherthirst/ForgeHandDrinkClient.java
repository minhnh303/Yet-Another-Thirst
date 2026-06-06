package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ForgeHandDrinkClient {

    private ForgeHandDrinkClient() {}

    public static void tryDrink() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level level = mc.level;
        if (player == null || level == null || !player.isCrouching() || player.isInvulnerable()) {
            return;
        }

        boolean handAvailable = ThirstConfig.DRINK_BOTH_HANDS_NEEDED
                ? player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                        && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()
                : player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty();

        if (!handAvailable) {
            return;
        }

        BlockHitResult hit = level.clip(new ClipContext(
                player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().scale(player.getBlockReach())),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return;
        }

        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        ForgeNetwork.sendDrinkByHand(pos);
    }
}
