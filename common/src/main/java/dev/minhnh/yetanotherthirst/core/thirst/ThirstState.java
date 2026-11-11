package dev.minhnh.yetanotherthirst.core.thirst;

import net.minecraft.nbt.CompoundTag;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

public class ThirstState {

    private final AtomicReference<ThirstData> data = new AtomicReference<>(ThirstData.DEFAULT);
    private int damageTimer;
    private int syncTimer;
    private float previousFoodExhaustion;
    private boolean initialSync = true;
    private boolean exhaustionRecalculate;
    private boolean justHealed;

    public ThirstData getData() {
        return data.get();
    }

    public void setData(ThirstData next) {
        this.data.set(next != null ? next : ThirstData.DEFAULT);
    }

    public ThirstData updateData(UnaryOperator<ThirstData> updater) {
        return this.data.updateAndGet(updater);
    }

    public boolean isExhaustionRecalculate() {
        return exhaustionRecalculate;
    }

    public void setExhaustionRecalculate(boolean val) {
        this.exhaustionRecalculate = val;
    }

    public boolean isJustHealed() {
        return justHealed;
    }

    public void setJustHealed(boolean val) {
        this.justHealed = val;
    }

    public int getThirst() {
        return data.get().thirst();
    }

    public void setThirst(int thirst) {
        data.updateAndGet(d -> d.withThirst(thirst));
    }

    public int getQuenched() {
        return data.get().quenched();
    }

    public void setQuenched(int quenched) {
        data.updateAndGet(d -> d.withQuenched(quenched));
    }

    public float getExhaustion() {
        return data.get().exhaustion();
    }

    public void setExhaustion(float exhaustion) {
        data.updateAndGet(d -> d.withExhaustion(exhaustion));
    }

    public boolean isEnabled() {
        return data.get().enabled();
    }

    public void setEnabled(boolean enabled) {
        data.updateAndGet(d -> d.withEnabled(enabled));
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
        data.updateAndGet(d -> d.drink(thirst, quenched));
    }

    public void addExhaustion(float amount) {
        data.updateAndGet(d -> d.addExhaustion(amount));
    }

    public void depleteOneLevel() {
        data.updateAndGet(ThirstData::depleteOneLevel);
    }

    public void reset() {
        data.set(ThirstData.DEFAULT);
        damageTimer = 0;
        syncTimer = 0;
        previousFoodExhaustion = 0.0F;
        initialSync = true;
        exhaustionRecalculate = false;
        justHealed = false;
    }

    public void copyFrom(ThirstState other) {
        this.data.set(other.getData());
        initialSync = true;
        exhaustionRecalculate = other.exhaustionRecalculate;
        justHealed = other.justHealed;
    }

    public CompoundTag save() {
        return data.get().save();
    }

    public void load(CompoundTag tag) {
        this.data.set(ThirstData.load(tag));
    }
}
