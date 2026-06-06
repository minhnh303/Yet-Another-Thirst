package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public final class ForgeClientConfig {

    static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.IntValue HUD_X_OFFSET;
    private static final ForgeConfigSpec.IntValue HUD_Y_OFFSET;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("hud");
        HUD_X_OFFSET = builder
                .comment("Horizontal pixel offset for the thirst bar (positive = right)")
                .defineInRange("hudXOffset", 0, -512, 512);
        HUD_Y_OFFSET = builder
                .comment("Vertical pixel offset for the thirst bar (positive = down)")
                .defineInRange("hudYOffset", 0, -512, 512);
        builder.pop();

        SPEC = builder.build();
    }

    static void sync() {

        ThirstConfig.HUD_X_OFFSET = HUD_X_OFFSET.get();
        ThirstConfig.HUD_Y_OFFSET = HUD_Y_OFFSET.get();
    }

    private ForgeClientConfig() {
    }
}
