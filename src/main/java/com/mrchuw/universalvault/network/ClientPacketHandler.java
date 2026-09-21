package com.mrchuw.universalvault.network;

import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    public static void handleSync(S2CVaultSyncPayload payload) {
        //? if >=26.2 {
        if (Minecraft.getInstance().gui.screen() instanceof VaultScreen screen) {
        //?} else {
        /*if (Minecraft.getInstance().screen instanceof VaultScreen screen) {
         *///?}
            screen.onVaultSync(payload);
        }
    }
}