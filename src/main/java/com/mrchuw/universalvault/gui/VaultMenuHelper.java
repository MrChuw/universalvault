package com.mrchuw.universalvault.gui;

import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

public class VaultMenuHelper {

    public static void openVault(ServerPlayer player, UUID targetVaultUUID, Component title) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new VaultMenu(id, inv, targetVaultUUID), title), buf ->
            buf.writeUUID(targetVaultUUID)
        );
    }
}
