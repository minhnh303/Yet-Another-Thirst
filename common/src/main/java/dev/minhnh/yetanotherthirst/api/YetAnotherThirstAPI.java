package dev.minhnh.yetanotherthirst.api;

import dev.minhnh.yetanotherthirst.core.purity.WaterPurity;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstState;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstValues;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Main API entrypoint for Yet Another Thirst.
 * Provides helper methods to query and manipulate player thirst and water purity.
 */
public final class YetAnotherThirstAPI {

    private YetAnotherThirstAPI() {
    }

    /**
     * Retrieves the thirst state of a player.
     *
     * @param player The player to query.
     * @return The ThirstState object containing thirst details.
     */
    public static ThirstState getThirstState(Player player) {
        return ThirstStorage.get(player);
    }

    /**
     * Gets the thirst level of a player.
     *
     * @param player The player to query.
     * @return The thirst level (0 - 20).
     */
    public static int getThirst(Player player) {
        return getThirstState(player).getThirst();
    }

    /**
     * Sets the thirst level of a player and synchronizes the change.
     *
     * @param player The player to modify.
     * @param thirst The thirst level (0 - 20).
     */
    public static void setThirst(Player player, int thirst) {
        getThirstState(player).setThirst(thirst);
        sync(player);
    }

    /**
     * Gets the quenched (saturation) level of a player.
     *
     * @param player The player to query.
     * @return The quenched level (0 - 20).
     */
    public static int getQuenched(Player player) {
        return getThirstState(player).getQuenched();
    }

    /**
     * Sets the quenched (saturation) level of a player and synchronizes the change.
     *
     * @param player   The player to modify.
     * @param quenched The quenched level (0 - 20).
     */
    public static void setQuenched(Player player, int quenched) {
        getThirstState(player).setQuenched(quenched);
        sync(player);
    }

    /**
     * Gets the thirst exhaustion level of a player.
     *
     * @param player The player to query.
     * @return The current exhaustion value.
     */
    public static float getExhaustion(Player player) {
        return getThirstState(player).getExhaustion();
    }

    /**
     * Sets the thirst exhaustion level of a player and synchronizes the change.
     *
     * @param player     The player to modify.
     * @param exhaustion The exhaustion value.
     */
    public static void setExhaustion(Player player, float exhaustion) {
        getThirstState(player).setExhaustion(exhaustion);
        sync(player);
    }

    /**
     * Adds thirst exhaustion to a player and synchronizes the change.
     *
     * @param player The player to modify.
     * @param amount The exhaustion amount to add.
     */
    public static void addExhaustion(Player player, float amount) {
        getThirstState(player).addExhaustion(amount);
        sync(player);
    }

    /**
     * Checks if thirst mechanics are enabled for a player.
     *
     * @param player The player to check.
     * @return True if thirst is enabled.
     */
    public static boolean isThirstEnabled(Player player) {
        return getThirstState(player).isEnabled();
    }

    /**
     * Sets whether thirst mechanics are enabled for a player and synchronizes the change.
     *
     * @param player  The player to modify.
     * @param enabled True to enable thirst.
     */
    public static void setThirstEnabled(Player player, boolean enabled) {
        getThirstState(player).setEnabled(enabled);
        sync(player);
    }

    /**
     * Adds thirst and quenched hydration levels to a player, simulating drinking, and synchronizes.
     *
     * @param player   The player to hydrate.
     * @param thirst   The thirst hydration to add.
     * @param quenched The quenched hydration to add.
     */
    public static void drink(Player player, int thirst, int quenched) {
        getThirstState(player).drink(thirst, quenched);
        sync(player);
    }

    /**
     * Triggers a manual synchronization of the player's thirst state to the client.
     *
     * @param player The player to sync.
     */
    public static void sync(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            ThirstStorage.sync(serverPlayer);
        }
    }

    /**
     * Registers a drink item programmatically.
     * Note: Does not overwrite user configs.
     *
     * @param item     The drink item to register.
     * @param thirst   The hydration level to restore.
     * @param quenched The saturation level to restore.
     */
    public static void registerDrink(Item item, int thirst, int quenched) {
        ThirstValues.registerDrink(item, thirst, quenched);
    }

    /**
     * Registers a food item programmatically.
     * Note: Does not overwrite user configs.
     *
     * @param item     The food item to register.
     * @param thirst   The hydration level to restore.
     * @param quenched The saturation level to restore.
     */
    public static void registerFood(Item item, int thirst, int quenched) {
        ThirstValues.registerFood(item, thirst, quenched);
    }

    /**
     * Checks if an item stack represents a water-filled container.
     *
     * @param stack The item stack to check.
     * @return True if the item is filled with water.
     */
    public static boolean isWaterFilledContainer(ItemStack stack) {
        return WaterPurity.isWaterFilledContainer(stack);
    }

    /**
     * Gets the water purity of an item stack.
     *
     * @param stack The item stack to check.
     * @return The purity value: 0 (Dirty), 1 (Slightly Dirty), 2 (Acceptable), 3 (Purified).
     */
    public static int getPurity(ItemStack stack) {
        return WaterPurity.getPurity(stack);
    }

    /**
     * Sets the water purity level of an item stack.
     *
     * @param stack  The item stack to update.
     * @param purity The purity value (0 - 3).
     */
    public static void setPurity(ItemStack stack, int purity) {
        WaterPurity.addPurity(stack, purity);
    }

    /**
     * Applies standard bad-status effects (poison, hunger, nausea) to a player based on a purity level.
     *
     * @param player The player to affect.
     * @param purity The purity level (0 - 3).
     * @return True if the drink should still restore hydration after effects are applied.
     */
    public static boolean givePurityEffects(Player player, int purity) {
        return WaterPurity.givePurityEffects(player, purity);
    }
}
