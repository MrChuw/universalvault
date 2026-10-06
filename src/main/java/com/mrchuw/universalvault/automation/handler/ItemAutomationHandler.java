package com.mrchuw.universalvault.automation.handler;

import net.minecraft.world.item.ItemStack;


public interface ItemAutomationHandler {

    int size();

    ItemStack getStackInSlot(int slot);

    long getAmountInSlot(int slot);

    default long getCapacityForSlot(int slot, ItemStack probe) {
        return Long.MAX_VALUE;
    }

    long insertIntoSlot(int slot, ItemStack stack, boolean simulate);

    ItemStack extractFromSlot(int slot, long amount, boolean simulate);
}
