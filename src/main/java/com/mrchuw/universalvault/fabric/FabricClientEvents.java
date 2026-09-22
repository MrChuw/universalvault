package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.client.VaultKeyBindings;
import com.mrchuw.universalvault.gui.screen.VaultFilterScreen;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.registry.ModRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.1 {
/^import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
 ^///?} else {
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//?}
import net.minecraft.client.gui.screens.MenuScreens;

public class FabricClientEvents {

    public static void register() {
        MenuScreens.register(ModRegistry.VAULT_MENU.get(), VaultScreen::new);
        MenuScreens.register(ModRegistry.VAULT_FILTER_MENU.get(), VaultFilterScreen::new);

        //? if <26.1 {
        /^KeyBindingHelper.registerKeyBinding(VaultKeyBindings.OPEN_GLOBAL_VAULT);
        KeyBindingHelper.registerKeyBinding(VaultKeyBindings.OPEN_PERSONAL_VAULT);
        ^///?} else {
        KeyMappingHelper.registerKeyMapping(VaultKeyBindings.OPEN_GLOBAL_VAULT);
        KeyMappingHelper.registerKeyMapping(VaultKeyBindings.OPEN_PERSONAL_VAULT);
        //?}

        ClientTickEvents.END_CLIENT_TICK.register(client -> VaultKeyBindings.handleClientTick());
    }
}
*///?}