package dev.minhnh.yetanotherthirst.core.block;

public interface IWaterBoiler {
    int getInputAmount();
    int getInputPurity();
    int getOutputAmount();
    int getOutputPurity();
    int getTankCapacity();
    boolean isUpper();
    IWaterBoiler getLowerEntity();
}
