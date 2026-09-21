package com.mrchuw.universalvault.storage;

import com.mrchuw.universalvault.UniversalVault;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultManager {

    @Nullable
    public static VaultStorage getGlobalVault(Level level) {
        return getVault(level, UniversalVault.GLOBAL_VAULT_UUID);
    }

    @Nullable
    public static VaultStorage getPlayerVault(Player player) {
        return getVault(player.level(), player.getUUID());
    }

    @Nullable
    public static VaultStorage getVault(Level level, UUID targetUUID) {
        if (level instanceof ServerLevel serverLevel) {
            return VaultSavedData.get(serverLevel).getOrCreateVault(targetUUID);
        }
        return null;
    }
}