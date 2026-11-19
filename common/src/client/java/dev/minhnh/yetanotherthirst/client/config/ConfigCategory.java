package dev.minhnh.yetanotherthirst.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** One sidebar page of the config screen: an icon, a title, and its sections of options. */
public record ConfigCategory(String id, Supplier<ItemStack> icon, List<Section> sections, List<ConfigOption<?>> options) {

    public ConfigCategory(String id, Supplier<ItemStack> icon, List<Section> sections) {
        this(id, icon, List.copyOf(sections), collectOptions(sections));
    }

    private static List<ConfigOption<?>> collectOptions(List<Section> sections) {
        List<ConfigOption<?>> all = new ArrayList<>();
        for (Section section : sections) {
            all.addAll(section.options());
        }
        return List.copyOf(all);
    }

    public record Section(String id, String requiredMod, boolean hudPreview, List<ConfigOption<?>> options) {
        public Section {
            options = List.copyOf(options);
        }

        public Component title() {
            return Component.translatable(ConfigOption.LANG + "section." + id);
        }
    }

    static ConfigCategory category(String id, Supplier<ItemStack> icon, Section... sections) {
        return new ConfigCategory(id, icon, List.of(sections));
    }

    static Section section(String id, ConfigOption<?>... options) {
        return new Section(id, null, false, List.of(options));
    }

    /** Section followed by a live HUD preview row driven by its pending values. */
    static Section hudPreviewSection(String id, ConfigOption<?>... options) {
        return new Section(id, null, true, List.of(options));
    }

    static Section modSection(String id, String modId, ConfigOption<?>... options) {
        for (ConfigOption<?> option : options) {
            option.requiredMod(modId);
        }
        return new Section(id, modId, false, List.of(options));
    }

    public Component title() {
        return Component.translatable(ConfigOption.LANG + "category." + id);
    }

    public Component description() {
        return Component.translatable(ConfigOption.LANG + "category." + id + ".desc");
    }
}

