package com.mrchuw.universalvault.gui.modal;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SQuickCraftPayload;
import com.mrchuw.universalvault.gui.base.AbstractModalScreen;
import com.mrchuw.universalvault.storage.ItemKey;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
        //?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class QuickCraftModal extends AbstractModalScreen {

    private static final int FIELD_H = 18;
    private static final int ROW_GAP = 6;
    private static final int PREVIEW_W = 20;

    private static final int[] BASE_VALUES    = { 1,  8, 16 };
    private static final int[] SHIFT_VALUES   = { 16, 32, 64 };

    private final ItemKey item;
    private EditBox amountField;

    public QuickCraftModal(Screen parent, ItemKey item) {
        super(parent, Component.translatable("gui.universal_vault.quick_craft"));
        this.item = item;
    }

    @Override protected int panelWidth()  { return 220; }
    @Override protected int panelHeight() { return 148; }

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

        int fieldX = contentX + PREVIEW_W + GAP;
        int fieldW = contentW - PREVIEW_W - GAP;
        amountField = new EditBox(font, fieldX, y, fieldW, FIELD_H, Component.literal("amount"));
        amountField.setValue("1");
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
                Component.literal("REQUEST CRAFT"), this::submit);
    }

    private static int value(int idx) {
        boolean shift = Minecraft.getInstance().hasShiftDown();
        return (shift ? SHIFT_VALUES : BASE_VALUES)[idx];
    }

    @Override
    //? if >=26.1 {
    protected void drawContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*protected void drawContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
     *///?}
        int itemX = px + PAD;
        int itemY = py + PAD + TITLE_H + GAP + BTN_H + ROW_GAP;
        ItemStack stack = item.toStack(1);
        if (!stack.isEmpty()) {
            //? if >=26.1 {
            g.item(stack, itemX, itemY);
            //?} else {
            /*g.renderItem(stack, itemX, itemY);
             *///?}
        }
    }

    private void adjust(int delta) {
        int cur = 1;
        try { cur = Integer.parseInt(amountField.getValue().trim()); } catch (Exception ignored) {}
        cur = Math.max(1, cur + delta);
        amountField.setValue(String.valueOf(cur));
    }

    private void submit() {
        int amount = 1;
        try { amount = Integer.parseInt(amountField.getValue().trim()); } catch (Exception ignored) {}
        amount = Math.max(1, Math.min(amount, 4096));
        Platform.INSTANCE.sendToServer(new C2SQuickCraftPayload(item, amount));
        onClose();
    }
}
