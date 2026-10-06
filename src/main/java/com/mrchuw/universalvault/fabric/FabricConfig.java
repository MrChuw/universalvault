package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.VaultConfig;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public class FabricConfig implements VaultConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(UniversalVault.MOD_ID + ".json");

    public static class Data {
        public boolean debugLogging = false;
        public int maxSlots = 0;
        public long maxPerSlot = 0L;
        public long maxTotalItems = 0L;
        public boolean hopperInteraction = true;
        public FilterMode defaultFilterMode = FilterMode.WHITELIST;
        public boolean syncOnEveryChange = true;
        public int syncIntervalTicks = 5;
        public int searchDebounceMs = 150;
        public boolean autoCreatePersonalVault = true;
        public int customStationTickInterval = 5;

        public double baselineExtractionRate = 1.0;
        public double maxVelocityMultiplier = 16.0;
        public long quickCraftCooldownMs = 200L;
        public boolean enableUniversalQuickCraft = false;
    }

    private final Data data;

    public FabricConfig() {
        this.data = loadOrCreate();
        validate();
    }

    private static Data loadOrCreate() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Data loaded = GSON.fromJson(reader, Data.class);
                if (loaded != null) return loaded;
            } catch (Exception e) {
                System.err.println("[" + UniversalVault.MOD_ID
                        + "] Failed to read config file, falling back to defaults: " + e.getMessage());
            }
        }
        Data defaultData = new Data();
        save(defaultData);
        return defaultData;
    }

    public static void save(Data toSave) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(toSave, writer);
            }
        } catch (Exception e) {
            System.err.println("[" + UniversalVault.MOD_ID + "] Failed to save config file: " + e.getMessage());
        }
    }

    private void validate() {
        if (data.maxVelocityMultiplier < 1.0) {
            System.err.println("[" + UniversalVault.MOD_ID
                    + "] maxVelocityMultiplier < 1.0; forcing 1.0");
            data.maxVelocityMultiplier = 1.0;
        }
        if (data.baselineExtractionRate <= 0.0) {
            System.err.println("[" + UniversalVault.MOD_ID
                    + "] baselineExtractionRate <= 0; forcing 1.0");
            data.baselineExtractionRate = 1.0;
        }
    }

    @Override public boolean debugLogging() { return data.debugLogging; }
    @Override public int maxSlots() { return data.maxSlots; }
    @Override public long maxPerSlot() { return data.maxPerSlot; }
    @Override public long maxTotalItems() { return data.maxTotalItems; }
    @Override public boolean hopperInteraction() { return data.hopperInteraction; }
    @Override public FilterMode defaultFilterMode() { return data.defaultFilterMode; }
    @Override public boolean syncOnEveryChange() { return data.syncOnEveryChange; }
    @Override public int syncIntervalTicks() { return data.syncIntervalTicks; }
    @Override public int searchDebounceMs() { return data.searchDebounceMs; }
    @Override public boolean autoCreatePersonalVault() { return data.autoCreatePersonalVault; }
    @Override public int customStationTickInterval() { return data.customStationTickInterval; }
    @Override public double baselineExtractionRate() { return data.baselineExtractionRate; }
    @Override public double maxVelocityMultiplier() { return data.maxVelocityMultiplier; }
    @Override public long quickCraftCooldownMs() { return data.quickCraftCooldownMs; }
    @Override public boolean enableUniversalQuickCraft() { return data.enableUniversalQuickCraft; }
}
*///?}
