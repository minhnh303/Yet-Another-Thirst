package dev.minhnh.yetanotherthirst.core.block;

import net.minecraft.util.StringRepresentable;

public enum FilterCoreType implements StringRepresentable {
    EMPTY("empty"),
    FABRIC("fabric"),
    SAND("sand"),
    CARBON("carbon"),
    CLOGGED_FABRIC("clogged_fabric"),
    CLOGGED_SAND("clogged_sand"),
    CLOGGED_CARBON("clogged_carbon");

    private final String name;

    FilterCoreType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
