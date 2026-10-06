package com.mrchuw.universalvault.automation.station;

//? if >=1.21 {
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.automation.runtime.ProductionTask;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public class CrafterStation implements ILogisticsStation {

    private static final int SLOT_COUNT = 9;

    private final BlockPos pos;
    private ProductionTask currentTask;
    private int outputCount = 1;
    private final Map<Integer, ItemKey> expectedGrid = new HashMap<>();

    public CrafterStation(BlockPos pos) {
        this.pos = pos;
    }

    @Override public Kind kind() { return Kind.CRAFTING; }

    @Override
    public boolean matches(Level level, BlockPos pos, BlockState state, Direction face) {
        return state.is(Blocks.CRAFTER);
    }

    @Override public boolean isIdle() { return currentTask == null; }
    @Override public ProductionTask currentTask() { return currentTask; }
    @Override public BlockPos position() { return pos; }
    @Override public int tickIntervalTicks() { return 6; }

    @Override
    public boolean accept(ServerLevel level, ProductionTask task, VaultStorage storage) {
        if (!isIdle()) return false;
        if (!(task.recipe() instanceof RecipeView.Crafting recipe)) return false;

        CrafterBlockEntity crafter = crafterAt(level);
        if (crafter == null) return false;

        if (!isGridEmpty(crafter)) {
            drainToVault(crafter, storage);
            if (!isGridEmpty(crafter)) {
                UniversalVault.LOGGER.warn("[crafter] dirty grid at {} - rejecting", pos);
                return false;
            }
        }

        if (!fillGrid(crafter, storage, recipe.grid())) {
            drainToVault(crafter, storage);
            return false;
        }

        this.expectedGrid.clear();
        this.expectedGrid.putAll(recipe.grid());
        this.outputCount = Math.max(1, recipe.outputCount());
        this.currentTask = task;
        return true;
    }

    @Override
    public void tick(ServerLevel level, VaultStorage storage) {
        CrafterBlockEntity crafter = crafterAt(level);
        if (crafter == null) {
            currentTask = null;
            expectedGrid.clear();
            return;
        }

        if (currentTask == null) {
            drainToVault(crafter, storage);
            return;
        }

        if (!isGridEmpty(crafter)) {
            consumeAndProduce(level, crafter, storage);
        }

        if (currentTask != null && isGridEmpty(crafter)) {
            RecipeView.Crafting recipe = (RecipeView.Crafting) currentTask.recipe();
            if (!fillGrid(crafter, storage, recipe.grid())) {
                UniversalVault.LOGGER.debug(
                        "[crafter] missing ingredients for refill at {}", pos);
                currentTask = null;
            }
        }
    }

    @Override
    public void abortTask(ServerLevel level, VaultStorage storage) {
        CrafterBlockEntity crafter = crafterAt(level);
        if (crafter != null) drainToVault(crafter, storage);
        currentTask = null;
        expectedGrid.clear();
    }

    @Override
    public void onAnchorLost(LogisticsNodeBlockEntity node, VaultStorage storage) {}

    @Override
    public void forceRelease(ServerLevel level, VaultStorage storage) {
        abortTask(level, storage);
    }

    private @Nullable CrafterBlockEntity crafterAt(ServerLevel level) {
        if (level.getBlockEntity(pos) instanceof CrafterBlockEntity c) return c;
        return null;
    }

    private boolean isGridEmpty(CrafterBlockEntity crafter) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!crafter.getItem(i).isEmpty()) return false;
        }
        return true;
    }

    private boolean fillGrid(CrafterBlockEntity crafter, VaultStorage storage,
                             Map<Integer, ItemKey> grid) {
        for (Map.Entry<Integer, ItemKey> e : grid.entrySet()) {
            int slot = e.getKey() - 1;
            if (slot < 0 || slot >= SLOT_COUNT) continue;

            ItemStack existing = crafter.getItem(slot);
            if (!existing.isEmpty()) {
                ItemKey existingKey = ItemKey.of(existing);
                if (existingKey != null && existingKey.equals(e.getValue())) continue;
                return false;
            }

            ItemStack extracted = storage.extract(e.getValue(), 1,
                    VaultStorage.ExtractMode.CRAFT, false);
            if (extracted.isEmpty()) return false;
            crafter.setItem(slot, extracted);
        }
        crafter.setChanged();
        return true;
    }

    private void consumeAndProduce(ServerLevel level, CrafterBlockEntity crafter,
                                   VaultStorage storage) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = crafter.getItem(i);
            if (stack.isEmpty()) continue;
            stack.shrink(1);
            if (stack.isEmpty()) crafter.setItem(i, ItemStack.EMPTY);
        }
        crafter.setChanged();

        ItemStack output = currentTask.output().toStack(outputCount);
        if (!output.isEmpty()) storage.insert(output, false);

        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(CrafterBlock.CRAFTING)
                && !state.getValue(CrafterBlock.CRAFTING)) {
            level.setBlock(pos, state.setValue(CrafterBlock.CRAFTING, true), 2);
            crafter.setCraftingTicksRemaining(6);
        }

        currentTask = currentTask.consumeOne();
    }

    private void drainToVault(CrafterBlockEntity crafter, VaultStorage storage) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = crafter.getItem(i);
            if (stack.isEmpty()) continue;
            long moved = storage.insert(stack, false);
            if (moved > 0) {
                stack.shrink((int) moved);
                if (stack.isEmpty()) crafter.setItem(i, ItemStack.EMPTY);
            }
        }
        crafter.setChanged();
    }
}
//?}
