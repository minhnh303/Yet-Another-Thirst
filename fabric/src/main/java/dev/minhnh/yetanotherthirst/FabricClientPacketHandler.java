package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.core.thirst.ThirstStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class FabricClientPacketHandler {

    private FabricClientPacketHandler() {}

    public static void handleThirstSync(int thirst, int quenched, float exhaustion, boolean enabled) {

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            ThirstStorage.applySync(minecraft.player, thirst, quenched, exhaustion, enabled);
        }
    }
}
