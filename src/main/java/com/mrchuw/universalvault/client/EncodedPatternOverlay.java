package com.mrchuw.universalvault.client;

import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.gui.base.AbstractVaultScreen;
import com.mrchuw.universalvault.registry.ModRegistry;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}

public final class EncodedPatternOverlay {

    private EncodedPatternOverlay() {}

    //? if <26.1 {
    /*public static boolean renderForSlot(GuiGraphics g,
    *///?} else {
    public static boolean renderForSlot(GuiGraphicsExtractor g,
     //?}
                                        AbstractContainerScreen<?> screen,
                                        @Nullable Slot hoveredSlot,
                                        Slot slot) {
        if (!Minecraft.getInstance().hasShiftDown()) return false;
        if (!(slot.container instanceof Inventory)) return false;

        ItemStack stack = slot.getItem();
        if (stack.isEmpty()) return false;

        VaultPattern p = stack.get(ModRegistry.ENCODED_PATTERN_DATA.get());
        if (p == null) return false;

        ItemStack output = p.output().toStack(1);
        if (output.isEmpty()) return false;

        int sx = slot.x;
        int sy = slot.y;

        if (slot == hoveredSlot && slot.isHighlightable()) {
            g.fill(sx, sy, sx + 16, sy + 16, 0x80FFFFFF);
        }

        //? if <26.1 {
        /*g.renderItem(output, sx, sy);
        *///?} else {
        g.item(output, sx, sy);
         //?}

        int oc = p.outputCount();
        if (oc > 1) {
            //? if <26.1 {
            /*g.renderItemDecorations(Minecraft.getInstance().font, output, sx, sy,
                    String.valueOf(oc));
            *///?} else {
            g.itemDecorations(Minecraft.getInstance().font, output, sx, sy,
                    String.valueOf(oc));
            //?}
        }
        return true;
    }
}
