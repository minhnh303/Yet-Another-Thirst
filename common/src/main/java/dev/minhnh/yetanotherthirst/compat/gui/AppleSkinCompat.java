package dev.minhnh.yetanotherthirst.compat.gui;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig;

public final class AppleSkinCompat {

    private AppleSkinCompat() {
    }

    public static boolean showsThirstTooltip() {
        return ThirstConfig.COMPAT_APPLESKIN && ThirstConfig.APPLESKIN_THIRST_TOOLTIP;
    }

    public static boolean showsHudPreview() {
        return ThirstConfig.COMPAT_APPLESKIN && ThirstConfig.APPLESKIN_THIRST_HUD_PREVIEW;
    }
}

