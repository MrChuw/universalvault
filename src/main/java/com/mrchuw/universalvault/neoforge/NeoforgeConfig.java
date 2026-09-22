package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.config.VaultConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoforgeConfig implements VaultConfig {

    public final ModConfigSpec.BooleanValue debugLogging;
    public final ModConfigSpec.IntValue maxSlots;
    public final ModConfigSpec.LongValue maxPerSlot;
    public final ModConfigSpec.LongValue maxTotalItems;
    public final ModConfigSpec.IntValue maxVirtualSlots;
    public final ModConfigSpec.BooleanValue hopperInteraction;
    public final ModConfigSpec.EnumValue<FilterMode> defaultFilterMode;
    public final ModConfigSpec.BooleanValue syncOnEveryChange;
    public final ModConfigSpec.IntValue syncIntervalTicks;
    public final ModConfigSpec.IntValue searchDebounceMs;
    public final ModConfigSpec.BooleanValue autoCreatePersonalVault;
    public final ModConfigSpec.BooleanValue announceTargetOnCycle;

    public NeoforgeConfig(ModConfigSpec.Builder b) {
        b.push("general");
        debugLogging = b.define("debugLogging", false);
        maxSlots = b.defineInRange("maxSlots", 0, 0, Integer.MAX_VALUE);
        maxPerSlot = b.defineInRange("maxPerSlot", 0L, 0L, Long.MAX_VALUE);
        maxTotalItems = b.defineInRange("maxTotalItems", 0L, 0L, Long.MAX_VALUE);
        maxVirtualSlots = b.defineInRange("maxVirtualSlots", 54, 2, 128);
        hopperInteraction = b.define("hopperInteraction", true);
        defaultFilterMode = b.defineEnum("defaultFilterMode", FilterMode.WHITELIST);
        b.pop();
        b.push("sync");
        syncOnEveryChange = b.define("syncOnEveryChange", true);
        syncIntervalTicks = b.defineInRange("syncIntervalTicks", 5, 1, 200);
        searchDebounceMs = b.defineInRange("searchDebounceMs", 150, 0, 2000);
        b.pop();
        b.push("behavior");
        autoCreatePersonalVault = b.define("autoCreatePersonalVault", true);
        announceTargetOnCycle = b.define("announceTargetOnCycle", true);
        b.pop();
    }

    @Override public boolean debugLogging() { return debugLogging.get(); }
    @Override public int maxSlots() { return maxSlots.get(); }
    @Override public long maxPerSlot() { return maxPerSlot.get(); }
    @Override public long maxTotalItems() { return maxTotalItems.get(); }
    @Override public int maxVirtualSlots() { return maxVirtualSlots.get(); }
    @Override public boolean hopperInteraction() { return hopperInteraction.get(); }
    @Override public FilterMode defaultFilterMode() { return defaultFilterMode.get(); }
    @Override public boolean syncOnEveryChange() { return syncOnEveryChange.get(); }
    @Override public int syncIntervalTicks() { return syncIntervalTicks.get(); }
    @Override public int searchDebounceMs() { return searchDebounceMs.get(); }
    @Override public boolean autoCreatePersonalVault() { return autoCreatePersonalVault.get(); }
    @Override public boolean announceTargetOnCycle() { return announceTargetOnCycle.get(); }
}
//?}