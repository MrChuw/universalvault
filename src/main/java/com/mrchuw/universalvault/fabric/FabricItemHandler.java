package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class FabricItemHandler implements SlottedStorage<ItemVariant> {

    private final VaultIOBlockEntity blockEntity;
    private final Map<TransactionContext, Map<ItemKey, Long>> snapshots = new WeakHashMap<>();

    public FabricItemHandler(VaultIOBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private VaultStorage storage() {
        return blockEntity.getStorage();
    }

    private boolean enabled() {
        return VaultConfig.get().hopperInteraction();
    }

    private boolean validFor(VaultStorage storage, ItemVariant resource) {
        if (!enabled() || resource.isBlank()) return false;
        ItemStack sample = resource.toStack(1);
        if (sample.isEmpty()) return false;
        return blockEntity.matchesFilter(sample);
    }

    private void captureSnapshot(TransactionContext transaction) {
        snapshots.computeIfAbsent(transaction, tx -> {
            VaultStorage s = storage();
            Map<ItemKey, Long> snap = s == null
                    ? new HashMap<>()
                    : new HashMap<>(s.getAllItems());

            tx.addCloseCallback((ctx, result) -> {
                if (result.wasCommitted()) {
                    blockEntity.setChanged();
                } else {
                    VaultStorage s2 = storage();
                    if (s2 != null) s2.loadFromMap(snap);
                }
                snapshots.remove(ctx);
            });
            return snap;
        });
    }

    // -----------------------------------------------------------------
    // SlottedStorage
    // -----------------------------------------------------------------

    @Override
    public int getSlotCount() {
        return getSlots().size();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        List<SingleSlotStorage<ItemVariant>> slots = getSlots();
        if (slot < 0 || slot >= slots.size()) {
            throw new IndexOutOfBoundsException("Slot " + slot + " out of range");
        }
        return slots.get(slot);
    }

    @Override
    public List<SingleSlotStorage<ItemVariant>> getSlots() {
        VaultStorage s = storage();
        if (s == null || !enabled()) return List.of();

        List<SingleSlotStorage<ItemVariant>> views = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> e : s.getAllItems().entrySet()) {
            ItemStack sample = e.getKey().toStack(1);
            if (sample.isEmpty()) continue;
            if (!blockEntity.matchesFilter(sample)) continue;
            views.add(new VaultView(e.getKey()));
        }

        views.add(new EmptySlotView());
        return views;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
        return new ArrayList<StorageView<ItemVariant>>(getSlots()).iterator();
    }


    // -----------------------------------------------------------------
    // Storage (insert / extract / iterator)
    // -----------------------------------------------------------------

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        VaultStorage s = storage();
        if (s == null || maxAmount <= 0 || !validFor(s, resource)) return 0;

        int amount = (int) Math.min(maxAmount, Integer.MAX_VALUE);
        ItemStack stack = resource.toStack(amount);
        if (stack.isEmpty()) return 0;

        captureSnapshot(transaction);
        return s.insert(stack, false);
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        VaultStorage s = storage();
        if (s == null || maxAmount <= 0 || !validFor(s, resource)) return 0;

        ItemKey key = ItemKey.of(resource.toStack(1));
        if (key == null) return 0;

        long current = s.getAmount(key);
        if (current <= 0) return 0;

        int toExtract = (int) Math.min(Math.min(maxAmount, current), Integer.MAX_VALUE);
        captureSnapshot(transaction);

        ItemStack extracted = s.extract(key, toExtract, false);
        return extracted.isEmpty() ? 0 : extracted.getCount();
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new ArrayList<StorageView<ItemVariant>>(getSlots()).iterator();
    }

    // -----------------------------------------------------------------
    // VaultView
    // -----------------------------------------------------------------

    private class VaultView implements SingleSlotStorage<ItemVariant> {
        private final ItemKey key;

        VaultView(ItemKey key) {
            this.key = key;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!resource.matches(key.toStack(1))) return 0;
            return FabricItemHandler.this.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (!resource.matches(key.toStack(1))) return 0;
            return FabricItemHandler.this.extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return false;
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(key.toStack(1));
        }

        @Override
        public long getAmount() {
            VaultStorage s = storage();
            return s == null ? 0 : s.getAmount(key);
        }

        @Override
        public long getCapacity() {
            return Long.MAX_VALUE;
        }
    }

    private class EmptySlotView implements SingleSlotStorage<ItemVariant> {
        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return FabricItemHandler.this.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return true;
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.blank();
        }

        @Override
        public long getAmount() {
            return 0;
        }

        @Override
        public long getCapacity() {
            return Long.MAX_VALUE;
        }
    }

}
*///?}