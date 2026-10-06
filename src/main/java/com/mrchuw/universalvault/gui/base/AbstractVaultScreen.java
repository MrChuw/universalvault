package com.mrchuw.universalvault.gui.base;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.config.VaultClientConfig;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.VaultStyle;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.ItemKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractVaultScreen<E extends AbstractVaultScreen.GridEntry>
        extends AbstractContainerScreen<VaultMenu>
        implements VaultStyle {

    public interface GridEntry {
        ItemKey key();
        String displayName();
        String modId();
        Set<Identifier> tags();
        List<Component> tooltip();
    }

    protected static final int GUI_WIDTH = 176;
    protected static final int MIN_GUI_HEIGHT = 208;
    protected static final int INV_TOP_FROM_BOTTOM = 82;
    protected static final int HOTBAR_FROM_BOTTOM = 24;
    protected static final int TAB_STRIP_WIDTH = 22;
    protected static final int TAB_HEIGHT = 22;
    protected static final int TAB_GAP = 2;
    protected static final int TAB_TOP_OFFSET = 4;
    protected static final int SLOT_X_OFFSET = 5;
    protected static final int GRID_COLS = 9;
    protected static final int GRID_TOP = 24;
    protected static final int GRID_BOTTOM_FROM_BOTTOM = 100;
    protected static final int GRID_WIDTH = GRID_COLS * CELL_SIZE;
    protected static final int SCROLLBAR_X = 170;
    protected static final int SCROLLBAR_W = 4;
    protected static final int SORT_BTN_W = 70;
    protected static final int SORT_BTN_H = 12;
    protected static final int SORT_BTN_RIGHT_MARGIN = 10;

    protected int guiHeight = MIN_GUI_HEIGHT;
    protected int gridRows;
    protected int gridHeight;
    protected int invTopY;
    protected int hotbarY;

    protected boolean draggingScrollbar = false;
    protected VaultSortMode sortMode;

    protected EditBox searchBox;
    protected final List<E> allEntries = new ArrayList<>();
    protected final List<E> filteredEntries = new ArrayList<>();
    protected int scrollOffset = 0;

    protected double lastMouseX = 0;
    protected double lastMouseY = 0;

    protected String pendingSearch = null;
    protected long lastSearchTime = 0;

    protected final Component originalTitle;
    protected Component currentTitle;
    protected boolean forceNextSyncFullRebuild = false;
    protected boolean pendingRebuild = false;

    protected AbstractVaultScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        //? if >=26.1 {
        super(menu, playerInventory, title, GUI_WIDTH, initialGuiHeight());
        //?} else {
        /*super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = initialGuiHeight();
        *///?}
        this.originalTitle = title;
        this.sortMode = defaultSortMode();
    }

    protected abstract VaultTab currentTab();
    protected abstract VaultSortMode defaultSortMode();
    protected abstract void sendSyncRequest();
    protected abstract int compareEntries(E a, E b);

    protected boolean isContainerView() { return true; }

    protected boolean hasSearchBox() { return isContainerView(); }

    //? if >=26.1 {
    protected void drawEntryExtra(GuiGraphicsExtractor g, E entry, int cellX, int cellY) {}
    //?} else {
    /*protected void drawEntryExtra(GuiGraphics g, E entry, int cellX, int cellY) {}
     *///?}

    protected abstract boolean onGridClick(@Nullable E entry, int gridIndex, int button, boolean shift);

    protected boolean shouldDeferRebuild(int mouseX, int mouseY) { return false; }

    //? if >=26.1 {
    protected void drawCustomContent(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {}
    //?} else {
    /*protected void drawCustomContent(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}
     *///?}

    protected static int initialGuiHeight() {
        int windowHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return Math.max(MIN_GUI_HEIGHT, windowHeight - 40);
    }

    protected void computeLayout() {
        this.guiHeight = Math.max(MIN_GUI_HEIGHT, this.height - 40);
        this.invTopY = this.guiHeight - INV_TOP_FROM_BOTTOM;
        this.hotbarY = this.guiHeight - HOTBAR_FROM_BOTTOM;

        int gridBottom = this.guiHeight - gridBottomFromBottom();
        int availableHeight = gridBottom - GRID_TOP;
        this.gridRows = Math.max(1, availableHeight / CELL_SIZE);
        this.gridHeight = this.gridRows * CELL_SIZE;
    }

    @Override
    protected void init() {
        this.sortMode = VaultClientConfig.get().sortMode();
        computeLayout();

        //? if <26.1 {
        /*this.imageWidth = GUI_WIDTH;
        this.imageHeight = this.guiHeight;
        *///?}
        super.init();

        this.leftPos = (this.width - GUI_WIDTH) / 2;
        this.topPos = (this.height - this.guiHeight) / 2;

        this.titleLabelX = 5;
        this.titleLabelY = 9;
        this.inventoryLabelX = 5;
        this.inventoryLabelY = this.invTopY - 15;

        this.currentTitle = computeTitle();

        this.menu.repositionPlayerSlots(SLOT_X_OFFSET, this.invTopY, this.hotbarY);

        this.menu.repositionPlayerSlots(SLOT_X_OFFSET, this.invTopY, this.hotbarY);
        this.menu.repositionPatternSlot(-10000, -10000);   // esconde em todas as telas

        if (hasSearchBox()) {
            this.searchBox = new EditBox(
                    this.font, this.leftPos + 87, this.topPos + 6, 80, 14,
                    Component.translatable("gui.universal_vault.search_hint"));
            this.searchBox.setMaxLength(64);
            this.searchBox.setResponder(q -> {
                int debounce = VaultConfig.get().searchDebounceMs();
                if (debounce <= 0) {
                    applyFilter(q, true);
                    pendingSearch = null;
                } else {
                    pendingSearch = q;
                    lastSearchTime = System.currentTimeMillis();
                }
            });
            this.addRenderableWidget(this.searchBox);
        }

        sendSyncRequest();
    }

    // -----------------------------------------------------------------
    // Render
    // -----------------------------------------------------------------


    //? if >=26.1 {
    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderScreen(graphics, mouseX, mouseY, partialTick);
    }
    //?} else {
    /*@Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderScreen(graphics, mouseX, mouseY, partialTick);
    }
    *///?}

    //? if >=26.1 {
    private void renderScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*private void renderScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
     *///?}
        if (pendingSearch != null) {
            int debounce = VaultConfig.get().searchDebounceMs();
            if (System.currentTimeMillis() - lastSearchTime >= debounce) {
                applyFilter(pendingSearch, true);
                pendingSearch = null;
            }
        }

        if (pendingRebuild && !shouldDeferRebuild(mouseX, mouseY)) {
            pendingRebuild = false;
            applyFilter(this.searchBox != null ? this.searchBox.getValue() : "", false);
        }

        if (isContainerView()) {
            drawGrid(graphics, mouseX, mouseY);
            drawScrollbar(graphics);
            drawSortButton(graphics, mouseX, mouseY);
        } else {
            drawCustomContent(graphics, mouseX, mouseY, partialTick);
        }
        drawTabs(graphics, mouseX, mouseY);
        drawExtra(graphics, mouseX, mouseY, partialTick);

        //? if >=26.1 {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        //?} else {
        /*super.render(graphics, mouseX, mouseY, partialTick);
         *///?}

        if (isContainerView()) {
            int idx = getHoveredIndex(mouseX, mouseY);
            if (idx >= 0 && idx < filteredEntries.size()) {
                ItemStack stack = filteredEntries.get(idx).key().toStack(1);
                //? if >=26.1 {
                graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                //?} else {
                /*graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
                 *///?}
            }
        }
        // onPostRender(graphics, mouseX, mouseY, partialTick);
    }

    //? if >=26.1 {
    @Override
    public void extractBackground(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
    //?} else {
    /*@Override
    protected void renderBg(@Nonnull GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        *///?}
        int x0 = this.leftPos;
        int y0 = this.topPos;
        int x1 = x0 + GUI_WIDTH;

        drawBevel(graphics, x0, y0, GUI_WIDTH, this.guiHeight, COLOR_BG);

        int sepY = y0 + this.invTopY - 6;
        graphics.fill(x0 + 4, sepY, x1 - 10, sepY + 1, COLOR_BORDER_DARK);
        graphics.fill(x0 + 4, sepY + 1, x1 - 10, sepY + 2, COLOR_BORDER_LIGHT);

        for (Slot slot : this.menu.slots) {
            drawSlotBackground(graphics, x0 + slot.x - 1, y0 + slot.y - 1);
        }

        if (!isContainerView()) return;

        int gx = x0 + SLOT_X_OFFSET;
        int gy = y0 + GRID_TOP;
        int gGridBottom = gy + this.gridHeight;
        graphics.fill(gx, gy, gx + GRID_WIDTH, gGridBottom, COLOR_GRID_BG);

        int startIndex = scrollOffset * GRID_COLS;
        for (int row = 0; row < this.gridRows; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cellX = gx + col * CELL_SIZE;
                int cellY = gy + row * CELL_SIZE;
                int idx = startIndex + row * GRID_COLS + col;
                if (idx < filteredEntries.size()) {
                    drawCellBackground(graphics, filteredEntries.get(idx), cellX, cellY);
                } else {
                    drawSlotBackground(graphics, cellX, cellY);
                }
            }
        }
    }

    //? if >=26.1 {
    protected void drawCellBackground(GuiGraphicsExtractor g, E entry, int cellX, int cellY) {
    //?} else {
    /*protected void drawCellBackground(GuiGraphics g, E entry, int cellX, int cellY) {
     *///?}
        drawSlotBackground(g, cellX, cellY);
    }

    //? if >=26.1 {
    protected void drawSlotBackground(GuiGraphicsExtractor g, int sx, int sy) {
    //?} else {
    /*protected void drawSlotBackground(GuiGraphics g, int sx, int sy) {
     *///?}
        drawSlotBackground(g, sx, sy, COLOR_SLOT_BG);
    }

    //? if >=26.1 {
    protected void drawSlotBackground(GuiGraphicsExtractor g, int sx, int sy, int interiorColor) {
    //?} else {
    /*protected void drawSlotBackground(GuiGraphics g, int sx, int sy, int interiorColor) {
     *///?}
        drawBevelInset(g, sx, sy, CELL_SIZE, CELL_SIZE, interiorColor);
    }

    //? if >=26.1 {
    private void drawGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    //?} else {
    /*private void drawGrid(GuiGraphics graphics, int mouseX, int mouseY) {
     *///?}
        int startX = this.leftPos + SLOT_X_OFFSET;
        int startY = this.topPos + GRID_TOP;
        int startIndex = scrollOffset * GRID_COLS;
        int hovered = getHoveredIndex(mouseX, mouseY);

        for (int row = 0; row < this.gridRows; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int idx = startIndex + row * GRID_COLS + col;
                if (idx >= filteredEntries.size()) return;

                E entry = filteredEntries.get(idx);
                int cellX = startX + col * CELL_SIZE;
                int cellY = startY + row * CELL_SIZE;

                if (idx == hovered) {
                    graphics.fill(cellX + 1, cellY + 1, cellX + 17, cellY + 17, 0x80FFFFFF);
                }

                ItemStack stack = entry.key().toStack(1);
                //? if >=26.1 {
                graphics.item(stack, cellX + 1, cellY + 1);
                //?} else {
                /*graphics.renderItem(stack, cellX + 1, cellY + 1);
                 *///?}

                drawEntryExtra(graphics, entry, cellX, cellY);
            }
        }
    }

    //? if >=26.1 {
    private void drawScrollbar(GuiGraphicsExtractor graphics) {
    //?} else {
    /*private void drawScrollbar(GuiGraphics graphics) {
     *///?}
        int totalRows = currentRowCount();
        int maxScroll = Math.max(0, totalRows - this.gridRows);
        if (maxScroll <= 0) return;

        int trackX = this.leftPos + SCROLLBAR_X;
        int trackY = this.topPos + GRID_TOP;
        int trackH = this.gridHeight;

        graphics.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + trackH, COLOR_GRID_BG);

        int thumbH = Math.max(10, (trackH * this.gridRows) / totalRows);
        int thumbY = trackY + ((trackH - thumbH) * scrollOffset) / maxScroll;
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, 0xFFDDDDDD);
        graphics.fill(trackX, thumbY, trackX + 1, thumbY + thumbH, COLOR_BORDER_LIGHT);
        graphics.fill(trackX, thumbY + thumbH - 1, trackX + SCROLLBAR_W, thumbY + thumbH, COLOR_BORDER_DARK);
    }

    protected int sortBtnX() { return this.leftPos + GUI_WIDTH; }
    protected int sortBtnY() { return this.topPos + GRID_TOP; }
    protected int sortBtnW() { return TAB_STRIP_WIDTH; }
    protected int sortBtnH() { return TAB_HEIGHT; }
    protected boolean isOnSortButton(double mx, double my) {
        int bx = sortBtnX(), by = sortBtnY();
        return mx >= bx && mx < bx + sortBtnW()
                && my >= by && my < by + sortBtnH();
    }

    //? if >=26.1 {
    private void drawSortButton(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawSortButton(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        int bx = sortBtnX(), by = sortBtnY();
        int bw = sortBtnW(), bh = sortBtnH();
        boolean hover = isOnSortButton(mouseX, mouseY);

        int bg = hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG;
        g.fill(bx, by, bx + bw, by + bh, bg);
        g.fill(bx, by, bx + bw, by + 1, COLOR_BORDER_LIGHT);
        g.fill(bx, by, bx + 1, by + bh, COLOR_BORDER_LIGHT);
        g.fill(bx, by + bh - 1, bx + bw, by + bh, COLOR_BORDER_DARK);
        g.fill(bx + bw - 1, by, bx + bw, by + bh, COLOR_BORDER_DARK);

        Component label = Component.literal(sortMode.shortLabel);
        int tw = this.font.width(label);
        int tx = bx + (bw - tw) / 2;
        int ty = by + (bh - 8) / 2 + 1;
        //? if >=26.1 {
        g.text(this.font, label, tx, ty, COLOR_BTN_TEXT, false);
        //?} else {
        /*g.drawString(this.font, label, tx, ty, COLOR_BTN_TEXT, false);
         *///?}
    }

    //? if >=26.1 {
    private void drawTabs(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawTabs(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        VaultTab current = currentTab();
        for (VaultTab tab : VaultTab.values()) {
            int[] r = tabRect(tab);
            int x = r[0], y = r[1], w = r[2], h = r[3];
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
            boolean active = tab == current;

            int bg = (active || hover) ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG;
            g.fill(x, y, x + w, y + h, bg);
            g.fill(x, y, x + w, y + 1, COLOR_BORDER_LIGHT);
            g.fill(x, y, x + 1, y + h, COLOR_BORDER_LIGHT);
            g.fill(x, y + h - 1, x + w, y + h, COLOR_BORDER_DARK);
            g.fill(x + w - 1, y, x + w, y + h, COLOR_BORDER_DARK);

            Component label = Component.literal(tab.label);
            int tw = this.font.width(label);
            int tx = x + (w - tw) / 2;
            int ty = y + (h - 8) / 2 + 1;

            //? if >=26.1 {
            g.text(this.font, label, tx, ty, COLOR_BTN_TEXT, false);
            //?} else {
            /*g.drawString(this.font, label, tx, ty, COLOR_BTN_TEXT, false);
             *///?}
        }
    }

    //? if >=26.1 {
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(this.font, this.currentTitle,
                this.titleLabelX, this.titleLabelY, COLOR_TITLE_TEXT, false);
        graphics.text(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, COLOR_LABEL_TEXT, false);

        if (isContainerView()) {
            Component counter = Component.literal(filteredEntries.size() + " / " + allEntries.size());
            int textW = this.font.width(counter);
            graphics.text(this.font, counter, (GUI_WIDTH - textW) / 2, this.guiHeight + 1,
                    COLOR_COUNTER_TEXT, false);
        }
    }
    //?} else {
    /*@Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.currentTitle,
                this.titleLabelX, this.titleLabelY, COLOR_TITLE_TEXT, false);
        graphics.drawString(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, COLOR_LABEL_TEXT, false);

        if (isContainerView()) {
            Component counter = Component.literal(filteredEntries.size() + " / " + allEntries.size());
            int textW = this.font.width(counter);
            graphics.drawString(this.font, counter, (GUI_WIDTH - textW) / 2, this.guiHeight + 1,
                    COLOR_COUNTER_TEXT, false);
        }
    }
    *///?}

    @Override
    public void mouseMoved(double mx, double my) {
        this.lastMouseX = mx;
        this.lastMouseY = my;
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (isContainerView() && isInsideGrid(mx, my)) {
            int maxScroll = Math.max(0, currentRowCount() - this.gridRows);
            scrollOffset = Math.clamp(scrollOffset - (int) Math.signum(sy), 0, maxScroll);
            return true;
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int button = event.button();
        boolean shift = event.hasShiftDown();

        VaultTab clickedTab = tabAt(mx, my);
        if (clickedTab != null) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT && clickedTab != currentTab()) {
                VaultScreenRouter.open(clickedTab, this.menu, this.originalTitle);
            }
            return true;
        }

        if (onExtraClick(mx, my, button, shift)) return true;

        if (isContainerView()) {
            if (isOnSortButton(mx, my)) {
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    this.sortMode = this.sortMode.next();
                } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                    this.sortMode = this.sortMode.previous();
                } else {
                    return true;
                }
                VaultClientConfig.get().setSortMode(this.sortMode);
                applyFilter(this.searchBox != null ? this.searchBox.getValue() : "", true);
                return true;
            }

            if (isOnScrollbar(mx, my)) {
                this.draggingScrollbar = true;
                updateScrollFromMouse(my);
                return true;
            }

            if (isInsideGrid(mx, my)) {
                int idx = getHoveredIndex(mx, my);
                if (idx < 0) return true;
                E entry = (idx < filteredEntries.size()) ? filteredEntries.get(idx) : null;
                if (onGridClick(entry, idx, button, shift)) return true;
                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(@Nonnull MouseButtonEvent event) {
        if (this.draggingScrollbar) {
            this.draggingScrollbar = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(@Nonnull MouseButtonEvent event, double dragX, double dragY) {
        if (this.draggingScrollbar) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean keyPressed(@Nonnull KeyEvent event) {
        return super.keyPressed(event);
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        int stripLeft = xo - TAB_STRIP_WIDTH;
        if (mx >= stripLeft && mx < xo && my >= yo && my < yo + this.guiHeight) return false;
        int stripRight = xo + GUI_WIDTH;
        if (mx >= stripRight && mx < stripRight + TAB_STRIP_WIDTH
                && my >= yo && my < yo + this.guiHeight) return false;
        return (mx < (double) xo || my < (double) yo
                || mx >= (double) (xo + GUI_WIDTH) || my >= (double) (yo + this.guiHeight));
    }

    protected void applyFilter(String query, boolean resetScroll) {
        filteredEntries.clear();
        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            filteredEntries.addAll(allEntries);
        } else {
            boolean modMode = trimmed.startsWith("@");
            boolean tagMode = trimmed.startsWith("#");
            String term = trimmed.substring(modMode || tagMode ? 1 : 0).toLowerCase(Locale.ROOT);
            for (E data : allEntries) {
                boolean matches;
                if (modMode) {
                    matches = data.modId().toLowerCase(Locale.ROOT).contains(term);
                } else if (tagMode) {
                    matches = data.tags().stream()
                            .anyMatch(t -> t.toString().toLowerCase(Locale.ROOT).contains(term));
                } else {
                    matches = data.displayName().toLowerCase(Locale.ROOT).contains(term)
                            || data.tooltip().stream()
                            .anyMatch(c -> c.getString().toLowerCase(Locale.ROOT).contains(term));
                }
                if (matches) filteredEntries.add(data);
            }
        }
        filteredEntries.sort(this::compareEntries);

        if (resetScroll) scrollOffset = 0;
        else {
            int maxScroll = Math.max(0, currentRowCount() - this.gridRows);
            scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
        }
    }

    protected void setEntries(List<E> entries) {
        this.allEntries.clear();
        this.allEntries.addAll(entries);
        applyFilter(this.searchBox != null ? this.searchBox.getValue() : "", true);
    }

    protected boolean isInsideGrid(double mx, double my) {
        int gx = this.leftPos + SLOT_X_OFFSET;
        int gy = this.topPos + GRID_TOP;
        return mx >= gx && mx < gx + GRID_WIDTH && my >= gy && my < gy + this.gridHeight;
    }

    protected int getHoveredIndex(double mx, double my) {
        if (!isInsideGrid(mx, my)) return -1;
        int gx = this.leftPos + SLOT_X_OFFSET;
        int gy = this.topPos + GRID_TOP;
        int col = (int) ((mx - gx) / CELL_SIZE);
        int row = (int) ((my - gy) / CELL_SIZE);
        if (col < 0 || col >= GRID_COLS || row < 0 || row >= this.gridRows) return -1;
        return scrollOffset * GRID_COLS + row * GRID_COLS + col;
    }

    protected boolean isOnScrollbar(double mx, double my) {
        int maxScroll = Math.max(0, currentRowCount() - this.gridRows);
        if (maxScroll <= 0) return false;
        int trackX = this.leftPos + SCROLLBAR_X;
        int trackY = this.topPos + GRID_TOP;
        return mx >= trackX && mx < trackX + SCROLLBAR_W && my >= trackY && my < trackY + this.gridHeight;
    }

    protected void updateScrollFromMouse(double my) {
        int totalRows = currentRowCount();
        int maxScroll = Math.max(0, totalRows - this.gridRows);
        if (maxScroll <= 0) return;
        int trackY = this.topPos + GRID_TOP;
        int trackH = this.gridHeight;
        int thumbH = Math.max(10, (trackH * this.gridRows) / totalRows);
        double localY = my - trackY - thumbH / 2.0;
        double maxThumbY = trackH - thumbH;
        double thumbY = Math.clamp(localY, 0, maxThumbY);
        double ratio = maxThumbY <= 0 ? 0 : thumbY / maxThumbY;
        this.scrollOffset = (int) Math.round(ratio * maxScroll);
    }

    protected int currentRowCount() {
        return (int) Math.ceil(filteredEntries.size() / (double) GRID_COLS);
    }

    protected int[] tabRect(VaultTab tab) {
        int x = this.leftPos - TAB_STRIP_WIDTH;
        int y = this.topPos + TAB_TOP_OFFSET + tab.ordinal() * (TAB_HEIGHT + TAB_GAP);
        return new int[] { x, y, TAB_STRIP_WIDTH, TAB_HEIGHT };
    }

    @Nullable
    protected VaultTab tabAt(double mx, double my) {
        for (VaultTab tab : VaultTab.values()) {
            int[] r = tabRect(tab);
            if (mx >= r[0] && mx < r[0] + r[2] && my >= r[1] && my < r[1] + r[3]) return tab;
        }
        return null;
    }

    protected Component computeTitle() {
        return Component.translatable(currentTab().titleKey);
    }

    protected static int patternBgColor(VaultPattern p, long stock) {
        if (p.paused()) return BG_STATUS_PAUSED;
        if (p.triggerMode() == VaultPattern.TriggerMode.MANUAL) return BG_STATUS_MANUAL;
        if (p.max() == 0 && p.min() == 0) return BG_STATUS_UNKNOWN;
        if (stock >= p.max()) return BG_STATUS_SATISFIED;
        if (stock >= p.min()) return BG_STATUS_PROCESSING;
        return BG_STATUS_BLOCKED;
    }

    protected int gridBottomFromBottom() { return GRID_BOTTOM_FROM_BOTTOM; }

    //? if >=26.1 {
    protected void drawExtra(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {}
    //?} else {
    /*protected void drawExtra(GuiGraphics g, int mouseX, int mouseY, float partialTick) {}
     *///?}

    protected boolean onExtraClick(double mx, double my, int button, boolean shift) { return false; }
}
