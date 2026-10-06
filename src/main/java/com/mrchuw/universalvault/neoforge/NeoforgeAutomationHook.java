package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.automation.AutomationInit;
import com.mrchuw.universalvault.automation.runtime.AutomationTickHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class NeoforgeAutomationHook {

    private NeoforgeAutomationHook() {}

    public static void register(IEventBus modBus) {
        AutomationInit.registerStations();

        NeoForge.EVENT_BUS.addListener(NeoforgeAutomationHook::onServerStarting);
        NeoForge.EVENT_BUS.addListener(NeoforgeAutomationHook::onServerTick);
        NeoForge.EVENT_BUS.addListener(NeoforgeAutomationHook::onServerStopped);
    }

    private static void onServerStarting(ServerStartingEvent event) {}

    private static void onServerTick(ServerTickEvent.Post event) {
        AutomationTickHandler.serverTick(event.getServer());
    }

    private static void onServerStopped(ServerStoppedEvent event) {}
}
//?}