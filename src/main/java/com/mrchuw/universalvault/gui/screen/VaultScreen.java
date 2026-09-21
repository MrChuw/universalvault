package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.config.UniversalVaultClientConfig;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.payload.C2SRequestSyncPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class VaultScreen extends AbstractContainerScreen<VaultMenu> {

    // -----------------------------------------------------------------
    // Layout
    // -----------------------------------------------------------------

    private static final int GUI_WIDTH = 176;
    private static final int MIN_GUI_HEIGHT = 208;

    private static final int INV_TOP_FROM_BOTTOM = 82;
    private static final int HOTBAR_FROM_BOTTOM = 24;

    private static final int SLOT_X_OFFSET = 5;
    private static final int CELL_SIZE = 18;
    private static final int GRID_COLS = 9;
    private static final int GRID_TOP = 24;
    private static final int GRID_BOTTOM_FROM_BOTTOM = 100;
    private static final int GRID_WIDTH = GRID_COLS * CELL_SIZE;

    private static final int SCROLLBAR_X = 170;
    private static final int SCROLLBAR_W = 4;

    private static final int SORT_BTN_W = 70;
    private static final int SORT_BTN_H = 12;
    private static final int SORT_BTN_RIGHT_MARGIN = 10;

    private static final int COLOR_BG = 0xFFC6C6C6;
    private static final int COLOR_BORDER_DARK = 0xFF373737;
    private static final int COLOR_BORDER_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_GRID_BG = 0xFF555555;
    private static final int COLOR_SLOT_BG = 0xFF8B8B8B;
    private static final int COLOR_SLOT_SHADOW = 0xFF373737;
    private static final int COLOR_SLOT_LIGHT = 0xFFFFFFFF;
    private static final int COLOR_BTN_BG = 0xFF9E9E9E;
    private static final int COLOR_BTN_BG_HOVER = 0xFFB8B8B8;
    private static final int COLOR_BTN_TEXT = 0xFF202020;

    private int guiHeight = MIN_GUI_HEIGHT;
    private int gridRows;
    private int gridHeight;
    private int invTopY;
    private int hotbarY;

    private boolean draggingScrollbar = false;
    private VaultSortMode sortMode = VaultSortMode.COUNT_DESC;

    private boolean pendingRebuild = false;
    private long actionFreezeUntil = 0;
    private static final long ACTION_FREEZE_MS = 1000;

    private EditBox searchBox;
    private final List<FilterableItemData> allItems = new ArrayList<>();
    private final List<FilterableItemData> filteredItems = new ArrayList<>();
    private int scrollOffset = 0;

    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private String pendingSearch = null;
    private long lastSearchTime = 0;

    private record FilterableItemData(
            ItemKey key,
            long count,
            String displayName,
            String modId,
            Set<Identifier> tags,
            List<Component> tooltip
    ) {}

    public VaultScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        //? if >=26.1 {
        super(menu, playerInventory, title, GUI_WIDTH, initialGuiHeight());
        //?} else {
        /*super(menu, playerInventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = initialGuiHeight();
        *///?}
    }

    private static int initialGuiHeight() {
        int windowHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return Math.max(MIN_GUI_HEIGHT, windowHeight - 40);
    }

    private void computeLayout() {
        this.guiHeight = Math.max(MIN_GUI_HEIGHT, this.height - 40);
        this.invTopY = this.guiHeight - INV_TOP_FROM_BOTTOM;
        this.hotbarY = this.guiHeight - HOTBAR_FROM_BOTTOM;

        int gridBottom = this.guiHeight - GRID_BOTTOM_FROM_BOTTOM;
        int availableHeight = gridBottom - GRID_TOP;
        this.gridRows = Math.max(1, availableHeight / CELL_SIZE);
        this.gridHeight = this.gridRows * CELL_SIZE;
    }

    @Override
    protected void init() {
        this.sortMode = UniversalVaultClientConfig.CONFIG.sortMode.get();
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

        this.menu.repositionPlayerSlots(SLOT_X_OFFSET, this.invTopY, this.hotbarY);

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + 87,
                this.topPos + 6,
                80,
                14,
                Component.translatable("gui.universal_vault.search_hint")
        );
        this.searchBox.setMaxLength(64);
        this.searchBox.setResponder(q -> {
            int debounce = UniversalVaultConfig.CONFIG.searchDebounceMs.get();
            if (debounce <= 0) {
                applyFilter(q, true);
                pendingSearch = null;
            } else {
                pendingSearch = q;
                lastSearchTime = System.currentTimeMillis();
            }
        });
        this.addRenderableWidget(this.searchBox);

        ClientPacketDistributor.sendToServer(new C2SRequestSyncPayload());
    }

    // -----------------------------------------------------------------
    // RENDER
    // -----------------------------------------------------------------

    //? if >=26.1 {
    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderVaultScreen(graphics, mouseX, mouseY, partialTick);
    }
     //?} else {
    /*@Override
    public void render(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderVaultScreen(graphics, mouseX, mouseY, partialTick);
    }

    *///?}

    //? if >=26.1 {
    private void renderVaultScreen(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      //?} else {
    /*private void renderVaultScreen(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        *///?}
        if (pendingSearch != null) {
            int debounce = UniversalVaultConfig.CONFIG.searchDebounceMs.get();
            if (System.currentTimeMillis() - lastSearchTime >= debounce) {
                applyFilter(pendingSearch, true);
                pendingSearch = null;
            }
        }

        if (pendingRebuild && !isFrozen(mouseX, mouseY)) {
            pendingRebuild = false;
            rebuildFiltered(false);
        }

        drawGrid(graphics, mouseX, mouseY);
        drawScrollbar(graphics);
        drawSortButton(graphics, mouseX, mouseY);

        //? if >=26.1 {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
          //?} else {
        /*super.render(graphics, mouseX, mouseY, partialTick);
        *///?}

        int idx = getHoveredIndex(mouseX, mouseY);
        if (idx >= 0 && idx < filteredItems.size()) {
            ItemStack stack = filteredItems.get(idx).key().toStack(1);
            //? if >=26.1 {
            graphics.setTooltipForNextFrame(this.font, stack, mouseX, mouseY);
              //?} else {
            /*graphics.setTooltipForNextFrame(
                    this.font,
                    stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL),
                    stack.getTooltipImage(),
                    stack,
                    mouseX,
                    mouseY,
                    (Identifier) stack.get(DataComponents.TOOLTIP_STYLE)
            );
            *///?}
        }
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
        int y1 = y0 + this.guiHeight;

        graphics.fill(x0, y0, x1, y1, COLOR_BG);
        graphics.fill(x0, y0, x1, y0 + 1, COLOR_BORDER_LIGHT);
        graphics.fill(x0, y0, x0 + 1, y1, COLOR_BORDER_LIGHT);
        graphics.fill(x0, y1 - 1, x1, y1, COLOR_BORDER_DARK);
        graphics.fill(x1 - 1, y0, x1, y1, COLOR_BORDER_DARK);

        int sepY = y0 + this.invTopY - 6;
        graphics.fill(x0 + 4, sepY, x1 - 10, sepY + 1, COLOR_BORDER_DARK);
        graphics.fill(x0 + 4, sepY + 1, x1 - 10, sepY + 2, COLOR_BORDER_LIGHT);

        for (Slot slot : this.menu.slots) {
            drawSlotBackground(graphics, x0 + slot.x - 1, y0 + slot.y - 1);
        }

        int gx = x0 + SLOT_X_OFFSET;
        int gy = y0 + GRID_TOP;
        int gGridBottom = gy + this.gridHeight;
        graphics.fill(gx, gy, gx + GRID_WIDTH, gGridBottom, COLOR_GRID_BG);

        for (int row = 0; row < this.gridRows; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                int cellX = gx + col * CELL_SIZE;
                int cellY = gy + row * CELL_SIZE;
                drawSlotBackground(graphics, cellX, cellY);
            }
        }
    }

    //? if >=26.1 {
    private void drawSlotBackground(GuiGraphicsExtractor g, int sx, int sy) {
      //?} else {
    /*private void drawSlotBackground(GuiGraphics g, int sx, int sy) {
        *///?}
        g.fill(sx, sy, sx + 18, sy + 1, COLOR_SLOT_SHADOW);
        g.fill(sx, sy, sx + 1, sy + 18, COLOR_SLOT_SHADOW);
        g.fill(sx, sy + 17, sx + 18, sy + 18, COLOR_SLOT_LIGHT);
        g.fill(sx + 17, sy, sx + 18, sy + 18, COLOR_SLOT_LIGHT);
        g.fill(sx + 1, sy + 1, sx + 17, sy + 17, COLOR_SLOT_BG);
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
                if (idx >= filteredItems.size()) return;

                FilterableItemData data = filteredItems.get(idx);
                int cellX = startX + col * CELL_SIZE;
                int cellY = startY + row * CELL_SIZE;

                if (idx == hovered) {
                    graphics.fill(cellX + 1, cellY + 1, cellX + 17, cellY + 17, 0x80FFFFFF);
                }

                ItemStack stack = data.key().toStack(1);
                //? if >=26.1 {
                graphics.item(stack, cellX + 1, cellY + 1);
            graphics.itemDecorations(this.font, stack, cellX + 1, cellY + 1, formatCount(data.count()));
             //?} else {
                /*graphics.renderItem(stack, cellX + 1, cellY + 1);
                graphics.renderItemDecorations(this.font, stack, cellX + 1, cellY + 1, formatCount(data.count()));
                *///?}
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

        graphics.fill(trackX, trackY, trackX + SCROLLBAR_W, trackY + trackH, 0xFF555555);

        int thumbH = Math.max(10, (trackH * this.gridRows) / totalRows);
        int thumbY = trackY + ((trackH - thumbH) * scrollOffset) / maxScroll;
        graphics.fill(trackX, thumbY, trackX + SCROLLBAR_W, thumbY + thumbH, 0xFFDDDDDD);
        graphics.fill(trackX, thumbY, trackX + 1, thumbY + thumbH, 0xFFFFFFFF);
        graphics.fill(trackX, thumbY + thumbH - 1, trackX + SCROLLBAR_W, thumbY + thumbH, 0xFF373737);
    }

    private int sortBtnX() {
        return this.leftPos + GUI_WIDTH - SORT_BTN_W - SORT_BTN_RIGHT_MARGIN;
    }

    private int sortBtnY() {
        return this.topPos + this.invTopY - 19;
    }

    private boolean isOnSortButton(double mx, double my) {
        int bx = sortBtnX();
        int by = sortBtnY();
        return mx >= bx && mx < bx + SORT_BTN_W && my >= by && my < by + SORT_BTN_H;
    }

    //? if >=26.1 {
    private void drawSortButton(GuiGraphicsExtractor g, int mouseX, int mouseY) {
      //?} else {
    /*private void drawSortButton(GuiGraphics g, int mouseX, int mouseY) {
        *///?}
        int bx = sortBtnX();
        int by = sortBtnY();
        boolean hover = isOnSortButton(mouseX, mouseY);

        int bg = hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG;
        g.fill(bx, by, bx + SORT_BTN_W, by + SORT_BTN_H, bg);

        g.fill(bx, by, bx + SORT_BTN_W, by + 1, COLOR_BORDER_LIGHT);
        g.fill(bx, by, bx + 1, by + SORT_BTN_H, COLOR_BORDER_LIGHT);
        g.fill(bx, by + SORT_BTN_H - 1, bx + SORT_BTN_W, by + SORT_BTN_H, COLOR_BORDER_DARK);
        g.fill(bx + SORT_BTN_W - 1, by, bx + SORT_BTN_W, by + SORT_BTN_H, COLOR_BORDER_DARK);

        Component label = Component.translatable(sortMode.langKey);
        int textW = this.font.width(label);
        int textX = bx + (SORT_BTN_W - textW) / 2;
        int textY = by + (SORT_BTN_H - 8) / 2 + 1;

        //? if >=26.1 {
        g.text(this.font, label, textX, textY, COLOR_BTN_TEXT, false);
          //?} else {
        /*g.drawString(this.font, label, textX, textY, COLOR_BTN_TEXT, false);
        *///?}
    }

    //? if >=26.1 {
    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);

        Component counter = Component.literal(filteredItems.size() + " / " + allItems.size());
        int textW = this.font.width(counter);
        int x = (GUI_WIDTH - textW) / 2;
        int y = this.guiHeight + 1;
        graphics.text(this.font, counter, x, y, 0xFF606060, false);
    }
     //?} else {
    /*@Override
    protected void renderLabels(@Nonnull GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);

        Component counter = Component.literal(filteredItems.size() + " / " + allItems.size());
        int textW = this.font.width(counter);
        int x = (GUI_WIDTH - textW) / 2;
        int y = this.guiHeight + 1;
        graphics.drawString(this.font, counter, x, y, 0xFF606060, false);
    }

    *///?}

    // -----------------------------------------------------------------
    // INPUT
    // -----------------------------------------------------------------

    @Override
    public void mouseMoved(double mx, double my) {
        this.lastMouseX = mx;
        this.lastMouseY = my;
        super.mouseMoved(mx, my);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (isInsideGrid(mx, my)) {
            int totalRows = currentRowCount();
            int maxScroll = Math.max(0, totalRows - this.gridRows);
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

        if (isOnSortButton(mx, my)) {
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                this.sortMode = this.sortMode.next();
            } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
                this.sortMode = this.sortMode.previous();
            } else {
                return true;
            }

            UniversalVaultClientConfig.CONFIG.sortMode.set(this.sortMode);
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
            ItemKey key = idx >= 0 && idx < filteredItems.size() ? filteredItems.get(idx).key() : null;

            ItemStack carried = this.menu.getCarried();

            if (!carried.isEmpty()) {
                C2SVaultActionPayload.ActionType action =
                        button == InputConstants.MOUSE_BUTTON_LEFT
                                ? C2SVaultActionPayload.ActionType.DEPOSIT_ALL
                                : C2SVaultActionPayload.ActionType.DEPOSIT_ONE;
                sendVaultAction(action, key);
            } else if (key != null) {
                C2SVaultActionPayload.ActionType action;
                if (shift) {
                    action =
                            button == InputConstants.MOUSE_BUTTON_LEFT
                                    ? C2SVaultActionPayload.ActionType.QUICK_MOVE
                                    : C2SVaultActionPayload.ActionType.QUICK_MOVE_HALF;
                } else {
                    action =
                            button == InputConstants.MOUSE_BUTTON_LEFT
                                    ? C2SVaultActionPayload.ActionType.PICKUP_ALL
                                    : C2SVaultActionPayload.ActionType.PICKUP_HALF;
                }
                sendVaultAction(action, key);
            }
            return true;
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
        applyModifierState(event, true);
        if (event.key() == InputConstants.KEY_Q) {
            int idx = getHoveredIndex(this.lastMouseX, this.lastMouseY);
            if (idx >= 0 && idx < filteredItems.size()) {
                ItemKey key = filteredItems.get(idx).key();
                C2SVaultActionPayload.ActionType action = event.hasShiftDown()
                        ? C2SVaultActionPayload.ActionType.DROP_STACK
                        : C2SVaultActionPayload.ActionType.DROP_ONE;
                sendVaultAction(action, key);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private void sendVaultAction(C2SVaultActionPayload.ActionType action, ItemKey key) {
        ClientPacketDistributor.sendToServer(new C2SVaultActionPayload(action, key, 0, -1));
        registerAction();
    }

    @Override
    public boolean keyReleased(@Nonnull KeyEvent event) {
        applyModifierState(event, false);
        return super.keyReleased(event);
    }

    @Override
    protected boolean hasClickedOutside(double mx, double my, int xo, int yo) {
        return (
                mx < (double) xo ||
                        my < (double) yo ||
                        mx >= (double) (xo + GUI_WIDTH) ||
                        my >= (double) (yo + this.guiHeight)
        );
    }

    // -----------------------------------------------------------------
    // FILTER / SORT
    // -----------------------------------------------------------------

    private void applyFilter(String query, boolean resetScroll) {
        filteredItems.clear();

        String trimmed = query.trim();
        if (trimmed.isEmpty()) {
            filteredItems.addAll(allItems);
        } else {
            boolean modMode = trimmed.startsWith("@");
            boolean tagMode = trimmed.startsWith("#");
            String term = trimmed.substring(modMode || tagMode ? 1 : 0).toLowerCase(Locale.ROOT);
            for (FilterableItemData data : allItems) {
                boolean matches;
                if (modMode) {
                    matches = data.modId().toLowerCase(Locale.ROOT).contains(term);
                } else if (tagMode) {
                    matches = data
                            .tags()
                            .stream()
                            .anyMatch(t -> t.toString().toLowerCase(Locale.ROOT).contains(term));
                } else {
                    matches =
                            data.displayName().toLowerCase(Locale.ROOT).contains(term) ||
                                    data
                                            .tooltip()
                                            .stream()
                                            .anyMatch(c -> c.getString().toLowerCase(Locale.ROOT).contains(term));
                }
                if (matches) filteredItems.add(data);
            }
        }

        sortFiltered();

        if (resetScroll) {
            scrollOffset = 0;
        } else {
            int maxScroll = Math.max(0, currentRowCount() - this.gridRows);
            scrollOffset = Math.clamp(scrollOffset, 0, maxScroll);
        }
    }

    private void rebuildFiltered(boolean resetScroll) {
        applyFilter(this.searchBox != null ? this.searchBox.getValue() : "", resetScroll);
    }

    private void sortFiltered() {
        switch (sortMode) {
            case COUNT_DESC -> filteredItems.sort((a, b) -> Long.compare(b.count(), a.count()));
            case COUNT_ASC -> filteredItems.sort(Comparator.comparingLong(FilterableItemData::count));
            case NAME_ASC -> filteredItems.sort((a, b) -> a.displayName().compareToIgnoreCase(b.displayName()));
            case NAME_DESC -> filteredItems.sort((a, b) -> b.displayName().compareToIgnoreCase(a.displayName()));
            case MOD -> filteredItems.sort((a, b) -> {
                int c = a.modId().compareToIgnoreCase(b.modId());
                if (c != 0) return c;
                return a.displayName().compareToIgnoreCase(b.displayName());
            });
        }
    }

    // -----------------------------------------------------------------
    // SYNC
    // -----------------------------------------------------------------

    public void onVaultSync(S2CVaultSyncPayload payload) {
        this.allItems.clear();
        Minecraft client = Minecraft.getInstance();
        for (S2CVaultSyncPayload.Entry entry : payload.entries()) {
            ItemKey key = entry.key();
            ItemStack stack = key.toStack(1);
            if (stack.isEmpty()) continue;

            String displayName = stack.getHoverName().getString();
            Identifier regKey = key.itemId();
            String modId = regKey.getNamespace();

            Item item = key.resolveItem();
            Set<Identifier> tags =
                    item == null
                            ? Set.of()
                            : BuiltInRegistries.ITEM.wrapAsHolder(item)
                            .tags()
                            .map(TagKey::location)
                            .collect(Collectors.toSet());

            List<Component> tooltip = stack.getTooltipLines(
                    Item.TooltipContext.EMPTY,
                    client.player,
                    TooltipFlag.NORMAL
            );
            this.allItems.add(new FilterableItemData(key, entry.count(), displayName, modId, tags, tooltip));
        }

        if (isFrozen(this.lastMouseX, this.lastMouseY)) {
            updateVisibleCountsInPlace();
            this.pendingRebuild = true;
        } else {
            rebuildFiltered(false);
        }
    }

    // -----------------------------------------------------------------
    // HELPERS
    // -----------------------------------------------------------------

    private void updateVisibleCountsInPlace() {
        if (filteredItems.isEmpty()) return;

        Map<ItemKey, Long> newCounts = new HashMap<>();
        for (FilterableItemData d : allItems) {
            newCounts.put(d.key(), d.count());
        }

        for (int i = 0; i < filteredItems.size(); i++) {
            FilterableItemData old = filteredItems.get(i);
            Long newCount = newCounts.get(old.key());

            if (newCount == null) {
                if (old.count() != 0) {
                    filteredItems.set(
                            i,
                            new FilterableItemData(old.key(), 0L, old.displayName(), old.modId(), old.tags(), old.tooltip())
                    );
                }
            } else if (newCount != old.count()) {
                filteredItems.set(
                        i,
                        new FilterableItemData(
                                old.key(),
                                newCount,
                                old.displayName(),
                                old.modId(),
                                old.tags(),
                                old.tooltip()
                        )
                );
            }
        }
    }

    private boolean isInsideGrid(double mx, double my) {
        int gx = this.leftPos + SLOT_X_OFFSET;
        int gy = this.topPos + GRID_TOP;
        return mx >= gx && mx < gx + GRID_WIDTH && my >= gy && my < gy + this.gridHeight;
    }

    private int getHoveredIndex(double mx, double my) {
        if (!isInsideGrid(mx, my)) return -1;
        int gx = this.leftPos + SLOT_X_OFFSET;
        int gy = this.topPos + GRID_TOP;
        int col = (int) ((mx - gx) / CELL_SIZE);
        int row = (int) ((my - gy) / CELL_SIZE);
        if (col < 0 || col >= GRID_COLS || row < 0 || row >= this.gridRows) return -1;
        int idx = scrollOffset * GRID_COLS + row * GRID_COLS + col;
        return idx < filteredItems.size() ? idx : -1;
    }

    private String formatCount(long count) {
        if (count >= 1_000_000) return count / 1_000_000 + "M";
        if (count >= 1_000) return count / 1_000 + "k";
        return String.valueOf(count);
    }

    private boolean isOnScrollbar(double mx, double my) {
        int totalRows = currentRowCount();
        int maxScroll = Math.max(0, totalRows - this.gridRows);
        if (maxScroll <= 0) return false;

        int trackX = this.leftPos + SCROLLBAR_X;
        int trackY = this.topPos + GRID_TOP;
        return mx >= trackX && mx < trackX + SCROLLBAR_W && my >= trackY && my < trackY + this.gridHeight;
    }

    private void updateScrollFromMouse(double my) {
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

    private int currentRowCount() {
        return (int) Math.ceil(filteredItems.size() / (double) GRID_COLS);
    }

    private boolean shiftHeld = false;
    private boolean ctrlHeld = false;
    private boolean altHeld = false;

    private boolean isModifierHeld() {
        return shiftHeld || ctrlHeld || altHeld;
    }

    private void applyModifierState(KeyEvent event, boolean pressed) {
        if (event.hasShiftDown()) shiftHeld = pressed;
        if (event.hasControlDown()) ctrlHeld = pressed;
        if (event.hasAltDown()) altHeld = pressed;
    }

    private boolean isFrozen(double mouseX, double mouseY) {
        if (isInsideGrid(mouseX, mouseY) && isModifierHeld()) return true;
        return System.currentTimeMillis() < actionFreezeUntil;
    }

    private void registerAction() {
        this.actionFreezeUntil = System.currentTimeMillis() + ACTION_FREEZE_MS;
    }
}
