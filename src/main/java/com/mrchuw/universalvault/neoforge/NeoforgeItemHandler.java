package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NeoforgeItemHandler implements ResourceHandler<ItemResource> {

    private static final Map<VaultStorage, SharedVaultJournal> JOURNALS = new WeakHashMap<>();

    private final VaultIOBlockEntity blockEntity;

    private List<ItemKey> cachedKeys = null;
    private Map<ItemKey, Long> cachedAmounts = null;
    private long lastSizeCheckTime = -1;

    public NeoforgeItemHandler(VaultIOBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private VaultStorage getStorage() {
        return blockEntity.getStorage();
    }

    private SharedVaultJournal getJournal() {
        VaultStorage storage = getStorage();
        if (storage == null) return null;
        synchronized (JOURNALS) {
            SharedVaultJournal journal = JOURNALS.computeIfAbsent(storage, SharedVaultJournal::new);
            journal.addListener(blockEntity);
            return journal;
        }
    }

    private boolean automationEnabled() {
        return VaultConfig.get().hopperInteraction();
    }

    private void refreshKeys() {
        VaultStorage storage = getStorage();
        if (storage == null) {
            cachedKeys = List.of();
            cachedAmounts = Map.of();
            return;
        }

        Map<ItemKey, Long> allItems = storage.getAllItems();
        List<ItemKey> all = new ArrayList<>(allItems.keySet());
        all.sort(Comparator
                .comparing((ItemKey k) -> k.itemId().toString())
                .thenComparing(k -> k.components().toString()));

        if (!blockEntity.hasAnyFilter()) {
            cachedKeys = all;
            cachedAmounts = new HashMap<>(allItems);
            return;
        }

        List<ItemKey> filtered = new ArrayList<>();
        Map<ItemKey, Long> filteredAmounts = new HashMap<>();
        for (ItemKey key : all) {
            ItemStack stack = key.toStack(1);
            if (!stack.isEmpty() && blockEntity.matchesFilter(stack)) {
                filtered.add(key);
                filteredAmounts.put(key, allItems.get(key));
            }
        }
        cachedKeys = filtered;
        cachedAmounts = filteredAmounts;
    }

    private List<ItemKey> getCurrentKeys() {
        if (cachedKeys == null) {
            refreshKeys();
        }
        return cachedKeys;
    }

    @Override
    public int size() {
        if (!automationEnabled()) return 0;
        long time = -1;
        if (blockEntity.getLevel() != null) {
            time = blockEntity.getLevel().getGameTime();
        }
        if (time == -1 || time != lastSizeCheckTime) {
            refreshKeys();
            lastSizeCheckTime = time;
        }

        return getCurrentKeys().size() + 1;
    }

    @Override
    public @Nonnull ItemResource getResource(int index) {
        if (!automationEnabled()) return ItemResource.EMPTY;
        List<ItemKey> keys = getCurrentKeys();
        if (index < 0 || index >= keys.size()) return ItemResource.EMPTY;
        ItemKey key = keys.get(index);
        if (key == null) return ItemResource.EMPTY;

        long amount = cachedAmounts != null ? cachedAmounts.getOrDefault(key, 0L) : 0L;
        if (amount <= 0) return ItemResource.EMPTY;

        return ItemResource.of(key.toStack(1));
    }

    @Override
    public long getAmountAsLong(int index) {
        if (!automationEnabled()) return 0;
        List<ItemKey> keys = getCurrentKeys();
        if (index < 0 || index >= keys.size()) return 0;
        ItemKey key = keys.get(index);
        if (key == null || cachedAmounts == null) return 0;
        return cachedAmounts.getOrDefault(key, 0L);
    }

    @Override
    public long getCapacityAsLong(int index, @Nonnull ItemResource resource) {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, @Nullable ItemResource resource) {
        if (!automationEnabled()) return false;
        if (resource == null || resource.isEmpty()) return false;
        ItemStack sample = resource.toStack(1);
        if (sample.isEmpty()) return false;
        if (!blockEntity.matchesFilter(sample)) return false;

        ItemKey requestedKey = ItemKey.of(sample);
        if (requestedKey == null) return false;

        List<ItemKey> keys = getCurrentKeys();
        if (index >= 0 && index < keys.size()) {
            return requestedKey.equals(keys.get(index));
        }
        if (index == keys.size()) {
            return !keys.contains(requestedKey);
        }
        return false;
    }

    @Override
    public int insert(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        if (!automationEnabled()) return 0;
        if (!isValid(index, resource)) return 0;
        return insert(resource, amount, transaction);
    }

    @Override
    public int insert(@Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        if (!automationEnabled()) return 0;
        VaultStorage storage = getStorage();
        if (storage == null || resource.isEmpty() || amount <= 0) return 0;

        ItemStack sample = resource.toStack(1);
        if (!blockEntity.matchesFilter(sample)) return 0;

        ItemStack stack = resource.toStack(amount);
        if (stack.isEmpty()) return 0;

        SharedVaultJournal journal = getJournal();
        if (journal != null) {
            journal.updateSnapshots(transaction);
        }

        long inserted = storage.insert(stack, false);
        return (int) inserted;
    }

    @Override
    public int extract(int index, @Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        ItemStack sample = matchingSample(resource, amount);
        if (sample == null) return 0;

        List<ItemKey> keys = getCurrentKeys();
        if (index < 0 || index >= keys.size()) return 0;

        ItemKey slotKey = keys.get(index);
        if (slotKey == null) return 0;

        ItemKey requestedKey = ItemKey.of(sample);
        if (!slotKey.equals(requestedKey)) return 0;

        VaultStorage storage = getStorage();
        if (storage == null) return 0;

        return extractFromStorage(storage, slotKey, amount, transaction);
    }

    @Override
    public int extract(@Nonnull ItemResource resource, int amount, @Nonnull TransactionContext transaction) {
        ItemStack sample = matchingSample(resource, amount);
        if (sample == null) return 0;

        VaultStorage storage = getStorage();
        if (storage == null) return 0;

        ItemKey requestedKey = ItemKey.of(sample);
        if (requestedKey == null) return 0;

        return extractFromStorage(storage, requestedKey, amount, transaction);
    }

    private @Nullable ItemStack matchingSample(ItemResource resource, int amount) {
        if (!automationEnabled()) return null;
        if (resource.isEmpty() || amount <= 0) return null;
        ItemStack sample = resource.toStack(1);
        if (sample.isEmpty()) return null;
        if (!blockEntity.matchesFilter(sample)) return null;
        return sample;
    }

    private int extractFromStorage(VaultStorage storage, ItemKey key, int amount, TransactionContext transaction) {
        long current = storage.getAmount(key);
        if (current <= 0) return 0;

        int toExtract = (int) Math.min(amount, current);

        SharedVaultJournal journal = getJournal();
        if (journal != null) {
            journal.updateSnapshots(transaction);
        }

        ItemStack extracted = storage.extract(key, toExtract, false);
        return extracted.isEmpty() ? 0 : extracted.getCount();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof NeoforgeItemHandler other)) return false;

        if (!this.blockEntity.getTargetVaultUUID().equals(other.blockEntity.getTargetVaultUUID())) {
            return false;
        }

        for (int i = 0; i < VaultIOBlockEntity.FILTER_SLOTS; i++) {
            ItemStack thisFilter = this.blockEntity.getFilter(i);
            ItemStack otherFilter = other.blockEntity.getFilter(i);

            if (thisFilter.isEmpty() != otherFilter.isEmpty()) return false;
            if (!thisFilter.isEmpty() && !ItemStack.isSameItemSameComponents(thisFilter, otherFilter)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.blockEntity.getTargetVaultUUID().hashCode();
    }

    private static class SharedVaultJournal extends SnapshotJournal<Map<ItemKey, Long>> {
        private final VaultStorage storage;
        private final List<VaultIOBlockEntity> listeners = new ArrayList<>();

        public SharedVaultJournal(VaultStorage storage) {
            this.storage = storage;
        }

        public void addListener(VaultIOBlockEntity be) {
            if (!listeners.contains(be)) {
                listeners.add(be);
            }
        }

        @Override
        protected Map<ItemKey, Long> createSnapshot() {
            return new HashMap<>(storage.getAllItems());
        }

        @Override
        protected void revertToSnapshot(Map<ItemKey, Long> snapshot) {
            storage.loadFromMap(snapshot);
        }

        @Override
        protected void onRootCommit(Map<ItemKey, Long> originalState) {
            for (VaultIOBlockEntity be : listeners) {
                be.setChanged();
            }
        }
    }
}
//?}