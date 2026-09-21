package com.mrchuw.universalvault.network;

import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import net.minecraft.world.entity.player.Player;

/**
 * Lógica server-side compartilhada entre Fabric e NeoForge.
 * Só delega: nada de API específica de loader aqui.
 */
public final class ServerHandlers {

    private ServerHandlers() {}

    public static void handleAction(Player player, C2SVaultActionPayload payload) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        VaultActionHandler.handle(sp, payload);
    }

    public static void handleOpen(Player player, C2SVaultOpenPayload payload) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer sp)) return;
        VaultOpenHandler.handle(sp, payload);
    }
}