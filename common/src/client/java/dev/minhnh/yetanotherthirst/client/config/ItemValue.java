package dev.minhnh.yetanotherthirst.client.config;

import java.util.Objects;

/** Domain record for a drink or food hydration entry in the config GUI. */
public record ItemValue(String itemId, int thirst, int quenched) {
    public ItemValue {
        Objects.requireNonNull(itemId, "itemId");
    }
}

