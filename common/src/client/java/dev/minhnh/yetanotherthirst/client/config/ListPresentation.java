package dev.minhnh.yetanotherthirst.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Optional visual presentation for list elements in {@link ListEditorScreen}. */
public interface ListPresentation<T> {
    Component displayName(T value);

    ItemStack icon(T value);

    List<Component> tooltip(T value);
}

