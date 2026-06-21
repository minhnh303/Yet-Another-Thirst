package dev.minhnh.yetanotherthirst;

import dev.minhnh.yetanotherthirst.client.ClientThirstTooltipComponent;
import dev.minhnh.yetanotherthirst.client.ThirstTooltipComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public final class FabricClientEvents {

    private FabricClientEvents() {}

    public static void register() {

        // Tooltip component factory
        net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof ThirstTooltipComponent c) {
                return new ClientThirstTooltipComponent(c);
            }
            return null;
        });

        // Register thirst sync packet handler (Server→Client)
        ClientPlayNetworking.registerGlobalReceiver(FabricNetwork.THIRST_SYNC, (client, handler, buf, responseSender) -> {
            int thirst = buf.readInt();
            int quenched = buf.readInt();
            float exhaustion = buf.readFloat();
            boolean enabled = buf.readBoolean();
            client.execute(() -> FabricClientPacketHandler.handleThirstSync(thirst, quenched, exhaustion, enabled));
        });
    }
}
