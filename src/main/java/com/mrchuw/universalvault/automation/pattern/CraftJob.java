package com.mrchuw.universalvault.automation.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import javax.annotation.Nullable;

public record CraftJob(
        UUID jobId,
        UUID patternId,
        UUID owner,
        Origin origin,
        ItemKey topOutput,
        long requestedThisCycle,
        long completedThisCycle,
        Map<ItemKey, Long> queued,
        List<JobSlice> slices,
        State state,
        long createdAt
) {

    public enum Origin { AUTO, MANUAL, QUICKCRAFT }
    public enum State { RUNNING, PAUSED, CANCELLED, DONE }

    public static final Codec<CraftJob> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("job_id").forGetter(CraftJob::jobId),
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("pattern_id").forGetter(CraftJob::patternId),
            net.minecraft.core.UUIDUtil.CODEC.fieldOf("owner").forGetter(CraftJob::owner),
            Codec.STRING.xmap(Origin::valueOf, Enum::name).fieldOf("origin").forGetter(CraftJob::origin),
            ItemKey.CODEC.fieldOf("top_output").forGetter(CraftJob::topOutput),
            Codec.LONG.fieldOf("requested").forGetter(CraftJob::requestedThisCycle),
            Codec.LONG.optionalFieldOf("completed", 0L).forGetter(CraftJob::completedThisCycle),
            kvMapCodec().optionalFieldOf("queued", Map.of()).forGetter(CraftJob::queued),
            JobSlice.CODEC.listOf().optionalFieldOf("slices", List.of()).forGetter(CraftJob::slices),
            Codec.STRING.xmap(State::valueOf, Enum::name)
                    .optionalFieldOf("state", State.RUNNING).forGetter(CraftJob::state),
            Codec.LONG.optionalFieldOf("created_at", 0L).forGetter(CraftJob::createdAt)
    ).apply(inst, CraftJob::new));

    public CraftJob {
        queued = Map.copyOf(queued);
        slices = List.copyOf(slices);
    }

    public boolean isFinished() {
        return completedThisCycle >= requestedThisCycle;
    }

    public List<JobSlice> slicesInState(JobSlice.State s) {
        List<JobSlice> out = new ArrayList<>();
        for (JobSlice sl : slices) {
            if (sl.state() == s) out.add(sl);
        }
        return out;
    }

    public long countDoneTopSlices() {
        long n = 0;
        for (JobSlice sl : slices) {
            if (sl.state() != JobSlice.State.DONE) continue;
            if (!sl.output().equals(topOutput)) continue;
            int oc = Math.max(1, sl.recipe().primaryOutputCount());
            n += (long) sl.units() * oc;
        }
        return n;
    }

    public boolean hasRemainingWork() {
        for (JobSlice sl : slices) {
            if (sl.state() != JobSlice.State.DONE) return true;
        }
        return false;
    }

    public CraftJob withState(State s) {
        return new CraftJob(jobId, patternId, owner, origin, topOutput,
                requestedThisCycle, completedThisCycle, queued, slices, s, createdAt);
    }

    public CraftJob withCompleted(long n) {
        return new CraftJob(jobId, patternId, owner, origin, topOutput,
                requestedThisCycle, n, queued, slices, state, createdAt);
    }

    public CraftJob withSlices(List<JobSlice> next) {
        return new CraftJob(jobId, patternId, owner, origin, topOutput,
                requestedThisCycle, completedThisCycle, queued, next, state, createdAt);
    }

    public CraftJob replaceSlice(JobSlice newSlice) {
        List<JobSlice> next = new ArrayList<>(slices.size());
        for (JobSlice sl : slices) {
            next.add(sl.sliceId().equals(newSlice.sliceId()) ? newSlice : sl);
        }
        return withSlices(next);
    }

    private static Codec<Map<ItemKey, Long>> kvMapCodec() {
        return Kv.CODEC.listOf().xmap(
                list -> {
                    Map<ItemKey, Long> m = new HashMap<>();
                    for (Kv kv : list) m.put(kv.key(), kv.value());
                    return m;
                },
                map -> {
                    var l = new ArrayList<Kv>(map.size());
                    for (var e : map.entrySet()) l.add(new Kv(e.getKey(), e.getValue()));
                    return l;
                });
    }

    private record Kv(ItemKey key, long value) {
        static final Codec<Kv> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ItemKey.CODEC.fieldOf("k").forGetter(Kv::key),
                Codec.LONG.fieldOf("v").forGetter(Kv::value)
        ).apply(inst, Kv::new));
    }
}