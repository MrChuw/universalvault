package com.mrchuw.universalvault.neoforge;

//? neoforge {
/*import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NeoforgeItemHandler implements ResourceHandler<ItemResource> {

    private final VaultIOBlockEntity blockEntity;
    private final VaultJournal journal = new VaultJournal();

    public NeoforgeItemHandler(VaultIOBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    private VaultStorage getStorage() {
        return blockEntity.getStorage();
    }

    private boolean automationEnabled() {
        return VaultConfig.get().hopperInteraction();
    }

    private List<ItemKey> getCurrentKeys() {
        VaultStorage storage = getStorage();
        if (storage == null) return List.of();

        List<ItemKey> all = new ArrayList<>(storage.getAllItems().keySet());
        if (!blockEntity.hasAnyFilter()) return all;

        List<ItemKey> filtered = new ArrayList<>();
        for (ItemKey key : all) {
            ItemStack stack = key.toStack(1);
            if (stack.isEmpty()) continue;
            if (blockEntity.matchesFilter(stack)) filtered.add(key);
        }
        return filtered;
    }

    @Override
    public int size() {
        if (!automationEnabled()) return 0;
        return getCurrentKeys().size() + 1;
    }

    @Override
    public @Nonnull ItemResource getResource(int index) {
        if (!automationEnabled()) return ItemResource.EMPTY;
        List<ItemKey> keys = getCurrentKeys();
        if (index < 0 || index >= keys.size()) return ItemResource.EMPTY;
        ItemKey key = keys.get(index);
        if (key == null) return ItemResource.EMPTY;
        VaultStorage storage = getStorage();
        if (storage == null) return ItemResource.EMPTY;
        long amount = storage.getAmount(key);
        if (amount <= 0) return ItemResource.EMPTY;
        return ItemResource.of(key.toStack(1));
    }

    @Override
    public long getAmountAsLong(int index) {
        if (!automationEnabled()) return 0;
        List<ItemKey> keys = getCurrentKeys();
        if (index < 0 || index >= keys.size()) return 0;
        ItemKey key = keys.get(index);
        if (key == null) return 0;
        VaultStorage storage = getStorage();
        if (storage == null) return 0;
        return storage.getAmount(key);
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

        journal.updateSnapshots(transaction);

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
        journal.updateSnapshots(transaction);

        ItemStack extracted = storage.extract(key, toExtract, false);
        return extracted.isEmpty() ? 0 : extracted.getCount();
    }

    private class VaultJournal extends SnapshotJournal<Map<ItemKey, Long>> {
        @Override
        protected Map<ItemKey, Long> createSnapshot() {
            VaultStorage storage = getStorage();
            if (storage == null) return new HashMap<>();
            return new HashMap<>(storage.getAllItems());
        }

        @Override
        protected void revertToSnapshot(Map<ItemKey, Long> snapshot) {
            VaultStorage storage = getStorage();
            if (storage != null) storage.loadFromMap(snapshot);
        }

        @Override
        protected void onRootCommit(Map<ItemKey, Long> originalState) {
            blockEntity.setChanged();
        }
    }
}
*///?}