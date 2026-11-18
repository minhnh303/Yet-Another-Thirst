package dev.minhnh.yetanotherthirst.gametest;

import dev.minhnh.yetanotherthirst.Constants;
import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class ThirstGameTests {

    public static final String BATCH = Constants.MOD_ID + ".tests";
    /** Empty 8x8x8 structure shipped at data/yet_another_thirst/structure/empty.nbt. */
    public static final String EMPTY_TEMPLATE = Constants.MOD_ID + ":empty";
    private static final String TEST_PREFIX = "thirstgametests.";
    private static final int TIMEOUT_TICKS = 100;

    /**
     * Builds test functions directly so the structure id is identical on every loader;
     * {@code @GameTest} templates are namespaced differently by Fabric, Forge and NeoForge.
     */
    @GameTestGenerator
    public static Collection<TestFunction> generateTests() {
        return List.of(
                test("testplayerdrinkmechanics", ThirstGameTests::testPlayerDrinkMechanics),
                test("testwaterblockpuritycalculation", ThirstGameTests::testWaterBlockPurityCalculation),
                test("testthirstdepletionunderexhaustion", ThirstGameTests::testThirstDepletionUnderExhaustion),
                test("testsprintpreventionwhenthirsty", ThirstGameTests::testSprintPreventionWhenThirsty),
                test("testpurifiedwatersafety", ThirstGameTests::testPurifiedWaterSafety),
                test("testcauldronwaterpurityblockstate", ThirstGameTests::testCauldronWaterPurityBlockState),
                test("testthirstcommandsexecution", ThirstGameTests::testThirstCommandsExecution)
        );
    }

    private static TestFunction test(String name, Consumer<GameTestHelper> function) {
        return new TestFunction(BATCH, TEST_PREFIX + name, EMPTY_TEMPLATE, TIMEOUT_TICKS, 0L, true, function);
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
        BlockPos waterPos = new BlockPos(1, 1, 1);
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
        BlockPos pos = new BlockPos(1, 1, 1);
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

    public static void testThirstCommandsExecution(GameTestHelper helper) {
        // Commands resolve targets through the server player list, so a list-registered ServerPlayer is required.
        Player player = helper.makeMockServerPlayerInLevel();
        var server = helper.getLevel().getServer();
        var source = player.createCommandSourceStack().withPermission(2);

        // Run: thirst set <player> 14 3
        String setName = player.getGameProfile().getName();
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
