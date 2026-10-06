package com.mrchuw.universalvault.gui.base;

import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.gui.screen.LibraryScreen;
import com.mrchuw.universalvault.gui.screen.PatternEncodeScreen;
import com.mrchuw.universalvault.gui.screen.RunningCraftsScreen;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class VaultScreenRouter {

    private VaultScreenRouter() {}

    public static void open(VaultTab tab, VaultMenu menu, Component title) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        var inv = mc.player.getInventory();
        Screen next = switch (tab) {
            case VAULT -> new VaultScreen(menu, inv, title);
            case LIBRARY -> new LibraryScreen(menu, inv, title);
            case ENCODE -> new PatternEncodeScreen(menu, inv, title);
            case CRAFTS -> new RunningCraftsScreen(menu, inv, title);
        };
        mc.execute(() -> {
            //? if <26.2 {
            /*mc.setScreen(next);
            *///?} else {
            mc.gui.setScreen(next);
             //?}
        });
    }
}
