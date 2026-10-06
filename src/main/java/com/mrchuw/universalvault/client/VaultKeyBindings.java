package com.mrchuw.universalvault.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;


public class VaultKeyBindings {

    public static final KeyMapping.Category VAULT_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "keys")
    );

    public static final KeyMapping OPEN_PERSONAL_VAULT = new KeyMapping(
            "key.universal_vault.open_personal",
            //? if <=26.2 {
            /*InputConstants.Type.KEYSYM,
            *///?} else {
            InputConstants.Type.KEYBOARD,
             //?}
            InputConstants.KEY_B,
            VAULT_CATEGORY
    );

    public static void handleClientTick() {
        Minecraft mc = Minecraft.getInstance();
        //? if >=26.2 {
        if (mc.player == null || mc.gui.screen() != null) return;
        //?} else {
        /*if (mc.player == null || mc.screen != null) return;
         *///?}

        while (OPEN_PERSONAL_VAULT.consumeClick()) {
            Platform.INSTANCE.sendToServer(new C2SVaultOpenPayload(mc.player.getUUID()));
        }
    }
}