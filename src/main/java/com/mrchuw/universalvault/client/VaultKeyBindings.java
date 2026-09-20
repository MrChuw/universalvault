package com.mrchuw.universalvault.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public class VaultKeyBindings {

    public static final KeyMapping.Category VAULT_CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "keys")
    );

    public static final KeyMapping OPEN_GLOBAL_VAULT = new KeyMapping(
            "key.universal_vault.open_global",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            VAULT_CATEGORY
    );

    public static final KeyMapping OPEN_PERSONAL_VAULT = new KeyMapping(
            "key.universal_vault.open_personal",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            VAULT_CATEGORY
    );

    public static void handleClientTick() {
        Minecraft mc = Minecraft.getInstance();

        // 26.2 expôs Gui.screen(); 26.1 só tem o campo público Minecraft.screen.
        //? if >=26.2 {
        /*if (mc.player == null || mc.gui.screen() != null) return;
        *///?} else {
        if (mc.player == null || mc.screen != null) return;
         //?}

        sendOpenRequests(OPEN_GLOBAL_VAULT, UniversalVault.GLOBAL_VAULT_UUID);
        sendOpenRequests(OPEN_PERSONAL_VAULT, mc.player.getUUID());
    }

    private static void sendOpenRequests(KeyMapping keyMapping, UUID targetVault) {
        while (keyMapping.consumeClick()) {
            ClientPacketDistributor.sendToServer(new C2SVaultOpenPayload(targetVault));
        }
    }
}