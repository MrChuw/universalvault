package com.mrchuw.universalvault.gui.base;

import com.mrchuw.universalvault.gui.VaultStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
        //?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public abstract class AbstractModalScreen extends Screen implements VaultStyle {

    protected final Screen parent;
    protected int px, py, pw, ph;

    protected record Btn(int x, int y, int w, int h, Supplier<Component> label, Runnable onClick) {}
    protected final List<Btn> buttons = new ArrayList<>();

    protected AbstractModalScreen(Screen parent, Component title) {
        super(title);
        this.parent = parent;
    }

    protected abstract int panelWidth();
    protected abstract int panelHeight();
    protected abstract void layoutContent();

    //? if >=26.1 {
    protected abstract void drawContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick);
    //?} else {
    /*protected abstract void drawContent(GuiGraphics g, int mouseX, int mouseY, float partialTick);
     *///?}

    @Override
    protected void init() {
        this.pw = panelWidth();
        this.ph = panelHeight();
        this.px = (this.width  - this.pw) / 2;
        this.py = (this.height - this.ph) / 2;
        this.buttons.clear();
        layoutContent();
    }

    @Override
    public void onClose() {
        Minecraft mc = Minecraft.getInstance();
        //? if neoforge && <26.2 {
        /*if (mc.screen == this) {
            mc.popGuiLayer();
        }
        *///?} elif neoforge {
        if (mc.gui.screen() == this) {
            mc.gui.popScreenLayer();
        }
        //?}
        if (parent != null) {
            //? if <26.2 {
            /*Minecraft.getInstance().setScreen(parent);
             *///?} else {
            Minecraft.getInstance().gui.setScreen(parent);
            //?}
        } else {
            super.onClose();
        }
    }

    protected void addButton(int x, int y, int w, int h, Component label, Runnable onClick) {
        buttons.add(new Btn(x, y, w, h, () -> label, onClick));
    }

    protected void addButton(int x, int y, int w, int h, Supplier<Component> label, Runnable onClick) {
        buttons.add(new Btn(x, y, w, h, label, onClick));
    }

    @Override
    //? if <26.1 {
    /*public void renderBackground(@Nonnull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
    *///?} else {
    public void extractBackground(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
    //?}
        g.fill(0, 0, this.width, this.height, COLOR_DIM);
        drawBevel(g, px, py, pw, ph, COLOR_BG);
    }

    @Override
    //? if <26.1 {
    /*public void render(@Nonnull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.drawString(this.font, this.getTitle(), px + PAD, py + PAD, COLOR_TITLE_TEXT, false);
        drawContent(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
    *///?} else {
    public void extractRenderState(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        g.text(this.font, this.getTitle(), px + PAD, py + PAD, COLOR_TITLE_TEXT, false);
        drawContent(g, mouseX, mouseY, partialTick);
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    //?}
        drawButtons(g, mouseX, mouseY);
    }

    //? if >=26.1 {
    private void drawButtons(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        //?} else {
        /*private void drawButtons(GuiGraphics g, int mouseX, int mouseY) {
         *///?}
        for (Btn b : buttons) {
            boolean hover = mouseX >= b.x() && mouseX < b.x() + b.w()
                    && mouseY >= b.y() && mouseY < b.y() + b.h();
            int bg = hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG;
            drawBevel(g, b.x(), b.y(), b.w(), b.h(), bg);

            Component text = b.label().get();
            int tw = this.font.width(text);
            int tx = b.x() + (b.w() - tw) / 2;
            int ty = b.y() + (b.h() - 8) / 2 + 1;
            //? if >=26.1 {
            g.text(this.font, text, tx, ty, COLOR_BTN_TEXT, false);
            //?} else {
            /*g.drawString(this.font, text, tx, ty, COLOR_BTN_TEXT, false);
             *///?}
        }
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        for (Btn b : buttons) {
            if (mx >= b.x() && mx < b.x() + b.w()
                    && my >= b.y() && my < b.y() + b.h()) {
                b.onClick().run();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}