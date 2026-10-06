package com.mrchuw.universalvault.storage;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.VaultConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class VaultStorage {

    public enum ExtractMode { CRAFT, BOOTSTRAP, PLAYER }

    private final Map<ItemKey, Long> items = new ConcurrentHashMap<>();

    private final Map<Identifier, Long> reserved = new ConcurrentHashMap<>();

    private final Map<Identifier, Set<ItemKey>> byItemId = new ConcurrentHashMap<>();

    private long totalItems = 0;

    private long version = 0;

    private Runnable changeListener;

    public void setChangeListener(Runnable listener) {
        this.changeListener = listener;
    }

    private void markDirty() {
        version++;
        if (changeListener != null) changeListener.run();
    }

    private static void debug(String msg, Object... args) {
        if (VaultConfig.get().debugLogging()) {
            UniversalVault.LOGGER.info("[vault] " + msg, args);
        }
    }

    public long insert(ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) return 0;
        ItemKey key = ItemKey.of(stack);
        if (key == null) return 0;

        int countToInsert = stack.getCount();

        int maxSlots = VaultConfig.get().maxSlots();
        boolean isNewKey = !items.containsKey(key);
        if (maxSlots > 0 && isNewKey && items.size() >= maxSlots) {
            debug("insert rejected: maxSlots ({}) reached", maxSlots);
            return 0;
        }

        long maxPerSlot = VaultConfig.get().maxPerSlot();
        if (maxPerSlot > 0) {
            long current = items.getOrDefault(key, 0L);
            long space = maxPerSlot - current;
            if (space <= 0) {
                debug("insert rejected: maxPerSlot ({}) reached for {}", maxPerSlot, key);
                return 0;
            }
            countToInsert = (int) Math.min(countToInsert, space);
        }

        // maxTotalItems: usa contador corrente, não soma map.values().
        long maxTotalItems = VaultConfig.get().maxTotalItems();
        if (maxTotalItems > 0) {
            long space = maxTotalItems - this.totalItems;
            if (space <= 0) {
                debug("insert rejected: maxTotalItems ({}) reached", maxTotalItems);
                return 0;
            }
            countToInsert = (int) Math.min(countToInsert, space);
        }

        if (countToInsert <= 0) return 0;
        if (simulate) return countToInsert;

        final int toInsert = countToInsert;
        items.merge(key, (long) toInsert, Long::sum);
        this.totalItems += toInsert;
        byItemId.computeIfAbsent(key.itemId(), k -> ConcurrentHashMap.newKeySet()).add(key);

        markDirty();
        debug("insert {} x{} (total {})", key, toInsert, items.get(key));
        return toInsert;
    }

    public ItemStack extract(ItemKey key, int maxAmount, ExtractMode mode, boolean simulate) {
        if (key == null || maxAmount <= 0) return ItemStack.EMPTY;

        long ceiling = switch (mode) {
            case CRAFT     -> getAvailable(key);
            case BOOTSTRAP -> items.getOrDefault(key, 0L);
            case PLAYER    -> items.getOrDefault(key, 0L);
        };

        if (ceiling <= 0) return ItemStack.EMPTY;

        int toTake = (int) Math.min(ceiling, maxAmount);

        if (simulate) {
            return key.toStack(toTake);
        }

        AtomicInteger extracted = new AtomicInteger(0);
        items.compute(key, (k, v) -> {
            if (v == null || v <= 0) { extracted.set(0); return null; }
            int n = (int) Math.min(v, toTake);
            extracted.set(n);
            long left = v - n;
            return left > 0 ? left : null;
        });

        int amount = extracted.get();
        if (amount > 0) {
            this.totalItems -= amount;

            if (!items.containsKey(key)) {
                Set<ItemKey> family = byItemId.get(key.itemId());
                if (family != null) {
                    family.remove(key);
                    if (family.isEmpty()) byItemId.remove(key.itemId());
                }
            }

            markDirty();
            debug("extract {} x{} (mode={})", key, amount, mode);
            return key.toStack(amount);
        }
        return ItemStack.EMPTY;
    }

    @Deprecated
    public ItemStack extract(ItemKey key, int maxAmount, boolean simulate) {
        return extract(key, maxAmount, ExtractMode.PLAYER, simulate);
    }

    public long getStock(ItemKey key) {
        return items.getOrDefault(key, 0L);
    }

    public long getAvailable(ItemKey key) {
        long stock = items.getOrDefault(key, 0L);
        long res = reserved.getOrDefault(key.itemId(), 0L);
        return Math.max(0L, stock - res);
    }

    @Deprecated
    public long getAmount(ItemKey key) {
        return getStock(key);
    }

    public long getStockFamily(Identifier itemId) {
        Set<ItemKey> family = byItemId.get(itemId);
        if (family == null) return 0L;
        long sum = 0;
        for (ItemKey k : family) sum += items.getOrDefault(k, 0L);
        return sum;
    }

    public long getAvailableConsideringDurability(Identifier itemId, double minDurabilityPct) {
        Set<ItemKey> family = byItemId.get(itemId);
        if (family == null) return 0L;

        double threshold = Math.max(0.0, Math.min(100.0, minDurabilityPct)) / 100.0;
        long usable = 0;

        for (ItemKey key : family) {
            long count = items.getOrDefault(key, 0L);
            if (count <= 0) continue;

            ItemStack sample = key.toStack(1);
            if (sample.isEmpty()) continue;

            int max = sample.getMaxDamage();
            if (max <= 0) {
                usable += count;
                continue;
            }

            int damage = sample.getDamageValue();
            double remaining = 1.0 - (double) damage / (double) max;
            if (remaining >= threshold) {
                usable += count;
            }
        }

        long res = reserved.getOrDefault(itemId, 0L);
        return Math.max(0L, usable - res);
    }

    public long getReserved(Identifier itemId) {
        return reserved.getOrDefault(itemId, 0L);
    }

    public long getTotalStock() {
        return this.totalItems;
    }

    public long getTotalReserved() {
        long sum = 0;
        for (long v : reserved.values()) sum += v;
        return sum;
    }

    public long getTotalAvailable() {
        return Math.max(0L, this.totalItems - getTotalReserved());
    }

    public Map<ItemKey, Long> getAllItems() {
        return Collections.unmodifiableMap(items);
    }

    public Map<Identifier, Long> getAllReserved() {
        return Collections.unmodifiableMap(reserved);
    }

    public long version() {
        return version;
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

    public long setReserved(Identifier itemId, long amount) {
        if (amount < 0) amount = 0;
        long old = reserved.getOrDefault(itemId, 0L);
        if (old == amount) return 0;

        if (amount == 0) reserved.remove(itemId);
        else reserved.put(itemId, amount);

        markDirty();
        UniversalVault.LOGGER.info(
                "reserva {} {} → {} (disponível agora {})",
                itemId, old, amount, getStockFamily(itemId) - amount
        );
        return amount - old;
    }

    public void loadFromMap(Map<ItemKey, Long> rawData) {
        loadFromMap(rawData, Map.of());
    }

    public void loadFromMap(Map<ItemKey, Long> rawData, Map<Identifier, Long> rawReserved) {
        items.clear();
        byItemId.clear();
        reserved.clear();
        long sum = 0;

        for (Map.Entry<ItemKey, Long> e : rawData.entrySet()) {
            if (e.getValue() == null || e.getValue() <= 0) continue;
            items.put(e.getKey(), e.getValue());
            byItemId
                    .computeIfAbsent(e.getKey().itemId(), k -> ConcurrentHashMap.newKeySet())
                    .add(e.getKey());
            sum += e.getValue();
        }
        this.totalItems = sum;

        for (Map.Entry<Identifier, Long> e : rawReserved.entrySet()) {
            if (e.getValue() != null && e.getValue() > 0) {
                reserved.put(e.getKey(), e.getValue());
            }
        }

        markDirty();
    }
}