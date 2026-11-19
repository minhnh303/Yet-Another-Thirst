package dev.minhnh.yetanotherthirst.client.config;

import net.minecraft.client.gui.components.AbstractWidget;

/** Factory that constructs an editing control widget for a typed {@link ConfigOption}. */
@FunctionalInterface
public interface ConfigControlFactory<T> {
    AbstractWidget create(
            ConfigOption<T> option,
            ConfigSession session,
            Runnable onChanged
    );
}

