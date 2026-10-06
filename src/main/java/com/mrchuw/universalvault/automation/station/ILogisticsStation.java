package com.mrchuw.universalvault.automation.station;

import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.runtime.ProductionTask;
import com.mrchuw.universalvault.storage.VaultStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

public interface ILogisticsStation {

    enum Kind { CRAFTING, CUSTOM }

    Kind kind();

    boolean matches(Level level, BlockPos pos, BlockState state, Direction face);

    boolean isIdle();

    @Nullable ProductionTask currentTask();

    boolean accept(ServerLevel level, ProductionTask task, VaultStorage storage);

    void tick(ServerLevel level, VaultStorage storage);

    void abortTask(ServerLevel level, VaultStorage storage);

    void onAnchorLost(LogisticsNodeBlockEntity node, VaultStorage storage);

    default int tickIntervalTicks() { return 20; }

    default void forceRelease(ServerLevel level, VaultStorage storage) {}

    default int maxAcceptableUnits(net.minecraft.server.level.ServerLevel level,
                                   com.mrchuw.universalvault.automation.recipe.RecipeView recipe) {
        return Integer.MAX_VALUE;
    }

    BlockPos position();
}