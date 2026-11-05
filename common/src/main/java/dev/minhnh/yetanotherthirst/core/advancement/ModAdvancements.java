package dev.minhnh.yetanotherthirst.core.advancement;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class ModAdvancements {

    public static final Identifier ROOT = Constants.asResource("root");
    public static final Identifier HAND_DRINKING = Constants.asResource("hand_drinking");
    public static final Identifier RAIN_DRINKING = Constants.asResource("rain_drinking");
    public static final Identifier DIRTY_WATER = Constants.asResource("dirty_water");
    public static final Identifier FILTER_FRAME = Constants.asResource("filter_frame");
    public static final Identifier CLOGGED_FILTER = Constants.asResource("clogged_filter");
    public static final Identifier WASH_FILTER = Constants.asResource("wash_filter");
    public static final Identifier WATER_BOILER = Constants.asResource("water_boiler");
    public static final Identifier PURIFIED_WATER = Constants.asResource("purified_water");
    public static final Identifier DEHYDRATION_SURVIVAL = Constants.asResource("dehydration_survival");
    public static final Identifier VAMPIRE_IMMUNITY = Constants.asResource("vampire_immunity");

    private ModAdvancements() {}

    public static void award(ServerPlayer player, Identifier id) {
        if (player.level().getServer() == null) return;
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(id);
        if (advancement != null) {
            player.getAdvancements().award(advancement, "trigger");
        }
    }
}
