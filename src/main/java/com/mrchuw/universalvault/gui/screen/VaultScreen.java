package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.S2CPatternsSyncPayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.config.VaultConfig;import com.mrchuw.universalvault.gui.base.AbstractVaultScreen;
import com.mrchuw.universalvault.gui.base.VaultTab;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.gui.modal.QuickCraftModal;
import com.mrchuw.universalvault.network.payload.C2SRequestSyncPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import com.mrchuw.universalvault.storage.ItemKey;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.client.input.KeyEvent;import net.minecraft.client.input.MouseButtonEvent;import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;


public class VaultScreen extends AbstractVaultScreen<VaultScreen.EntryData> {

    public record EntryData(
            ItemKey key,
            long count,
            String displayName,
            String modId,
            Set<Identifier> tags,
            List<Component> tooltip
    ) implements GridEntry {}

    private final Map<ItemKey, S2CPatternsSyncPayload.Entry> patternByOutput = new HashMap<>();

    public VaultScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override protected VaultTab currentTab() { return VaultTab.VAULT; }

    @Override protected VaultSortMode defaultSortMode() { return VaultSortMode.COUNT_DESC; }

    @Override protected void sendSyncRequest() {
        Platform.INSTANCE.sendToServer(new C2SRequestSyncPayload());
    }

    @Override protected int compareEntries(EntryData a, EntryData b) {
        return switch (sortMode) {
            case COUNT_DESC -> Long.compare(b.count(), a.count());
            case COUNT_ASC -> Long.compare(a.count(), b.count());
            case NAME_ASC -> a.displayName().compareToIgnoreCase(b.displayName());
            case NAME_DESC -> b.displayName().compareToIgnoreCase(a.displayName());
            case MOD -> {
                int c = a.modId().compareToIgnoreCase(b.modId());
                if (c != 0) yield c;
                yield a.displayName().compareToIgnoreCase(b.displayName());
            }
        };
    }

    //? if >=26.1 {
    @Override protected void drawEntryExtra(GuiGraphicsExtractor g, EntryData entry, int cellX, int cellY) {
        ItemStack stack = entry.key().toStack(1);
        g.itemDecorations(this.font, stack, cellX + 1, cellY + 1, formatCount(entry.count()));
    }
    //?} else {
    /*@Override protected void drawEntryExtra(GuiGraphics g, EntryData entry, int cellX, int cellY) {
        ItemStack stack = entry.key().toStack(1);
        g.renderItemDecorations(this.font, stack, cellX + 1, cellY + 1, formatCount(entry.count()));
    }
    *///?}

    @Override
            //? if >=26.1 {
    protected void drawCellBackground(GuiGraphicsExtractor g, EntryData entry, int cellX, int cellY) {
        //?} else {
        /*protected void drawCellBackground(GuiGraphics g, EntryData entry, int cellX, int cellY) {
         *///?}
        var pEntry = patternByOutput.get(entry.key());
        int bg = pEntry != null
                ? patternBgColor(pEntry.pattern(), pEntry.currentStock())
                : COLOR_SLOT_BG;
        drawSlotBackground(g, cellX, cellY, bg);
    }

    @Override
    protected boolean onGridClick(EntryData entry, int gridIndex, int button, boolean shift) {
        ItemKey key = entry != null ? entry.key() : null;
        ItemStack carried = this.menu.getCarried();

        if (!carried.isEmpty()) {
            C2SVaultActionPayload.ActionType action =
                    button == InputConstants.MOUSE_BUTTON_LEFT
                            ? C2SVaultActionPayload.ActionType.DEPOSIT_ALL
                            : C2SVaultActionPayload.ActionType.DEPOSIT_ONE;
            sendVaultAction(action, key);
            return true;
        }
        if (button == InputConstants.MOUSE_BUTTON_MIDDLE && key != null) {
            boolean hasPattern = patternByOutput.containsKey(key);
            boolean universal = VaultConfig.get()
                    .enableUniversalQuickCraft();
            if (hasPattern || universal) {
                Minecraft mc = Minecraft.getInstance();

                //? if neoforge {
                //? if <26.2 {
                /*mc.pushGuiLayer(new QuickCraftModal(this, key));
                 *///?} else {
                mc.gui.pushScreenLayer(new QuickCraftModal(this, key));
                //?}
                //?} else {
                /*//? if <26.2 {
                /^Minecraft.getInstance().setScreen(new QuickCraftModal(this, key));
                 ^///?} else {
                Minecraft.getInstance().gui.setScreen(new QuickCraftModal(this, key));
                //?}
                *///? }
            }
            return true;
        }
        if (key != null) {
            C2SVaultActionPayload.ActionType action;
            if (shift) {
                action = button == InputConstants.MOUSE_BUTTON_LEFT
                        ? C2SVaultActionPayload.ActionType.QUICK_MOVE
                        : C2SVaultActionPayload.ActionType.QUICK_MOVE_HALF;
            } else {
                action = button == InputConstants.MOUSE_BUTTON_LEFT
                        ? C2SVaultActionPayload.ActionType.PICKUP_ALL
                        : C2SVaultActionPayload.ActionType.PICKUP_HALF;
            }
            sendVaultAction(action, key);
            return true;
        }
        return false;
    }

    public void onVaultSync(S2CVaultSyncPayload payload) {
        onPatternsSync(new S2CPatternsSyncPayload(payload.patterns()));

        List<EntryData> parsed = new ArrayList<>(payload.entries().size());
        Set<ItemKey> present = new HashSet<>();
        Minecraft client = Minecraft.getInstance();

        for (S2CVaultSyncPayload.Entry entry : payload.entries()) {
            ItemKey key = entry.key();
            ItemStack stack = key.toStack(1);
            if (stack.isEmpty()) continue;
            present.add(key);
            parsed.add(buildEntry(key, entry.count(), stack, client));
        }

        for (S2CPatternsSyncPayload.Entry p : payload.patterns()) {
            ItemKey key = p.pattern().output();
            if (present.contains(key)) continue;
            ItemStack stack = key.toStack(1);
            if (stack.isEmpty()) continue;
            parsed.add(buildEntry(key, 0L, stack, client));
        }

        if (forceNextSyncFullRebuild) {
            forceNextSyncFullRebuild = false;
            this.pendingRebuild = false;
            setEntries(parsed);
            return;
        }

        this.allEntries.clear();
        this.allEntries.addAll(parsed);
        if (isFrozen(this.lastMouseX, this.lastMouseY)) {
            updateVisibleCountsInPlace();
            this.pendingRebuild = true;
        } else {
            applyFilter(this.searchBox != null ? this.searchBox.getValue() : "", false);
        }
    }

    public void onPatternsSync(S2CPatternsSyncPayload payload) {
        patternByOutput.clear();
        for (S2CPatternsSyncPayload.Entry e : payload.entries()) {
            patternByOutput.put(e.pattern().output(), e);
        }
    }

    private EntryData buildEntry(ItemKey key, long count, ItemStack stack, Minecraft client) {
        String displayName = stack.getHoverName().getString();
        Identifier regKey = key.itemId();
        String modId = regKey.getNamespace();
        Item item = key.resolveItem();
        Set<Identifier> tags = item == null
                ? Set.of()
                : BuiltInRegistries.ITEM.wrapAsHolder(item).tags()
                .map(TagKey::location).collect(Collectors.toSet());
        List<Component> tooltip = stack.getTooltipLines(
                Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);
        return new EntryData(key, count, displayName, modId, tags, tooltip);
    }

    @Override
    public boolean mouseClicked(@Nonnull MouseButtonEvent event, boolean doubleClick) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(@Nonnull KeyEvent event) {
        if (event.key() == InputConstants.KEY_Q) {
            int idx = getHoveredIndex(this.lastMouseX, this.lastMouseY);
            if (idx >= 0 && idx < filteredEntries.size()) {
                ItemKey key = filteredEntries.get(idx).key();
                C2SVaultActionPayload.ActionType action = event.hasShiftDown()
                        ? C2SVaultActionPayload.ActionType.DROP_STACK
                        : C2SVaultActionPayload.ActionType.DROP_ONE;
                sendVaultAction(action, key);
                return true;
            }
        }
        return super.keyPressed(event);
    }

    private boolean isFrozen(double mouseX, double mouseY) {
        if (!isInsideGrid(mouseX, mouseY)) return false;
        Minecraft mc = Minecraft.getInstance();
        return mc.hasShiftDown() || mc.hasControlDown() || mc.hasAltDown();
    }

    @Override
    protected boolean shouldDeferRebuild(int mouseX, int mouseY) {
        return isFrozen(mouseX, mouseY);
    }

    protected void sendVaultAction(C2SVaultActionPayload.ActionType action, ItemKey key) {
        Platform.INSTANCE.sendToServer(new C2SVaultActionPayload(action, key, 0, -1));
    }

    private void updateVisibleCountsInPlace() {
        if (filteredEntries.isEmpty()) return;

        Map<ItemKey, Long> newCounts = new HashMap<>();
        for (EntryData d : allEntries) {
            newCounts.put(d.key(), d.count());
        }

        for (int i = 0; i < filteredEntries.size(); i++) {
            EntryData old = filteredEntries.get(i);
            Long newCount = newCounts.get(old.key());
            long value = (newCount == null) ? 0L : newCount;
            if (value != old.count()) {
                filteredEntries.set(i, new EntryData(
                        old.key(), value, old.displayName(), old.modId(), old.tags(), old.tooltip()));
            }
        }
    }

    private String formatCount(long count) {
        if (count >= 1_000_000) return count / 1_000_000 + "M";
        if (count >= 1_000) return count / 1_000 + "k";
        return String.valueOf(count);
    }
}
