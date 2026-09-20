package com.mrchuw.universalvault.client;

public enum VaultSortMode {
    COUNT_DESC("gui.universal_vault.sort.count_desc"),
    COUNT_ASC ("gui.universal_vault.sort.count_asc"),
    NAME_ASC  ("gui.universal_vault.sort.name_asc"),
    NAME_DESC ("gui.universal_vault.sort.name_desc"),
    MOD       ("gui.universal_vault.sort.mod");

    public final String langKey;

    VaultSortMode(String langKey) {
        this.langKey = langKey;
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
