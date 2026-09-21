package com.mrchuw.universalvault.fabric;

//? fabric {
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.VaultConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfig implements VaultConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(UniversalVault.MOD_ID + ".json");

    // Internal data POJO with default values matching NeoForge
    public static class Data {
        public boolean debugLogging = false;
        public int maxSlots = 0;
        public long maxPerSlot = 0L;
        public long maxTotalItems = 0L;
        public int maxVirtualSlots = 54;
        public boolean hopperInteraction = true;
        public FilterMode defaultFilterMode = FilterMode.WHITELIST;
        public boolean syncOnEveryChange = true;
        public int syncIntervalTicks = 5;
        public int searchDebounceMs = 150;
        public boolean autoCreatePersonalVault = true;
        public boolean announceTargetOnCycle = true;
    }

    private final Data data;

    public FabricConfig() {
        this.data = loadOrCreate();
    }

    private static Data loadOrCreate() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Data loaded = GSON.fromJson(reader, Data.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (Exception e) {
                System.err.println("[" + UniversalVault.MOD_ID + "] Failed to read config file, falling back to defaults: " + e.getMessage());
            }
        }

        // If file does not exist or failed to load, write default JSON to disk
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

    // VaultConfig overrides reading from the POJO
    @Override public boolean debugLogging() { return data.debugLogging; }
    @Override public int maxSlots() { return data.maxSlots; }
    @Override public long maxPerSlot() { return data.maxPerSlot; }
    @Override public long maxTotalItems() { return data.maxTotalItems; }
    @Override public int maxVirtualSlots() { return data.maxVirtualSlots; }
    @Override public boolean hopperInteraction() { return data.hopperInteraction; }
    @Override public FilterMode defaultFilterMode() { return data.defaultFilterMode; }
    @Override public boolean syncOnEveryChange() { return data.syncOnEveryChange; }
    @Override public int syncIntervalTicks() { return data.syncIntervalTicks; }
    @Override public int searchDebounceMs() { return data.searchDebounceMs; }
    @Override public boolean autoCreatePersonalVault() { return data.autoCreatePersonalVault; }
    @Override public boolean announceTargetOnCycle() { return data.announceTargetOnCycle; }
}
//?}