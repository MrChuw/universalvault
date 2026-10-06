package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.gui.screen.LibraryScreen;
import com.mrchuw.universalvault.gui.screen.NodeConfigScreen;
import com.mrchuw.universalvault.gui.screen.RunningCraftsScreen;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import net.minecraft.client.Minecraft;

public class ClientPacketHandler {

    private ClientPacketHandler() {}

    public static void handlePatternsSync(S2CPatternsSyncPayload payload) {
        //? if >=26.2 {
        var screen = Minecraft.getInstance().gui.screen();
        //?} else {
        /*var screen = Minecraft.getInstance().screen;
         *///?}
        if (screen instanceof LibraryScreen ls) {
            ls.onPatternsSync(payload);
        }
        if (screen instanceof VaultScreen vs) {
            vs.onPatternsSync(payload);
        }
        if (screen instanceof NodeConfigScreen ns) {
            ns.onPatternsSync(payload);
        }
    }

    public static void handleJobsSync(S2CJobsSyncPayload payload) {
        //? if >=26.2 {
        var screen = Minecraft.getInstance().gui.screen();
        //?} else {
        /*var screen = Minecraft.getInstance().screen;
         *//*?}*/
        if (screen instanceof RunningCraftsScreen rcs) {
            rcs.onJobsSync(payload);
        }
    }

    public static void handleNodeConfigSync(S2CNodeConfigSyncPayload payload) {
        //? if >=26.2 {
        var current = Minecraft.getInstance().gui.screen();
        //?} else {
        /*var current = Minecraft.getInstance().screen;
         *//*?}*/
        if (current instanceof NodeConfigScreen node) {
            node.onNodeConfigSync(payload);
            return;
        }
        Minecraft.getInstance().execute(() -> {
            Minecraft.getInstance().setScreenAndShow(new NodeConfigScreen(
                    payload.nodePos(),
                    payload.pullSides(),
                    payload.patternOverrides(),
                    payload.bindSides()));
        });
    }
}