package com.mrchuw.universalvault.event;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.storage.VaultManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = UniversalVault.MOD_ID)
public class VaultPlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!UniversalVaultConfig.CONFIG.autoCreatePersonalVault.get()) return;
        if (event.getEntity() instanceof ServerPlayer sp) {
            VaultManager.getPlayerVault(sp);
        }
    }
}