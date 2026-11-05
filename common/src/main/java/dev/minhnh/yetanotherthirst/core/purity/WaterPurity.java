package dev.minhnh.yetanotherthirst.core.purity;

import dev.minhnh.yetanotherthirst.core.advancement.ModAdvancements;
import dev.minhnh.yetanotherthirst.core.effect.ModEffects;
import dev.minhnh.yetanotherthirst.core.item.ModItems;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
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
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

@SuppressWarnings("null")
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

    private static Map<Item, CauldronInteraction> getDispatcherItems(CauldronInteraction.Dispatcher dispatcher) {
        return ((dev.minhnh.yetanotherthirst.mixin.CauldronInteractionDispatcherAccessor) dispatcher).yet_another_thirst$getItems();
    }

    /** Called from common setup after item registration is complete. */
    public static void init() {
        CONTAINERS.clear();
        CONTAINERS.add(new ContainerWithPurity(
                new ItemStack(Items.GLASS_BOTTLE),
                PotionContents.createItemStack(Items.POTION, Potions.WATER))
                .setEqualsFilled(stack -> stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)));
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
        Map<Item, CauldronInteraction> water = getDispatcherItems(CauldronInteractions.WATER);
        Map<Item, CauldronInteraction> empty = getDispatcherItems(CauldronInteractions.EMPTY);

        // Glass bottle from full cauldron
        water.remove(Items.GLASS_BOTTLE);
        water.put(Items.GLASS_BOTTLE, (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                WaterPurity.addPurity(result, level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.SUCCESS;
        });

        // Bucket from full cauldron
        water.remove(Items.BUCKET);
        water.put(Items.BUCKET, (blockState, level, pos, player, hand, itemStack) -> {
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
            return InteractionResult.SUCCESS;
        });

        // Bowl from full cauldron
        water.put(Items.BOWL, (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = WaterPurity.addPurity(new ItemStack(ModItems.WOODEN_WATER_BOWL.get()), level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.SUCCESS;
        });

        // Terracotta bowl from full cauldron
        water.put(ModItems.TERRACOTTA_BOWL.get(), (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                ItemStack result = WaterPurity.addPurity(new ItemStack(ModItems.TERRACOTTA_WATER_BOWL.get()), level, pos);
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(level.getBlockState(pos), level, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return InteractionResult.SUCCESS;
        });

        // Filling cauldron from water bucket
        empty.put(Items.WATER_BUCKET, (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 3, getPurity(itemStack), new ItemStack(Items.BUCKET)));
        water.put(Items.WATER_BUCKET, (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 3, getPurity(itemStack), new ItemStack(Items.BUCKET)));

        // Filling cauldron from water bottle
        empty.put(Items.POTION, (blockState, level, pos, player, hand, itemStack) -> {
            if (!isWaterBottle(itemStack)) return InteractionResult.PASS;
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.GLASS_BOTTLE));
        });
        water.put(Items.POTION, (blockState, level, pos, player, hand, itemStack) -> {
            if (!isWaterBottle(itemStack)) return InteractionResult.PASS;
            return fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.GLASS_BOTTLE));
        });

        // Filling cauldron from terracotta water bowl
        empty.put(ModItems.TERRACOTTA_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(ModItems.TERRACOTTA_BOWL.get())));
        water.put(ModItems.TERRACOTTA_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(ModItems.TERRACOTTA_BOWL.get())));

        // Filling cauldron from wooden water bowl
        empty.put(ModItems.WOODEN_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.BOWL)));
        water.put(ModItems.WOODEN_WATER_BOWL.get(), (blockState, level, pos, player, hand, itemStack) ->
            fillCauldron(blockState, level, pos, player, hand, itemStack, 1, getPurity(itemStack), new ItemStack(Items.BOWL)));

        // Washing clogged fabric filter in water cauldron
        water.put(ModItems.CLOGGED_FABRIC_FILTER.get(), (blockState, level, pos, player, hand, itemStack) -> {
            if (!level.isClientSide()) {
                ItemStack result = new ItemStack(ModItems.FABRIC_FILTER_CORE.get());
                player.setItemInHand(hand, ItemUtils.createFilledResult(itemStack, player, result));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, pos);
                level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.BLOCKS, 1.0F, 1.0F);
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    ModAdvancements.award(sp, ModAdvancements.WASH_FILTER);
                }
            }
            return InteractionResult.SUCCESS;
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

            // Empty cauldron takes the added purity as-is; mixing dilutes to the worst of the two purities
            int finalPurity = (currentLevel == 0) ? addedPurity : Math.min(currentPurity, addedPurity);

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

        return InteractionResult.SUCCESS;
    }

    public static boolean isWaterFilledContainer(ItemStack stack) {
        for (ContainerWithPurity c : CONTAINERS)
            if (c.equalsFilled(stack)) return true;
        return false;
    }

    public static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);
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
        if (!isWaterFilledContainer(stack)) return false;
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.copyTag().contains("Purity");
    }

    public static int getPurity(ItemStack stack) {
        if (!isWaterFilledContainer(stack) || !hasPurity(stack)) {
            return ThirstConfig.DEFAULT_PURITY;
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return ThirstConfig.DEFAULT_PURITY;
        return customData.copyTag().getIntOr("Purity", ThirstConfig.DEFAULT_PURITY);
    }

    public static ItemStack addPurity(ItemStack stack, int purity) {
        if (!isWaterFilledContainer(stack)) {
            return stack;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("Purity", purity));
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
        if (getPurity(first) != getPurity(second)) return false;

        if (!Objects.equals(waterBottleTagWithoutPurity(first), waterBottleTagWithoutPurity(second))) {
            return false;
        }

        var comp1 = first.getComponents();
        var comp2 = second.getComponents();
        Set<DataComponentType<?>> keys = new HashSet<>(comp1.keySet());
        keys.addAll(comp2.keySet());
        keys.remove(DataComponents.CUSTOM_DATA);

        for (DataComponentType<?> key : keys) {
            if (!Objects.equals(comp1.get(key), comp2.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static CompoundTag waterBottleTagWithoutPurity(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return null;

        CompoundTag normalized = customData.copyTag();
        normalized.remove("Purity");
        return normalized.isEmpty() ? null : normalized;
    }


    public static int getBlockPurity(Level level, BlockPos pos) {
        // Cauldron: stored purity takes precedence over open-water derivation
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof AbstractCauldronBlock) {
            if (blockState.hasProperty(BLOCK_PURITY)) {
                int val = blockState.getValue(BLOCK_PURITY);
                if (val == 0) {
                    int defaultPurity = ThirstConfig.DEFAULT_PURITY;
                    // If cauldron does not have dirty water data yet, set it to the default
                    if (!level.isClientSide()) {
                        level.setBlockAndUpdate(pos, blockState.setValue(BLOCK_PURITY, defaultPurity + 1));
                    }
                    return defaultPurity;
                }
                return val - 1;
            }
            return ThirstConfig.DEFAULT_PURITY;
        }

        // Open water: derive from Y-coordinate bands and flow
        int purity = 0;
        int y = pos.getY();
        // Mountain water (above MOUNTAINS_Y) or deep cave water (below both CAVES_Y and MOUNTAINS_Y-32) gets base purity 1
        if (y > ThirstConfig.MOUNTAINS_Y || (y < ThirstConfig.CAVES_Y && y < ThirstConfig.MOUNTAINS_Y - 32)) {
            purity = 1;
        }
        if (level.getFluidState(pos).is(FluidTags.WATER) && !level.getFluidState(pos).isSource()) {
            purity = Math.min(purity + ThirstConfig.RUNNING_WATER_PURIFICATION_AMOUNT, MAX_PURITY);
        }
        return purity;
    }

    public static int getDisplayPurity(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getBlock() instanceof AbstractCauldronBlock) {
            if (blockState.hasProperty(BLOCK_PURITY)) {
                int val = blockState.getValue(BLOCK_PURITY);
                return val == 0 ? ThirstConfig.DEFAULT_PURITY : val - 1;
            }
            return ThirstConfig.DEFAULT_PURITY;
        }
        return getOpenWaterPurity(level, pos);
    }

    public static boolean isWaterDisplayTarget(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        return blockState.is(Blocks.WATER_CAULDRON) || level.getFluidState(pos).is(FluidTags.WATER);
    }

    private static int getOpenWaterPurity(Level level, BlockPos pos) {
        int purity = 0;
        int y = pos.getY();
        // Mountain water (above MOUNTAINS_Y) or deep cave water (below both CAVES_Y and MOUNTAINS_Y-32) gets base purity 1
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
            boolean debuffed = false;
            if (chance < nauseaChance) {
                sp.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20 * 5, 0));
                sp.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 30, 0));
                sp.addEffect(new MobEffectInstance(ModEffects.THIRSTY.get(), 20 * 30, 0));
                debuffed = true;
            }
            // <= (not <) intentional: when chance == poisonChance both nausea and poison can apply simultaneously
            if (chance <= poisonChance) {
                sp.addEffect(new MobEffectInstance(MobEffects.POISON, 20 * 10, 0));
                shouldDrink = false;
                debuffed = true;
            }
            if (debuffed) {
                ModAdvancements.award(sp, ModAdvancements.DIRTY_WATER);
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

    public static Component purityComponent(int purity) {
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
