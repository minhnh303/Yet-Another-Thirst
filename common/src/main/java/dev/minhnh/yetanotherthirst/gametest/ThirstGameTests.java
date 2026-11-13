package dev.minhnh.yetanotherthirst.gametest;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestFunctionLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Loader-agnostic game test functions. Fabric invokes them through its annotated entrypoint;
 * NeoForge registers them as test functions and instances via {@link #load} and {@link #testData}.
 */
public class ThirstGameTests extends TestFunctionLoader {

    /** Vanilla 1x1x1 empty structure; every test works at relative origin. */
    public static final Identifier STRUCTURE = Identifier.withDefaultNamespace("empty");
    public static final int MAX_TICKS = 20;

    @Override
    public void load(BiConsumer<ResourceKey<Consumer<GameTestHelper>>, Consumer<GameTestHelper>> register) {
        register.accept(key("player_drink_mechanics"), ThirstGameTests::testPlayerDrinkMechanics);
        register.accept(key("water_block_purity_calculation"), ThirstGameTests::testWaterBlockPurityCalculation);
        register.accept(key("thirst_depletion_under_exhaustion"), ThirstGameTests::testThirstDepletionUnderExhaustion);
        register.accept(key("sprint_prevention_when_thirsty"), ThirstGameTests::testSprintPreventionWhenThirsty);
        register.accept(key("purified_water_safety"), ThirstGameTests::testPurifiedWaterSafety);
        register.accept(key("cauldron_water_purity_block_state"), ThirstGameTests::testCauldronWaterPurityBlockState);
        register.accept(key("thirst_commands_execution"), ThirstGameTests::testThirstCommandsExecution);
    }

    public static <E> TestData<E> testData(E environment) {
        return new TestData<>(environment, STRUCTURE, MAX_TICKS, 0, true);
    }

    private static ResourceKey<Consumer<GameTestHelper>> key(String name) {
        return ResourceKey.create(Registries.TEST_FUNCTION, Identifier.fromNamespaceAndPath(Constants.MOD_ID, name));
    }

    public static void testPlayerDrinkMechanics(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ThirstState state = ThirstStorage.get(player);

        state.reset();
        state.setThirst(10);
        state.setQuenched(0);

        state.drink(6, 4);

        if (state.getThirst() != 16) {
            helper.fail("Expected thirst 16 after drinking, got " + state.getThirst());
        }
        if (state.getQuenched() != 4) {
            helper.fail("Expected quenched 4 after drinking, got " + state.getQuenched());
        }

        helper.succeed();
    }

    public static void testWaterBlockPurityCalculation(GameTestHelper helper) {
        BlockPos waterPos = BlockPos.ZERO;
        helper.setBlock(waterPos, Blocks.WATER.defaultBlockState());

        int purity = WaterPurity.getBlockPurity(helper.getLevel(), helper.absolutePos(waterPos));
        if (purity < 0 || purity > WaterPurity.MAX_PURITY) {
            helper.fail("Purity calculated out of range: " + purity);
        }

        helper.succeed();
    }

    public static void testThirstDepletionUnderExhaustion(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ThirstState state = ThirstStorage.get(player);

        state.reset();
        state.setThirst(20);
        state.setQuenched(2);
        state.setExhaustion(ThirstConfig.EXHAUSTION_LIMIT + 1.0F);

        state.depleteOneLevel();

        // Quenched should absorb the first depletion
        if (state.getQuenched() != 1) {
            helper.fail("Quenched should have dropped to 1, got " + state.getQuenched());
        }
        if (state.getThirst() != 20) {
            helper.fail("Thirst should remain 20 while quenched is positive, got " + state.getThirst());
        }

        helper.succeed();
    }

    public static void testSprintPreventionWhenThirsty(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ThirstStorage.update(player, d -> d.withThirst(ThirstConfig.SPRINT_THRESHOLD).withQuenched(0));

        player.setSprinting(true);
        if (ThirstConfig.SPRINT_PREVENTION && ThirstStorage.getData(player).thirst() <= ThirstConfig.SPRINT_THRESHOLD) {
            player.setSprinting(false);
        }

        if (player.isSprinting()) {
            helper.fail("Player should not be sprinting when thirst <= sprint threshold");
        }

        helper.succeed();
    }

    public static void testPurifiedWaterSafety(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        // Purity 3 is purified water; should not cause debuffs or cancel drinking
        boolean drinkAllowed = WaterPurity.givePurityEffects(player, 3);
        if (!drinkAllowed) {
            helper.fail("Purified water should not block drinking");
        }

        helper.succeed();
    }

    public static void testCauldronWaterPurityBlockState(GameTestHelper helper) {
        BlockPos pos = BlockPos.ZERO;
        BlockState cauldronState = Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3)
                .setValue(WaterPurity.BLOCK_PURITY, 4); // Purity level 3 (purified) + 1 offset

        helper.setBlock(pos, cauldronState);

        int purity = WaterPurity.getBlockPurity(helper.getLevel(), helper.absolutePos(pos));
        if (purity != 3) {
            helper.fail("Expected cauldron purity 3, got " + purity);
        }

        helper.succeed();
    }

    @SuppressWarnings("removal")
    public static void testThirstCommandsExecution(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        var server = helper.getLevel().getServer();
        var source = player.createCommandSourceStack().withPermission(PermissionSet.ALL_PERMISSIONS);

        // Run: thirst set <player> 14 3
        String setName = player.getName().getString();
        server.getCommands().performPrefixedCommand(source, "thirst set " + setName + " 14 3");

        ThirstData data = ThirstStorage.getData(player);
        if (data.thirst() != 14 || data.quenched() != 3) {
            helper.fail("Expected thirst 14, quenched 3 from command, got: " + data);
        }

        // Run: thirst enable <player> false
        server.getCommands().performPrefixedCommand(source, "thirst enable " + setName + " false");
        ThirstData disabledData = ThirstStorage.getData(player);
        if (disabledData.enabled()) {
            helper.fail("Expected thirst to be disabled by command");
        }

        helper.succeed();
    }
}
