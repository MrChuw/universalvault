package com.mrchuw.universalvault.automation.station;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class StationRegistry {

    @FunctionalInterface
    public interface Provider {
        ILogisticsStation create(ServerLevel level, BlockPos pos, BlockState state, Direction face);
    }

    private static final List<Provider> PROVIDERS = new ArrayList<>();

    private StationRegistry() {}

    public static synchronized void register(Provider provider) {
        PROVIDERS.add(provider);
    }

    public static ILogisticsStation createStation(
            ServerLevel level, BlockPos pos, BlockState state, Direction face
    ) {
        for (Provider p : PROVIDERS) {
            ILogisticsStation s = p.create(level, pos, state, face);
            if (s != null) return s;
        }
        return null;
    }
}