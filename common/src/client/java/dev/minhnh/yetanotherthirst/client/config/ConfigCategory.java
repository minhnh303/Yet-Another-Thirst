package dev.minhnh.yetanotherthirst.client.config;

import dev.minhnh.yetanotherthirst.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * One sidebar page of the config screen: an icon, a title and its sections of options. The flattened option
 * list is built once, since the sidebar reads it every frame for its unsaved-change badges.
 */
public final class ConfigCategory {

    /**
     * A titled group of options. {@code requiredMod} marks a compatibility group whose options only matter
     * when that mod is installed; {@code hudPreview} draws the live thirst-bar preview below the options.
     */
    public record Section(String id, String requiredMod, boolean hudPreview, List<ConfigOption<?>> options) {
        public Section {
            options = List.copyOf(options);
        }

        public Component title() {
            return Component.translatable(Lang.CONFIG_PREFIX + "section." + id);
        }
    }

    private final String id;
    private final Supplier<ItemStack> icon;
    private final List<Section> sections;
    private final List<ConfigOption<?>> options;

    public ConfigCategory(String id, Supplier<ItemStack> icon, List<Section> sections) {
        this.id = id;
        this.icon = icon;
        this.sections = List.copyOf(sections);
        List<ConfigOption<?>> all = new ArrayList<>();
        for (Section section : this.sections) {
            all.addAll(section.options());
        }
        this.options = List.copyOf(all);
    }

    public String id() {
        return id;
    }

    public Supplier<ItemStack> icon() {
        return icon;
    }

    public List<Section> sections() {
        return sections;
    }

    /** Every option of every section, in display order. */
    public List<ConfigOption<?>> options() {
        return options;
    }

    public Component title() {
        return Component.translatable(Lang.CONFIG_PREFIX + "category." + id);
    }

    public Component description() {
        return Component.translatable(Lang.CONFIG_PREFIX + "category." + id + ".desc");
    }
}
