package com.mrchuw.universalvault.gui.screen;

import com.mrchuw.universalvault.gui.VaultStyle;
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

public class VaultFilterScreen extends AbstractContainerScreen<VaultFilterMenu> implements VaultStyle {

    private static final int WIDTH  = 176;
    private static final int HEIGHT = 166;

    public VaultFilterScreen(VaultFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - WIDTH) / 2;
        this.topPos = (this.height - HEIGHT) / 2;

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

        drawBevel(g, x0, y0, WIDTH, HEIGHT, COLOR_BG);

        for (Slot s : this.menu.slots) {
            drawBevelInset(g, x0 + s.x - 1, y0 + s.y - 1, CELL_SIZE, CELL_SIZE, COLOR_SLOT_BG);
        }
    }

    //? if >=26.1 {
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY) {
        // Custom: usa COLOR_TITLE_TEXT em vez do default.
        g.text(this.font, this.title,
                this.titleLabelX, this.titleLabelY, COLOR_TITLE_TEXT, false);
        g.text(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, COLOR_LABEL_TEXT, false);
    }
    //?} else {
    /*@Override
    protected void renderLabels(@Nonnull GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(this.font, this.title,
                this.titleLabelX, this.titleLabelY, COLOR_TITLE_TEXT, false);
        g.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, COLOR_LABEL_TEXT, false);
    }
    *///?}
}
