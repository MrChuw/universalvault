package com.mrchuw.universalvault.storage;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.item.ItemStack;

public class VaultStorage {

    private final Map<ItemKey, Long> items = new ConcurrentHashMap<>();
    private Runnable changeListener;

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void markDirty() {
        if (changeListener != null) changeListener.run();
    }

    private static void debug(String msg, Object... args) {
        if (UniversalVaultConfig.CONFIG.debugLogging.get()) {
            UniversalVault.LOGGER.info("[vault] " + msg, args);
        }
    }

    public long insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        ItemKey key = ItemKey.of(stack);
        if (key == null) return 0;

        int countToInsert = stack.getCount();

        // Limit: distinct item types
        int maxSlots = UniversalVaultConfig.CONFIG.maxSlots.get();
        if (maxSlots > 0 && !items.containsKey(key) && items.size() >= maxSlots) {
            debug("insert rejected: maxSlots ({}) reached", maxSlots);
            return 0;
        }

        // Limit: per item type
        long maxPerSlot = UniversalVaultConfig.CONFIG.maxPerSlot.get();
        if (maxPerSlot > 0) {
            long current = items.getOrDefault(key, 0L);
            long space = maxPerSlot - current;
            if (space <= 0) {
                debug("insert rejected: maxPerSlot ({}) reached for {}", maxPerSlot, key);
                return 0;
            }
            countToInsert = (int) Math.min(countToInsert, space);
        }

        // Limit: total items
        long maxTotalItems = UniversalVaultConfig.CONFIG.maxTotalItems.get();
        if (maxTotalItems > 0) {
            long total = 0;
            for (long v : items.values()) total += v;
            long space = maxTotalItems - total;
            if (space <= 0) {
                debug("insert rejected: maxTotalItems ({}) reached", maxTotalItems);
                return 0;
            }
            countToInsert = (int) Math.min(countToInsert, space);
        }

        if (countToInsert <= 0) return 0;
        if (simulate) return countToInsert;

        items.merge(key, (long) countToInsert, Long::sum);
        markDirty();
        debug("insert {} x{} (total {})", key, countToInsert, items.get(key));
        return countToInsert;
    }

    public ItemStack extract(ItemKey key, int maxAmount, boolean simulate) {
        if (key == null || maxAmount <= 0) return ItemStack.EMPTY;

        if (simulate) {
            Long current = items.get(key);
            if (current == null || current <= 0) return ItemStack.EMPTY;
            return key.toStack((int) Math.min(current, maxAmount));
        }

        AtomicInteger extracted = new AtomicInteger(0);
        items.compute(key, (k, v) -> {
            if (v == null || v <= 0) { extracted.set(0); return null; }
            int toExtract = (int) Math.min(v, maxAmount);
            extracted.set(toExtract);
            long left = v - toExtract;
            return left > 0 ? left : null;
        });

        int amount = extracted.get();
        if (amount > 0) {
            markDirty();
            debug("extract {} x{}", key, amount);
            return key.toStack(amount);
        }
        return ItemStack.EMPTY;
    }

    public long getAmount(ItemKey key) {
        return items.getOrDefault(key, 0L);
    }

    public Map<ItemKey, Long> getAllItems() {
        return Collections.unmodifiableMap(items);
    }

    public void loadFromMap(Map<ItemKey, Long> rawData) {
        items.clear();
        items.putAll(rawData);
    }

    public boolean hasOrphans() {
        for (ItemKey key : items.keySet()) {
            if (!key.isResolved()) return true;
        }
        return false;
    }

    public Map<ItemKey, Long> getResolvedItems() {
        Map<ItemKey, Long> resolved = new HashMap<>();
        for (Map.Entry<ItemKey, Long> e : items.entrySet()) {
            if (e.getKey().isResolved()) resolved.put(e.getKey(), e.getValue());
        }
        return resolved;
    }
}