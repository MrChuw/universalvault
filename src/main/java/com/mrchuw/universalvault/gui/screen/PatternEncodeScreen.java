package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SEncodePatternPayload;
import com.mrchuw.universalvault.automation.network.C2SImportPatternPayload;
import com.mrchuw.universalvault.automation.network.C2SRequestCraftResultPayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import com.mrchuw.universalvault.gui.base.VaultScreenRouter;
import com.mrchuw.universalvault.gui.base.VaultTab;
import com.mrchuw.universalvault.gui.menu.EncodedPatternSlot;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.ItemKey;

import java.util.*;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.Nonnull;

public class PatternEncodeScreen extends VaultScreen {

    private enum Mode { CRAFTING, CUSTOM }

    private static final int CRAFT_SIZE = 3;
    private static final int CRAFT_SIDE = CRAFT_SIZE * CELL_SIZE;

    private static final int ENCODER_RESERVED = 160;

    private static final int CUSTOM_VISIBLE_ROWS = 3;
    private static final int CUSTOM_MAX_ROWS = 24;

    private Mode mode = Mode.CRAFTING;

    private boolean outputOverridden = false;
    private final Map<Integer, ItemStack> craftGrid = new HashMap<>();
    private ItemStack craftOutput = ItemStack.EMPTY;

    private final Map<Integer, ItemStack> customInputs = new LinkedHashMap<>();
    private final Map<Integer, ItemStack> customOutputs = new LinkedHashMap<>();
    private int customScroll = 0;
    private int customTotalRows = CUSTOM_VISIBLE_ROWS;

    private ItemStack lastPatternSlotStack = ItemStack.EMPTY;
    private VaultPattern loadedPattern = null;

    public PatternEncodeScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int slotX = GUI_WIDTH - PAD - encodeBtnW();
        int slotY = this.guiHeight - ENCODER_RESERVED + 6 + CELL_SIZE + 2;
        this.menu.repositionPatternSlot(slotX, slotY);
    }

    @Override protected VaultTab currentTab() { return VaultTab.ENCODE; }

    @Override protected int gridBottomFromBottom() { return ENCODER_RESERVED; }

    @Override
    protected boolean onGridClick(EntryData entry, int gridIndex, int button, boolean shift) {
        ItemStack carried = this.menu.getCarried();
        if (!carried.isEmpty()) return super.onGridClick(entry, gridIndex, button, shift);

        ItemKey key = entry != null ? entry.key() : null;
        if (key == null) return false;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            sendVaultAction(C2SVaultActionPayload.ActionType.PICKUP_ALL, key);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            sendVaultAction(C2SVaultActionPayload.ActionType.PICKUP_HALF, key);
        } else {
            return false;
        }
        return true;
    }

    private static final int TAB_ICON_SIZE = CELL_SIZE;
    private static final int TAB_ICON_GAP = 2;

    private int encoderTop() {
        return this.topPos + this.guiHeight - ENCODER_RESERVED;
    }

    private int tabsX() { return this.leftPos + SLOT_X_OFFSET; }

    private int tabCraftingY() {
        return encoderTop() + 4;
    }

    private int tabFurnaceY() {
        return tabCraftingY() + TAB_ICON_SIZE + TAB_ICON_GAP;
    }

    private int contentX() {
        return tabsX() + TAB_ICON_SIZE + GAP;
    }

    private int encodeBtnW() { return CELL_SIZE; }
    private int encodeBtnH() { return CELL_SIZE; }
    private int encodeBtnX() { return this.leftPos + GUI_WIDTH - PAD - encodeBtnW() - 1; }
    private int encodeBtnY() { return encoderTop() + 6; }

    private int craftGridX() { return contentX(); }
    private int craftGridY() { return encoderTop() + 8; }
    private int craftOutX() { return craftGridX() + CRAFT_SIDE + GAP * 2; }
    private int craftOutY() { return craftGridY() + (CRAFT_SIDE - CELL_SIZE) / 2; }

    private int customGridX() { return contentX(); }
    private int customGridY() { return encoderTop() + 8; }

    private int customInputColX(int col) { return customGridX() + col * CELL_SIZE; }
    private int customInputRowY(int visualRow) { return customGridY() + visualRow * CELL_SIZE; }
    private int customOutputColX() { return customGridX() + 4 * CELL_SIZE + GAP; }

    private static final int SCROLL_BTN = CELL_SIZE;

    private int customScrollUpX() { return customOutputColX() + CELL_SIZE + GAP; }
    private int customScrollUpY() { return customGridY(); }
    private int customScrollDnX() { return customScrollUpX(); }
    private int customScrollDnY() { return customGridY() + CELL_SIZE + 2; }
    private int customScrollBtnW() { return SCROLL_BTN; }
    private int customScrollBtnH() { return SCROLL_BTN; }

    @Override
    //? if >=26.1 {
    protected void drawExtra(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
    //?} else {
    /*protected void drawExtra(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
     *///?}
        syncWithPatternSlot();
        drawTabIcon(g, tabsX(), tabCraftingY(), new ItemStack(Blocks.CRAFTING_TABLE),
                mode == Mode.CRAFTING, mouseX, mouseY);
        drawTabIcon(g, tabsX(), tabFurnaceY(), new ItemStack(Blocks.FURNACE),
                mode == Mode.CUSTOM, mouseX, mouseY);

        if (mode == Mode.CRAFTING) {
            drawCraftingMode(g, mouseX, mouseY);
        } else {
            drawCustomMode(g, mouseX, mouseY);
        }

        drawEncodeButton(g, mouseX, mouseY);
    }

    //? if >=26.1 {
    private void drawTabIcon(GuiGraphicsExtractor g, int x, int y, ItemStack icon,
                             boolean active, int mouseX, int mouseY) {
    //?} else {
    /*private void drawTabIcon(GuiGraphics g, int x, int y, ItemStack icon,
                             boolean active, int mouseX, int mouseY) {
     *///?}
        boolean hover = mouseX >= x && mouseX < x + TAB_ICON_SIZE
                && mouseY >= y && mouseY < y + TAB_ICON_SIZE;

        if (active) {
            drawBevelInset(g, x, y, TAB_ICON_SIZE, TAB_ICON_SIZE,
                    COLOR_BTN_BG_PRESSED);
        } else {
            drawBevel(g, x, y, TAB_ICON_SIZE, TAB_ICON_SIZE,
                    hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG);
        }

        //? if >=26.1 {
        g.item(icon, x + 1, y + 1);
        //?} else {
        /*g.renderItem(icon, x + 1, y + 1);
         *///?}
    }

    //? if >=26.1 {
    private void drawEncodeButton(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawEncodeButton(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        int bx = encodeBtnX(), by = encodeBtnY();
        int bw = encodeBtnW(), bh = encodeBtnH();
        boolean hover = mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh;
        drawBevel(g, bx, by, bw, bh, hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG);

        ItemStack icon = new ItemStack(ModRegistry.VAULT_IO_BLOCK_ITEM.get());
        //? if >=26.1 {
        g.item(icon, bx + (bw - 16) / 2, by + (bh - 16) / 2);
        //?} else {
        /*g.renderItem(icon, bx + (bw - 16) / 2, by + (bh - 16) / 2);
         *///?}
    }

    //? if >=26.1 {
    private void drawCraftingMode(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawCraftingMode(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        int gx = craftGridX(), gy = craftGridY();
        for (int row = 0; row < CRAFT_SIZE; row++) {
            for (int col = 0; col < CRAFT_SIZE; col++) {
                int idx = row * CRAFT_SIZE + col + 1;
                int cx = gx + col * CELL_SIZE;
                int cy = gy + row * CELL_SIZE;
                drawSlotBackground(g, cx, cy);
                ItemStack s = craftGrid.get(idx);
                if (s != null && !s.isEmpty()) {
                    //? if >=26.1 {
                    g.item(s, cx + 1, cy + 1);
                    //?} else {
                    /*g.renderItem(s, cx + 1, cy + 1);
                     *///?}
                }
            }
        }

        Component arrow = Component.literal("→");
        int arrowW = this.font.width(arrow);
        int arrowX = gx + CRAFT_SIDE + (GAP * 2 - arrowW) / 2;
        int arrowY = gy + CRAFT_SIDE / 2 - 4;
        //? if >=26.1 {
        g.text(this.font, arrow, arrowX, arrowY, COLOR_LABEL_TEXT, false);
        //?} else {
        /*g.drawString(this.font, arrow, arrowX, arrowY, COLOR_LABEL_TEXT, false);
         *///?}

        int ox = craftOutX(), oy = craftOutY();
        drawSlotBackground(g, ox, oy);
        if (!craftOutput.isEmpty()) {
            drawStackWithCount(g, craftOutput, ox + 1, oy + 1);
        }
    }

    //? if >=26.1 {
    private void drawCustomMode(GuiGraphicsExtractor g, int mouseX, int mouseY) {
    //?} else {
    /*private void drawCustomMode(GuiGraphics g, int mouseX, int mouseY) {
     *///?}
        for (int vRow = 0; vRow < CUSTOM_VISIBLE_ROWS; vRow++) {
            int row = customScroll + vRow;
            int rowY = customInputRowY(vRow);

            for (int col = 0; col < 3; col++) {
                int slotIdx = row * 3 + col;
                int cx = customInputColX(col);
                drawSlotBackground(g, cx, rowY);
                ItemStack s = customInputs.get(slotIdx);
                if (s != null && !s.isEmpty()) {
                    drawStackWithCount(g, s, cx + 1, rowY + 1);
                }
            }

            int ox = customOutputColX();
            drawSlotBackground(g, ox, rowY);
            ItemStack o = customOutputs.get(row);
            if (o != null && !o.isEmpty()) {
                drawStackWithCount(g, o, ox + 1, rowY + 1);
            }
        }

        boolean canUp = customScroll > 0;
        boolean canDn = customScroll + CUSTOM_VISIBLE_ROWS < customTotalRows;
        drawScrollBtn(g, customScrollUpX(), customScrollUpY(), canUp, true, mouseX, mouseY);
        drawScrollBtn(g, customScrollDnX(), customScrollDnY(), canDn, false, mouseX, mouseY);
    }

    //? if >=26.1 {
    private void drawScrollBtn(GuiGraphicsExtractor g, int x, int y, boolean enabled,
                               boolean up, int mouseX, int mouseY) {
    //?} else {
    /*private void drawScrollBtn(GuiGraphics g, int x, int y, boolean enabled,
                               boolean up, int mouseX, int mouseY) {
    *///?}
        boolean hover = enabled && mouseX >= x && mouseX < x + customScrollBtnW()
                && mouseY >= y && mouseY < y + customScrollBtnH();
        int bg = !enabled ? COLOR_BTN_BG_PRESSED
                : (hover ? COLOR_BTN_BG_HOVER : COLOR_BTN_BG);
        drawBevel(g, x, y, customScrollBtnW(), customScrollBtnH(), bg);

        int cx = x + customScrollBtnW() / 2;
        int cy = y + customScrollBtnH() / 2;
        int col = enabled ? COLOR_BTN_TEXT : COLOR_COUNTER_TEXT;

        // up=true: wide at bottom, narrow at top → ▲
        // up=false: wide at top, narrow at bottom → ▼
        for (int i = 0; i < 4; i++) {
            int half = 3 - i;
            int rowY = up ? (cy + 3 - i) : (cy - 3 + i);
            g.fill(cx - half, rowY, cx + half + 1, rowY + 1, col);
        }
    }

    private void drawStackWithCount(
            //? if >=26.1 {
            GuiGraphicsExtractor g,
            //?} else {
            /*GuiGraphics g,
            *///?}
            ItemStack stack, int x, int y) {
        if (stack.isEmpty()) return;
        // "" (não null) suprime o número no count=1.
        String label = stack.getCount() >= 2 ? String.valueOf(stack.getCount()) : "";
        //? if >=26.1 {
        g.item(stack, x, y);
        g.itemDecorations(this.font, stack, x, y, label);
        //?} else {
        /*g.renderItem(stack, x, y);
        g.renderItemDecorations(this.font, stack, x, y, label);
         *///?}
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (mode == Mode.CUSTOM && Minecraft.getInstance().hasShiftDown()) {
            int slotIdx = customInputSlotAt(mx, my);
            if (slotIdx >= 0) {
                adjustCustomCount(customInputs, slotIdx, (int) Math.signum(sy));
                return true;
            }
            int outRow = customOutputRowAt(mx, my);
            if (outRow >= 0) {
                adjustCustomCount(customOutputs, outRow, (int) Math.signum(sy));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, sx, sy);
    }

    private int customInputSlotAt(double mx, double my) {
        for (int vRow = 0; vRow < CUSTOM_VISIBLE_ROWS; vRow++) {
            int row = customScroll + vRow;
            int rowY = customInputRowY(vRow);
            for (int col = 0; col < 3; col++) {
                int cx = customInputColX(col);
                if (inRect(mx, my, cx, rowY, CELL_SIZE, CELL_SIZE)) {
                    return row * 3 + col;
                }
            }
        }
        return -1;
    }

    private int customOutputRowAt(double mx, double my) {
        for (int vRow = 0; vRow < CUSTOM_VISIBLE_ROWS; vRow++) {
            int row = customScroll + vRow;
            int rowY = customInputRowY(vRow);
            int ox = customOutputColX();
            if (inRect(mx, my, ox, rowY, CELL_SIZE, CELL_SIZE)) return row;
        }
        return -1;
    }

    private void adjustCustomCount(Map<Integer, ItemStack> map, int key, int dir) {
        ItemStack s = map.get(key);
        if (s == null || s.isEmpty()) return;

        int step = scrollStep();
        int current = s.getCount();
        int next = current + dir * step;
        next = Math.max(1, Math.min(next, 4096));
        if (next == current) return;
        map.put(key, s.copyWithCount(next));
    }

    private static int scrollStep() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hasAltDown()) return 64;
        if (mc.hasControlDown()) return 8;
        return 1;
    }

    @Override
    protected boolean onExtraClick(double mx, double my, int button, boolean shift) {
        if (inRect(mx, my, tabsX(), tabCraftingY(), TAB_ICON_SIZE, TAB_ICON_SIZE)) {
            switchMode(Mode.CRAFTING);
            return true;
        }
        if (inRect(mx, my, tabsX(), tabFurnaceY(), TAB_ICON_SIZE, TAB_ICON_SIZE)) {
            switchMode(Mode.CUSTOM);
            return true;
        }

        if (inRect(mx, my, encodeBtnX(), encodeBtnY(), encodeBtnW(), encodeBtnH())) {
            Minecraft mc = Minecraft.getInstance();
            C2SEncodePatternPayload.Destination dest;
            if (mc.hasControlDown()) {
                dest = C2SEncodePatternPayload.Destination.LIBRARY;
            } else if (shift) {
                dest = C2SEncodePatternPayload.Destination.INVENTORY;
            } else {
                dest = C2SEncodePatternPayload.Destination.SLOT;
            }
            submitEncode(dest);
            return true;
        }

        if (mode == Mode.CRAFTING) {
            return handleCraftingClick(mx, my, button);
        }
        return handleCustomClick(mx, my, button);
    }

    private void switchMode(Mode newMode) {
        mode = newMode;
    }

    private boolean handleCraftingClick(double mx, double my, int button) {
        int gx = craftGridX(), gy = craftGridY();
        for (int row = 0; row < CRAFT_SIZE; row++) {
            for (int col = 0; col < CRAFT_SIZE; col++) {
                int idx = row * CRAFT_SIZE + col + 1;
                int cx = gx + col * CELL_SIZE;
                int cy = gy + row * CELL_SIZE;
                if (inRect(mx, my, cx, cy, CELL_SIZE, CELL_SIZE)) {
                    handleCraftCell(idx, button);
                    return true;
                }
            }
        }
        if (inRect(mx, my, craftOutX(), craftOutY(), CELL_SIZE, CELL_SIZE)) {
            handleCraftOutput(button);
            return true;
        }
        return false;
    }

    private void handleCraftCell(int idx, int button) {
        ItemStack carried = this.menu.getCarried();
        ItemStack cur = craftGrid.get(idx);

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (!carried.isEmpty()) {
                craftGrid.put(idx, carried.copyWithCount(1));
            } else if (cur != null && !cur.isEmpty()) {
                craftGrid.remove(idx);
            }
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            craftGrid.remove(idx);
        }
        outputOverridden = false;
        refreshOutputFromRecipe();
    }

    private void handleCraftOutput(int button) {
        ItemStack carried = this.menu.getCarried();
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (!carried.isEmpty()) {
                craftOutput = carried.copyWithCount(1);
                outputOverridden = true;
            } else if (!craftOutput.isEmpty()) {
                craftOutput = ItemStack.EMPTY;
                outputOverridden = false;
                refreshOutputFromRecipe();
            }
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            craftOutput = ItemStack.EMPTY;
            outputOverridden = false;
            refreshOutputFromRecipe();
        }
    }

    private boolean handleCustomClick(double mx, double my, int button) {
        if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
            int slotIdx = customInputSlotAt(mx, my);
            if (slotIdx >= 0) { openAmountPicker(customInputs, slotIdx); return true; }
            int outRow = customOutputRowAt(mx, my);
            if (outRow >= 0) { openAmountPicker(customOutputs, outRow); return true; }
            return false;
        }

        for (int vRow = 0; vRow < CUSTOM_VISIBLE_ROWS; vRow++) {
            int row = customScroll + vRow;
            int rowY = customInputRowY(vRow);

            for (int col = 0; col < 3; col++) {
                int slotIdx = row * 3 + col;
                int cx = customInputColX(col);
                if (inRect(mx, my, cx, rowY, CELL_SIZE, CELL_SIZE)) {
                    handleCustomInput(slotIdx, button);
                    return true;
                }
            }

            int ox = customOutputColX();
            if (inRect(mx, my, ox, rowY, CELL_SIZE, CELL_SIZE)) {
                handleCustomOutput(row, button);
                return true;
            }
        }

        if (button != InputConstants.MOUSE_BUTTON_LEFT) return false;

        boolean canUp = customScroll > 0;
        boolean canDn = customScroll + CUSTOM_VISIBLE_ROWS < customTotalRows;

        if (canUp && inRect(mx, my, customScrollUpX(), customScrollUpY(),
                customScrollBtnW(), customScrollBtnH())) {
            customScroll = Math.max(0, customScroll - 1);
            return true;
        }
        if (canDn && inRect(mx, my, customScrollDnX(), customScrollDnY(),
                customScrollBtnW(), customScrollBtnH())) {
            customScroll++;
            return true;
        }
        return false;
    }

    private void handleCustomInput(int slotIdx, int button) {
        ItemStack carried = this.menu.getCarried();
        ItemStack cur = customInputs.get(slotIdx);

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (!carried.isEmpty()) {
                customInputs.put(slotIdx, carried.copyWithCount(1));
            } else if (cur != null && !cur.isEmpty()) {
                customInputs.remove(slotIdx);
            }
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            customInputs.remove(slotIdx);
        }
        updateCustomTotalRows();
    }

    private void handleCustomOutput(int row, int button) {
        ItemStack carried = this.menu.getCarried();
        ItemStack cur = customOutputs.get(row);

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            if (!carried.isEmpty()) {
                customOutputs.put(row, carried.copyWithCount(1));
            } else if (cur != null && !cur.isEmpty()) {
                customOutputs.remove(row);
            }
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            customOutputs.remove(row);
        }
        updateCustomTotalRows();
    }

    private void updateCustomTotalRows() {
        int highest = -1;
        for (int slotIdx : customInputs.keySet()) {
            int row = slotIdx / 3;
            if (row > highest) highest = row;
        }
        for (int row : customOutputs.keySet()) {
            if (row > highest) highest = row;
        }
        int needed = Math.max(highest + 2, CUSTOM_VISIBLE_ROWS);
        customTotalRows = Math.min(needed, CUSTOM_MAX_ROWS);

        int maxScroll = Math.max(0, customTotalRows - CUSTOM_VISIBLE_ROWS);
        if (customScroll > maxScroll) customScroll = maxScroll;
    }

    private static boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void submitEncode(C2SEncodePatternPayload.Destination destination) {
        RecipeView recipe = buildRecipe();
        if (recipe == null) return;

        long min = 0L, max = 0L;
        int batch = 0, priority = 0;
        VaultPattern.TriggerMode trigger = VaultPattern.TriggerMode.BATCH_HYSTERESIS;
        VaultPattern.PushMode push = VaultPattern.PushMode.PUSH_ALL;
        VaultPattern.Modifiers mods = VaultPattern.Modifiers.DEFAULT;
        Optional<UUID> existingId = Optional.empty();

        EncodedPatternSlot slot = this.menu.getPatternSlot();
        if (slot != null) {
            VaultPattern current = slot.getItem().get(ModRegistry.ENCODED_PATTERN_DATA.get());
            if (current != null) {
                existingId = Optional.of(current.patternId());
                min = current.min();
                max = current.max();
                batch = current.batch();
                priority = current.priority();
                trigger = current.triggerMode();
                push = current.pushMode();
                mods = current.modifiers();
            }
        }

        Platform.INSTANCE.sendToServer(new C2SEncodePatternPayload(
                recipe, min, max, batch, priority, trigger, push, mods,
                existingId, destination));
    }

    private RecipeView buildRecipe() {
        if (mode == Mode.CRAFTING) return buildCraftingRecipe();
        return buildCustomRecipe();
    }

    private RecipeView buildCraftingRecipe() {
        if (craftOutput.isEmpty()) return null;

        Map<Integer, ItemKey> grid = new HashMap<>();
        for (Map.Entry<Integer, ItemStack> e : craftGrid.entrySet()) {
            ItemKey k = ItemKey.of(e.getValue());
            if (k != null) grid.put(e.getKey(), k);
        }
        if (grid.isEmpty()) return null;

        ItemKey outKey = ItemKey.of(craftOutput);
        if (outKey == null) return null;

        String id = "user:" + UUID.randomUUID();
        return new RecipeView.Crafting(id, grid, outKey, craftOutput.getCount());
    }

    private RecipeView buildCustomRecipe() {
        if (customInputs.isEmpty() || customOutputs.isEmpty()) return null;

        List<RecipeView.SlotSpec> inputs = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> e : customInputs.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            ItemKey k = ItemKey.of(e.getValue());
            if (k == null) continue;
            inputs.add(new RecipeView.SlotSpec(e.getKey(), k, e.getValue().getCount()));
        }
        inputs.sort(Comparator.comparingInt(RecipeView.SlotSpec::slotIndex));
        if (inputs.isEmpty()) return null;

        List<RecipeView.SlotSpec> outputs = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> e : customOutputs.entrySet()) {
            if (e.getValue().isEmpty()) continue;
            ItemKey k = ItemKey.of(e.getValue());
            if (k == null) continue;
            outputs.add(new RecipeView.SlotSpec(e.getKey(), k, e.getValue().getCount()));
        }
        outputs.sort(Comparator.comparingInt(RecipeView.SlotSpec::slotIndex));
        if (outputs.isEmpty()) return null;

        String id = "user:" + UUID.randomUUID();
        return new RecipeView.Custom(id, inputs, outputs);
    }

    private void refreshOutputFromRecipe() {
        if (outputOverridden) return;

        List<ItemStack> items = new ArrayList<>(9);
        boolean hasAny = false;
        for (int i = 1; i <= 9; i++) {
            ItemStack s = craftGrid.get(i);
            if (s != null && !s.isEmpty()) {
                hasAny = true;
                items.add(s);
            } else {
                items.add(ItemStack.EMPTY);
            }
        }

        if (!hasAny) {
            this.craftOutput = ItemStack.EMPTY;
            return;
        }

        Platform.INSTANCE.sendToServer(new C2SRequestCraftResultPayload(items));
    }

    public void setReceivedCraftOutput(ItemStack output) {
        if (!this.outputOverridden) {
            this.craftOutput = output == null ? ItemStack.EMPTY : output.copy();
        }
    }

    public static void handleServerResult(ItemStack output) {
        Minecraft mc = Minecraft.getInstance();
        //? if >=26.2 {
        if (mc.gui.screen() instanceof PatternEncodeScreen screen) {
        //?} else {
        /*if (mc.screen instanceof PatternEncodeScreen screen) {
         *//*?}*/
            screen.setReceivedCraftOutput(output);
        }
    }

    private void syncWithPatternSlot() {
        EncodedPatternSlot slot = this.menu.getPatternSlot();
        if (slot == null) return;
        ItemStack current = slot.getItem();
        if (ItemStack.matches(current, lastPatternSlotStack)) return;
        lastPatternSlotStack = current.copy();

        if (current.isEmpty()) {
            loadedPattern = null;
            return;
        }
        VaultPattern p = current.get(ModRegistry.ENCODED_PATTERN_DATA.get());
        if (p == null) return;
        if (loadedPattern != null && loadedPattern.patternId().equals(p.patternId())) return;

        loadPatternIntoEditor(p);
    }

    private void loadPatternIntoEditor(VaultPattern p) {
        loadedPattern = p;
        if (p.recipe() instanceof RecipeView.Crafting c) {
            mode = Mode.CRAFTING;
            craftGrid.clear();
            for (Map.Entry<Integer, ItemKey> e : c.grid().entrySet()) {
                craftGrid.put(e.getKey(), e.getValue().toStack(1));
            }
            craftOutput = c.output().toStack(c.outputCount());
            outputOverridden = true;
        } else if (p.recipe() instanceof RecipeView.Custom c) {
            mode = Mode.CUSTOM;
            customInputs.clear();
            customOutputs.clear();
            for (RecipeView.SlotSpec s : c.inputs()) {
                customInputs.put(s.slotIndex(), s.item().toStack(s.count()));
            }
            for (RecipeView.SlotSpec s : c.outputs()) {
                customOutputs.put(s.slotIndex(), s.item().toStack(s.count()));
            }
            customScroll = 0;
            updateCustomTotalRows();
        }
    }

    private void openAmountPicker(Map<Integer, ItemStack> map, int key) {
        ItemStack s = map.get(key);
        if (s == null || s.isEmpty()) return;

        var modal = new com.mrchuw.universalvault.gui.modal.AmountPickerModal(
                this, s.getCount(), 1, 4096, v -> {
            ItemStack cur = map.get(key);
            if (cur != null && !cur.isEmpty()) {
                map.put(key, cur.copyWithCount(v));
            }
        });

        //? if neoforge && <26.2 {
        /*Minecraft.getInstance().pushGuiLayer(modal);
         *///?} elif neoforge {
        Minecraft.getInstance().gui.pushScreenLayer(modal);
         //?} elif <26.2 {
        /*Minecraft.getInstance().setScreen(modal);
         *///?} else {
        /*Minecraft.getInstance().gui.setScreen(modal);
        *///?}
    }
}
