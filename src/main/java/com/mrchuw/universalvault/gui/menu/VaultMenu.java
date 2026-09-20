package com.mrchuw.universalvault.gui.menu;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.NonNull;

public class VaultMenu extends AbstractContainerMenu {

    public static final int PLAYER_INV_X = 9;
    public static final int PLAYER_INV_Y = 126;
    public static final int HOTBAR_X = 9;
    public static final int HOTBAR_Y = 184;
    public static final int TEXTURE_HEIGHT = 208;

    private static final int PLAYER_INV_START = 0;
    private static final int PLAYER_INV_END = 27;
    private static final int HOTBAR_START = 27;
    private static final int HOTBAR_END = 36;

    private final Player player;
    private final Level level;
    private final UUID targetVaultUUID;
    private static final Map<UUID, List<ServerPlayer>> VIEWERS = new HashMap<>();

    /** Vaults awaiting a sync when syncOnEveryChange=false. */
    private static final Set<UUID> PENDING_SYNC = ConcurrentHashMap.newKeySet();
    private static int syncCooldown = 0;

    public VaultMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readUUID());
    }

    public VaultMenu(int containerId, Inventory playerInv, UUID targetVaultUUID) {
        super(ModRegistry.VAULT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.level = playerInv.player.level();
        this.targetVaultUUID = targetVaultUUID;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                this.addSlot(new Slot(playerInv, index, 9 + col * 18, 126 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 9 + col * 18, 184));
        }

        registerViewer();
    }

    public void repositionPlayerSlots(int slotX, int invTopY, int hotbarY) {
        Inventory playerInv = this.player.getInventory();
        this.slots.clear();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                this.addSlot(new Slot(playerInv, index, slotX + col * 18, invTopY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, slotX + col * 18, hotbarY));
        }
    }

    private void registerViewer() {
        if (player instanceof ServerPlayer sp && !player.level().isClientSide()) {
            VIEWERS.computeIfAbsent(targetVaultUUID, k -> Collections.synchronizedList(new ArrayList<>())).add(sp);
            if (UniversalVaultConfig.CONFIG.debugLogging.get()) {
                UniversalVault.LOGGER.info("VaultMenu registered viewer {} for vault {}",
                        sp.getName().getString(), targetVaultUUID);
            }
        }
    }

    private void unregisterViewer() {
        if (player instanceof ServerPlayer sp && !player.level().isClientSide()) {
            removeViewer(targetVaultUUID, sp);
        }
    }

    private static void removeViewer(UUID vaultUUID, ServerPlayer viewer) {
        List<ServerPlayer> viewers = VIEWERS.get(vaultUUID);
        if (viewers != null) {
            viewers.remove(viewer);
            if (viewers.isEmpty()) VIEWERS.remove(vaultUUID);
        }
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        if (listener instanceof ServerPlayer sp) {
            VIEWERS.computeIfAbsent(targetVaultUUID, k -> new ArrayList<>()).add(sp);
        }
    }

    @Override
    public void removeSlotListener(ContainerListener listener) {
        super.removeSlotListener(listener);
        if (listener instanceof ServerPlayer sp) {
            removeViewer(targetVaultUUID, sp);
        }
    }

    public void syncVaultData(ServerPlayer serverPlayer) {
        VaultStorage storage = VaultManager.getVault(this.level, this.targetVaultUUID);
        if (storage == null) return;

        List<S2CVaultSyncPayload.Entry> entries = buildSyncEntries(storage, true);
        PacketDistributor.sendToPlayer(serverPlayer, new S2CVaultSyncPayload(entries));
    }

    private static List<S2CVaultSyncPayload.Entry> buildSyncEntries(VaultStorage storage, boolean resolvedOnly) {
        List<S2CVaultSyncPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> entry : storage.getAllItems().entrySet()) {
            if (resolvedOnly && !entry.getKey().isResolved()) continue;
            entries.add(new S2CVaultSyncPayload.Entry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        unregisterViewer();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return player.isAlive() && !player.isRemoved();
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        if (this.level.isClientSide()) return ItemStack.EMPTY;

        if (index >= PLAYER_INV_START && index < HOTBAR_END) {
            ItemStack stack = slot.getItem();
            VaultStorage storage = VaultManager.getVault(this.level, this.targetVaultUUID);
            if (storage != null) {
                long inserted = storage.insert(stack, false);
                if (inserted > 0) {
                    ItemStack copy = stack.copy();
                    stack.shrink((int) inserted);
                    if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
                    else slot.setChanged();
                    return copy;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    public UUID getTargetVaultUUID() {
        return this.targetVaultUUID;
    }

    public static void broadcastVaultUpdate(UUID vaultUUID, ServerLevel level) {
        List<ServerPlayer> viewers = VIEWERS.get(vaultUUID);
        if (viewers == null || viewers.isEmpty()) return;

        VaultStorage storage = VaultManager.getVault(level, vaultUUID);
        if (storage == null) return;

        List<S2CVaultSyncPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> entry : storage.getAllItems().entrySet()) {
            entries.add(new S2CVaultSyncPayload.Entry(entry.getKey(), entry.getValue()));
        }
        S2CVaultSyncPayload payload = new S2CVaultSyncPayload(entries);

        for (ServerPlayer player : viewers) {
            if (player.containerMenu instanceof VaultMenu menu
                    && menu.targetVaultUUID.equals(vaultUUID)
                    && player.isAlive()) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    // -----------------------------------------------------------------
    // Batched sync (used when syncOnEveryChange=false)
    // -----------------------------------------------------------------

    public static void markPendingSync(UUID vaultUUID) {
        PENDING_SYNC.add(vaultUUID);
    }

    public static void processPendingSyncs(MinecraftServer server) {
        if (PENDING_SYNC.isEmpty()) return;
        int interval = UniversalVaultConfig.CONFIG.syncIntervalTicks.get();
        if (++syncCooldown < interval) return;
        syncCooldown = 0;

        ServerLevel overworld = server.overworld();
        for (UUID uuid : PENDING_SYNC) {
            broadcastVaultUpdate(uuid, overworld);
        }
        PENDING_SYNC.clear();
    }
}