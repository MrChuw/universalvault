package com.mrchuw.universalvault.automation.runtime;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.node.LogisticsNodeRegistry;
import com.mrchuw.universalvault.automation.pattern.CraftJob;
import com.mrchuw.universalvault.automation.pattern.CraftJobsData;
import com.mrchuw.universalvault.automation.pattern.JobSlice;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.pattern.VaultPatternsData;
import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.automation.station.ILogisticsStation;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public final class TaskDispatcher {

    private static final Random RNG = new Random();

    private TaskDispatcher() {}

    public static void dispatchOwner(UUID owner, ServerLevel level) {
        CraftJobsData jobsData = CraftJobsData.get(level);
        VaultPatternsData patternsData = VaultPatternsData.get(level);
        VaultStorage storage = com.mrchuw.universalvault.storage.VaultManager
                .getVault(level, owner);
        if (storage == null) return;

        List<CraftJob> active = jobsData.getActiveJobs(owner);
        if (active.isEmpty()) return;

        for (CraftJob job : active) {
            CraftJob updated = dispatchJob(job, patternsData, storage, level);
            if (updated != job) {
                jobsData.putJob(owner, updated);
            }
        }
    }

    private static CraftJob dispatchJob(CraftJob job,
                                        VaultPatternsData patterns,
                                        VaultStorage storage,
                                        ServerLevel level) {
        CraftJob current = job;
        boolean progress = true;

        while (progress) {
            progress = false;

            List<JobSlice> waiting = current.slicesInState(JobSlice.State.WAITING);
            if (waiting.isEmpty()) break;

            for (JobSlice slice : waiting) {
                RecipeKind kind = RecipeKind.of(slice.recipe());
                List<Candidate> idle = collectCandidates(
                        current.owner(), level, kind, slice.patternId());
                if (idle.isEmpty()) continue;
                Collections.shuffle(idle, RNG);

                if (isAtomic(patterns, current.owner(), slice)) {
                    boolean placed = false;
                    for (Candidate c : idle) {
                        if (tryPlace(current, slice, c, storage, level)) {
                            placed = true;
                            CraftJob refetched = CraftJobsData.get(level)
                                    .find(current.owner(), current.jobId());
                            if (refetched == null) return current;
                            current = refetched;
                            break;
                        }
                    }
                    if (placed) { progress = true; break; }
                    continue;
                }

                List<JobSlice> pieces = splitSliceForCandidates(slice, idle, level);
                if (pieces.isEmpty()) continue;

                current = replaceSliceWithPieces(current, slice, pieces);
                CraftJobsData.get(level).putJob(current.owner(), current);

                int placed = 0;
                Iterator<Candidate> it = idle.iterator();
                for (JobSlice piece : pieces) {
                    if (!it.hasNext()) break;
                    Candidate c = it.next();
                    if (tryPlace(current, piece, c, storage, level)) {
                        placed++;
                        CraftJob refetched = CraftJobsData.get(level)
                                .find(current.owner(), current.jobId());
                        if (refetched == null) return current;
                        current = refetched;
                    }
                }

                if (placed > 0) { progress = true; break; }
            }
        }
        return current;
    }

    private static boolean isAtomic(VaultPatternsData patterns, UUID owner, JobSlice slice) {
        VaultPattern p = patterns.find(owner, slice.patternId());
        if (p == null) return false;
        return p.modifiers().atomic();
    }

    private static CraftJob replaceSliceWithPieces(CraftJob job,
                                                   JobSlice original,
                                                   List<JobSlice> pieces) {
        List<JobSlice> next = new ArrayList<>(job.slices().size()
                + pieces.size() - 1);
        for (JobSlice s : job.slices()) {
            if (s.sliceId().equals(original.sliceId())) {
                next.addAll(pieces);
            } else {
                next.add(s);
            }
        }
        return job.withSlices(next);
    }

    private static List<JobSlice> splitSliceForCandidates(
            JobSlice slice, List<Candidate> idle, ServerLevel level) {

        int units = slice.units();
        if (units <= 0) return List.of();

        List<Integer> caps = new ArrayList<>(idle.size());
        for (Candidate c : idle) {
            int cap;
            try {
                cap = c.station.maxAcceptableUnits(level, slice.recipe());
            } catch (Throwable t) {
                UniversalVault.LOGGER.warn(
                        "[dispatch] maxAcceptableUnits failed at {}: {}",
                        c.station.position(), t.toString());
                cap = 0;
            }
            if (cap > 0) caps.add(cap);
        }
        if (caps.isEmpty()) return List.of();

        int nParts = Math.min(caps.size(), units);
        if (nParts <= 1) {
            int cap = caps.get(0);
            int take = Math.min(units, cap);
            if (take <= 0) return List.of();
            return List.of(newSliceWithUnits(slice, take));
        }

        List<JobSlice> out = new ArrayList<>(nParts);
        long assigned = 0;
        for (int i = 0; i < nParts; i++) {
            int cap = caps.get(i);
            long target = (long) (i + 1) * units / nParts;
            long take = Math.min(target - assigned, cap);
            if (take <= 0) continue;
            out.add(newSliceWithUnits(slice, (int) take));
            assigned += take;
        }
        return out;
    }

    private static JobSlice newSliceWithUnits(JobSlice original, int units) {
        int origUnits = original.units();
        Map<ItemKey, Long> reserved;
        if (origUnits <= 0 || units == origUnits) {
            reserved = original.reservedIngredients();
        } else {
            reserved = new HashMap<>();
            for (Map.Entry<ItemKey, Long> e : original.reservedIngredients().entrySet()) {
                long share = e.getValue() * units / origUnits;
                if (share > 0) reserved.put(e.getKey(), share);
            }
        }
        return new JobSlice(
                UUID.randomUUID(),
                original.patternId(),
                original.recipe(),
                original.output(),
                units,
                reserved,
                Map.of(),
                null,
                JobSlice.State.WAITING);
    }

    private static boolean tryPlace(CraftJob job, JobSlice slice, Candidate candidate,
                                    VaultStorage storage, ServerLevel level) {
        ProductionTask pt = new ProductionTask(
                slice.sliceId(),
                slice.patternId(),
                slice.recipe(),
                slice.output(),
                slice.units(),
                slice.reservedIngredients(),
                com.mrchuw.universalvault.automation.recipe.RecipeKind.of(slice.recipe())
        );

        if (!candidate.station.accept(level, pt, storage)) {
            return false;
        }

        JobSlice active = slice
                .withState(JobSlice.State.ACTIVE)
                .withStation(candidate.station.position());
        CraftJob updated = job.replaceSlice(active);

        CraftJobsData.get(level).putJob(job.owner(), updated);
        return true;
    }

    private static List<Candidate> collectCandidates(UUID owner,
                                                     ServerLevel level,
                                                     RecipeKind kind,
                                                     UUID patternId) {
        List<Candidate> out = new ArrayList<>();

        for (LogisticsNodeBlockEntity node : LogisticsNodeRegistry.all()) {
            if (!node.getOwnerUUID().equals(owner)) continue;
            if (node.getLevel() != level) continue;
            if (!node.acceptsRecipe(kind, patternId)) continue;

            for (LogisticsNodeBlockEntity.StationBind b : node.activeStations()) {
                ILogisticsStation s = b.station();
                if (!s.isIdle()) continue;

                if (kind == RecipeKind.CRAFTING
                        && s.kind() != ILogisticsStation.Kind.CRAFTING) continue;
                if (kind == RecipeKind.CUSTOM
                        && s.kind() != ILogisticsStation.Kind.CUSTOM) continue;

                out.add(new Candidate(node, s));
            }
        }
        return out;
    }

    private static void refund(Map<ItemKey, Long> buf, VaultStorage storage) {
        for (Map.Entry<ItemKey, Long> e : buf.entrySet()) {
            if (e.getValue() <= 0) continue;
            ItemStack stack = e.getKey().toStack(
                    (int) Math.min(e.getValue(), Integer.MAX_VALUE));
            if (!stack.isEmpty()) storage.insert(stack, false);
        }
    }

    private record Candidate(LogisticsNodeBlockEntity node, ILogisticsStation station) {}
}
