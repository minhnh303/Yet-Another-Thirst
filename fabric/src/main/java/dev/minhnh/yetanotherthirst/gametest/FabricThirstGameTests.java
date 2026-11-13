package dev.minhnh.yetanotherthirst.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Fabric game test entrypoint. Fabric only discovers public, non-static {@link GameTest} methods,
 * so each method delegates to the shared implementation in {@link ThirstGameTests}.
 */
public class FabricThirstGameTests {

    @GameTest
    public void playerDrinkMechanics(GameTestHelper helper) {
        ThirstGameTests.testPlayerDrinkMechanics(helper);
    }

    @GameTest
    public void waterBlockPurityCalculation(GameTestHelper helper) {
        ThirstGameTests.testWaterBlockPurityCalculation(helper);
    }

    @GameTest
    public void thirstDepletionUnderExhaustion(GameTestHelper helper) {
        ThirstGameTests.testThirstDepletionUnderExhaustion(helper);
    }

    @GameTest
    public void sprintPreventionWhenThirsty(GameTestHelper helper) {
        ThirstGameTests.testSprintPreventionWhenThirsty(helper);
    }

    @GameTest
    public void purifiedWaterSafety(GameTestHelper helper) {
        ThirstGameTests.testPurifiedWaterSafety(helper);
    }

    @GameTest
    public void cauldronWaterPurityBlockState(GameTestHelper helper) {
        ThirstGameTests.testCauldronWaterPurityBlockState(helper);
    }

    @GameTest
    public void thirstCommandsExecution(GameTestHelper helper) {
        ThirstGameTests.testThirstCommandsExecution(helper);
    }
}
