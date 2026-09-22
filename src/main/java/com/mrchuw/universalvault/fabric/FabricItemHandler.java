package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class FabricItemHandler implements Storage<ItemVariant> {

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
        VaultStorage s = storage();
        if (s == null || !enabled()) {
            return List.<StorageView<ItemVariant>>of().iterator();
        }

        List<StorageView<ItemVariant>> views = new ArrayList<>();
        for (Map.Entry<ItemKey, Long> e : s.getAllItems().entrySet()) {
            ItemStack sample = e.getKey().toStack(1);
            if (sample.isEmpty()) continue;
            if (!blockEntity.matchesFilter(sample)) continue;
            views.add(new VaultView(e.getKey()));
        }
        return views.iterator();
    }

    private class VaultView implements StorageView<ItemVariant> {
        private final ItemKey key;

        VaultView(ItemKey key) {
            this.key = key;
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
}
*///?}