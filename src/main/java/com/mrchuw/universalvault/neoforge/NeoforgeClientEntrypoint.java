package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.client.VaultKeyBindings;
import com.mrchuw.universalvault.gui.screen.VaultFilterScreen;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.registry.ModRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = UniversalVault.MOD_ID, value = Dist.CLIENT)
public class NeoforgeClientEntrypoint {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModRegistry.VAULT_MENU.get(), VaultScreen::new);
        event.register(ModRegistry.VAULT_FILTER_MENU.get(), VaultFilterScreen::new);
    }

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(VaultKeyBindings.OPEN_GLOBAL_VAULT);
        event.register(VaultKeyBindings.OPEN_PERSONAL_VAULT);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        VaultKeyBindings.handleClientTick();
    }
}
//?}