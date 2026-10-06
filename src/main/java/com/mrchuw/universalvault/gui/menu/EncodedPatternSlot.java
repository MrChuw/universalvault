package com.mrchuw.universalvault.gui.menu;

import com.mrchuw.universalvault.registry.ModRegistry;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nonnull;

public class EncodedPatternSlot extends Slot {

    public EncodedPatternSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
    }

    @Override
    public boolean mayPlace(@Nonnull ItemStack stack) {
        return !stack.isEmpty() && stack.has(ModRegistry.ENCODED_PATTERN_DATA.get());
    }

    @Override
    public int getMaxStackSize() { return 1; }
}
