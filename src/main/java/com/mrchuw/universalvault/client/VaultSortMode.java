package com.mrchuw.universalvault.client;

public enum VaultSortMode {
    COUNT_DESC("gui.universal_vault.sort.count_desc", "↓"),
    COUNT_ASC ("gui.universal_vault.sort.count_asc",  "↑"),
    NAME_ASC  ("gui.universal_vault.sort.name_asc",   "A-Z"),
    NAME_DESC ("gui.universal_vault.sort.name_desc",  "Z-A"),
    MOD       ("gui.universal_vault.sort.mod",        "M");

    public final String langKey;
    public final String shortLabel;

    VaultSortMode(String langKey, String shortLabel) {
        this.langKey = langKey;
        this.shortLabel = shortLabel;
    }

    public VaultSortMode next() {
        VaultSortMode[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public VaultSortMode previous() {
        VaultSortMode[] v = values();
        return v[(ordinal() - 1 + v.length) % v.length];
    }
}
