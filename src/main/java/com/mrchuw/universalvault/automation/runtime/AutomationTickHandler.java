package com.mrchuw.universalvault.automation.runtime;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.node.LogisticsNodeRegistry;
import com.mrchuw.universalvault.automation.pattern.*;
import com.mrchuw.universalvault.automation.planning.CraftingGraphResolver;
import com.mrchuw.universalvault.automation.planning.PlannedTask;
import com.mrchuw.universalvault.automation.station.ILogisticsStation;
import com.mrchuw.universalvault.automation.tracking.AdaptiveVelocityTracker;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

public final class AutomationTickHandler {

    private static final int PLAN_INTERVAL_TICKS = 20;
    private static long lastPlanGameTime = -1;

    private AutomationTickHandler() {}

    public static void serverTick(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        long now = overworld.getGameTime();
        if (now - lastPlanGameTime < PLAN_INTERVAL_TICKS) return;
        lastPlanGameTime = now;

        AdaptiveVelocityTracker.update(overworld, now);

        VaultPatternsData patterns = VaultPatternsData.get(overworld);
        CraftJobsData jobs = CraftJobsData.get(overworld);

        for (Map.Entry<UUID, List<VaultPattern>> entry : patterns.allOwners()) {
            UUID owner = entry.getKey();
            VaultStorage storage = VaultManager.getVault(overworld, owner);
            if (storage == null) continue;

            try {
                List<CraftingGraphResolver.PatternPlan> plans =
                        CraftingGraphResolver.buildAllPlans(patterns, jobs, storage, owner);
                for (CraftingGraphResolver.PatternPlan plan : plans) {
                    createJobFromPlan(owner, plan, jobs, overworld);
                }
            } catch (Throwable t) {
                UniversalVault.LOGGER.error("[planner] failed to generate plans for {}", owner, t);
            }
        }

        for (Map.Entry<UUID, List<CraftJob>> entry : jobs.allOwners()) {
            UUID owner = entry.getKey();
            try {
                TaskDispatcher.dispatchOwner(owner, overworld);
            } catch (Throwable t) {
                UniversalVault.LOGGER.error("[dispatch] failed for {}", owner, t);
            }
        }

        updateActiveSlices(overworld, jobs);

        closeFinishedJobs(overworld, jobs);
    }

    private static void createJobFromPlan(UUID owner,
                                          CraftingGraphResolver.PatternPlan plan,
                                          CraftJobsData jobs,
                                          ServerLevel level) {
        long total = plan.topAmount();
        if (total <= 0) return;

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

        ItemKey topOutput = null;
        for (PlannedTask t : plan.tasks()) {
            if (t.patternId().equals(plan.topPatternId())) {
                topOutput = t.output();
                break;
            }
        }
        if (topOutput == null) return;

        CraftJob job = new CraftJob(
                UUID.randomUUID(),
                plan.topPatternId(),
                owner,
                CraftJob.Origin.AUTO,
                topOutput,
                total,
                0,
                Map.of(),
                slices,
                CraftJob.State.RUNNING,
                level.getGameTime()
        );
        jobs.putJob(owner, job);
    }

    private static void updateActiveSlices(ServerLevel level, CraftJobsData jobs) {
        for (Map.Entry<UUID, List<CraftJob>> entry : jobs.allOwners()) {
            UUID owner = entry.getKey();
            for (CraftJob job : jobs.getActiveJobs(owner)) {
                List<JobSlice> active = job.slicesInState(JobSlice.State.ACTIVE);
                if (active.isEmpty()) continue;

                boolean changed = false;
                CraftJob updated = job;
                for (JobSlice slice : active) {
                    BlockPos pos = slice.stationPos();
                    if (pos == null) continue;

                    ILogisticsStation station = findStationAt(level, pos);
                    if (station == null) {
                        JobSlice reset = slice.withState(JobSlice.State.WAITING).withStation(null);
                        updated = updated.replaceSlice(reset);
                        changed = true;
                        continue;
                    }
                    if (station.isIdle()) {
                        updated = updated.replaceSlice(slice.withState(JobSlice.State.DONE));
                        changed = true;
                    }
                }

                if (changed) {
                    long done = updated.countDoneTopSlices();
                    updated = updated.withCompleted(done);
                    jobs.putJob(owner, updated);
                }
            }
        }
    }

    private static void closeFinishedJobs(ServerLevel level, CraftJobsData jobs) {
        for (Map.Entry<UUID, List<CraftJob>> entry : jobs.allOwners()) {
            UUID owner = entry.getKey();
            List<CraftJob> active = jobs.getActiveJobs(owner);
            for (CraftJob job : active) {
                if (job.isFinished() && !job.hasRemainingWork()) {
                    jobs.putJob(owner, job.withState(CraftJob.State.DONE));
                }
            }
        }
    }

    private static ILogisticsStation findStationAt(ServerLevel level, BlockPos pos) {
        for (LogisticsNodeBlockEntity node : LogisticsNodeRegistry.all()) {
            if (node.getLevel() != level) continue;
            for (LogisticsNodeBlockEntity.StationBind b : node.activeStations()) {
                if (b.station().position().equals(pos)) return b.station();
            }
        }
        return null;
    }
}
