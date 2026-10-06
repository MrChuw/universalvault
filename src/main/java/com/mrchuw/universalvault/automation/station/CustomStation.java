package com.mrchuw.universalvault.automation.station;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.handler.ItemAutomationHandler;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.automation.runtime.ProductionTask;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.storage.VaultStorage;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class CustomStation implements ILogisticsStation {

    private final BlockPos pos;
    private final Direction face;
    private ProductionTask currentTask;
    private RecipeView.Custom recipe;

    private int unitsTarget = 0;

    private final Map<Integer, Long> pushedPerSlot = new HashMap<>();
    private final Map<Integer, Long> drainedPerSlot = new HashMap<>();

    public CustomStation(BlockPos pos, Direction face) {
        this.pos = pos;
        this.face = face;
    }

    @Override public Kind kind() { return Kind.CUSTOM; }

    @Override
    public boolean matches(Level level, BlockPos pos, BlockState state, Direction face) {
        return handlerAt(level, pos, face) != null;
    }

    @Override public boolean isIdle() { return currentTask == null; }
    @Override public ProductionTask currentTask() { return currentTask; }
    @Override public BlockPos position() { return pos; }

    @Override
    public int tickIntervalTicks() {
        return VaultConfig.get().customStationTickInterval();
    }

    @Override
    public boolean accept(ServerLevel level, ProductionTask task, VaultStorage storage) {
        if (!isIdle()) return false;
        if (!(task.recipe() instanceof RecipeView.Custom c)) return false;

        ItemAutomationHandler handler = handlerAt(level, pos, face);
        if (handler == null) return false;
        if (task.remaining() <= 0) return false;

        int size = handler.size();
        for (RecipeView.SlotSpec spec : c.inputs()) {
            if (spec.slotIndex() < 0 || spec.slotIndex() >= size) {
                UniversalVault.LOGGER.warn(
                        "[custom] {} input slot {} out of bounds (size={})",
                        pos, spec.slotIndex(), size);
                return false;
            }
        }
        for (RecipeView.SlotSpec spec : c.outputs()) {
            int phys = physicalOutputSlot(c, spec.slotIndex());
            if (phys < 0 || phys >= size) {
                UniversalVault.LOGGER.warn(
                        "[custom] {} physical output slot {} out of bounds (size={})",
                        pos, phys, size);
                return false;
            }
        }

        this.recipe = c;
        this.currentTask = task;
        this.unitsTarget = task.remaining();
        this.pushedPerSlot.clear();
        this.drainedPerSlot.clear();

        if (!refillInputs(handler, storage)) {
            this.currentTask = null;
            this.recipe = null;
            this.unitsTarget = 0;
            return false;
        }
        return true;
    }

    @Override
    public void tick(ServerLevel level, VaultStorage storage) {
        if (currentTask != null) {
            tickWithTask(level, storage);
            return;
        }
        drainIdleOutputs(level, storage);
    }

    private void tickWithTask(ServerLevel level, VaultStorage storage) {
        ItemAutomationHandler handler = handlerAt(level, pos, face);
        if (handler == null) {
            currentTask = null;
            return;
        }

        drainOutputsToTracking(handler, storage);

        int unitsDone = computeUnitsDone();
        if (unitsDone > 0) {
            currentTask = currentTask.consumeN(unitsDone);
            if (currentTask == null) return;
        }

        refillInputs(handler, storage);
    }

    private boolean refillInputs(ItemAutomationHandler handler, VaultStorage storage) {
        boolean any = false;
        for (RecipeView.SlotSpec spec : recipe.inputs()) {
            long target = (long) spec.count() * unitsTarget;
            long pushed = pushedPerSlot.getOrDefault(spec.slotIndex(), 0L);
            if (pushed >= target) continue;

            ItemStack probe = spec.item().toStack(1);
            long space = handler.getCapacityForSlot(spec.slotIndex(), probe);
            if (space <= 0) continue;

            long toPush = Math.min(target - pushed, space);
            if (toPush <= 0) continue;

            int toExtract = (int) Math.min(toPush, Integer.MAX_VALUE);
            ItemStack got = storage.extract(spec.item(), toExtract,
                    VaultStorage.ExtractMode.CRAFT, false);
            if (got.isEmpty()) continue;

            long inserted = handler.insertIntoSlot(spec.slotIndex(), got, false);
            if (inserted > 0) {
                pushedPerSlot.merge(spec.slotIndex(), inserted, Long::sum);
                any = true;
                if (inserted < got.getCount()) {
                    storage.insert(got.copyWithCount(got.getCount() - (int) inserted), false);
                }
            } else {
                storage.insert(got, false);
            }
        }
        return any;
    }

    private void drainOutputsToTracking(ItemAutomationHandler handler, VaultStorage storage) {
        for (RecipeView.SlotSpec s : recipe.outputs()) {
            int slot = physicalOutputSlot(recipe, s.slotIndex());
            if (slot < 0 || slot >= handler.size()) continue;
            long amount = handler.getAmountInSlot(slot);
            if (amount <= 0) continue;

            ItemStack extracted = handler.extractFromSlot(slot, amount, false);
            if (!extracted.isEmpty()) {
                storage.insert(extracted, false);
                drainedPerSlot.merge(s.slotIndex(), (long) extracted.getCount(), Long::sum);
            }
        }
    }

    private int computeUnitsDone() {
        int minUnits = Integer.MAX_VALUE;
        for (RecipeView.SlotSpec s : recipe.outputs()) {
            long drained = drainedPerSlot.getOrDefault(s.slotIndex(), 0L);
            long perUnit = Math.max(1, s.count());
            minUnits = (int) Math.min(minUnits, drained / perUnit);
        }
        if (minUnits == Integer.MAX_VALUE || minUnits <= 0) return 0;

        for (RecipeView.SlotSpec s : recipe.outputs()) {
            long consumed = (long) minUnits * Math.max(1, s.count());
            drainedPerSlot.merge(s.slotIndex(), -consumed, Long::sum);
        }
        return minUnits;
    }

    @Override
    public void abortTask(ServerLevel level, VaultStorage storage) {
        if (recipe != null) {
            ItemAutomationHandler handler = handlerAt(level, pos, face);
            if (handler != null) drainInputs(handler, storage);
        }
        currentTask = null;
        recipe = null;
        unitsTarget = 0;
        pushedPerSlot.clear();
        drainedPerSlot.clear();
    }

    @Override
    public void onAnchorLost(LogisticsNodeBlockEntity node, VaultStorage storage) {}

    @Override
    public void forceRelease(ServerLevel level, VaultStorage storage) {
        abortTask(level, storage);
    }

    private @Nullable ItemAutomationHandler handlerAt(Level level, BlockPos p, Direction side) {
        return Platform.INSTANCE.findItemHandler(level, p, side);
    }

    private static int physicalOutputSlot(RecipeView.Custom c, int virtualOutputSlot) {
        return c.inputs().size() + virtualOutputSlot;
    }

    private void drainInputs(ItemAutomationHandler handler, VaultStorage storage) {
        int size = handler.size();
        for (RecipeView.SlotSpec s : recipe.inputs()) {
            int slot = s.slotIndex();
            if (slot < 0 || slot >= size) continue;
            long amount = handler.getAmountInSlot(slot);
            if (amount <= 0) continue;
            ItemStack extracted = handler.extractFromSlot(slot, amount, false);
            if (!extracted.isEmpty()) {
                storage.insert(extracted, false);
            }
        }
    }

    private void drainIdleOutputs(ServerLevel level, VaultStorage storage) {
        if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) {
            drainContainerSlot(furnace, 2, storage);
            return;
        }
        if (level.getBlockEntity(pos) instanceof BrewingStandBlockEntity stand) {
            for (int i = 0; i <= 2; i++) drainContainerSlot(stand, i, storage);
            return;
        }
        if (recipe == null) return;
        ItemAutomationHandler handler = handlerAt(level, pos, face);
        if (handler == null) return;
        for (RecipeView.SlotSpec s : recipe.outputs()) {
            drainHandlerSlot(handler, physicalOutputSlot(recipe, s.slotIndex()), storage);
        }
    }

    private void drainContainerSlot(Container container, int slot, VaultStorage storage) {
        if (slot < 0 || slot >= container.getContainerSize()) return;
        ItemStack s = container.getItem(slot);
        if (s.isEmpty()) return;

        long moved = storage.insert(s.copy(), false);
        if (moved > 0) {
            s.shrink((int) moved);
            if (s.isEmpty()) container.setItem(slot, ItemStack.EMPTY);
            container.setChanged();
        }
    }

    private void drainHandlerSlot(ItemAutomationHandler handler, int slot, VaultStorage storage) {
        if (slot < 0 || slot >= handler.size()) return;
        long amount = handler.getAmountInSlot(slot);
        if (amount <= 0) return;
        ItemStack extracted = handler.extractFromSlot(slot, amount, false);
        if (!extracted.isEmpty()) {
            storage.insert(extracted, false);
        }
    }
}
