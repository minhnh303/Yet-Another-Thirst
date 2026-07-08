package dev.minhnh.yetanotherthirst;

import net.fabricmc.api.ClientModInitializer;

public class YetAnotherThirstFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        FabricClientEvents.register();
        FabricClientGameEvents.register();
    }
}
