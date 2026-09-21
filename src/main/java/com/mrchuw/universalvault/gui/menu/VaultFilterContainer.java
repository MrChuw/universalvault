package com.mrchuw.universalvault.gui.menu;

import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultFilterContainer implements Container {

    private final VaultIOBlockEntity blockEntity;

    public VaultFilterContainer(VaultIOBlockEntity be) {
        this.blockEntity = be;
    }

    @Override public int getContainerSize() { return VaultIOBlockEntity.FILTER_SLOTS; }

    @Override public boolean isEmpty() {
        for (int i = 0; i < getContainerSize(); i++) if (!getItem(i).isEmpty()) return false;
        return true;
    }

    @Override public @Nonnull ItemStack getItem(int slot) { return blockEntity.getFilter(slot); }

    @Override public @Nonnull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = getItem(slot);
        if (removed.isEmpty()) return ItemStack.EMPTY;
        blockEntity.setFilter(slot, ItemStack.EMPTY);
        return removed.copyWithCount(1);
    }

    @Override public @Nonnull ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = getItem(slot);
        blockEntity.setFilter(slot, ItemStack.EMPTY);
        return removed;
    }

    @Override public void setItem(int slot, @Nonnull ItemStack stack) {
        blockEntity.setFilter(slot, stack);
    }

    @Override public void setChanged() { blockEntity.setChanged(); }
    @Override public boolean stillValid( @Nonnull Player player) { return true; }

    @Override public void clearContent() {
        for (int i = 0; i < getContainerSize(); i++) blockEntity.setFilter(i, ItemStack.EMPTY);
    }
}
