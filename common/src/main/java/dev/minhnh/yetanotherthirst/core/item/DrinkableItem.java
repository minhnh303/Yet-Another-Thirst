package dev.minhnh.yetanotherthirst.core.item;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class DrinkableItem extends Item {

    private final Supplier<Item> container;

    public DrinkableItem(Supplier<Item> container) {
        super(new Properties().stacksTo(64));
        this.container = container;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));

            boolean shouldDrink = !ThirstCompat.usesExternalThirst(player) && WaterPurity.givePurityEffects(entity, stack);
            if (shouldDrink) {
                ThirstValues.get(stack).ifPresent(value -> {
                    ThirstStorage.get(player).drink(value.thirst(), value.quenched());
                    ThirstStorage.sync(player);
                });
            }

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    level.gameEvent(entity, GameEvent.ITEM_INTERACT_FINISH, entity.getEyePosition());
                    return new ItemStack(container.get());
                }
                ItemStack containerStack = new ItemStack(container.get());
                if (!player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
        }
        level.gameEvent(entity, GameEvent.ITEM_INTERACT_FINISH, entity.getEyePosition());
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
