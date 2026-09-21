package com.mrchuw.universalvault.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.UUIDUtil;
//? if >=26.1 {
/*import net.minecraft.resources.Identifier;
*///?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class VaultSavedData extends SavedData {

    private transient ServerLevel level;

    private record Entry(ItemKey key, long count) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ItemKey.CODEC.fieldOf("key").forGetter(Entry::key),
                        Codec.LONG.fieldOf("count").forGetter(Entry::count)
                ).apply(instance, Entry::new)
        );
    }

    private record VaultEntry(UUID owner, List<Entry> entries) {
        public static final Codec<VaultEntry> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        UUIDUtil.CODEC.fieldOf("owner").forGetter(VaultEntry::owner),
                        Entry.CODEC.listOf().fieldOf("entries").forGetter(VaultEntry::entries)
                ).apply(instance, VaultEntry::new)
        );
    }

    private static final Codec<List<VaultEntry>> LIST_CODEC = VaultEntry.CODEC.listOf();

    public static final Codec<VaultSavedData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    LIST_CODEC.fieldOf("vaults").forGetter(data -> {
                        List<VaultEntry> vaultList = new ArrayList<>();
                        for (Map.Entry<UUID, VaultStorage> entry : data.vaults.entrySet()) {
                            List<Entry> entries = new ArrayList<>();
                            for (Map.Entry<ItemKey, Long> itemEntry : entry.getValue().getAllItems().entrySet()) {
                                entries.add(new Entry(itemEntry.getKey(), itemEntry.getValue()));
                            }
                            vaultList.add(new VaultEntry(entry.getKey(), entries));
                        }
                        return vaultList;
                    })
            ).apply(instance, vaultList -> {
                VaultSavedData data = new VaultSavedData();
                for (VaultEntry ve : vaultList) {
                    VaultStorage storage = data.getOrCreateVault(ve.owner());
                    Map<ItemKey, Long> itemMap = new HashMap<>();
                    for (Entry e : ve.entries()) {
                        if (e.key() != null && e.count() > 0) itemMap.put(e.key(), e.count());
                    }
                    storage.loadFromMap(itemMap);
                }
                return data;
            })
    );

    public static final SavedDataType<VaultSavedData> TYPE = new SavedDataType<>(
            //? if >=26.1 {
            /*Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_data"),
            *///?} else {
            UniversalVault.MOD_ID + "_vault_data",
             //?}
            VaultSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, VaultStorage> vaults = new ConcurrentHashMap<>();

    public VaultSavedData() {
        getOrCreateVault(UniversalVault.GLOBAL_VAULT_UUID);
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
        for (Map.Entry<UUID, VaultStorage> entry : vaults.entrySet()) {
            applyListener(entry.getKey(), entry.getValue());
        }
    }

    public VaultStorage getOrCreateVault(UUID uuid) {
        return vaults.computeIfAbsent(uuid, k -> {
            VaultStorage storage = new VaultStorage();
            applyListener(k, storage);
            return storage;
        });
    }

    private void applyListener(UUID uuid, VaultStorage storage) {
        storage.setChangeListener(() -> {
            VaultSavedData.this.setDirty();
            if (VaultSavedData.this.level == null) return;

            if (VaultConfig.get().syncOnEveryChange()) {
                VaultMenu.broadcastVaultUpdate(uuid, VaultSavedData.this.level);
            } else {
                VaultMenu.markPendingSync(uuid);
            }
        });
    }

    public static VaultSavedData get(ServerLevel level) {
        MinecraftServer server = level.getServer();
        //? if >=26.1 {
        /*VaultSavedData data = server.getDataStorage().computeIfAbsent(TYPE);
        data.setLevel(server.overworld());
        *///?} else {
        VaultSavedData data = level.getDataStorage().computeIfAbsent(TYPE);
        data.setLevel(level);
        //?}
        return data;
    }
}