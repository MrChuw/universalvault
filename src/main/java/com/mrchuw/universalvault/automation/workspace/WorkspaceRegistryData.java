package com.mrchuw.universalvault.automation.workspace;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class WorkspaceRegistryData extends SavedData {

    private static final int CURRENT_SCHEMA = 1;
    private static final String DATA_NAME = UniversalVault.MOD_ID + "_workspace";

    private int schema = CURRENT_SCHEMA;

    private final Map<BlockPos, UUID> claimedStations = new HashMap<>();

    public WorkspaceRegistryData() {}

    public int schema() {
        return schema;
    }

    public boolean isClaimed(BlockPos pos) {
        return claimedStations.containsKey(pos);
    }

    public UUID getOwner(BlockPos pos) {
        return claimedStations.get(pos);
    }

    public boolean claimWithTiebreak(BlockPos pos, UUID nodeUuid) {
        UUID current = claimedStations.get(pos);
        if (current == null) {
            claimedStations.put(pos, nodeUuid);
            setDirty();
            return true;
        }
        if (current.equals(nodeUuid)) return true;

        boolean ownerLoaded = false;
        for (var node : com.mrchuw.universalvault.automation.node.LogisticsNodeRegistry.all()) {
            if (node.getNodeUUID().equals(current)) { ownerLoaded = true; break; }
        }
        if (!ownerLoaded) {
            UniversalVault.LOGGER.info(
                    "[workspace] stealing orphan claim at {} from node {} -> {}",
                    pos, current, nodeUuid);
            claimedStations.put(pos, nodeUuid);
            setDirty();
            return true;
        }

        if (nodeUuid.compareTo(current) < 0) {
            claimedStations.put(pos, nodeUuid);
            setDirty();
            return true;
        }
        return false;
    }

    public int unbindAllForNode(UUID nodeUuid) {
        int removed = 0;
        var it = claimedStations.entrySet().iterator();
        while (it.hasNext()) {
            if (it.next().getValue().equals(nodeUuid)) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) setDirty();
        return removed;
    }

    public void unbind(BlockPos pos) {
        if (claimedStations.remove(pos) != null) setDirty();
    }

    private record ClaimEntry(BlockPos pos, UUID node) {
        static final Codec<ClaimEntry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(ClaimEntry::pos),
                UUIDUtil.CODEC.fieldOf("node").forGetter(ClaimEntry::node)
        ).apply(inst, ClaimEntry::new));
    }

    public static final Codec<WorkspaceRegistryData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("schema", CURRENT_SCHEMA).forGetter(WorkspaceRegistryData::schema),
            ClaimEntry.CODEC.listOf().optionalFieldOf("claims", java.util.List.of())
                    .forGetter(WorkspaceRegistryData::asEntries)
    ).apply(inst, (schema, entries) -> {
        WorkspaceRegistryData data = new WorkspaceRegistryData();
        data.schema = schema;
        for (ClaimEntry e : entries) data.claimedStations.put(e.pos(), e.node());
        return data;
    }));

    private java.util.List<ClaimEntry> asEntries() {
        java.util.List<ClaimEntry> out = new java.util.ArrayList<>(claimedStations.size());
        for (Map.Entry<BlockPos, UUID> e : claimedStations.entrySet()) {
            out.add(new ClaimEntry(e.getKey(), e.getValue()));
        }
        return out;
    }

    public static final SavedDataType<WorkspaceRegistryData> TYPE = new SavedDataType<>(
            //? if >=26.1 {
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "workspace"),
            //?} else {
            /*UniversalVault.MOD_ID + "_workspace",
             *///?}
            WorkspaceRegistryData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    public static WorkspaceRegistryData get(ServerLevel level) {
        //? if >=26.1 {
        return level.getServer().getDataStorage().computeIfAbsent(TYPE);
        //?} else {
        /*return level.getDataStorage().computeIfAbsent(TYPE);
         *///?}
    }

    public java.util.Map<BlockPos, UUID> allClaims() {
        return java.util.Map.copyOf(claimedStations);
    }
}
