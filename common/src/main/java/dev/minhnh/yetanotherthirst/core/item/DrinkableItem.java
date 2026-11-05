package dev.minhnh.yetanotherthirst.core.item;

import dev.minhnh.yetanotherthirst.compat.ThirstCompat;
import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

public class DrinkableItem extends Item {

    private final Supplier<Item> container;

    public DrinkableItem(Properties properties, Supplier<Item> container) {
        super(properties.stacksTo(64).component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK));
        this.container = container;
    }

    public DrinkableItem(Supplier<Item> container) {
        this(new Properties(), container);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));

            if (WaterPurity.hasPurity(stack) && WaterPurity.getPurity(stack) == WaterPurity.MAX_PURITY) {
                ModAdvancements.award(player, ModAdvancements.PURIFIED_WATER);
            }

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
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }
}
