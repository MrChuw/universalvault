package com.mrchuw.universalvault.automation;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.station.CrafterStation;
import com.mrchuw.universalvault.automation.station.CraftingTableStation;
import com.mrchuw.universalvault.automation.station.CustomStation;
import com.mrchuw.universalvault.automation.station.StationRegistry;
import net.minecraft.world.level.block.Blocks;

public final class AutomationInit {

    private AutomationInit() {}

    public static void registerStations() {
        StationRegistry.register((level, pos, state, face) ->
                state.is(Blocks.CRAFTING_TABLE) ? new CraftingTableStation(pos) : null);

        //? if >=1.21 {
        StationRegistry.register((level, pos, state, face) ->
                state.is(Blocks.CRAFTER) ? new CrafterStation(pos) : null);
        //?}

        StationRegistry.register((level, pos, state, face) -> {
            if (Platform.INSTANCE.findItemHandler(level, pos, face) == null) {
                return null;
            }
            return new CustomStation(pos, face);
        });
    }
}