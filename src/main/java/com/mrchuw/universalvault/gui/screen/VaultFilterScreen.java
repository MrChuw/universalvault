package com.mrchuw.universalvault.gui.screen;

import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
 //?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import javax.annotation.Nonnull;

public class VaultFilterScreen extends AbstractContainerScreen<VaultFilterMenu> {

    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;

    private static final int COLOR_BG           = 0xFFC6C6C6;
    private static final int COLOR_BORDER_DARK  = 0xFF373737;
    private static final int COLOR_BORDER_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_SLOT_BG      = 0xFF8B8B8B;
    private static final int COLOR_SLOT_SHADOW  = 0xFF373737;
    private static final int COLOR_SLOT_LIGHT   = 0xFFFFFFFF;

    public VaultFilterScreen(VaultFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

        // Label positions (used by the superclass's rendering)
        this.titleLabelX = 7;
        this.titleLabelY = 6;
        this.inventoryLabelX = 7;
        this.inventoryLabelY = VaultFilterMenu.PLAYER_INV_Y - 12;
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
    //?} else {
    /*@Override
    protected void renderBg(@Nonnull GuiGraphics g, float partialTick, int mouseX, int mouseY) {
    *///?}
        int x0 = this.leftPos, y0 = this.topPos;
        int x1 = x0 + WIDTH, y1 = y0 + HEIGHT;

        g.fill(x0, y0, x1, y1, COLOR_BG);
        g.fill(x0, y0, x1, y0 + 1, COLOR_BORDER_LIGHT);
        g.fill(x0, y0, x0 + 1, y1, COLOR_BORDER_LIGHT);
        g.fill(x0, y1 - 1, x1, y1, COLOR_BORDER_DARK);
        g.fill(x1 - 1, y0, x1, y1, COLOR_BORDER_DARK);

        for (Slot s : this.menu.slots) {
            drawSlot(g, x0 + s.x - 1, y0 + s.y - 1);
        }
    }

    //? if >=26.1 {
    private void drawSlot(GuiGraphicsExtractor g, int sx, int sy) {
    //?} else {
    /*private void drawSlot(GuiGraphics g, int sx, int sy) {
     *///?}
        g.fill(sx,     sy,     sx + 18, sy + 1,  COLOR_SLOT_SHADOW);
        g.fill(sx,     sy,     sx + 1,  sy + 18, COLOR_SLOT_SHADOW);
        g.fill(sx,     sy + 17, sx + 18, sy + 18, COLOR_SLOT_LIGHT);
        g.fill(sx + 17, sy,     sx + 18, sy + 18, COLOR_SLOT_LIGHT);
        g.fill(sx + 1, sy + 1, sx + 17, sy + 17, COLOR_SLOT_BG);
    }

    //? if >=26.1 {
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY) {
        super.extractLabels(g, mouseX, mouseY);
    }
    //?} else {
    /*@Override
    protected void renderLabels(@Nonnull GuiGraphics g, int mouseX, int mouseY) {
        super.renderLabels(g, mouseX, mouseY);
    }
    *///?}
}
