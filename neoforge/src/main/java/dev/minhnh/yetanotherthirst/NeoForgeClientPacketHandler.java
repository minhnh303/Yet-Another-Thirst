package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.minecraft.client.Minecraft;

public final class NeoForgeClientPacketHandler {

    private NeoForgeClientPacketHandler() {
    }

    public static void handleThirstSync(int thirst, int quenched, float exhaustion, boolean enabled) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ThirstStorage.applySync(minecraft.player, thirst, quenched, exhaustion, enabled);
        }
    }
}
