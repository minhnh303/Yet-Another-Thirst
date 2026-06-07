package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
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

    public static boolean tryDrink() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isCrouching() || player.isInvulnerable()) {
            return false;
        }
        Level level = mc.level;
        if (level == null) {
            return false;
        }

        boolean handAvailable = player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()
                && (!ThirstConfig.DRINK_BOTH_HANDS_NEEDED || player.getItemInHand(InteractionHand.OFF_HAND).isEmpty());

        if (!handAvailable) {
            return false;
        }

        var state = ThirstStorage.get(player);
        if (!state.isEnabled() || state.getThirst() >= ThirstConfig.MAX_THIRST) {
            return false;
        }

        var eyePos = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(
                eyePos,
                eyePos.add(player.getLookAngle().scale(player.blockInteractionRange())),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY,
                player));

        BlockPos pos = hit.getBlockPos();
        if (!level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }

        level.playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 1.0F, 1.0F);
        player.swing(InteractionHand.MAIN_HAND);
        ForgeNetwork.sendDrinkByHand(pos);
        return true;
    }

    public static void handleRightClickBlock(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (tryDrink()) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
