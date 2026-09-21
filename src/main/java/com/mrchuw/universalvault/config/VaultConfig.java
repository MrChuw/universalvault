package com.mrchuw.universalvault.config;

import com.mrchuw.universalvault.Platform;

public interface VaultConfig {

    enum FilterMode { WHITELIST, BLACKLIST }

    static VaultConfig get() { return Platform.INSTANCE.config(); }

    boolean debugLogging();
    int maxSlots();
    long maxPerSlot();
    long maxTotalItems();
    int maxVirtualSlots();
    boolean hopperInteraction();
    FilterMode defaultFilterMode();
    boolean syncOnEveryChange();
    int syncIntervalTicks();
    int searchDebounceMs();
    boolean autoCreatePersonalVault();
    boolean announceTargetOnCycle();
}
