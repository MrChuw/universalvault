package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.registry.ModRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import com.mrchuw.universalvault.command.VaultCommand;
import com.mrchuw.universalvault.storage.VaultManager;

public class NeoforgeEvents {

    public static void register(IEventBus modBus) {
        modBus.addListener(NeoforgeEvents::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(NeoforgeEvents::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(NeoforgeEvents::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(NeoforgeEvents::onServerTick);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModRegistry.VAULT_IO_BLOCK_ENTITY.get(),
                (be, side) -> new NeoforgeItemHandler(be));
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        VaultCommand.register(event.getDispatcher());
    }

    private static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!VaultConfig.get().autoCreatePersonalVault()) return;
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer sp) {
            VaultManager.getPlayerVault(sp);
        }
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        if (VaultConfig.get().syncOnEveryChange()) return;
        VaultMenu.processPendingSyncs(event.getServer());
    }
}
//?}