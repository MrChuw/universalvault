package com.mrchuw.universalvault.automation.station;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.automation.runtime.ProductionTask;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class CraftingTableStation implements ILogisticsStation {

    private final BlockPos pos;
    private ProductionTask currentTask;
    private int outputCount = 1;
    private final Map<ItemKey, Long> buffer = new HashMap<>();
    private Map<ItemKey, Long> usesPerUnit = Map.of();

    public CraftingTableStation(BlockPos pos) {
        this.pos = pos;
    }

    @Override public Kind kind() { return Kind.CRAFTING; }

    @Override
    public boolean matches(Level level, BlockPos pos, BlockState state, Direction face) {
        return state.is(Blocks.CRAFTING_TABLE);
    }

    @Override public boolean isIdle() { return currentTask == null; }
    @Override public ProductionTask currentTask() { return currentTask; }
    @Override public BlockPos position() { return pos; }
    @Override public int tickIntervalTicks() { return 1; }

    @Override
    public boolean accept(ServerLevel level, ProductionTask task, VaultStorage storage) {
        if (!isIdle()) return false;
        if (!(task.recipe() instanceof RecipeView.Crafting recipe)) return false;

        int units = task.remaining();
        if (units <= 0) return false;

        Map<ItemKey, Long> extracted = new HashMap<>();
        for (Map.Entry<ItemKey, Long> e : task.reservedIngredients().entrySet()) {
            long want = e.getValue();
            if (want <= 0) continue;
            int wantInt = (int) Math.min(want, Integer.MAX_VALUE);
            ItemStack got = storage.extract(e.getKey(), wantInt,
                    VaultStorage.ExtractMode.CRAFT, false);
            if (got.getCount() < wantInt) {
                refund(extracted, storage);
                if (!got.isEmpty()) storage.insert(got, false);
                UniversalVault.LOGGER.debug(
                        "[crafting-table] insufficient ingredients at {}: {} obtained {}/{}",
                        pos, e.getKey(), got.getCount(), want);
                return false;
            }
            extracted.put(e.getKey(), (long) got.getCount());
        }

        Map<ItemKey, Long> perUnit = new HashMap<>();
        for (Map.Entry<ItemKey, Long> e : extracted.entrySet()) {
            long v = e.getValue() / units;
            if (v <= 0) { refund(extracted, storage); return false; }
            perUnit.put(e.getKey(), v);
        }

        this.buffer.clear();
        this.buffer.putAll(extracted);
        this.usesPerUnit = perUnit;
        this.outputCount = Math.max(1, recipe.outputCount());
        this.currentTask = task;
        return true;
    }

    @Override
    public void tick(ServerLevel level, VaultStorage storage) {
        if (currentTask == null) return;

        boolean hasIngredients = true;
        for (Map.Entry<ItemKey, Long> e : usesPerUnit.entrySet()) {
            if (buffer.getOrDefault(e.getKey(), 0L) < e.getValue()) {
                hasIngredients = false;
                break;
            }
        }
        if (!hasIngredients) {
            refund(buffer, storage);
            buffer.clear();
            currentTask = null;
            return;
        }

        for (Map.Entry<ItemKey, Long> e : usesPerUnit.entrySet()) {
            long left = buffer.merge(e.getKey(), -e.getValue(), Long::sum);
            if (left <= 0) buffer.remove(e.getKey());
        }

        ItemStack output = currentTask.output().toStack(outputCount);
        if (!output.isEmpty()) storage.insert(output, false);

        currentTask = currentTask.consumeOne();
    }

    @Override
    public void abortTask(ServerLevel level, VaultStorage storage) {
        refund(buffer, storage);
        buffer.clear();
        currentTask = null;
    }

    @Override
    public void onAnchorLost(LogisticsNodeBlockEntity node, VaultStorage storage) {
        abortTask(node.getLevel() instanceof ServerLevel sl ? sl : null, storage);
    }

    @Override
    public void forceRelease(ServerLevel level, VaultStorage storage) {
        abortTask(level, storage);
    }

    private static void refund(Map<ItemKey, Long> buf, VaultStorage storage) {
        for (Map.Entry<ItemKey, Long> e : buf.entrySet()) {
            if (e.getValue() <= 0) continue;
            ItemStack stack = e.getKey().toStack(
                    (int) Math.min(e.getValue(), Integer.MAX_VALUE));
            if (!stack.isEmpty()) storage.insert(stack, false);
        }
    }
}
