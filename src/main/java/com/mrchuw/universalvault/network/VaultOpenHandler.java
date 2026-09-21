package com.mrchuw.universalvault.network;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.gui.VaultMenuHelper;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions;
//?}

public final class VaultOpenHandler {

    private VaultOpenHandler() {}

    public static void handle(ServerPlayer player, C2SVaultOpenPayload payload) {
        UUID target = payload.targetVaultUUID();

        if (target.equals(UniversalVault.GLOBAL_VAULT_UUID)) {
            VaultMenuHelper.openVault(player, target,
                    Component.translatable("gui.universal_vault.global_title"));
            return;
        }

        //? if >=1.21.11 {
        boolean isAdmin = player.permissions().hasPermission(Permissions.COMMANDS_OWNER);
        //?} else {
        /*boolean isAdmin = player.hasPermissions(4);
         *///?}

        if (target.equals(player.getUUID()) || isAdmin) {
            VaultMenuHelper.openVault(player, target,
                    Component.translatable("gui.universal_vault.personal_title"));
        }
    }
}