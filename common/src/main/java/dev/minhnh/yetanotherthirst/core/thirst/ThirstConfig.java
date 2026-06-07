package dev.minhnh.yetanotherthirst.core.thirst;

public final class ThirstConfig {

    public static final int MAX_THIRST = 20;
    public static final int DEFAULT_THIRST = 20;
    public static final int DEFAULT_QUENCHED = 5;
    public static final float EXHAUSTION_LIMIT = 4.0F;
    public static final int SYNC_INTERVAL_TICKS = 10;
    public static int DAMAGE_INTERVAL_TICKS = 40;
    public static float DEHYDRATION_DAMAGE = 1.0F;
    public static float DEHYDRATION_DAMAGE_EASY_LIMIT = 10.0F;
    public static float DEHYDRATION_DAMAGE_NORMAL_LIMIT = 0.0F;
    public static float DEHYDRATION_DAMAGE_HARD_LIMIT = 0.0F;

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
    public static boolean BIOME_DEHYDRATION_MODIFIER = true;
    public static boolean FIRE_PROTECTION_DEHYDRATION_MODIFIER = true;
    public static float ENVIRONMENT_MODIFIER_HARSHNESS = 0.5F;

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
    public static int RUNNING_WATER_PURIFICATION_AMOUNT = 1;

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
    public static boolean COMPAT_LETS_DO_FARM_AND_CHARM = false;

    // Configured effects that suspend thirst
    public static java.util.List<String> SUSPENDED_THIRST_EFFECTS_RAW = new java.util.ArrayList<>();
    public static java.util.List<EffectCondition> SUSPENDED_THIRST_EFFECTS = new java.util.ArrayList<>();

    // Configured effects that pause thirst depletion
    public static java.util.List<String> PAUSE_DEPLETION_EFFECTS_RAW = new java.util.ArrayList<>();
    public static java.util.List<EffectCondition> PAUSE_DEPLETION_EFFECTS = new java.util.ArrayList<>();

    public static void setPauseDepletionEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        PAUSE_DEPLETION_EFFECTS_RAW = list;
        PAUSE_DEPLETION_EFFECTS = conditions;
    }

    // Configured effects that regenerate thirst
    public static java.util.List<String> REGEN_THIRST_EFFECTS_RAW = new java.util.ArrayList<>();
    public static java.util.List<EffectCondition> REGEN_THIRST_EFFECTS = new java.util.ArrayList<>();
    public static int REGEN_THIRST_INTERVAL = 40;
    public static int REGEN_THIRST_AMOUNT = 1;

    public static void setRegenThirstEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        REGEN_THIRST_EFFECTS_RAW = list;
        REGEN_THIRST_EFFECTS = conditions;
    }

    public static void setSuspendThirstEffects(java.util.List<? extends String> rawList) {
        java.util.List<String> list = new java.util.ArrayList<>();
        java.util.List<EffectCondition> conditions = new java.util.ArrayList<>();
        for (String entry : rawList) {
            list.add(entry);
            EffectCondition cond = EffectCondition.parse(entry);
            if (cond != null) {
                conditions.add(cond);
            }
        }
        SUSPENDED_THIRST_EFFECTS_RAW = list;
        SUSPENDED_THIRST_EFFECTS = conditions;
    }

    public static class EffectCondition {
        public final net.minecraft.resources.ResourceLocation effectId;
        public final String operator;
        public final int amplifier;

        public EffectCondition(net.minecraft.resources.ResourceLocation effectId, String operator, int amplifier) {
            this.effectId = effectId;
            this.operator = operator;
            this.amplifier = amplifier;
        }

        public static EffectCondition parse(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }
            input = input.trim();
            String operator = null;
            int opIndex = -1;
            String[] operators = {">=", "<=", "==", ">", "<", "="};
            for (String op : operators) {
                int idx = input.indexOf(op);
                if (idx != -1) {
                    operator = op;
                    opIndex = idx;
                    break;
                }
            }

            net.minecraft.resources.ResourceLocation effectId;
            int amplifier = 0;
            if (operator != null) {
                String idPart = input.substring(0, opIndex).trim();
                String valPart = input.substring(opIndex + operator.length()).trim();
                try {
                    effectId = new net.minecraft.resources.ResourceLocation(idPart);
                } catch (Exception e) {
                    effectId = null;
                }
                try {
                    amplifier = Integer.parseInt(valPart);
                } catch (NumberFormatException e) {
                    amplifier = 0;
                }
            } else {
                try {
                    effectId = new net.minecraft.resources.ResourceLocation(input);
                } catch (Exception e) {
                    effectId = null;
                }
                operator = ">=";
                amplifier = 0;
            }

            if (effectId == null) {
                return null;
            }
            return new EffectCondition(effectId, operator, amplifier);
        }

        public boolean matches(int activeAmplifier) {
            if (amplifier < 0) {
                return true;
            }
            switch (operator) {
                case ">=":
                    return activeAmplifier >= amplifier;
                case ">":
                    return activeAmplifier > amplifier;
                case "<=":
                    return activeAmplifier <= amplifier;
                case "<":
                    return activeAmplifier < amplifier;
                case "==":
                case "=":
                    return activeAmplifier == amplifier;
                default:
                    return activeAmplifier >= amplifier;
            }
        }
    }

    private ThirstConfig() {
    }
}
