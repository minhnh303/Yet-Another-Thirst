package dev.minhnh.yetanotherthirst.core.thirst;

public final class ThirstConfig {

    public static final int MAX_THIRST = 20;
    public static final int DEFAULT_THIRST = 20;
    public static final int DEFAULT_QUENCHED = 5;
    public static final float EXHAUSTION_LIMIT = 4.0F;
    public static final int SYNC_INTERVAL_TICKS = 10;
    public static final int DAMAGE_INTERVAL_TICKS = 40;

    public static boolean EXTRA_HYDRATION_CONVERTS_TO_QUENCHED = true;
    public static boolean THIRST_DEPLETES_IN_PEACEFUL = false;
    public static boolean DRINK_RAIN_WATER = true;
    public static boolean DEPLETES_WHEN_NAUSEA = true;
    public static boolean SPRINT_PREVENTION = true;
    public static int SPRINT_THRESHOLD = 6;
    public static boolean DEHYDRATION_HALTS_HEALTH_REGEN = true;

    public static float THIRST_DEPLETION_MODIFIER = 1.2F;
    public static float NETHER_THIRST_DEPLETION_MODIFIER = 3.0F;
    public static float FIRE_RESISTANCE_DEHYDRATION_MODIFIER = 0.0F;

    // Purity system
    public static int DEFAULT_PURITY = 2;
    public static boolean QUENCH_WHEN_DEBUFFED = true;
    public static float DIRTY_NAUSEA_CHANCE = 1.0F;
    public static float DIRTY_POISON_CHANCE = 0.3F;
    public static float SLIGHTLY_DIRTY_NAUSEA_CHANCE = 0.5F;
    public static float SLIGHTLY_DIRTY_POISON_CHANCE = 0.1F;
    public static float ACCEPTABLE_NAUSEA_CHANCE = 0.05F;
    public static float ACCEPTABLE_POISON_CHANCE = 0.0F;
    public static float PURIFIED_NAUSEA_CHANCE = 0.0F;
    public static float PURIFIED_POISON_CHANCE = 0.0F;

    // World purity thresholds
    public static int MOUNTAINS_Y = 100;
    public static int CAVES_Y = 48;

    // Hand drinking
    public static boolean CAN_DRINK_BY_HAND = false;
    public static int HAND_DRINKING_THIRST = 3;
    public static int HAND_DRINKING_QUENCHED = 2;
    public static boolean DRINK_BOTH_HANDS_NEEDED = false;

    // Items
    public static int WATER_BOTTLE_STACKSIZE = 64;

    // Client-side only — set by the loader's client config
    public static int HUD_X_OFFSET = 0;
    public static int HUD_Y_OFFSET = 0;

    // Mod compatibility flags (set by loader during common setup)
    public static boolean COMPAT_TOMBSTONE = false;
    public static boolean COMPAT_VAMPIRISM = false;
    public static boolean COMPAT_FARMERS_DELIGHT = false;
    public static boolean COMPAT_LETS_DO_BAKERY = false;
    public static boolean COMPAT_LETS_DO_BREWERY = false;

    private ThirstConfig() {
    }
}
