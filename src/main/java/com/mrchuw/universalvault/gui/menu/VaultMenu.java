package com.mrchuw.universalvault.gui.menu;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.network.S2CPatternsSyncPayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.pattern.VaultPatternsData;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import com.mrchuw.universalvault.Platform;

import java.util.*;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultMenu extends AbstractContainerMenu {

    public static final int PLAYER_INV_X = 9;
    public static final int PLAYER_INV_Y = 126;
    public static final int HOTBAR_X = 9;
    public static final int HOTBAR_Y = 184;
    public static final int TEXTURE_HEIGHT = 208;

    private static final int PLAYER_INV_START = 0;
    private static final int HOTBAR_END = 36;
    private final PatternEditContainer patternContainer = new PatternEditContainer();
    private EncodedPatternSlot patternSlot;

    private final Player player;
    private final Level level;
    private UUID ownerUUID;
    private static final Map<UUID, List<ServerPlayer>> VIEWERS = new HashMap<>();

    private static final Set<UUID> PENDING_SYNC = ConcurrentHashMap.newKeySet();
    private static int syncCooldown = 0;

    public VaultMenu(int containerId, Inventory playerInv, FriendlyByteBuf buf) {
        this(containerId, playerInv, buf.readUUID());
    }

    public VaultMenu(int containerId, Inventory playerInv, UUID ownerUUID) {
        super(ModRegistry.VAULT_MENU.get(), containerId);
        this.player = playerInv.player;
        this.level = playerInv.player.level();
        this.ownerUUID = ownerUUID;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                this.addSlot(new Slot(playerInv, index, 9 + col * 18, 126 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 9 + col * 18, 184));
        }

        this.patternSlot = new EncodedPatternSlot(this.patternContainer, 0, -10000, -10000);
        this.addSlot(this.patternSlot);

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
            VIEWERS.computeIfAbsent(ownerUUID,
                    k -> Collections.synchronizedList(new ArrayList<>())).add(sp);
            if (VaultConfig.get().debugLogging()) {
                UniversalVault.LOGGER.info("VaultMenu registered viewer {} for owner {}",
                        sp.getName().getString(), ownerUUID);
            }
        }
    }

    private void unregisterViewer() {
        if (player instanceof ServerPlayer sp && !player.level().isClientSide()) {
            removeViewer(ownerUUID, sp);
        }
    }

    private static void removeViewer(UUID ownerUUID, ServerPlayer viewer) {
        List<ServerPlayer> viewers = VIEWERS.get(ownerUUID);
        if (viewers != null) {
            viewers.remove(viewer);
            if (viewers.isEmpty()) VIEWERS.remove(ownerUUID);
        }
    }

    @Override
    public void addSlotListener(@Nonnull ContainerListener listener) {
        super.addSlotListener(listener);
        if (listener instanceof ServerPlayer sp) {
            VIEWERS.computeIfAbsent(ownerUUID, k -> new ArrayList<>()).add(sp);
        }
    }

    @Override
    public void removeSlotListener(@Nonnull ContainerListener listener) {
        super.removeSlotListener(listener);
        if (listener instanceof ServerPlayer sp) {
            removeViewer(ownerUUID, sp);
        }
    }

    public void syncVaultData(ServerPlayer serverPlayer) {
        VaultStorage storage = VaultManager.getVault(this.level, this.ownerUUID);
        if (storage == null) return;

        List<S2CVaultSyncPayload.Entry> entries = buildSyncEntries(storage);
        List<S2CPatternsSyncPayload.Entry> patterns = collectPatternEntries();
        Platform.INSTANCE.sendToPlayer(serverPlayer,
                new S2CVaultSyncPayload(entries, patterns));
    }

    private List<S2CPatternsSyncPayload.Entry> collectPatternEntries() {
        if (!(this.level instanceof ServerLevel sl)) return List.of();
        VaultPatternsData data = VaultPatternsData.get(sl);
        VaultStorage storage = VaultManager.getVault(sl, this.ownerUUID);

        List<VaultPattern> patterns = data.getPatterns(this.ownerUUID);
        List<S2CPatternsSyncPayload.Entry> out = new ArrayList<>(patterns.size());
        for (VaultPattern p : patterns) {
            long stock = storage != null ? storage.getStock(p.output()) : 0L;
            out.add(new S2CPatternsSyncPayload.Entry(p, stock));
        }
        return out;
    }

    private static List<S2CVaultSyncPayload.Entry> buildSyncEntries(VaultStorage storage) {
        List<S2CVaultSyncPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> entry : storage.getAllItems().entrySet()) {
            if (!entry.getKey().isResolved()) continue;
            entries.add(new S2CVaultSyncPayload.Entry(entry.getKey(), entry.getValue()));
        }
        return entries;
    }

    @Override
    public void removed(@Nonnull Player player) {
        super.removed(player);
        unregisterViewer();

        if (!patternContainer.isEmpty()) {
            ItemStack stack = patternContainer.removeItemNoUpdate(0);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                //? if <=26.2 {
                /*player.drop(stack, false);
                 *///?} else {
                player.drop(stack, false, net.minecraft.util.Prediction.PREDICTED);
                //?}
            }
        }
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return player.isAlive() && !player.isRemoved();
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(@Nonnull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        if (this.level.isClientSide()) return ItemStack.EMPTY;

        if (slot instanceof EncodedPatternSlot) {
            ItemStack stack = slot.getItem();
            ItemStack copy = stack.copy();
            if (player.getInventory().add(stack)) {
                slot.set(ItemStack.EMPTY);
                return copy;
            }
            return ItemStack.EMPTY;
        }

        if (index >= PLAYER_INV_START && index < HOTBAR_END) {
            ItemStack stack = slot.getItem();
            VaultStorage storage = VaultManager.getVault(this.level, this.ownerUUID);
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

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public static void broadcastVaultUpdate(UUID ownerUUID, ServerLevel level) {
        List<ServerPlayer> viewers = VIEWERS.get(ownerUUID);
        if (viewers == null || viewers.isEmpty()) return;

        VaultStorage storage = VaultManager.getVault(level, ownerUUID);
        if (storage == null) return;

        List<S2CVaultSyncPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> entry : storage.getAllItems().entrySet()) {
            entries.add(new S2CVaultSyncPayload.Entry(entry.getKey(), entry.getValue()));
        }

        VaultPatternsData data = VaultPatternsData.get(level);
        List<VaultPattern> patterns = data.getPatterns(ownerUUID);
        List<S2CPatternsSyncPayload.Entry> patternEntries = new ArrayList<>(patterns.size());
        for (VaultPattern p : patterns) {
            long stock = storage.getStock(p.output());
            patternEntries.add(new S2CPatternsSyncPayload.Entry(p, stock));
        }

        S2CVaultSyncPayload payload = new S2CVaultSyncPayload(entries, patternEntries);

        for (ServerPlayer player : viewers) {
            if (player.containerMenu instanceof VaultMenu menu
                    && menu.ownerUUID.equals(ownerUUID)
                    && player.isAlive()) {
                Platform.INSTANCE.sendToPlayer(player, payload);
            }
        }
    }

    public static void markPendingSync(UUID ownerUUID) {
        PENDING_SYNC.add(ownerUUID);
    }

    public static void processPendingSyncs(MinecraftServer server) {
        if (PENDING_SYNC.isEmpty()) return;
        int interval = VaultConfig.get().syncIntervalTicks();
        if (++syncCooldown < interval) return;
        syncCooldown = 0;

        ServerLevel overworld = server.overworld();
        for (UUID uuid : PENDING_SYNC) {
            broadcastVaultUpdate(uuid, overworld);
        }
        PENDING_SYNC.clear();
    }

    public @Nullable EncodedPatternSlot getPatternSlot() {
        return patternSlot;
    }

    public void repositionPatternSlot(int x, int y) {
        if (this.patternSlot != null) {
            this.slots.remove(this.patternSlot);
        }
        this.patternSlot = new EncodedPatternSlot(this.patternContainer, 0, x, y);
        this.addSlot(this.patternSlot);
    }
}
