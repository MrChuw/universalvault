package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.config.VaultConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoforgeConfig implements VaultConfig {

    public final ModConfigSpec.BooleanValue debugLogging;
    public final ModConfigSpec.IntValue maxSlots;
    public final ModConfigSpec.LongValue maxPerSlot;
    public final ModConfigSpec.LongValue maxTotalItems;
    public final ModConfigSpec.BooleanValue hopperInteraction;
    public final ModConfigSpec.EnumValue<FilterMode> defaultFilterMode;
    public final ModConfigSpec.BooleanValue syncOnEveryChange;
    public final ModConfigSpec.IntValue syncIntervalTicks;
    public final ModConfigSpec.IntValue searchDebounceMs;
    public final ModConfigSpec.BooleanValue autoCreatePersonalVault;
    public final ModConfigSpec.IntValue customStationTickInterval;
    public final ModConfigSpec.DoubleValue baselineExtractionRate;
    public final ModConfigSpec.DoubleValue maxVelocityMultiplier;
    public final ModConfigSpec.LongValue quickCraftCooldownMs;
    public final ModConfigSpec.BooleanValue enableUniversalQuickCraft;

    public NeoforgeConfig(ModConfigSpec.Builder b) {
        b.push("general");
        debugLogging = b.define("debugLogging", false);
        maxSlots = b.defineInRange("maxSlots", 0, 0, Integer.MAX_VALUE);
        maxPerSlot = b.defineInRange("maxPerSlot", 0L, 0L, Long.MAX_VALUE);
        maxTotalItems = b.defineInRange("maxTotalItems", 0L, 0L, Long.MAX_VALUE);
        hopperInteraction = b.define("hopperInteraction", true);
        defaultFilterMode = b.defineEnum("defaultFilterMode", FilterMode.WHITELIST);
        customStationTickInterval = b.defineInRange("customStationTickInterval", 5, 1, 200);
        b.pop();

        b.push("sync");
        syncOnEveryChange = b.define("syncOnEveryChange", true);
        syncIntervalTicks = b.defineInRange("syncIntervalTicks", 5, 1, 200);
        searchDebounceMs = b.defineInRange("searchDebounceMs", 150, 0, 2000);
        b.pop();

        b.push("behavior");
        autoCreatePersonalVault = b.define("autoCreatePersonalVault", true);
        b.pop();

        b.push("automation");

        b.comment("Velocidade adaptativa baseada na extração do jogador.")
                .push("velocity");
        baselineExtractionRate = b
                .comment("Taxa base de extração (itens/segundo) para normalizar o multiplicador.",
                        "Extração igual à taxa base → multiplicador 1.0.")
                .defineInRange("baselineExtractionRate", 1.0, 0.01, 1000.0);
        maxVelocityMultiplier = b
                .comment("Teto do multiplicador. Evita drenar o cofre além do razoável.")
                .defineInRange("maxVelocityMultiplier", 16.0, 1.0, 1000.0);
        b.pop();

        b.comment("Quick crafting.")
                .push("quickCraft");
        quickCraftCooldownMs = b
                .comment("Cooldown server-side entre dois pedidos de quick craft do mesmo jogador.")
                .defineInRange("quickCraftCooldownMs", 200L, 0L, 10_000L);
        enableUniversalQuickCraft = b
                .comment("Habilita Quick Crafting (clique do meio) em qualquer item com pattern registrado.")
                .define("enableUniversalQuickCraft", false);
        b.pop();

        b.pop();
    }

    @Override public boolean debugLogging() { return debugLogging.get(); }
    @Override public int maxSlots() { return maxSlots.get(); }
    @Override public long maxPerSlot() { return maxPerSlot.get(); }
    @Override public long maxTotalItems() { return maxTotalItems.get(); }
    @Override public boolean hopperInteraction() { return hopperInteraction.get(); }
    @Override public FilterMode defaultFilterMode() { return defaultFilterMode.get(); }
    @Override public boolean syncOnEveryChange() { return syncOnEveryChange.get(); }
    @Override public int syncIntervalTicks() { return syncIntervalTicks.get(); }
    @Override public int searchDebounceMs() { return searchDebounceMs.get(); }
    @Override public boolean autoCreatePersonalVault() { return autoCreatePersonalVault.get(); }
    @Override public int customStationTickInterval() { return customStationTickInterval.get(); }
    @Override public double baselineExtractionRate() { return baselineExtractionRate.get(); }
    @Override public double maxVelocityMultiplier() { return maxVelocityMultiplier.get(); }
    @Override public long quickCraftCooldownMs() { return quickCraftCooldownMs.get(); }
    @Override public boolean enableUniversalQuickCraft() { return enableUniversalQuickCraft.get(); }
}
//?}
