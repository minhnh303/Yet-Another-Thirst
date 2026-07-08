package dev.minhnh.yetanotherthirst.core.advancement;

import dev.minhnh.yetanotherthirst.Constants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
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

    public static void checkDatapack(MinecraftServer server) {
        var mgr = server.getAdvancements();
        long modCount = mgr.getAllAdvancements().stream()
                .filter(h -> h.id().getNamespace().equals(Constants.MOD_ID))
                .count();
        boolean clayBowlRecipeExists = server.getRecipeManager()
                .byKey(Constants.asResource("clay_bowl")).isPresent();
        String sampleKeys = mgr.getAllAdvancements().stream()
                .map(h -> h.id().toString())
                .limit(5)
                .collect(java.util.stream.Collectors.joining(", "));
        if (modCount == 0) {
            String platform = dev.minhnh.yetanotherthirst.platform.Services.PLATFORM.getPlatformName();
            String fallbackId = platform.equalsIgnoreCase("Fabric") ? "fabric:" + Constants.MOD_ID : "mod:" + Constants.MOD_ID;
            String datapackId = server.getPackRepository().getAvailableIds().stream()
                    .filter(id -> id.contains(Constants.MOD_ID))
                    .findFirst()
                    .orElse(fallbackId);

            Constants.LOG.warn("================================================================");
            Constants.LOG.warn("[{}] Mod datapack is NOT loaded on this server!", Constants.MOD_ID);
            Constants.LOG.warn("  advancements={} clayBowlRecipe={}", modCount, clayBowlRecipeExists);
            Constants.LOG.warn("  sample advancements: [{}]", sampleKeys);
            Constants.LOG.warn("Advancements will not work. Fix options:");
            Constants.LOG.warn("  1. Run: /datapack enable \"{}\"", datapackId);
            Constants.LOG.warn("  2. Create a new world (mod datapacks auto-enabled)");
            Constants.LOG.warn("================================================================");
        } else {
            Constants.LOG.info("[{}] Mod datapack loaded: {} advancements found.", Constants.MOD_ID, modCount);
        }
    }

    public static void award(ServerPlayer player, ResourceLocation id) {
        boolean debug = dev.minhnh.yetanotherthirst.core.thirst.ThirstConfig.DEBUG_LOGGING;
        if (debug) Constants.LOG.info("[Advancements] Attempting to award '{}' to '{}'", id, player.getName().getString());
        AdvancementHolder holder = player.server.getAdvancements().get(id);
        if (holder == null) {
            Constants.LOG.warn("[Advancements] Cannot award '{}' to '{}': advancement not found on server. Is mod datapack enabled?", id, player.getName().getString());
            return;
        }
        var progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) {
            if (debug) Constants.LOG.info("[Advancements] '{}' already completed for '{}', skipping", id, player.getName().getString());
            return;
        }
        boolean granted = player.getAdvancements().award(holder, "requirement");
        if (debug) {
            if (granted) {
                Constants.LOG.info("[Advancements] SUCCESS: Granted '{}' to '{}'", id, player.getName().getString());
            } else {
                Constants.LOG.warn("[Advancements] award('{}') returned false for '{}' — criterion 'requirement' not found in advancement JSON?", id, player.getName().getString());
            }
        }
    }
}
