package com.mrchuw.universalvault;

import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.integration.VaultItemHandler;
import com.mrchuw.universalvault.registry.ModRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = UniversalVault.MOD_ID)
public class VaultNeoForgeEvents {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModRegistry.VAULT_IO_BLOCK_ENTITY.get(),
                (be, side) -> new VaultItemHandler(be)
        );
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (UniversalVaultConfig.CONFIG.syncOnEveryChange.get()) return;
        VaultMenu.processPendingSyncs(event.getServer());
    }
}