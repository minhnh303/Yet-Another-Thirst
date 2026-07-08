package dev.minhnh.yetanotherthirst.core.block;

/**
 * Common interface for WaterBoilerBlockEntity (both upper and lower halves).
 * Used by WaterBoilerRenderer to read tank data without loader-specific imports.
 */
public interface IWaterBoiler {

    boolean isUpper();

    IWaterBoiler getLowerEntity(net.minecraft.world.level.Level level);

    int getInputAmount();
    int getInputCapacity();
    int getInputPurity();

    int getOutputAmount();
    int getOutputCapacity();
    int getOutputPurity();

    net.minecraft.core.Direction getFacing();
}
