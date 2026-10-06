package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.automation.handler.ItemAutomationHandler;
import javax.annotation.Nullable;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class NeoforgeItemAutomationHandler implements ItemAutomationHandler {

    private final ResourceHandler<ItemResource> handler;

    public NeoforgeItemAutomationHandler(ResourceHandler<ItemResource> handler) {
        this.handler = handler;
    }

    @Override public int size() { return handler.size(); }

    @Override public ItemStack getStackInSlot(int slot) {
        if (slot < 0 || slot >= handler.size()) return ItemStack.EMPTY;
        ItemResource res = handler.getResource(slot);
        if (res.isEmpty()) return ItemStack.EMPTY;
        long amount = handler.getAmountAsLong(slot);
        if (amount <= 0) return ItemStack.EMPTY;
        return res.toStack((int) Math.min(amount, Integer.MAX_VALUE));
    }

    @Override public long getAmountInSlot(int slot) {
        if (slot < 0 || slot >= handler.size()) return 0;
        return handler.getAmountAsLong(slot);
    }

    @Override public long getCapacityForSlot(int slot, ItemStack probe) {
        if (slot < 0 || slot >= handler.size() || probe.isEmpty()) return 0;
        return handler.getCapacityAsLong(slot, ItemResource.of(probe));
    }

    @Override public long insertIntoSlot(int slot, ItemStack stack, boolean simulate) {
        if (slot < 0 || slot >= handler.size() || stack.isEmpty()) return 0;
        ItemResource res = ItemResource.of(stack);
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(slot, res, stack.getCount(), tx);
            if (!simulate && inserted > 0) tx.commit();
            return inserted;
        }
    }

    @Override public ItemStack extractFromSlot(int slot, long amount, boolean simulate) {
        if (slot < 0 || slot >= handler.size() || amount <= 0) return ItemStack.EMPTY;
        ItemResource res = handler.getResource(slot);
        if (res.isEmpty()) return ItemStack.EMPTY;
        int toExtract = (int) Math.min(amount, Integer.MAX_VALUE);
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(slot, res, toExtract, tx);
            if (extracted <= 0) return ItemStack.EMPTY;
            ItemStack out = res.toStack(extracted);
            if (!simulate) tx.commit();
            return out;
        }
    }
}
//?}
