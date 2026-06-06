package dev.minhnh.yetanotherthirst.core.thirst;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class ThirstState {

    private int thirst = ThirstConfig.DEFAULT_THIRST;
    private int quenched = ThirstConfig.DEFAULT_QUENCHED;
    private float exhaustion;
    private boolean enabled = true;
    private int damageTimer;
    private int syncTimer;
    private float previousFoodExhaustion;
    private boolean initialSync = true;

    public int getThirst() {

        return thirst;
    }

    public void setThirst(int thirst) {

        this.thirst = Mth.clamp(thirst, 0, ThirstConfig.MAX_THIRST);
    }

    public int getQuenched() {

        return quenched;
    }

    public void setQuenched(int quenched) {

        this.quenched = Mth.clamp(quenched, 0, ThirstConfig.MAX_THIRST);
    }

    public float getExhaustion() {

        return exhaustion;
    }

    public void setExhaustion(float exhaustion) {

        this.exhaustion = Math.max(0.0F, exhaustion);
    }

    public boolean isEnabled() {

        return enabled;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
        this.initialSync = true;
    }

    int nextDamageTimer() {

        return ++damageTimer;
    }

    void resetDamageTimer() {

        damageTimer = 0;
    }

    int nextSyncTimer() {

        return ++syncTimer;
    }

    void resetSyncTimer() {

        syncTimer = 0;
    }

    float getPreviousFoodExhaustion() {

        return previousFoodExhaustion;
    }

    void setPreviousFoodExhaustion(float previousFoodExhaustion) {

        this.previousFoodExhaustion = previousFoodExhaustion;
    }

    boolean consumeInitialSync() {

        if (!initialSync) {
            return false;
        }
        initialSync = false;
        return true;
    }

    public void drink(int thirst, int quenched) {

        int overflow = Math.max(this.thirst + thirst - ThirstConfig.MAX_THIRST, 0);
        if (!ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED) {
            overflow = 0;
        }

        setThirst(this.thirst + thirst);
        this.quenched = Math.min(this.quenched + quenched + overflow, this.thirst);
    }

    public void addExhaustion(float amount) {

        setExhaustion(exhaustion + amount);
    }

    public void depleteOneLevel() {

        exhaustion = Math.max(0.0F, exhaustion - ThirstConfig.EXHAUSTION_LIMIT);
        if (quenched > 0) {
            quenched--;
        } else {
            thirst = Math.max(thirst - 1, 0);
        }
    }

    public void reset() {

        thirst = ThirstConfig.DEFAULT_THIRST;
        quenched = ThirstConfig.DEFAULT_QUENCHED;
        exhaustion = 0.0F;
        enabled = true;
        damageTimer = 0;
        syncTimer = 0;
        previousFoodExhaustion = 0.0F;
        initialSync = true;
    }

    public void copyFrom(ThirstState other) {

        thirst = other.thirst;
        quenched = other.quenched;
        exhaustion = other.exhaustion;
        enabled = other.enabled;
        initialSync = true;
    }

    public CompoundTag save() {

        CompoundTag tag = new CompoundTag();
        tag.putInt("thirst", thirst);
        tag.putInt("quenched", quenched);
        tag.putFloat("exhaustion", exhaustion);
        tag.putBoolean("enable", enabled);
        return tag;
    }

    public void load(CompoundTag tag) {

        if (tag.contains("thirst")) {
            setThirst(tag.getInt("thirst"));
        }
        if (tag.contains("quenched")) {
            setQuenched(tag.getInt("quenched"));
        }
        if (tag.contains("exhaustion")) {
            setExhaustion(tag.getFloat("exhaustion"));
        }
        enabled = !tag.contains("enable") || tag.getBoolean("enable");
    }
}
