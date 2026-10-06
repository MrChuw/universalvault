package com.mrchuw.universalvault.automation.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
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

public class CraftJobsData extends SavedData {

    private static final int CURRENT_SCHEMA = 1;

    private int schema = CURRENT_SCHEMA;
    private final Map<UUID, List<CraftJob>> jobs = new HashMap<>();

    public CraftJobsData() {}

    public List<CraftJob> getJobs(UUID owner) {
        return jobs.getOrDefault(owner, List.of());
    }

    public List<CraftJob> getActiveJobs(UUID owner) {
        List<CraftJob> out = new ArrayList<>();
        for (CraftJob j : getJobs(owner)) {
            if (j.state() == CraftJob.State.RUNNING) out.add(j);
        }
        return out;
    }

    @Nullable
    public CraftJob find(UUID owner, UUID jobId) {
        for (CraftJob j : getJobs(owner)) {
            if (j.jobId().equals(jobId)) return j;
        }
        return null;
    }

    @Nullable
    public CraftJob findActive(UUID owner, UUID patternId, CraftJob.Origin origin) {
        for (CraftJob j : getJobs(owner)) {
            if (j.state() != CraftJob.State.RUNNING) continue;
            if (!j.patternId().equals(patternId)) continue;
            if (j.origin() != origin) continue;
            return j;
        }
        return null;
    }

    public boolean hasActive(UUID owner, UUID patternId, CraftJob.Origin origin) {
        return findActive(owner, patternId, origin) != null;
    }

    public Iterable<Map.Entry<UUID, List<CraftJob>>> allOwners() {
        return jobs.entrySet();
    }

    public int schema() { return schema; }

    public void setJobs(UUID owner, List<CraftJob> list) {
        if (list.isEmpty()) {
            jobs.remove(owner);
        } else {
            jobs.put(owner, List.copyOf(list));
        }
        setDirty();
    }

    public void putJob(UUID owner, CraftJob job) {
        List<CraftJob> cur = new ArrayList<>(getJobs(owner));
        cur.removeIf(j -> j.jobId().equals(job.jobId()));
        cur.add(job);
        setJobs(owner, cur);
    }

    public void removeJob(UUID owner, UUID jobId) {
        List<CraftJob> cur = new ArrayList<>(getJobs(owner));
        if (cur.removeIf(j -> j.jobId().equals(jobId))) {
            setJobs(owner, cur);
        }
    }

    private record Entry(UUID owner, List<CraftJob> jobs) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner),
                CraftJob.CODEC.listOf().fieldOf("jobs").forGetter(Entry::jobs)
        ).apply(inst, Entry::new));
    }

    public static final Codec<CraftJobsData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA)
                    .forGetter(CraftJobsData::schema),
            Entry.CODEC.listOf().optionalFieldOf("owners", List.of())
                    .forGetter(CraftJobsData::asEntries)
    ).apply(inst, (schema, entries) -> {
        CraftJobsData data = new CraftJobsData();
        data.schema = schema;
        for (Entry e : entries) {
            if (e.jobs().isEmpty()) continue;
            data.jobs.put(e.owner(), List.copyOf(e.jobs()));
        }
        return data;
    }));

    private List<Entry> asEntries() {
        List<Entry> out = new ArrayList<>(jobs.size());
        for (Map.Entry<UUID, List<CraftJob>> e : jobs.entrySet()) {
            out.add(new Entry(e.getKey(), e.getValue()));
        }
        return out;
    }

    public static final SavedDataType<CraftJobsData> TYPE = new SavedDataType<>(
            //? if >= 26.1 {
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "jobs"),
             //?} else {
            /*UniversalVault.MOD_ID + "_jobs",
            *///?}
            CraftJobsData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static CraftJobsData get(ServerLevel level) {
        //? if >= 26.1 {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
         //?} else {
        /*return level.getDataStorage().computeIfAbsent(TYPE);
        *///?}
    }
}
