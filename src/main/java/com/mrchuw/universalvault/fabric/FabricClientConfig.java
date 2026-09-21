package com.mrchuw.universalvault.fabric;

//? fabric {
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.config.VaultClientConfig;

public class FabricClientConfig implements VaultClientConfig {
    private VaultSortMode mode = VaultSortMode.COUNT_DESC;
    @Override public VaultSortMode sortMode() { return mode; }
    @Override public void setSortMode(VaultSortMode m) { this.mode = m; }
}
//?}