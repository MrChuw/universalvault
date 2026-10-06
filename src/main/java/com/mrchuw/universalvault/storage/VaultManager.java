package com.mrchuw.universalvault.storage;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

public class VaultManager {

    @Nullable
    public static VaultStorage getPlayerVault(Player player) {
        return getVault(player.level(), player.getUUID());
    }

    @Nullable
    public static VaultStorage getVault(Level level, UUID owner) {
        if (level instanceof ServerLevel serverLevel) {
            return VaultSavedData.get(serverLevel).getOrCreateVault(owner);
        }
        return null;
    }
}