package dev.minhnh.yetanotherthirst.client.config;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.category;
import static dev.minhnh.yetanotherthirst.client.config.ConfigCategory.hudPreviewSection;
import static dev.minhnh.yetanotherthirst.client.config.ConfigOption.integer;
import static dev.minhnh.yetanotherthirst.platform.services.IConfigBackend.ConfigFile.CLIENT;

/** Client-side HUD position options. */
public final class ClientOptions {
    public static final ConfigOption<Integer> HUD_X_OFFSET =
            integer(CLIENT, "hudXOffset", -512, 512).stepper();
    public static final ConfigOption<Integer> HUD_Y_OFFSET =
            integer(CLIENT, "hudYOffset", -512, 512).stepper();

    private ClientOptions() {
    }

    static List<ConfigCategory> categories() {
        return List.of(
                category("hud", () -> new ItemStack(Items.SPYGLASS),
                        hudPreviewSection("hud_position", HUD_X_OFFSET, HUD_Y_OFFSET))
        );
    }
}

