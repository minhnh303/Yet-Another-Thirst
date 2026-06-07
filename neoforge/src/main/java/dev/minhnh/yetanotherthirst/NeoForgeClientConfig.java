package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class NeoForgeClientConfig {

    static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue HUD_X_OFFSET;
    private static final ModConfigSpec.IntValue HUD_Y_OFFSET;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

    private NeoForgeClientConfig() {
    }
}
