package com.mrchuw.universalvault.gui.modal;

import com.mrchuw.universalvault.gui.base.AbstractModalScreen;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AmountPickerModal extends AbstractModalScreen {

    private static final int FIELD_H = 18;
    private static final int ROW_GAP = 6;

    private static final int[] BASE_VALUES  = { 1,  8, 16 };
    private static final int[] SHIFT_VALUES = { 16, 32, 64 };

    private final int initial;
    private final int min;
    private final int max;
    private final IntConsumer onConfirm;
    private EditBox amountField;

    public AmountPickerModal(Screen parent, int initial, int min, int max,
                             IntConsumer onConfirm) {
        super(parent, Component.translatable("gui.universal_vault.amount_picker"));
        this.initial = initial;
        this.min = min;
        this.max = max;
        this.onConfirm = onConfirm;
    }

    @Override protected int panelWidth()  { return 200; }
    @Override protected int panelHeight() { return 116; }

    @Override
    protected void layoutContent() {
        int contentX = px + PAD;
        int contentW = pw - PAD * 2;
        int y = py + PAD + TITLE_H + GAP;

        int btnW = (contentW - GAP * 2) / 3;
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addButton(contentX + i * (btnW + GAP), y, btnW, BTN_H,
                    () -> Component.literal("+" + value(idx)),
                    () -> adjust(+value(idx)));
        }
        y += BTN_H + ROW_GAP;

        amountField = new EditBox(font, contentX, y, contentW, FIELD_H,
                Component.literal("amount"));
        amountField.setValue(String.valueOf(initial));
        addRenderableWidget(amountField);
        y += FIELD_H + ROW_GAP;

        for (int i = 0; i < 3; i++) {
            final int idx = i;
            addButton(contentX + i * (btnW + GAP), y, btnW, BTN_H,
                    () -> Component.literal("-" + value(idx)),
                    () -> adjust(-value(idx)));
        }

        int actionW = (contentW - GAP) / 2;
        int actionY = py + ph - PAD - BTN_H;
        addButton(px + PAD, actionY, actionW, BTN_H,
                Component.literal("CANCEL"), this::onClose);
        addButton(px + pw - PAD - actionW, actionY, actionW, BTN_H,
                Component.literal("OK"), this::confirm);
    }

    private static int value(int idx) {
        boolean shift = Minecraft.getInstance().hasShiftDown();
        return (shift ? SHIFT_VALUES : BASE_VALUES)[idx];
    }

    @Override
    //? if >=26.1 {
    protected void drawContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {}
    //?} else {
    /*protected void drawContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}
     *///?}

    private void adjust(int delta) {
        int cur = clamp(parse(amountField.getValue(), initial));
        cur = clamp(cur + delta);
        amountField.setValue(String.valueOf(cur));
    }

    private int parse(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }

    private int clamp(int v) {
        return Math.max(min, Math.min(v, max));
    }

    private void confirm() {
        int v = clamp(parse(amountField.getValue(), initial));
        onConfirm.accept(v);
        onClose();
    }
}
