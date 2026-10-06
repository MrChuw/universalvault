package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SNodeConfigPayload;
import com.mrchuw.universalvault.automation.network.C2SRequestPatternsSyncPayload;
import com.mrchuw.universalvault.automation.network.S2CNodeConfigSyncPayload;
import com.mrchuw.universalvault.automation.network.S2CPatternsSyncPayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.gui.VaultStyle;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;


public class NodeConfigScreen extends Screen implements VaultStyle {

    private static final int REF_W = 420;
    private static final int REF_H = 320;

    private static final float MIN_SCALE = 0.75f;
    private static final float MAX_SCALE = 1.15f;

    private static final int BASE_PANEL_W  = 300;
    private static final int BASE_PANEL_H  = 230;
    private static final int BASE_PAD      = 10;
    private static final int BASE_TITLE_H  = 11;
    private static final int BASE_TAB_H    = 14;
    private static final int BASE_BTN_H    = 14;
    private static final int BASE_FACE     = 32;
    private static final int BASE_FACE_GAP = 2;

    private static final int CELL = 18;

    private enum Tab { LIBRARY, PULL }

    private final BlockPos nodePos;
    private Tab currentTab = Tab.LIBRARY;

    private float uiScale = 1.0f;
    private int panelW, panelH, pad, titleH, tabH, btnH, faceSize, faceGap;
    private int px, py;

    private final Set<Direction> pullSides = EnumSet.noneOf(Direction.class);
    private final Set<Direction> bindSides = EnumSet.noneOf(Direction.class);
    private final Map<UUID, Boolean> patternOverrides = new HashMap<>();
    private final Map<UUID, VaultPattern> patternsById = new HashMap<>();

    private int libraryScroll = 0;

    private record Btn(int x, int y, int w, int h, Supplier<Component> label, Runnable onClick) {}
    private final java.util.List<Btn> buttons = new java.util.ArrayList<>();

    public NodeConfigScreen(BlockPos nodePos, Set<Direction> pullSides,
                            Map<UUID, Boolean> patternOverrides, Set<Direction> bindSides) {
        super(Component.translatable("gui.universal_vault.node_config"));
        this.nodePos = nodePos;
        this.pullSides.addAll(pullSides);
        this.patternOverrides.putAll(patternOverrides);
        this.bindSides.addAll(bindSides);
    }

    @Override
    protected void init() {
        float s = Math.min(this.width / (float) REF_W, this.height / (float) REF_H);
        this.uiScale = Math.max(MIN_SCALE, Math.min(s, MAX_SCALE));

        this.panelW   = Math.round(BASE_PANEL_W   * uiScale);
        this.panelH   = Math.round(BASE_PANEL_H   * uiScale);
        this.pad      = Math.round(BASE_PAD       * uiScale);
        this.titleH   = Math.round(BASE_TITLE_H   * uiScale);
        this.tabH     = Math.round(BASE_TAB_H     * uiScale);
        this.btnH     = Math.round(BASE_BTN_H     * uiScale);
        this.faceSize = Math.round(BASE_FACE      * uiScale);
        this.faceGap  = Math.round(BASE_FACE_GAP  * uiScale);

        this.px = (this.width  - panelW) / 2;
        this.py = (this.height - panelH) / 2;

        buttons.clear();

        int headerRowH = Math.max(titleH, tabH);
        int tabY = py + pad + (headerRowH - tabH) / 2;

        int tab1W = Math.round(62 * uiScale);
        int tab2W = Math.round(78 * uiScale);
        int tabGap = Math.round(4 * uiScale);

        int tabsEndX = px + panelW - pad;
        int tab2X = tabsEndX - tab2W;
        int tab1X = tab2X - tabGap - tab1W;

        addButton(tab1X, tabY, tab2W, tabH,
                () -> Component.literal("LIBRARY"),
                () -> currentTab = Tab.LIBRARY);
        addButton(tab2X, tabY, tab1W, tabH,
                () -> Component.literal("PULL"),
                () -> currentTab = Tab.PULL);

        int btnW = Math.round(78 * uiScale);
        int footerY = py + panelH - pad - btnH;
        int cancelX = px + panelW - pad - btnW * 2 - tabGap;
        int saveX   = px + panelW - pad - btnW;
        addButton(cancelX, footerY, btnW, btnH,
                () -> Component.translatable("gui.universal_vault.btn.cancel"),
                this::onClose);
        addButton(saveX, footerY, btnW, btnH,
                () -> Component.translatable("gui.universal_vault.btn.save"),
                this::save);

        Platform.INSTANCE.sendToServer(C2SRequestPatternsSyncPayload.forNode(nodePos));
    }

    private void addButton(int x, int y, int w, int h, Supplier<Component> label, Runnable onClick) {
        buttons.add(new Btn(x, y, w, h, label, onClick));
    }

    private int headerRowH() {
        return Math.max(titleH, tabH);
    }

    private int infoY() {
        return py + pad + headerRowH() + Math.round(3 * uiScale);
    }

    private int contentY() {
        return infoY() + titleH + Math.round(6 * uiScale);
    }

    private int netX() {
        int netW = 4 * faceSize + 3 * faceGap;
        return px + (panelW - netW) / 2;
    }

    private int netY() {
        return contentY() + Math.round(14 * uiScale);
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
        drawBevel(g, px, py, panelW, panelH, COLOR_BG);
    }

    @Override
    //? if <26.1 {
    /*public void render(@Nonnull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
    *///?} else {
    public void extractRenderState(@Nonnull GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
    //?}
        //? if >=26.1 {
        g.text(this.font, this.getTitle(), px + pad, py + pad + (headerRowH() - titleH) / 2,
                COLOR_TITLE_TEXT, false);
        //?} else {
        /*g.drawString(this.font, this.getTitle(), px + pad,
                py + pad + (headerRowH() - titleH) / 2, COLOR_TITLE_TEXT, false);
         *///?}

        String info = nodePos.toShortString()
                + " · pull " + pullSides.size()
                + " · bind " + (bindSides.isEmpty() ? "all" : String.valueOf(bindSides.size()));
        //? if >=26.1 {
        g.text(this.font, Component.literal(info), px + pad, infoY(), COLOR_LABEL_TEXT, false);
        //?} else {
        /*g.drawString(this.font, info, px + pad, infoY(), COLOR_LABEL_TEXT, false);
         *///?}

        if (currentTab == Tab.PULL) {
            drawPullTab(g, mouseX, mouseY);
        } else {
            drawLibraryTab(g, mouseX, mouseY);
        }

        drawButtons(g, mouseX, mouseY);
    }

    //? if >=26.1 {
    private void drawPullTab(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawPullTab(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        String bindLine = bindSides.isEmpty()
                ? "Bind: all sides"
                : "Bind: " + bindSides.stream().map(NodeConfigScreen::shortName)
                .sorted().reduce((a, b) -> a + ", " + b).orElse("");
        int bindW = this.font.width(bindLine);
        //? if >=26.1 {
        g.text(this.font, Component.literal(bindLine),
                px + (panelW - bindW) / 2, contentY(), COLOR_TITLE_TEXT, false);
        //?} else {
        /*g.drawString(this.font, bindLine, px + (panelW - bindW) / 2, contentY(),
                COLOR_TITLE_TEXT, false);
         *///?}

        int nx = netX();
        int ny = netY();
        int colN = nx + (faceSize + faceGap);

        drawFace(g, colN, ny, Direction.UP, mouseX, mouseY);

        Direction[] ring = { Direction.WEST, Direction.NORTH,
                Direction.EAST, Direction.SOUTH };
        int rowY = ny + faceSize + faceGap;
        for (int i = 0; i < 4; i++) {
            int fx = nx + i * (faceSize + faceGap);
            drawFace(g, fx, rowY, ring[i], mouseX, mouseY);
        }

        int downY = rowY + faceSize + faceGap;
        drawFace(g, colN, downY, Direction.DOWN, mouseX, mouseY);

        int legendY = downY + faceSize + Math.round(10 * uiScale);
        String[] lines = {
                "LMB: toggle pull",
                "Ctrl+LMB: toggle bind",
                "Shift+LMB: only this face pulls",
        };
        for (int i = 0; i < lines.length; i++) {
            //? if >=26.1 {
            g.text(this.font, Component.literal(lines[i]),
                    px + pad, legendY + i * 9, COLOR_LABEL_TEXT, false);
            //?} else {
            /*g.drawString(this.font, lines[i], px + pad, legendY + i * 9,
                    COLOR_LABEL_TEXT, false);
             *///?}
        }
    }

    //? if >=26.1 {
    private void drawFace(GuiGraphicsExtractor g, int x, int y, Direction dir, int mouseX, int mouseY) {
    //?} else {
    /*private void drawFace(GuiGraphics g, int x, int y, Direction dir, int mouseX, int mouseY) {
     *///?}
        boolean hover = mouseX >= x && mouseX < x + faceSize
                && mouseY >= y && mouseY < y + faceSize;
        boolean pullOn = pullSides.contains(dir);
        boolean bindOn = bindSides.contains(dir);

        int interior;
        if (pullOn && bindOn) {
            interior = hover ? 0xFF6FA8C7 : BG_STATUS_PROCESSING;
        } else if (bindOn) {
            interior = hover ? 0xFFC79B3E : BG_STATUS_BLOCKED;
        } else if (pullOn) {
            interior = hover ? 0xFF6FA8C7 : BG_STATUS_PROCESSING;
        } else {
            interior = hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG;
        }
        drawBevel(g, x, y, faceSize, faceSize, interior);

        if (bindOn) {
            int orange = 0xFFFFB347;
            g.fill(x, y, x + faceSize, y + 1, orange);
            g.fill(x, y + faceSize - 1, x + faceSize, y + faceSize, orange);
            g.fill(x, y, x + 1, y + faceSize, orange);
            g.fill(x + faceSize - 1, y, x + faceSize, y + faceSize, orange);
        }

        String label = shortName(dir);
        int tw = this.font.width(label);
        int ty = y + (faceSize - 8) / 2 + 1;
        //? if >=26.1 {
        g.text(this.font, Component.literal(label),
                x + (faceSize - tw) / 2, ty, COLOR_BTN_TEXT, false);
        //?} else {
        /*g.drawString(this.font, label, x + (faceSize - tw) / 2, ty,
                COLOR_BTN_TEXT, false);
         *///?}
    }

    private static String shortName(Direction d) {
        return switch (d) {
            case UP -> "UP";
            case DOWN -> "DN";
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "W";
        };
    }

    //? if >=26.1 {
    private void drawLibraryTab(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawLibraryTab(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        int cy = contentY();
        //? if >=26.1 {
        g.text(this.font, Component.literal("Patterns (custom require opt-in):"),
                px + pad, cy, COLOR_LABEL_TEXT, false);
        //?} else {
        /*g.drawString(this.font, "Patterns (custom require opt-in):",
                px + pad, cy, COLOR_LABEL_TEXT, false);
         *///?}

        int listY = cy + 12;
        int listH = (py + panelH - pad - btnH - 6) - listY;
        int cols = (panelW - pad * 2) / CELL;
        int rowsVisible = Math.max(1, listH / CELL);

        var list = patternsById.values().stream()
                .sorted((a, b) -> Long.compare(b.max(), a.max()))
                .toList();

        int start = libraryScroll * cols;
        for (int i = 0; i < rowsVisible * cols; i++) {
            int idx = start + i;
            if (idx >= list.size()) break;
            VaultPattern p = list.get(idx);

            int col = i % cols;
            int row = i / cols;
            int cellX = px + pad + col * CELL;
            int cellY = listY + row * CELL;

            boolean isCustom = p.recipe() instanceof RecipeView.Custom;
            Boolean override = patternOverrides.get(p.patternId());
            boolean enabled = override != null ? override : !isCustom;

            int bg = enabled ? BG_STATUS_SATISFIED : BG_STATUS_UNKNOWN;
            drawBevelInset(g, cellX, cellY, CELL, CELL, bg);

            ItemStack stack = p.output().toStack(1);
            //? if >=26.1 {
            g.item(stack, cellX + 1, cellY + 1);
            //?} else {
            /*g.renderItem(stack, cellX + 1, cellY + 1);
             *///?}
        }
    }

    //? if >=26.1 {
    private void drawButtons(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawButtons(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        Tab active = currentTab;
        for (int i = 0; i < buttons.size(); i++) {
            Btn b = buttons.get(i);
            boolean hover = mouseX >= b.x() && mouseX < b.x() + b.w()
                    && mouseY >= b.y() && mouseY < b.y() + b.h();

            boolean isTab = i < 2;
            boolean isActiveTab = isTab
                    && ((i == 0 && active == Tab.LIBRARY) || (i == 1 && active == Tab.PULL));

            int bg;
            if (isActiveTab) {
                bg = COLOR_BTN_BG_PRESSED;
            } else if (hover) {
                bg = COLOR_BTN_BG_HOVER;
            } else {
                bg = COLOR_BTN_BG;
            }
            drawBevel(g, b.x(), b.y(), b.w(), b.h(), bg);

            Component text = b.label().get();
            int tw = this.font.width(text);
            //? if >=26.1 {
            g.text(this.font, text, b.x() + (b.w() - tw) / 2, b.y() + (b.h() - 8) / 2 + 1,
                    COLOR_BTN_TEXT, false);
            //?} else {
            /*g.drawString(this.font, text, b.x() + (b.w() - tw) / 2,
                    b.y() + (b.h() - 8) / 2 + 1, COLOR_BTN_TEXT, false);
             *///?}
        }
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();

        for (Btn b : buttons) {
            if (mx >= b.x() && mx < b.x() + b.w() && my >= b.y() && my < b.y() + b.h()) {
                b.onClick().run();
                return true;
            }
        }

        if (currentTab == Tab.PULL) {
            handlePullClick(mx, my, button);
        } else {
            handleLibraryClick(mx, my, button);
        }

        return super.mouseClicked(event, doubleClick);
    }

    private void handlePullClick(double mx, double my, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) return;

        Direction hit = faceAt(mx, my);
        if (hit == null) return;

        Minecraft mc = Minecraft.getInstance();
        boolean ctrl = mc.hasControlDown();
        boolean shift = mc.hasShiftDown();

        if (ctrl) {
            if (bindSides.contains(hit)) bindSides.remove(hit);
            else bindSides.add(hit);
            return;
        }

        if (shift) {
            pullSides.clear();
            pullSides.add(hit);
            return;
        }

        if (pullSides.contains(hit)) pullSides.remove(hit);
        else pullSides.add(hit);
    }

    private Direction faceAt(double mx, double my) {
        int nx = netX();
        int ny = netY();
        int colN = nx + (faceSize + faceGap);
        int rowY = ny + faceSize + faceGap;
        int downY = rowY + faceSize + faceGap;

        if (hitFace(mx, my, colN, ny)) return Direction.UP;
        if (hitFace(mx, my, colN, downY)) return Direction.DOWN;

        Direction[] ring = { Direction.WEST, Direction.NORTH,
                Direction.EAST, Direction.SOUTH };
        for (int i = 0; i < 4; i++) {
            int fx = nx + i * (faceSize + faceGap);
            if (hitFace(mx, my, fx, rowY)) return ring[i];
        }
        return null;
    }

    private boolean hitFace(double mx, double my, int x, int y) {
        return mx >= x && mx < x + faceSize
                && my >= y && my < y + faceSize;
    }

    private void handleLibraryClick(double mx, double my, int button) {
        if (button != InputConstants.MOUSE_BUTTON_LEFT) return;

        int cy = contentY();
        int listY = cy + 12;
        int cols = (panelW - pad * 2) / CELL;

        var list = patternsById.values().stream()
                .sorted((a, b) -> Long.compare(b.max(), a.max()))
                .toList();

        int relX = (int) (mx - (px + pad));
        int relY = (int) (my - listY);
        if (relX < 0 || relY < 0) return;

        int col = relX / CELL;
        int row = relY / CELL;
        if (col < 0 || col >= cols) return;

        int idx = libraryScroll * cols + row * cols + col;
        if (idx < 0 || idx >= list.size()) return;

        VaultPattern p = list.get(idx);
        boolean isCustom = p.recipe() instanceof RecipeView.Custom;
        Boolean override = patternOverrides.get(p.patternId());
        boolean currentlyEnabled = override != null ? override : !isCustom;
        boolean newEnabled = !currentlyEnabled;

        boolean isDefault = isCustom ? !newEnabled : newEnabled;
        if (isDefault) {
            patternOverrides.remove(p.patternId());
        } else {
            patternOverrides.put(p.patternId(), newEnabled);
        }
    }

    public void onPatternsSync(S2CPatternsSyncPayload payload) {
        patternsById.clear();
        for (S2CPatternsSyncPayload.Entry e : payload.entries()) {
            patternsById.put(e.pattern().patternId(), e.pattern());
        }
    }

    public void onNodeConfigSync(S2CNodeConfigSyncPayload payload) {
        pullSides.clear();
        pullSides.addAll(payload.pullSides());
        patternOverrides.clear();
        patternOverrides.putAll(payload.patternOverrides());
        bindSides.clear();
        bindSides.addAll(payload.bindSides());
    }

    private void save() {
        Platform.INSTANCE.sendToServer(new C2SNodeConfigPayload(
                nodePos,
                EnumSet.copyOf(pullSides),
                new HashMap<>(patternOverrides),
                EnumSet.copyOf(bindSides)));
        onClose();
    }
}
