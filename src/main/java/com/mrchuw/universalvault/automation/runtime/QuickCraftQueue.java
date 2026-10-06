package com.mrchuw.universalvault.automation.runtime;

import com.mrchuw.universalvault.automation.pattern.CraftJob;
import com.mrchuw.universalvault.automation.pattern.CraftJobsData;
import com.mrchuw.universalvault.automation.pattern.JobSlice;
import com.mrchuw.universalvault.automation.planning.CraftingGraphResolver;
import com.mrchuw.universalvault.automation.planning.PlannedTask;
import com.mrchuw.universalvault.automation.pattern.VaultPatternsData;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;

public final class QuickCraftQueue {

    private QuickCraftQueue() {}

    public static CraftJob enqueue(ServerLevel level,
                                   UUID owner,
                                   ItemKey item,
                                   int amount) {
        if (amount <= 0) return null;

        VaultPatternsData patterns = VaultPatternsData.get(level);
        CraftJobsData jobs = CraftJobsData.get(level);
        VaultStorage storage = VaultManager.getVault(level, owner);
        if (storage == null) return null;

        CraftingGraphResolver.PatternPlan plan;
        try {
            plan = CraftingGraphResolver.buildQuickCraft(
                    patterns, jobs, storage, owner, item, amount);
        } catch (Throwable t) {
            return null;
        }
        if (plan == null || plan.tasks().isEmpty()) return null;

        long total = plan.topAmount();
        if (total <= 0) total = amount;

        List<JobSlice> slices = new ArrayList<>(plan.tasks().size());
        for (PlannedTask t : plan.tasks()) {
            slices.add(new JobSlice(
                    UUID.randomUUID(),
                    t.patternId(),
                    t.recipe(),
                    t.output(),
                    t.units(),
                    t.reservedIngredients(),
                    Map.of(),
                    null,
                    JobSlice.State.WAITING
            ));
        }

        CraftJob job = new CraftJob(
                UUID.randomUUID(),
                plan.topPatternId() != null ? plan.topPatternId() : new UUID(0, 0),
                owner,
                CraftJob.Origin.QUICKCRAFT,
                item,
                total,
                0,
                Map.of(),
                slices,
                CraftJob.State.RUNNING,
                level.getGameTime()
        );
        jobs.putJob(owner, job);
        return job;
    }
}
