package com.mrchuw.universalvault.gui.modal;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SPatternUpdatePayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.gui.base.AbstractModalScreen;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PatternConfigModal extends AbstractModalScreen {

    private static final int FIELD_W   = 80;
    private static final int FIELD_H   = 18;
    private static final int LABEL_H   = 9;
    private static final int LABEL_GAP = 2;
    private static final int ROW_GAP   = 6;
    private static final int CHECKBOX_PITCH = 20;

    private final VaultPattern original;
    private final UUID owner;

    private EditBox minField, maxField, batchField, priorityField, durabilityField;
    private Checkbox adaptive, ignoreNbt, atomic;

    private record Label(int x, int y, Component text) {}
    private final List<Label> labels = new ArrayList<>();

    public PatternConfigModal(Screen parent, VaultPattern pattern, UUID owner) {
        super(parent, Component.translatable("gui.universal_vault.pattern_modal"));
        this.original = pattern;
        this.owner = owner;
    }

    @Override protected int panelWidth()  { return 300; }
    @Override protected int panelHeight() { return 220; }

    @Override
    protected void layoutContent() {
        labels.clear();

        int leftX  = px + PAD;
        int rightX = px + pw - PAD - FIELD_W;
        int y = py + PAD + TITLE_H + GAP;

        labels.add(new Label(leftX,  y, Component.translatable("gui.universal_vault.field.min")));
        labels.add(new Label(rightX, y, Component.translatable("gui.universal_vault.field.max")));
        y += LABEL_H + LABEL_GAP;

        minField = new EditBox(font, leftX, y, FIELD_W, FIELD_H,
                Component.translatable("gui.universal_vault.field.min"));
        minField.setValue(String.valueOf(original.min()));
        addRenderableWidget(minField);

        maxField = new EditBox(font, rightX, y, FIELD_W, FIELD_H,
                Component.translatable("gui.universal_vault.field.max"));
        maxField.setValue(String.valueOf(original.max()));
        addRenderableWidget(maxField);
        y += FIELD_H + ROW_GAP;

        labels.add(new Label(leftX,  y, Component.translatable("gui.universal_vault.field.batch")));
        labels.add(new Label(rightX, y, Component.translatable("gui.universal_vault.field.priority")));
        y += LABEL_H + LABEL_GAP;

        batchField = new EditBox(font, leftX, y, FIELD_W, FIELD_H,
                Component.translatable("gui.universal_vault.field.batch"));
        batchField.setValue(String.valueOf(original.batch()));
        addRenderableWidget(batchField);

        priorityField = new EditBox(font, rightX, y, FIELD_W, FIELD_H,
                Component.translatable("gui.universal_vault.field.priority"));
        priorityField.setValue(String.valueOf(original.priority()));
        addRenderableWidget(priorityField);
        y += FIELD_H + ROW_GAP;

        labels.add(new Label(leftX, y,
                Component.translatable("gui.universal_vault.field.min_durability_pct")));
        y += LABEL_H + LABEL_GAP;

        durabilityField = new EditBox(font, leftX, y, FIELD_W, FIELD_H,
                Component.translatable("gui.universal_vault.field.min_durability_pct"));
        original.modifiers().minDurabilityPct()
                .ifPresent(v -> durabilityField.setValue(String.valueOf(v)));
        addRenderableWidget(durabilityField);
        y += FIELD_H + ROW_GAP + GAP;

        adaptive = addRenderableWidget(Checkbox.builder(
                        Component.translatable("gui.universal_vault.modifier.adaptive_velocity"), font)
                .pos(leftX, y).selected(original.modifiers().adaptiveVelocityScaling()).build());
        y += CHECKBOX_PITCH;

        ignoreNbt = addRenderableWidget(Checkbox.builder(
                        Component.translatable("gui.universal_vault.modifier.ignore_nbt"), font)
                .pos(leftX, y).selected(original.modifiers().ignoreNbt()).build());

        y += CHECKBOX_PITCH;

        atomic = addRenderableWidget(Checkbox.builder(
                        Component.translatable("gui.universal_vault.modifier.atomic"), font)
                .pos(leftX, y).selected(original.modifiers().atomic()).build());

        int btnY = py + ph - PAD - BTN_H;
        addButton(leftX, btnY, BTN_W, BTN_H,
                Component.translatable("gui.universal_vault.btn.cancel"), this::onClose);

        int rightStart = px + pw - PAD - BTN_W;
        addButton(rightStart, btnY, BTN_W, BTN_H,
                Component.translatable("gui.universal_vault.btn.save"), this::save);
    }

    //? if >=26.1 {
    @Override
    protected void drawContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        for (Label l : labels) {
            g.text(this.font, l.text(), l.x(), l.y(), COLOR_LABEL_TEXT, false);
        }
    }
    //?} else {
    /*@Override
    protected void drawContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        for (Label l : labels) {
            g.drawString(this.font, l.text(), l.x(), l.y(), COLOR_LABEL_TEXT, false);
        }
    }
    *///?}

    private void save() {
        long min = parseLong(minField.getValue(), original.min());
        long max = parseLong(maxField.getValue(), original.max());
        long batch = parseLong(batchField.getValue(), original.batch());
        int priority = (int) parseLong(priorityField.getValue(), original.priority());
        Optional<Double> dur = durabilityField.getValue().isBlank()
                ? Optional.empty()
                : Optional.of(parseDouble(durabilityField.getValue(), 0.0));

        VaultPattern.Modifiers mods = new VaultPattern.Modifiers(
                adaptive.selected(), ignoreNbt.selected(), dur, atomic.selected());

        VaultPattern updated = new VaultPattern(
                original.patternId(), original.recipe(),
                min, max, (int) batch, priority,
                original.triggerMode(), original.paused(),
                original.pushMode(), mods);

        Platform.INSTANCE.sendToServer(new C2SPatternUpdatePayload(owner, Optional.of(updated)));
        onClose();
    }

    private static long parseLong(String s, long fallback) {
        try { return Long.parseLong(s.trim()); } catch (Exception e) { return fallback; }
    }

    private static double parseDouble(String s, double fallback) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return fallback; }
    }
}
