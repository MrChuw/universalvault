package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.storage.VaultManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public class FabricEvents {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
                com.mrchuw.universalvault.command.VaultCommand.register(dispatcher));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (!VaultConfig.get().autoCreatePersonalVault()) return;
            ServerPlayer sp = handler.player;
            VaultManager.getPlayerVault(sp);
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (VaultConfig.get().syncOnEveryChange()) return;
            VaultMenu.processPendingSyncs(server);
        });
    }
}
*///?}
