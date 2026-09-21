package com.mrchuw.universalvault.config;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.client.VaultSortMode;

public interface VaultClientConfig {
    static VaultClientConfig get() { return Platform.INSTANCE.clientConfig(); }
    VaultSortMode sortMode();
    void setSortMode(VaultSortMode mode);
}
