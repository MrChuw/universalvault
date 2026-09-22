package com.mrchuw.universalvault.gui;

import com.mrchuw.universalvault.Platform;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class VaultMenuHelper {
    public static void openVault(ServerPlayer player, UUID targetVaultUUID, Component title) {
        Platform.INSTANCE.openVaultMenu(player, targetVaultUUID, title);
    }
}