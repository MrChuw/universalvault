package com.mrchuw.universalvault.config;

import com.mrchuw.universalvault.Platform;


public interface VaultConfig {

    enum FilterMode { WHITELIST, BLACKLIST }

    static VaultConfig get() { return Platform.INSTANCE.config(); }

    // -----------------------------------------------------------------
    // Geral
    // -----------------------------------------------------------------

    boolean debugLogging();
    int maxSlots();
    long maxPerSlot();
    long maxTotalItems();
    boolean hopperInteraction();
    FilterMode defaultFilterMode();
    boolean syncOnEveryChange();
    int syncIntervalTicks();
    int searchDebounceMs();
    boolean autoCreatePersonalVault();

    int customStationTickInterval();

    double baselineExtractionRate();

    double maxVelocityMultiplier();

    long quickCraftCooldownMs();

    boolean enableUniversalQuickCraft();
}