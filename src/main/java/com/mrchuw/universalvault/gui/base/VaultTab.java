package com.mrchuw.universalvault.gui.base;

public enum VaultTab {
    VAULT("V", "gui.universal_vault.tab.vault"),
    ENCODE("E", "gui.universal_vault.tab.encode"),
    LIBRARY("L", "gui.universal_vault.tab.library"),
    CRAFTS("R", "gui.universal_vault.tab.crafts");

    public final String label;
    public final String titleKey;

    VaultTab(String label, String titleKey) {
        this.label = label;
        this.titleKey = titleKey;
    }
}