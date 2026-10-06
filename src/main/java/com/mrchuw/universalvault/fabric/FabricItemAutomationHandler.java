package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.automation.handler.ItemAutomationHandler;
import javax.annotation.Nullable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

public class FabricItemAutomationHandler implements ItemAutomationHandler {

    private final @Nullable SlottedStorage<ItemVariant> slotted;

    public FabricItemAutomationHandler(Storage<ItemVariant> storage) {
        this.slotted = (storage instanceof SlottedStorage<ItemVariant> s) ? s : null;
    }

    @Override public int size() {
        return slotted == null ? 0 : slotted.getSlotCount();
    }

    @Override public ItemStack getStackInSlot(int slot) {
        SingleSlotStorage<ItemVariant> s = slot(slot);
        if (s == null || s.isResourceBlank() || s.getAmount() <= 0) return ItemStack.EMPTY;
        int n = (int) Math.min(s.getAmount(), Integer.MAX_VALUE);
        return s.getResource().toStack(n);
    }

    @Override public long getAmountInSlot(int slot) {
        SingleSlotStorage<ItemVariant> s = slot(slot);
        return s == null ? 0 : s.getAmount();
    }

    @Override public long getCapacityForSlot(int slot, ItemStack probe) {
        SingleSlotStorage<ItemVariant> s = slot(slot);
        return s == null ? 0 : s.getCapacity();
    }

    @Override public long insertIntoSlot(int slot, ItemStack stack, boolean simulate) {
        SingleSlotStorage<ItemVariant> s = slot(slot);
        if (s == null || stack.isEmpty()) return 0;
        ItemVariant variant = ItemVariant.of(stack);
        try (Transaction tx = Transaction.openOuter()) {
            long inserted = s.insert(variant, stack.getCount(), tx);
            if (!simulate && inserted > 0) tx.commit();
            return inserted;
        }
    }

    @Override public ItemStack extractFromSlot(int slot, long amount, boolean simulate) {
        SingleSlotStorage<ItemVariant> s = slot(slot);
        if (s == null || amount <= 0 || s.isResourceBlank()) return ItemStack.EMPTY;
        ItemVariant variant = s.getResource();
        try (Transaction tx = Transaction.openOuter()) {
            long extracted = s.extract(variant, amount, tx);
            if (extracted <= 0) return ItemStack.EMPTY;
            ItemStack out = variant.toStack((int) Math.min(extracted, Integer.MAX_VALUE));
            if (!simulate) tx.commit();
            return out;
        }
    }

    private @Nullable SingleSlotStorage<ItemVariant> slot(int slot) {
        if (slotted == null || slot < 0 || slot >= slotted.getSlotCount()) return null;
        return slotted.getSlot(slot);
    }
}
*///?}
