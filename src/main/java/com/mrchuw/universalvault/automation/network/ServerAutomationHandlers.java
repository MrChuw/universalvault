package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.node.LogisticsNodeRegistry;
import com.mrchuw.universalvault.automation.pattern.CraftJob;
import com.mrchuw.universalvault.automation.pattern.CraftJobsData;
import com.mrchuw.universalvault.automation.pattern.JobSlice;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.pattern.VaultPatternsData;
import com.mrchuw.universalvault.automation.runtime.QuickCraftQueue;
import com.mrchuw.universalvault.gui.menu.EncodedPatternSlot;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.11 {
import net.minecraft.server.permissions.Permissions;
//?}
import net.minecraft.world.item.ItemStack;

public final class ServerAutomationHandlers {

    private static final long DEFAULT_QUICK_CRAFT_COOLDOWN_MS = 200L;
    private static final ConcurrentHashMap<UUID, Long> LAST_QUICK_CRAFT = new ConcurrentHashMap<>();
    private static final int MAX_QUICK_CRAFT_AMOUNT = 36 * 64;

    private ServerAutomationHandlers() {}

    public static void handleEncodePattern(ServerPlayer player, C2SEncodePatternPayload p) {
        if (!isVaultMenu(player)) return;

        UUID patternId = p.existingPatternId().orElseGet(UUID::randomUUID);
        VaultPattern pattern = new VaultPattern(
                patternId,
                p.recipe(),
                p.min(), p.max(), p.batch(), p.priority(),
                p.triggerMode(),
                true,
                p.pushMode(),
                p.modifiers());

        switch (p.destination()) {
            case SLOT -> {
                ItemStack book = com.mrchuw.universalvault.item.EncodedPattern.create(pattern);
                if (player.containerMenu instanceof VaultMenu menu) {
                    EncodedPatternSlot slot = menu.getPatternSlot();
                    if (slot != null) {
                        slot.set(book);
                        player.containerMenu.broadcastChanges();
                        return;
                    }
                }
                if (!player.getInventory().add(book)) {
                    //? if <=26.2 {
                    /*player.drop(book, false);
                    *///?} else {
                    player.drop(book, false, net.minecraft.util.Prediction.PREDICTED);
                     //?}
                };
            }

            case INVENTORY -> {
                ItemStack book = com.mrchuw.universalvault.item.EncodedPattern.create(pattern);
                clearPatternSlot(player);
                if (!player.getInventory().add(book)) {
                    //? if <=26.2 {
                    /*player.drop(book, false);
                    *///?} else {
                    player.drop(book, false, net.minecraft.util.Prediction.PREDICTED);
                     //?}
                };
                player.containerMenu.broadcastChanges();
            }

            case LIBRARY -> {
                ServerLevel level = (ServerLevel) player.level();
                UUID owner = ownerFromMenu(player);
                VaultPatternsData data = VaultPatternsData.get(level);
                data.addPattern(owner, pattern);
                clearPatternSlot(player);
                player.containerMenu.broadcastChanges();
                sendPatternsSync(player, level, owner);
            }
        }
    }

    private static void clearPatternSlot(ServerPlayer player) {
        if (player.containerMenu instanceof VaultMenu menu) {
            EncodedPatternSlot slot = menu.getPatternSlot();
            if (slot != null) slot.set(ItemStack.EMPTY);
        }
    }

    public static void handleImportPattern(ServerPlayer player, C2SImportPatternPayload p) {
        if (!isVaultMenu(player)) return;
        UUID owner = ownerFromMenu(player);
        if (!canModify(player, owner)) return;

        ServerLevel level = (ServerLevel) player.level();
        VaultPatternsData data = VaultPatternsData.get(level);

        VaultPattern pattern = p.pattern();

        if (!consumeEncodedPattern(player, pattern.patternId())) return;

        data.addPattern(owner, pattern);
        sendPatternsSync(player, level, owner);
    }

    private static boolean consumeEncodedPattern(ServerPlayer player, UUID patternId) {
        ItemStack carried = player.containerMenu.getCarried();
        VaultPattern carriedP = carried.get(ModRegistry.ENCODED_PATTERN_DATA.get());
        if (carriedP != null && carriedP.patternId().equals(patternId)) {
            carried.shrink(1);
            player.containerMenu.setCarried(carried);
            return true;
        }

        if (player.containerMenu instanceof VaultMenu menu) {
            EncodedPatternSlot ps = menu.getPatternSlot();
            if (ps != null) {
                VaultPattern p = ps.getItem().get(ModRegistry.ENCODED_PATTERN_DATA.get());
                if (p != null && p.patternId().equals(patternId)) {
                    ps.set(ItemStack.EMPTY);
                    player.containerMenu.broadcastChanges();
                    return true;
                }
            }
        }

        var inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            VaultPattern p = s.get(ModRegistry.ENCODED_PATTERN_DATA.get());
            if (p != null && p.patternId().equals(patternId)) {
                s.shrink(1);
                if (s.isEmpty()) inv.setItem(i, ItemStack.EMPTY);
                player.containerMenu.slotsChanged(inv);
                return true;
            }
        }
        return false;
    }

    public static void handlePatternUpdate(ServerPlayer player, C2SPatternUpdatePayload p) {
        if (!isVaultMenu(player)) return;
        UUID owner = p.owner();
        if (!canModify(player, owner)) return;

        ServerLevel level = (ServerLevel) player.level();
        VaultPatternsData data = VaultPatternsData.get(level);

        if (p.pattern().isEmpty()) {
            UniversalVault.LOGGER.debug(
                    "[pattern] empty update from {} (use action=REMOVE)", player.getName().getString());
            return;
        }

        data.addPattern(owner, p.pattern().get());
        sendPatternsSync(player, level, owner);
    }

    public static void handlePatternAction(ServerPlayer player, C2SPatternActionPayload p) {
        if (!isVaultMenu(player)) return;
        UUID owner = ownerFromMenu(player);
        if (!canModify(player, owner)) return;

        ServerLevel level = (ServerLevel) player.level();
        VaultPatternsData patterns = VaultPatternsData.get(level);
        CraftJobsData jobs = CraftJobsData.get(level);

        VaultPattern pattern = patterns.find(owner, p.patternId());
        if (pattern == null) return;

        switch (p.action()) {
            case PAUSE_TOGGLE -> patterns.addPattern(owner, pattern.toggledPause());
            case REMOVE -> {
                patterns.removePattern(owner, p.patternId());
            }

            case WITHDRAW -> {
                VaultPattern cur = patterns.find(owner, p.patternId());
                if (cur == null) return;

                ItemStack carried = player.containerMenu.getCarried();
                if (!carried.isEmpty()) return;

                patterns.removePattern(owner, p.patternId());
                player.containerMenu.setCarried(
                        com.mrchuw.universalvault.item.EncodedPattern.create(cur));
            }

            case WITHDRAW_TO_INVENTORY -> {
                VaultPattern cur = patterns.find(owner, p.patternId());
                if (cur == null) return;

                patterns.removePattern(owner, p.patternId());
                ItemStack book = com.mrchuw.universalvault.item.EncodedPattern.create(cur);
                if (!player.getInventory().add(book)) {
                    //? if <=26.2 {
                    /*player.drop(book, false);
                    *///?} else {
                    player.drop(book, false, net.minecraft.util.Prediction.PREDICTED);
                     //?}
                }
            }

            case CANCEL_JOB -> cancelJobsForPattern(owner, p.patternId(), level, jobs);
        }

        sendPatternsSync(player, level, owner);
        sendJobsSync(player, level, owner);
    }

    public static void handleCancelJob(ServerPlayer player, C2SCancelJobPayload p) {
        if (!isVaultMenu(player)) return;
        UUID owner = ownerFromMenu(player);
        if (!canModify(player, owner)) return;

        ServerLevel level = (ServerLevel) player.level();
        CraftJobsData jobs = CraftJobsData.get(level);

        cancelJob(owner, p.jobId(), level, jobs);
        sendJobsSync(player, level, owner);
        sendPatternsSync(player, level, owner);
    }

    private static void cancelJobsForPattern(UUID owner, UUID patternId,
                                             ServerLevel level, CraftJobsData jobs) {
        for (CraftJob job : jobs.getActiveJobs(owner)) {
            if (job.patternId().equals(patternId)) {
                cancelJob(owner, job.jobId(), level, jobs);
            }
        }
    }

    private static void cancelJob(UUID owner, UUID jobId,
                                  ServerLevel level, CraftJobsData jobs) {
        CraftJob job = jobs.find(owner, jobId);
        if (job == null) return;

        for (JobSlice slice : job.slicesInState(JobSlice.State.ACTIVE)) {
            if (slice.stationPos() == null) continue;
            var station = findStationAt(level, slice.stationPos());
            if (station != null) {
                VaultStorage storage = VaultManager.getVault(level, owner);
                if (storage != null) station.abortTask(level, storage);
            }
        }

        jobs.removeJob(owner, jobId);

        if (job.origin() == CraftJob.Origin.AUTO) {
            VaultPatternsData patterns = VaultPatternsData.get(level);
            VaultPattern p = patterns.find(owner, job.patternId());
            if (p != null && !p.paused()) {
                patterns.addPattern(owner, p.toggledPause());
            }
        }
    }

    public static void handlePatternsRequest(ServerPlayer sp, C2SRequestPatternsSyncPayload p) {
        ServerLevel level = (ServerLevel) sp.level();
        UUID owner;

        if (p.nodePos().isPresent()) {
            BlockPos pos = p.nodePos().get();
            if (!(level.getBlockEntity(pos) instanceof LogisticsNodeBlockEntity node)) return;
            owner = node.getOwnerUUID();
            if (node.getOwnerUUID().equals(new UUID(0L, 0L))) return;
        } else {
            if (!isVaultMenu(sp)) return;
            owner = ownerFromMenu(sp);
        }

        sendPatternsSync(sp, level, owner);
    }

    public static void handleJobsRequest(ServerPlayer sp, C2SRequestJobsSyncPayload p) {
        if (!isVaultMenu(sp)) return;
        UUID owner = ownerFromMenu(sp);
        sendJobsSync(sp, (ServerLevel) sp.level(), owner);
    }

    public static void sendPatternsSync(ServerPlayer sp, ServerLevel level, UUID owner) {
        VaultPatternsData data = VaultPatternsData.get(level);
        VaultStorage storage = VaultManager.getVault(level, owner);

        List<VaultPattern> patterns = data.getPatterns(owner);
        List<S2CPatternsSyncPayload.Entry> entries = new ArrayList<>(patterns.size());
        for (VaultPattern pat : patterns) {
            long stock = storage != null ? storage.getStock(pat.output()) : 0L;
            entries.add(new S2CPatternsSyncPayload.Entry(pat, stock));
        }
        Platform.INSTANCE.sendToPlayer(sp, new S2CPatternsSyncPayload(entries));
    }

    public static void sendJobsSync(ServerPlayer sp, ServerLevel level, UUID owner) {
        CraftJobsData data = CraftJobsData.get(level);
        List<CraftJob> jobs = data.getJobs(owner);
        List<S2CJobsSyncPayload.Entry> entries = new ArrayList<>(jobs.size());
        for (CraftJob j : jobs) {
            if (j.state() != CraftJob.State.RUNNING) continue;
            entries.add(S2CJobsSyncPayload.Entry.of(j));
        }
        Platform.INSTANCE.sendToPlayer(sp, new S2CJobsSyncPayload(entries));
    }

    public static void handleQuickCraft(ServerPlayer player, C2SQuickCraftPayload p) {
        if (!isVaultMenu(player)) return;
        if (p.item() == null || !p.item().isResolved()) return;

        long now = System.currentTimeMillis();
        long cooldown = UniversalVault.getConfig().quickCraftCooldownMs();
        if (cooldown <= 0) cooldown = DEFAULT_QUICK_CRAFT_COOLDOWN_MS;

        Long last = LAST_QUICK_CRAFT.get(player.getUUID());
        if (last != null && now - last < cooldown) return;
        LAST_QUICK_CRAFT.put(player.getUUID(), now);

        int amount = Math.max(1, Math.min(p.amount(), MAX_QUICK_CRAFT_AMOUNT));
        UUID owner = ownerFromMenu(player);

        ServerLevel level = (ServerLevel) player.level();
        VaultPatternsData patterns = VaultPatternsData.get(level);

        boolean hasPattern = !patterns.findAllByOutput(owner, p.item()).isEmpty();
        boolean universal = UniversalVault.getConfig().enableUniversalQuickCraft();
        if (!hasPattern && !universal) {
            UniversalVault.LOGGER.debug(
                    "[quick-craft] {} has no pattern for {} and universal is disabled",
                    player.getName().getString(), p.item());
            return;
        }

        QuickCraftQueue.enqueue(level, owner, p.item(), amount);
        sendJobsSync(player, level, owner);
    }

    public static void handleNodeConfig(ServerPlayer player, C2SNodeConfigPayload p) {
        ServerLevel level = (ServerLevel) player.level();
        if (!(level.getBlockEntity(p.nodePos()) instanceof LogisticsNodeBlockEntity node)) return;
        if (!canModify(player, node.getOwnerUUID())) return;

        node.setPullSides(p.pullSides());
        node.setPatternOverrides(p.patternOverrides());
        node.setBindSides(p.bindSides());
    }

    private static boolean isVaultMenu(ServerPlayer player) {
        return player.containerMenu instanceof VaultMenu
                || player.containerMenu instanceof VaultFilterMenu;
    }

    private static boolean canModify(ServerPlayer player, UUID owner) {
        if (owner == null) return false;
        //? if >=1.21.11 {
        boolean isAdmin = player.permissions().hasPermission(Permissions.COMMANDS_OWNER);
        //?} else {
        /*boolean isAdmin = player.hasPermissions(4);
         *///?}
        return owner.equals(player.getUUID()) || isAdmin;
    }

    private static UUID ownerFromMenu(ServerPlayer player) {
        if (player.containerMenu instanceof VaultMenu v) {
            return v.getOwnerUUID();
        }
        return player.getUUID();
    }

    private static com.mrchuw.universalvault.automation.station.ILogisticsStation findStationAt(
            ServerLevel level, net.minecraft.core.BlockPos pos) {
        for (LogisticsNodeBlockEntity node : LogisticsNodeRegistry.all()) {
            if (node.getLevel() != level) continue;
            for (LogisticsNodeBlockEntity.StationBind b : node.activeStations()) {
                if (b.station().position().equals(pos)) return b.station();
            }
        }
        return null;
    }
}