package com.mrchuw.universalvault.automation.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import javax.annotation.Nullable;

public class VaultPatternsData extends SavedData {

    private static final int CURRENT_SCHEMA = 1;

    private int schema = CURRENT_SCHEMA;
    private final Map<UUID, List<VaultPattern>> patterns = new HashMap<>();

    public VaultPatternsData() {}

    public List<VaultPattern> getPatterns(UUID owner) {
        return patterns.getOrDefault(owner, List.of());
    }

    @Nullable
    public VaultPattern find(UUID owner, UUID patternId) {
        for (VaultPattern p : getPatterns(owner)) {
            if (p.patternId().equals(patternId)) return p;
        }
        return null;
    }

    @Nullable
    public VaultPattern find(UUID owner, ItemKey output) {
        for (VaultPattern p : getPatterns(owner)) {
            if (p.output().equals(output)) return p;
        }
        return null;
    }

    public List<VaultPattern> findAllByOutput(UUID owner, ItemKey output) {
        List<VaultPattern> out = new ArrayList<>();
        for (VaultPattern p : getPatterns(owner)) {
            if (p.output().equals(output)) out.add(p);
        }
        return out;
    }

    public Iterable<Map.Entry<UUID, List<VaultPattern>>> allOwners() {
        return patterns.entrySet();
    }

    public int schema() {
        return schema;
    }

    public void setPatterns(UUID owner, List<VaultPattern> list) {
        if (list.isEmpty()) {
            patterns.remove(owner);
        } else {
            patterns.put(owner, List.copyOf(list));
        }
        setDirty();
    }

    public void addPattern(UUID owner, VaultPattern pattern) {
        List<VaultPattern> cur = new ArrayList<>(getPatterns(owner));
        cur.removeIf(p -> p.patternId().equals(pattern.patternId()));
        cur.add(pattern);
        setPatterns(owner, cur);
    }

    public boolean removePattern(UUID owner, UUID patternId) {
        List<VaultPattern> cur = new ArrayList<>(getPatterns(owner));
        if (cur.removeIf(p -> p.patternId().equals(patternId))) {
            setPatterns(owner, cur);
            return true;
        }
        return false;
    }

    private record Entry(UUID owner, List<VaultPattern> patterns) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner),
                VaultPattern.CODEC.listOf().fieldOf("patterns").forGetter(Entry::patterns)
        ).apply(inst, Entry::new));
    }

    public static final Codec<VaultPatternsData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA)
                    .forGetter(VaultPatternsData::schema),
            Entry.CODEC.listOf().optionalFieldOf("owners", List.of())
                    .forGetter(VaultPatternsData::asEntries)
    ).apply(inst, (schema, entries) -> {
        VaultPatternsData data = new VaultPatternsData();
        data.schema = schema;
        for (Entry e : entries) {
            if (e.patterns().isEmpty()) continue;
            data.patterns.put(e.owner(), List.copyOf(e.patterns()));
        }
        return data;
    }));

    private List<Entry> asEntries() {
        List<Entry> out = new ArrayList<>(patterns.size());
        for (Map.Entry<UUID, List<VaultPattern>> e : patterns.entrySet()) {
            out.add(new Entry(e.getKey(), e.getValue()));
        }
        return out;
    }

    public static final SavedDataType<VaultPatternsData> TYPE = new SavedDataType<>(
            //? if >= 26.1 {
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "patterns"),
             //?} else {
            /*UniversalVault.MOD_ID + "_patterns",
            *///?}
            VaultPatternsData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static VaultPatternsData get(ServerLevel level) {
        //? if >= 26.1 {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
         //?} else {
        /*return level.getDataStorage().computeIfAbsent(TYPE);
        *///?}
    }
}