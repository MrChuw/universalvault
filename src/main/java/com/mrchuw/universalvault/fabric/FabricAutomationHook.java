package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.automation.AutomationInit;
import com.mrchuw.universalvault.automation.runtime.AutomationTickHandler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;


public final class FabricAutomationHook {

    private FabricAutomationHook() {}

    public static void register() {
        AutomationInit.registerStations();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {});

        ServerTickEvents.END_SERVER_TICK.register(AutomationTickHandler::serverTick);

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {});
    }
}
*///?}
