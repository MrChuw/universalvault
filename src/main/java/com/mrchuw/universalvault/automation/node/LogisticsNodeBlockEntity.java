package com.mrchuw.universalvault.automation.node;

import com.mojang.serialization.Codec;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.recipe.RecipeKind;
import com.mrchuw.universalvault.automation.station.ILogisticsStation;
import com.mrchuw.universalvault.automation.station.StationRegistry;
import com.mrchuw.universalvault.automation.workspace.WorkspaceRegistryData;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogisticsNodeBlockEntity extends BlockEntity {

    public record StationBind(ILogisticsStation station, Direction face) {}

    private static final UUID NO_OWNER = new UUID(0L, 0L);

    private boolean initialized = false;
    private boolean isBinding = false;

    private UUID nodeUUID = UUID.randomUUID();
    private UUID ownerUUID = NO_OWNER;

    private final List<StationBind> activeStations = new ArrayList<>();
    private Set<Direction> pullSides = EnumSet.allOf(Direction.class);
    private Map<UUID, Boolean> patternOverrides = new HashMap<>();
    private Set<Direction> bindSides = EnumSet.noneOf(Direction.class);

    private long lastTickGameTime = 0;

    public LogisticsNodeBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.LOGISTICS_NODE_BLOCK_ENTITY.get(), pos, state);
    }

    public UUID getNodeUUID() { return nodeUUID; }

    public UUID getOwnerUUID() { return ownerUUID; }

    public boolean hasOwner() { return !NO_OWNER.equals(ownerUUID); }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid != null ? uuid : NO_OWNER;
        setChanged();
    }

    public List<StationBind> activeStations() { return List.copyOf(activeStations); }

    public Set<Direction> getPullSides() { return Set.copyOf(pullSides); }

    public void setPullSides(Set<Direction> sides) {
        this.pullSides = sides.isEmpty() ? EnumSet.noneOf(Direction.class) : EnumSet.copyOf(sides);
        setChanged();
    }

    public Map<UUID, Boolean> getPatternOverrides() {
        return Map.copyOf(patternOverrides);
    }

    public void setPatternOverrides(Map<UUID, Boolean> overrides) {
        this.patternOverrides = overrides == null
                ? new HashMap<>()
                : new HashMap<>(overrides);
        setChanged();
    }

    public Set<Direction> getBindSides() { return Set.copyOf(bindSides); }

    public void setBindSides(Set<Direction> set) {
        this.bindSides = set == null || set.isEmpty()
                ? EnumSet.noneOf(Direction.class)
                : EnumSet.copyOf(set);
        setChanged();
        activeStations.clear();
        bindAdjacentStations();
    }

    public void bindAdjacentStations() {
        if (isBinding) return; // Trava contra reentrância
        if (!(this.level instanceof ServerLevel serverLevel)) return;

        isBinding = true;
        try {
            WorkspaceRegistryData registry = WorkspaceRegistryData.get(serverLevel);

            for (Direction dir : Direction.values()) {
                BlockPos adjPos = this.worldPosition.relative(dir);
                if (hasStationAt(adjPos)) continue;

                // Não tente checar chunks descarregados
                if (!serverLevel.hasChunkAt(adjPos)) continue;

                BlockState adjState = this.level.getBlockState(adjPos);
                ILogisticsStation station = StationRegistry.createStation(
                        serverLevel, adjPos, adjState, dir);
                if (station == null) continue;

                if (registry.claimWithTiebreak(adjPos, this.nodeUUID)) {
                    this.activeStations.add(new StationBind(station, dir));
                }
            }
        } finally {
            isBinding = false;
        }
    }

    private boolean hasStationAt(BlockPos pos) {
        for (StationBind b : activeStations) {
            if (b.station().position().equals(pos)) return true;
        }
        return false;
    }

    @Override
    public void setLevel(@Nonnull Level level) {
        super.setLevel(level);
    }

    @Override
    public void setRemoved() {
        LogisticsNodeRegistry.unregister(this);
        super.setRemoved();
    }

    public void serverTick(ServerLevel level) {
        long now = level.getGameTime();
        if (now == lastTickGameTime) return;
        lastTickGameTime = now;

        if (!this.initialized) {
            this.initialized = true;
            LogisticsNodeRegistry.register(this);
            bindAdjacentStations();
        }

        pruneInvalidStations(level);

        if (activeStations.isEmpty() && (now % 20 == 0)) {
            bindAdjacentStations();
        }

        VaultStorage storage = resolveStorage(level);
        if (storage == null) return;

        long offset = Math.floorMod(this.worldPosition.hashCode(), 20L);

        for (StationBind b : activeStations) {
            if (!pullSides.contains(b.face())) continue;

            int interval = Math.max(1, b.station().tickIntervalTicks());
            if ((now + offset) % interval != 0) continue;
            try {
                b.station().tick(level, storage);
            } catch (Throwable t) {
                UniversalVault.LOGGER.warn("Station tick failed at {}: {}",
                        worldPosition, t.toString());
            }
        }
    }

    private void pruneInvalidStations(ServerLevel level) {
        if (activeStations.isEmpty()) return;

        VaultStorage storage = resolveStorage(level);
        WorkspaceRegistryData registry = WorkspaceRegistryData.get(level);

        java.util.Iterator<StationBind> it = activeStations.iterator();
        while (it.hasNext()) {
            StationBind b = it.next();
            BlockPos p = b.station().position();
            BlockState state = level.getBlockState(p);

            if (b.station().matches(level, p, state, b.face())) continue;

            if (storage != null) {
                try {
                    b.station().abortTask(level, storage);
                } catch (Throwable t) {
                    UniversalVault.LOGGER.warn(
                            "[node] abortTask failed at {}: {}", p, t.toString());
                }
            }
            registry.unbind(p);
            it.remove();

            UniversalVault.LOGGER.info(
                    "[node] {} unbound station at {} (block removed)",
                    worldPosition, p);
        }
    }

    private @Nullable VaultStorage resolveStorage(ServerLevel level) {
        if (!hasOwner()) return null;
        return VaultManager.getVault(level, ownerUUID);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("NodeUUID", UUIDUtil.CODEC, this.nodeUUID);
        output.store("OwnerUUID", UUIDUtil.CODEC, this.ownerUUID);

        List<String> sides = new ArrayList<>();
        for (Direction d : pullSides) sides.add(d.getName());
        output.store("PullSides", Codec.STRING.listOf(), sides);

        List<String> bindNames = new ArrayList<>();
        for (Direction d : bindSides) bindNames.add(d.getName());
        output.store("BindSides", Codec.STRING.listOf(), bindNames);

        List<String> overrideIds = new ArrayList<>(patternOverrides.size());
        List<Boolean> overrideVals = new ArrayList<>(patternOverrides.size());
        for (Map.Entry<UUID, Boolean> e : patternOverrides.entrySet()) {
            overrideIds.add(e.getKey().toString());
            overrideVals.add(e.getValue());
        }
        output.store("OverrideIds", Codec.STRING.listOf(), overrideIds);
        output.store("OverrideVals", Codec.BOOL.listOf(), overrideVals);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("NodeUUID", UUIDUtil.CODEC).ifPresent(u -> this.nodeUUID = u);
        input.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(u -> this.ownerUUID = u);

        input.read("PullSides", Codec.STRING.listOf()).ifPresent(list -> {
            Set<Direction> sides = EnumSet.noneOf(Direction.class);
            for (String s : list) {
                Direction d = Direction.byName(s);
                if (d != null) sides.add(d);
            }
            this.pullSides = sides;
        });

        input.read("BindSides", Codec.STRING.listOf()).ifPresent(list -> {
            Set<Direction> s = EnumSet.noneOf(Direction.class);
            for (String n : list) {
                Direction d = Direction.byName(n);
                if (d != null) s.add(d);
            }
            this.bindSides = s;
        });

        input.read("CustomPatterns", UUIDUtil.CODEC.listOf()).ifPresent(list -> {
            for (UUID id : list) patternOverrides.put(id, true);
        });

        var idsOpt = input.read("OverrideIds", Codec.STRING.listOf());
        var valsOpt = input.read("OverrideVals", Codec.BOOL.listOf());
        if (idsOpt.isPresent() && valsOpt.isPresent()) {
            List<String> ids = idsOpt.get();
            List<Boolean> vals = valsOpt.get();
            int n = Math.min(ids.size(), vals.size());
            for (int i = 0; i < n; i++) {
                try {
                    patternOverrides.put(UUID.fromString(ids.get(i)), vals.get(i));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void onBlockDestroyed() {
        if (!(this.level instanceof ServerLevel serverLevel)) return;

        if (hasOwner()) {
            VaultStorage storage = VaultManager.getVault(serverLevel, ownerUUID);
            if (storage != null) {
                for (StationBind b : activeStations) {
                    try {
                        b.station().forceRelease(serverLevel, storage);
                    } catch (Throwable t) {
                        UniversalVault.LOGGER.warn(
                                "[node] forceRelease failed at {}: {}",
                                b.station().position(), t.toString());
                    }
                }
            } else {
                UniversalVault.LOGGER.warn(
                        "[node] {} destroyed without resolvable vault ({})",
                        worldPosition, ownerUUID);
            }
        }

        WorkspaceRegistryData registry = WorkspaceRegistryData.get(serverLevel);
        for (StationBind b : activeStations) {
            registry.unbind(b.station().position());
        }

        int extra = registry.unbindAllForNode(this.nodeUUID);
        if (extra > 0) {
            UniversalVault.LOGGER.info(
                    "[node] {} released {} orphan claim(s)", worldPosition, extra);
        }

        activeStations.clear();
    }

    @Override
    public void preRemoveSideEffects(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        onBlockDestroyed();
        super.preRemoveSideEffects(pos, state);
    }

    public boolean acceptsRecipe(RecipeKind kind, UUID patternId) {
        Boolean override = patternOverrides.get(patternId);
        boolean enabled = override != null ? override : (kind == RecipeKind.CRAFTING);
        if (!enabled) return false;

        for (StationBind b : activeStations) {
            ILogisticsStation s = b.station();
            if (kind == RecipeKind.CRAFTING
                    && s.kind() == ILogisticsStation.Kind.CRAFTING) return true;
            if (kind == RecipeKind.CUSTOM
                    && s.kind() == ILogisticsStation.Kind.CUSTOM) return true;
        }
        return false;
    }

//    //? if neoforge {
//    /*@Override
//    public void onLoad() {
//        super.onLoad();
//        if (this.level instanceof ServerLevel) {
//            LogisticsNodeRegistry.register(this);
//            this.needsInitialBind = true;
//        }
//    }
//    *///?}
}