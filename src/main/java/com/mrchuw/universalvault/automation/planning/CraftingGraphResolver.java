package com.mrchuw.universalvault.automation.planning;

import com.mrchuw.universalvault.automation.pattern.CraftJob;
import com.mrchuw.universalvault.automation.pattern.CraftJobsData;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.pattern.VaultPatternsData;
import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CraftingGraphResolver {

    private static final int MAX_DEPTH = 32;

    public record PatternPlan(UUID topPatternId, long topAmount, List<PlannedTask> tasks) {}

    private final VaultPatternsData patterns;
    private final CraftJobsData jobs;
    private final VaultStorage storage;
    private final UUID owner;

    private final List<PlannedTask> out = new ArrayList<>();
    private final Map<ItemKey, Long> planned = new HashMap<>();
    private final Set<UUID> visiting = new HashSet<>();
    private int depth = 0;

    private CraftingGraphResolver(VaultPatternsData patterns,
                                  CraftJobsData jobs,
                                  VaultStorage storage,
                                  UUID owner) {
        this.patterns = patterns;
        this.jobs = jobs;
        this.storage = storage;
        this.owner = owner;
    }

    public static List<PatternPlan> buildAllPlans(VaultPatternsData patterns,
                                                  CraftJobsData jobs,
                                                  VaultStorage storage,
                                                  UUID owner) {
        List<PatternPlan> plans = new ArrayList<>();
        for (VaultPattern p : patterns.getPatterns(owner)) {
            if (!shouldTrigger(p, storage, jobs, owner)) continue;
            PatternPlan plan = buildOne(patterns, jobs, storage, owner, p);
            if (plan != null && !plan.tasks().isEmpty()) {
                plans.add(plan);
            }
        }
        return plans;
    }

    public static PatternPlan buildQuickCraft(VaultPatternsData patterns,
                                              CraftJobsData jobs,
                                              VaultStorage storage,
                                              UUID owner,
                                              ItemKey item,
                                              long amount) {
        if (amount <= 0) return null;

        CraftingGraphResolver r = new CraftingGraphResolver(patterns, jobs, storage, owner);
        long available = storage.getAvailable(item);
        long target = available + amount;

        if (!r.planForOutput(item, target)) return null;
        if (r.out.isEmpty()) return null;

        PlannedTask top = r.out.get(r.out.size() - 1);
        int oc = Math.max(1, top.recipe().primaryOutputCount());
        long topAmount = (long) top.units() * oc;
        return new PatternPlan(top.patternId(), topAmount, List.copyOf(r.out));
    }

    private static PatternPlan buildOne(VaultPatternsData patterns,
                                        CraftJobsData jobs,
                                        VaultStorage storage,
                                        UUID owner,
                                        VaultPattern top) {
        long available = storage.getAvailable(top.output());
        long target = computeCycleTarget(top, available);
        if (target <= 0) return null;

        int oc = Math.max(1, top.outputCount());
        long crafts = ceilDiv(target, oc);
        if (crafts <= 0) return null;

        CraftingGraphResolver r = new CraftingGraphResolver(patterns, jobs, storage, owner);
        if (!r.planPattern(top, crafts)) return null;
        if (r.out.isEmpty()) return null;

        long topAmount = crafts * oc;
        return new PatternPlan(top.patternId(), topAmount, List.copyOf(r.out));
    }

    private static boolean shouldTrigger(VaultPattern pattern,
                                         VaultStorage storage,
                                         CraftJobsData jobs,
                                         UUID owner) {
        if (pattern.paused()) return false;
        if (pattern.triggerMode() == VaultPattern.TriggerMode.MANUAL) return false;
        if (pattern.max() <= 0) return false;

        long available = storage.getAvailable(pattern.output());

        long trigger = switch (pattern.triggerMode()) {
            case CONTINUOUS -> pattern.max();
            case BATCH_HYSTERESIS -> pattern.min();
            case MANUAL -> -1L;
        };
        if (available >= trigger) return false;

        if (jobs.hasActive(owner, pattern.patternId(), CraftJob.Origin.AUTO)) return false;

        return true;
    }

    private static long computeCycleTarget(VaultPattern pattern, long available) {
        long deficit = pattern.max() - available;
        if (deficit <= 0) return 0;
        long batch = pattern.batch();
        if (batch > 0) return Math.min(batch, deficit);
        return deficit;
    }

    private boolean planForOutput(ItemKey item, long need) {
        if (need <= 0) return true;
        if (depth > MAX_DEPTH) return false;

        long available = storage.getAvailable(item);
        long alreadyPlanned = planned.getOrDefault(item, 0L);
        long deficit = need - available - alreadyPlanned;
        if (deficit <= 0) return true;

        List<VaultPattern> producers = patterns.findAllByOutput(owner, item);
        if (producers.isEmpty()) return false;

        List<VaultPattern> sorted = new ArrayList<>(producers);
        sorted.sort(Comparator.comparingInt(VaultPattern::priority).reversed());

        Map<ItemKey, Long> plannedSnapshot = new HashMap<>(planned);
        int outSnapshot = out.size();

        planned.merge(item, deficit, Long::sum);

        for (VaultPattern p : sorted) {
            if (jobs.hasActive(owner, p.patternId(), CraftJob.Origin.AUTO)) continue;

            int oc = Math.max(1, p.outputCount());
            long crafts = ceilDiv(deficit, oc);

            if (planPattern(p, crafts)) {
                return true;
            }

            planned.clear();
            planned.putAll(plannedSnapshot);
            planned.merge(item, deficit, Long::sum);
            while (out.size() > outSnapshot) out.remove(out.size() - 1);
        }

        planned.clear();
        planned.putAll(plannedSnapshot);
        return false;
    }

    private boolean planPattern(VaultPattern pattern, long crafts) {
        if (crafts <= 0) return true;
        if (visiting.contains(pattern.patternId())) return false;
        if (depth > MAX_DEPTH) return false;

        Map<ItemKey, Long> perUnit = pattern.recipe().aggregateInputs();

        visiting.add(pattern.patternId());
        depth++;

        Map<ItemKey, Long> inputsForThis = new HashMap<>();
        boolean ok = true;
        for (Map.Entry<ItemKey, Long> e : perUnit.entrySet()) {
            long need = e.getValue() * crafts;
            inputsForThis.put(e.getKey(), need);
            if (!planForOutput(e.getKey(), need)) {
                ok = false;
                break;
            }
        }

        depth--;
        visiting.remove(pattern.patternId());

        if (!ok) return false;

        out.add(new PlannedTask(
                UUID.randomUUID(),
                pattern.patternId(),
                pattern.recipe(),
                pattern.output(),
                (int) Math.min(crafts, Integer.MAX_VALUE),
                inputsForThis,
                pattern.priority(),
                pattern.batch(),
                RecipeKind.of(pattern.recipe())
        ));
        return true;
    }

    private static long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }
}