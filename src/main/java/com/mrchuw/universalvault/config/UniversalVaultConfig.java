package com.mrchuw.universalvault.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class UniversalVaultConfig {

    public static final UniversalVaultConfig CONFIG;
    public static final ModConfigSpec SPEC;

    public enum FilterMode { WHITELIST, BLACKLIST }

    // General
    public final ModConfigSpec.BooleanValue debugLogging;
    public final ModConfigSpec.IntValue maxSlots;
    public final ModConfigSpec.LongValue maxPerSlot;
    public final ModConfigSpec.LongValue maxTotalItems;
    public final ModConfigSpec.IntValue maxVirtualSlots;
    public final ModConfigSpec.BooleanValue hopperInteraction;
    public final ModConfigSpec.EnumValue<FilterMode> defaultFilterMode;

    // Sync
    public final ModConfigSpec.BooleanValue syncOnEveryChange;
    public final ModConfigSpec.IntValue syncIntervalTicks;
    public final ModConfigSpec.IntValue searchDebounceMs;

    // Behavior
    public final ModConfigSpec.BooleanValue autoCreatePersonalVault;
    public final ModConfigSpec.BooleanValue announceTargetOnCycle;

    private UniversalVaultConfig(ModConfigSpec.Builder builder) {
        builder.comment("Universal Vault — general settings").push("general");

        debugLogging = builder
                .comment("Enable verbose logging (insert/extract, filters, capabilities).",
                        "Useful for debugging but spammy.")
                .define("debugLogging", false);

        maxSlots = builder
                .comment("Maximum number of distinct item types in a vault.",
                        "0 = infinite.")
                .defineInRange("maxSlots", 0, 0, Integer.MAX_VALUE);

        maxPerSlot = builder
                .comment("Maximum amount of a single item type per vault.",
                        "0 = infinite.")
                .defineInRange("maxPerSlot", 0L, 0L, Long.MAX_VALUE);

        maxTotalItems = builder
                .comment("Maximum total amount of all items combined in a vault.",
                        "0 = infinite.")
                .defineInRange("maxTotalItems", 0L, 0L, Long.MAX_VALUE);

        maxVirtualSlots = builder
                .comment("Number of virtual slots exposed to hoppers/minecarts via Container.",
                        "Minimum 2 (the last slot is always reserved as a wildcard).")
                .defineInRange("maxVirtualSlots", 54, 2, 128);

        hopperInteraction = builder
                .comment("Allow hoppers, minecarts and other automation to insert/extract.")
                .define("hopperInteraction", true);

        defaultFilterMode = builder
                .comment("WHITELIST: only items matching the filter can pass.",
                        "BLACKLIST: everything except the filtered items can pass.")
                .defineEnum("defaultFilterMode", FilterMode.WHITELIST);

        builder.pop();

        builder.comment("Universal Vault — sync settings").push("sync");

        syncOnEveryChange = builder
                .comment("Sync the GUI on every vault change.",
                        "Disable for large vaults / servers to reduce packet spam.")
                .define("syncOnEveryChange", true);

        syncIntervalTicks = builder
                .comment("When syncOnEveryChange=false, how often (in ticks) to sync the vault to viewers.",
                        "20 ticks = 1 second.")
                .defineInRange("syncIntervalTicks", 5, 1, 200);

        searchDebounceMs = builder
                .comment("Delay (in ms) before applying the search filter on the client.",
                        "Increase if the vault has thousands of item types and the GUI stutters.")
                .defineInRange("searchDebounceMs", 150, 0, 2000);

        builder.pop();

        builder.comment("Universal Vault — behavior").push("behavior");

        autoCreatePersonalVault = builder
                .comment("Automatically create a personal vault for a player on first login.")
                .define("autoCreatePersonalVault", true);

        announceTargetOnCycle = builder
                .comment("Show a chat message when a Vault I/O block cycles its target.")
                .define("announceTargetOnCycle", true);

        builder.pop();
    }

    static {
        Pair<UniversalVaultConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(UniversalVaultConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }
}