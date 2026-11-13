package dev.minhnh.yetanotherthirst.core.thirst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;

/**
 * Immutable player thirst state snapshot.
 */
public record ThirstData(int thirst, int quenched, float exhaustion, boolean enabled) {

    public static final Codec<ThirstData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("thirst", ThirstConfig.DEFAULT_THIRST).forGetter(ThirstData::thirst),
                    Codec.INT.optionalFieldOf("quenched", ThirstConfig.DEFAULT_QUENCHED).forGetter(ThirstData::quenched),
                    Codec.FLOAT.optionalFieldOf("exhaustion", 0.0F).forGetter(ThirstData::exhaustion),
                    Codec.BOOL.optionalFieldOf("enabled", true).forGetter(ThirstData::enabled)
            ).apply(instance, ThirstData::new)
    );

    public static final ThirstData DEFAULT = new ThirstData(
            ThirstConfig.DEFAULT_THIRST,
            ThirstConfig.DEFAULT_QUENCHED,
            0.0F,
            true
    );

    public ThirstData {
        thirst = Mth.clamp(thirst, 0, ThirstConfig.MAX_THIRST);
        quenched = Mth.clamp(quenched, 0, ThirstConfig.MAX_THIRST);
        exhaustion = Math.max(0.0F, exhaustion);
    }

    public ThirstData withThirst(int newThirst) {
        return new ThirstData(newThirst, this.quenched, this.exhaustion, this.enabled);
    }

    public ThirstData withQuenched(int newQuenched) {
        return new ThirstData(this.thirst, newQuenched, this.exhaustion, this.enabled);
    }

    public ThirstData withExhaustion(float newExhaustion) {
        return new ThirstData(this.thirst, this.quenched, newExhaustion, this.enabled);
    }

    public ThirstData withEnabled(boolean newEnabled) {
        return new ThirstData(this.thirst, this.quenched, this.exhaustion, newEnabled);
    }

    public ThirstData addExhaustion(float amount) {
        return withExhaustion(this.exhaustion + amount);
    }

    public ThirstData drink(int thirstAmount, int quenchedAmount) {
        int overflow = Math.max(this.thirst + thirstAmount - ThirstConfig.MAX_THIRST, 0);
        int actualQuenched = quenchedAmount;
        if (!ThirstConfig.EXTRA_HYDRATION_CONVERTS_TO_QUENCHED) {
            overflow = 0;
            if (this.thirst >= ThirstConfig.MAX_THIRST) {
                actualQuenched = 0;
            }
        }
        int nextThirst = this.thirst + thirstAmount;
        int nextQuenched = Math.min(this.quenched + actualQuenched + overflow, nextThirst);
        return new ThirstData(nextThirst, nextQuenched, this.exhaustion, this.enabled);
    }

    public ThirstData depleteOneLevel() {
        float nextExhaustion = Math.max(0.0F, this.exhaustion - ThirstConfig.EXHAUSTION_LIMIT);
        if (this.quenched > 0) {
            return new ThirstData(this.thirst, this.quenched - 1, nextExhaustion, this.enabled);
        } else {
            return new ThirstData(Math.max(this.thirst - 1, 0), 0, nextExhaustion, this.enabled);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("thirst", thirst);
        tag.putInt("quenched", quenched);
        tag.putFloat("exhaustion", exhaustion);
        tag.putBoolean("enable", enabled);
        return tag;
    }

    public static ThirstData load(CompoundTag tag) {
        int thirst = tag.contains("thirst") ? tag.getInt("thirst") : ThirstConfig.DEFAULT_THIRST;
        int quenched = tag.contains("quenched") ? tag.getInt("quenched") : ThirstConfig.DEFAULT_QUENCHED;
        float exhaustion = tag.contains("exhaustion") ? tag.getFloat("exhaustion") : 0.0F;
        boolean enabled = !tag.contains("enable") || tag.getBoolean("enable");
        return new ThirstData(thirst, quenched, exhaustion, enabled);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(thirst);
        buf.writeInt(quenched);
        buf.writeFloat(exhaustion);
        buf.writeBoolean(enabled);
    }

    public static ThirstData read(FriendlyByteBuf buf) {
        return new ThirstData(buf.readInt(), buf.readInt(), buf.readFloat(), buf.readBoolean());
    }
}

