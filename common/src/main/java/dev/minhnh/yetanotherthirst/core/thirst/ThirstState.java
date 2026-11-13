package dev.minhnh.yetanotherthirst.core.thirst;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
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

    public void setData(ThirstData newData) {
        ThirstData next = Objects.requireNonNull(newData, "newData");
        ThirstData previous = data.getAndSet(next);
        if (previous.enabled() != next.enabled()) {
            this.initialSync = true;
        }
    }

    public ThirstData updateData(UnaryOperator<ThirstData> operator) {
        Objects.requireNonNull(operator, "operator");
        while (true) {
            ThirstData current = data.get();
            ThirstData next = Objects.requireNonNull(operator.apply(current), "updated ThirstData");
            if (data.compareAndSet(current, next)) {
                if (current.enabled() != next.enabled()) {
                    this.initialSync = true;
                }
                return next;
            }
        }
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
        return getData().thirst();
    }

    public void setThirst(int thirst) {
        updateData(d -> d.withThirst(thirst));
    }

    public int getQuenched() {
        return getData().quenched();
    }

    public void setQuenched(int quenched) {
        updateData(d -> d.withQuenched(quenched));
    }

    public float getExhaustion() {
        return getData().exhaustion();
    }

    public void setExhaustion(float exhaustion) {
        updateData(d -> d.withExhaustion(exhaustion));
    }

    public boolean isEnabled() {
        return getData().enabled();
    }

    public void setEnabled(boolean enabled) {
        updateData(d -> d.withEnabled(enabled));
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
        updateData(d -> d.drink(thirst, quenched));
    }

    public void addExhaustion(float amount) {
        updateData(d -> d.addExhaustion(amount));
    }

    public void depleteOneLevel() {
        updateData(ThirstData::depleteOneLevel);
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
