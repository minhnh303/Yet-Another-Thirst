package dev.minhnh.yetanotherthirst.core.thirst;

import dev.minhnh.yetanotherthirst.core.item.DrinkableItem;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ThirstEvents {

    private ThirstEvents() {
    }

    public static void onPlayerTick(ServerPlayer player) {

        ThirstTicker.tick(player);
    }

    public static void onItemFinished(LivingEntity entity, ItemStack stack) {

        // DrinkableItem handles its own thirst inside finishUsingItem
        if (stack.getItem() instanceof DrinkableItem) {
            return;
        }
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        ThirstValues.get(stack).ifPresent(value -> {
            boolean shouldDrink = WaterPurity.givePurityEffects(entity, stack);
            if (shouldDrink) {
                ThirstStorage.get(player).drink(value.thirst(), value.quenched());
            }
            ThirstStorage.sync(player);
        });
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {

        ThirstStorage.sync(player);
    }

    public static void onPlayerLoggedOut(Player player) {

        ThirstStorage.remove(player);
    }

    public static void onPlayerClone(Player oldPlayer, Player newPlayer, boolean wasDeath) {

        ThirstStorage.copy(oldPlayer, newPlayer, wasDeath);
        if (newPlayer instanceof ServerPlayer serverPlayer) {
            ThirstStorage.sync(serverPlayer);
        }
    }
}
