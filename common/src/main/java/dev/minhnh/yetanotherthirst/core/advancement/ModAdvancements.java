package dev.minhnh.yetanotherthirst.core.advancement;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {

    public static final ResourceLocation ROOT = Constants.asResource("root");
    public static final ResourceLocation HAND_DRINKING = Constants.asResource("hand_drinking");
    public static final ResourceLocation RAIN_DRINKING = Constants.asResource("rain_drinking");
    public static final ResourceLocation DIRTY_WATER = Constants.asResource("dirty_water");
    public static final ResourceLocation FILTER_FRAME = Constants.asResource("filter_frame");
    public static final ResourceLocation CLOGGED_FILTER = Constants.asResource("clogged_filter");
    public static final ResourceLocation WASH_FILTER = Constants.asResource("wash_filter");
    public static final ResourceLocation WATER_BOILER = Constants.asResource("water_boiler");
    public static final ResourceLocation PURIFIED_WATER = Constants.asResource("purified_water");
    public static final ResourceLocation DEHYDRATION_SURVIVAL = Constants.asResource("dehydration_survival");
    public static final ResourceLocation VAMPIRE_IMMUNITY = Constants.asResource("vampire_immunity");

    private ModAdvancements() {}

    public static void award(ServerPlayer player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, "trigger");
        }
    }
}
