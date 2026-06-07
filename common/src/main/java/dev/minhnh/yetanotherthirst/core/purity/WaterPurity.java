package dev.minhnh.yetanotherthirst.core.purity;

import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class WaterPurity {

    public static final int MIN_PURITY = 0;
    public static final int MAX_PURITY = 3;

    /**
     * Block-state purity stored on cauldrons. Value 0 = unset (use DEFAULT_PURITY);
     * values 1–4 map to purity levels 0–3 (offset by 1 because 0 is the mixin-safe "null").
     */
    public static final IntegerProperty BLOCK_PURITY = IntegerProperty.create("purity", 0, 4);

    private static final List<ContainerWithPurity> CONTAINERS = new ArrayList<>();
    private static final Random RANDOM = new Random();

    private WaterPurity() {}

    /** Called after item registration is complete (ServerStartedEvent / FMLCommonSetupEvent). */
    public static void init() {
        CONTAINERS.clear();
        CONTAINERS.add(new ContainerWithPurity(
                new ItemStack(Items.GLASS_BOTTLE),
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER))
                .setEqualsFilled(stack -> stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER));
        CONTAINERS.add(new ContainerWithPurity(
                new ItemStack(ModItems.TERRACOTTA_BOWL.get()),
                new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get())));
        CONTAINERS.add(new ContainerWithPurity(
                new ItemStack(Items.BOWL),
                new ItemStack(ModItems.WOODEN_WATER_BOWL.get())));
        CONTAINERS.add(new ContainerWithPurity(
                new ItemStack(Items.BUCKET),
                new ItemStack(Items.WATER_BUCKET), false)
                .canHarvestRunningWater(false));

        registerCauldronInteractions();
    }

    public static void registerCauldronInteractions() {
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

        // ── Bowl from full cauldron ───────────────────────────────────────────
        CauldronInteraction.WATER.put(Items.BOWL, (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = WaterPurity.addPurity(new ItemStack(ModItems.WOODEN_WATER_BOWL.get()), level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // ── Terracotta bowl from full cauldron ─────────────────────────────────
        CauldronInteraction.WATER.put(ModItems.TERRACOTTA_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = WaterPurity.addPurity(new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()), level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        });

        // ── Filling cauldron from water bucket ────────────────────────────────
        CauldronInteraction.EMPTY.put(Items.WATER_BUCKET, (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 3, getPurity(itemStack), new ItemStack(Items.BUCKET));
        });
        CauldronInteraction.WATER.put(Items.WATER_BUCKET, (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 3, getPurity(itemStack), new ItemStack(Items.BUCKET));
        });

        // ── Filling cauldron from water bottle ────────────────────────────────
        CauldronInteraction.EMPTY.put(Items.POTION, (blockState, level, pos, player, hand, itemStack) -> {
            if (PotionUtils.getPotion(itemStack) != Potions.WATER) return InteractionResult.PASS;
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.GLASS_BOTTLE));
        });
        CauldronInteraction.WATER.put(Items.POTION, (blockState, level, pos, player, hand, itemStack) -> {
            if (PotionUtils.getPotion(itemStack) != Potions.WATER) return InteractionResult.PASS;
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.GLASS_BOTTLE));
        });

        // ── Filling cauldron from terracotta water bowl ───────────────────────
        CauldronInteraction.EMPTY.put(ModItems.TERRACOTTA_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(ModItems.TERRACOTTA_BOWL.get()));
        });
        CauldronInteraction.WATER.put(ModItems.TERRACOTTA_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(ModItems.TERRACOTTA_BOWL.get()));
        });

        // ── Filling cauldron from wooden water bowl ───────────────────────────
        CauldronInteraction.EMPTY.put(ModItems.WOODEN_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.BOWL));
        });
        CauldronInteraction.WATER.put(ModItems.WOODEN_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.BOWL));
        });
    }

    public static InteractionResult fillCauldron(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack itemStack, int addLevels, int addedPurity, ItemStack emptyContainer) {
        int currentLevel = 0;
        int currentPurity = ThirstConfig.DEFAULT_PURITY;

        if (blockState.is(Blocks.WATER_CAULDRON)) {
            currentLevel = blockState.getValue(LayeredCauldronBlock.LEVEL);
            int val = blockState.hasProperty(BLOCK_PURITY) ? blockState.getValue(BLOCK_PURITY) : 0;
            currentPurity = val == 0 ? ThirstConfig.DEFAULT_PURITY : val - 1;
        } else if (!blockState.is(Blocks.CAULDRON)) {
            return InteractionResult.PASS;
        }

        if (currentLevel >= 3) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            Item item = itemStack.getItem();
            int newLevel = Math.min(3, currentLevel + addLevels);
            int addedVolume = newLevel - currentLevel;

            int finalPurity;
            if (currentLevel == 0) {
                finalPurity = addedPurity;
            } else {
                double blended = (currentLevel * currentPurity + addedVolume * addedPurity) / (double) newLevel;
                finalPurity = (int) Math.round(blended);
            }

            BlockState newState = Blocks.WATER_CAULDRON.defaultBlockState()
                    .setValue(LayeredCauldronBlock.LEVEL, newLevel)
                    .setValue(BLOCK_PURITY, finalPurity + 1);

            level.setBlockAndUpdate(pos, newState);
            player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, emptyContainer.copy()));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));

            SoundEvent sound = addLevels >= 3 ? SoundEvents.BUCKET_EMPTY : SoundEvents.BOTTLE_EMPTY;
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public static boolean isWaterFilledContainer(ItemStack stack) {
        for (ContainerWithPurity c : CONTAINERS)
            if (c.equalsFilled(stack)) return true;
        return false;
    }

    public static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    public static boolean isEmptyWaterContainer(ItemStack stack) {
        for (ContainerWithPurity c : CONTAINERS)
            if (c.equalsEmpty(stack)) return true;
        return false;
    }

    public static boolean canHarvestRunningWater(ItemStack stack) {
        for (ContainerWithPurity c : CONTAINERS)
            if (c.equalsEmpty(stack) && c.canHarvestRunningWater()) return true;
        return false;
    }

    public static ContainerWithPurity getContainerForEmpty(ItemStack stack) {
        for (ContainerWithPurity c : CONTAINERS)
            if (c.equalsEmpty(stack)) return c;
        return null;
    }

    public static boolean hasPurity(ItemStack stack) {
        return isWaterFilledContainer(stack) && stack.hasTag() && Objects.requireNonNull(stack.getTag()).contains("Purity");
    }

    public static int getPurity(ItemStack stack) {
        if (!isWaterFilledContainer(stack) || !hasPurity(stack)) {
            return ThirstConfig.DEFAULT_PURITY;
        }
        return Objects.requireNonNull(stack.getTag()).getInt("Purity");
    }

    public static ItemStack addPurity(ItemStack stack, int purity) {
        if (!isWaterFilledContainer(stack)) {
            return stack;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Purity", purity);
        return stack;
    }

    public static ItemStack addPurity(ItemStack stack, Level level, BlockPos pos) {
        return addPurity(stack, getBlockPurity(level, pos));
    }

    public static void verifyItemStackPurity(ItemStack stack) {
        if (!stack.isEmpty() && isWaterFilledContainer(stack) && !hasPurity(stack)) {
            addPurity(stack, ThirstConfig.DEFAULT_PURITY);
        }
    }


    public static boolean waterBottleTagsMatchForStacking(ItemStack first, ItemStack second) {
        if (!isWaterBottle(first) || !isWaterBottle(second)) return false;
        return getPurity(first) == getPurity(second)
                && Objects.equals(waterBottleTagWithoutPurity(first), waterBottleTagWithoutPurity(second));
    }

    private static CompoundTag waterBottleTagWithoutPurity(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return null;

        CompoundTag normalized = tag.copy();
        normalized.remove("Purity");
        return normalized.isEmpty() ? null : normalized;
    }


    public static int getBlockPurity(Level level, BlockPos pos) {
        // Cauldron: stored purity takes precedence over open-water derivation
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof AbstractCauldronBlock) {
            if (blockState.hasProperty(BLOCK_PURITY)) {
                int val = blockState.getValue(BLOCK_PURITY);
                return val == 0 ? ThirstConfig.DEFAULT_PURITY : val - 1;
            }
            return ThirstConfig.DEFAULT_PURITY;
        }

        // Open water: derive from Y-coordinate bands and flow
        int purity = 0;
        int y = pos.getY();
        if (y > ThirstConfig.MOUNTAINS_Y || (y < ThirstConfig.CAVES_Y && y < ThirstConfig.MOUNTAINS_Y - 32)) {
            purity = 1;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos).isSource()) {
            purity = Math.min(purity + ThirstConfig.RUNNING_WATER_PURIFICATION_AMOUNT, MAX_PURITY);
        }
        return purity;
    }

    /**
     * Applies purity-based effects to the entity and returns whether thirst should be restored.
     * Returns true for non-purity-aware items (vanilla potions, food handled elsewhere).
     */
    public static boolean givePurityEffects(LivingEntity entity, ItemStack stack) {
        if (!isWaterFilledContainer(stack)) return true;
        if (!hasPurity(stack)) return true;
        return givePurityEffects(entity, getPurity(stack));
    }

    public static boolean givePurityEffects(LivingEntity entity, int purity) {
        boolean shouldDrink = true;
        float chance = RANDOM.nextFloat();

        float nauseaChance;
        float poisonChance;
        switch (purity) {
            case 0 -> { nauseaChance = ThirstConfig.DIRTY_NAUSEA_CHANCE; poisonChance = ThirstConfig.DIRTY_POISON_CHANCE; }
            case 1 -> { nauseaChance = ThirstConfig.SLIGHTLY_DIRTY_NAUSEA_CHANCE; poisonChance = ThirstConfig.SLIGHTLY_DIRTY_POISON_CHANCE; }
            case 2 -> { nauseaChance = ThirstConfig.ACCEPTABLE_NAUSEA_CHANCE; poisonChance = ThirstConfig.ACCEPTABLE_POISON_CHANCE; }
            default -> { nauseaChance = ThirstConfig.PURIFIED_NAUSEA_CHANCE; poisonChance = ThirstConfig.PURIFIED_POISON_CHANCE; }
        }

        if (entity instanceof ServerPlayer sp) {
            if (chance < nauseaChance) {
                sp.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 5, 0));
                sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));
            }
            if (chance <= poisonChance) {
                sp.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                shouldDrink = false;
            }
        }

        return shouldDrink || ThirstConfig.QUENCH_WHEN_DEBUFFED;
    }

    /** Appends a purity tooltip line only when the item carries an explicit Purity NBT tag. */
    public static void appendTooltip(ItemStack stack, List<Component> tooltip) {
        if (!isWaterFilledContainer(stack)) return;
        if (!hasPurity(stack)) return;
        int purity = getPurity(stack);
        if (purity < MIN_PURITY || purity > MAX_PURITY) return;
        tooltip.add(purityComponent(purity));
    }

    private static Component purityComponent(int purity) {
        String key = "yet_another_thirst.purity." + switch (purity) {
            case 0 -> "dirty";
            case 1 -> "slightly_dirty";
            case 2 -> "acceptable";
            default -> "purified";
        };
        int color = switch (purity) {
            case 0 -> 0xa84825;  // dirty   — dark brown
            case 1 -> 0x796c71;  // slightly dirty — gray-brown
            case 2 -> 0x5d829d;  // acceptable     — blue-gray
            default -> 0x21b1ff; // purified        — bright blue
        };
        return Component.translatable(key).withStyle(s -> s.withColor(color));
    }
}
