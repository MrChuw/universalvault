package com.mrchuw.universalvault.fabric;

//? fabric {
import net.fabricmc.api.ClientModInitializer;

public class FabricClientEntrypoint implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricClientEvents.register();
        FabricNetwork.registerClient();
    }
}
//?}