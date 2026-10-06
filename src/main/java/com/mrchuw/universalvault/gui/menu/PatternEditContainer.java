package com.mrchuw.universalvault.gui.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public class PatternEditContainer implements Container {

    private ItemStack stack = ItemStack.EMPTY;
    private Runnable onChanged = () -> {};

    public void setOnChanged(Runnable r) {
        this.onChanged = r != null ? r : () -> {};
    }

    @Override public int getContainerSize() { return 1; }
    @Override public boolean isEmpty() { return stack.isEmpty(); }

    @Override public @Nonnull ItemStack getItem(int slot) {
        return slot == 0 ? stack : ItemStack.EMPTY;
    }

    @Override public @Nonnull ItemStack removeItem(int slot, int amount) {
        if (slot != 0 || stack.isEmpty() || amount <= 0) return ItemStack.EMPTY;
        int take = Math.min(amount, stack.getCount());
        ItemStack out = stack.copyWithCount(take);
        stack.shrink(take);
        if (stack.isEmpty()) stack = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    @Override public @Nonnull ItemStack removeItemNoUpdate(int slot) {
        if (slot != 0) return ItemStack.EMPTY;
        ItemStack out = stack;
        stack = ItemStack.EMPTY;
        return out;
    }

    @Override public void setItem(int slot, @Nonnull ItemStack s) {
        if (slot != 0) return;
        this.stack = s.copy();
        setChanged();
    }

    @Override public void setChanged() { onChanged.run(); }
    @Override public boolean stillValid(@Nonnull Player player) { return true; }
    @Override public void clearContent() { stack = ItemStack.EMPTY; setChanged(); }
}
