package com.mrchuw.universalvault.gui;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}

public interface VaultStyle {
    int COLOR_BG           = 0xFFC6C6C6;
    int COLOR_BORDER_LIGHT = 0xFFFFFFFF;
    int COLOR_BORDER_DARK  = 0xFF373737;

    int COLOR_GRID_BG = 0xFF555555;

    int COLOR_SLOT_BG     = 0xFF8B8B8B;
    int COLOR_SLOT_SHADOW = 0xFF373737;
    int COLOR_SLOT_LIGHT  = 0xFFFFFFFF;

    int COLOR_BTN_BG         = 0xFF9E9E9E;
    int COLOR_BTN_BG_HOVER   = 0xFFB8B8B8;
    int COLOR_BTN_BG_PRESSED = 0xFF6E6E6E;
    int COLOR_BTN_TEXT       = 0xFF202020;

    int COLOR_TITLE_TEXT   = 0xFF404040;
    int COLOR_LABEL_TEXT   = 0xFF404040;
    int COLOR_COUNTER_TEXT = 0xFF606060;

    int COLOR_DIM = 0x80000000;

    int BG_STATUS_SATISFIED  = 0xFF4E9B4E; // verde
    int BG_STATUS_PROCESSING = 0xFF4E8B9B; // ciano
    int BG_STATUS_BLOCKED    = 0xFF9B6B3E; // âmbar
    int BG_STATUS_PAUSED     = 0xFF9B3E3E; // vermelho
    int BG_STATUS_MANUAL     = 0xFF7B3E9B; // roxo
    int BG_STATUS_UNKNOWN    = 0xFF6B6B6B; // cinza
    int BG_HAS_TARGET        = 0xFF4A6B9B; // azul, item com alvo no Vault

    int CELL_SIZE = 18;
    int PAD       = 8;
    int GAP       = 4;
    int TITLE_H   = 12;
    int BTN_H     = 18;
    int BTN_W     = 80;

    //? if >=26.1 {
    default void drawBevel(GuiGraphicsExtractor g, int x, int y, int w, int h, int interior) {
        g.fill(x,         y,         x + w,     y + 1,     COLOR_BORDER_LIGHT);
        g.fill(x,         y,         x + 1,     y + h,     COLOR_BORDER_LIGHT);
        g.fill(x,         y + h - 1, x + w,     y + h,     COLOR_BORDER_DARK);
        g.fill(x + w - 1, y,         x + w,     y + h,     COLOR_BORDER_DARK);
        g.fill(x + 1,     y + 1,     x + w - 1, y + h - 1, interior);
    }
    //?} else {
    /*default void drawBevel(GuiGraphics g, int x, int y, int w, int h, int interior) {
        g.fill(x,         y,         x + w,     y + 1,     COLOR_BORDER_LIGHT);
        g.fill(x,         y,         x + 1,     y + h,     COLOR_BORDER_LIGHT);
        g.fill(x,         y + h - 1, x + w,     y + h,     COLOR_BORDER_DARK);
        g.fill(x + w - 1, y,         x + w,     y + h,     COLOR_BORDER_DARK);
        g.fill(x + 1,     y + 1,     x + w - 1, y + h - 1, interior);
    }
    *///?}

    //? if >=26.1 {
    default void drawBevelInset(GuiGraphicsExtractor g, int x, int y, int w, int h, int interior) {
        g.fill(x,         y,         x + w,     y + 1,     COLOR_SLOT_SHADOW);
        g.fill(x,         y,         x + 1,     y + h,     COLOR_SLOT_SHADOW);
        g.fill(x,         y + h - 1, x + w,     y + h,     COLOR_SLOT_LIGHT);
        g.fill(x + w - 1, y,         x + w,     y + h,     COLOR_SLOT_LIGHT);
        g.fill(x + 1,     y + 1,     x + w - 1, y + h - 1, interior);
    }
    //?} else {
    /*default void drawBevelInset(GuiGraphics g, int x, int y, int w, int h, int interior) {
        g.fill(x,         y,         x + w,     y + 1,     COLOR_SLOT_SHADOW);
        g.fill(x,         y,         x + 1,     y + h,     COLOR_SLOT_SHADOW);
        g.fill(x,         y + h - 1, x + w,     y + h,     COLOR_SLOT_LIGHT);
        g.fill(x + w - 1, y,         x + w,     y + h,     COLOR_SLOT_LIGHT);
        g.fill(x + 1,     y + 1,     x + w - 1, y + h - 1, interior);
    }
    *///?}
}