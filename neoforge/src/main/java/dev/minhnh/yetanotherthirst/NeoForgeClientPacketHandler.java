package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstData;
import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.client.Minecraft;

public final class NeoForgeClientPacketHandler {

    private NeoForgeClientPacketHandler() {
    }

    public static void handleThirstSync(ThirstData data) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ThirstStorage.applySync(minecraft.player, data);
        }
    }

    public static void handleThirstSync(int thirst, int quenched, float exhaustion, boolean enabled) {
        handleThirstSync(new ThirstData(thirst, quenched, exhaustion, enabled));
    }
}
