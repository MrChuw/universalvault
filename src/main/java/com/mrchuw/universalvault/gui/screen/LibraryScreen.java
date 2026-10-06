package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SPatternActionPayload;
import com.mrchuw.universalvault.automation.network.C2SRequestPatternsSyncPayload;
import com.mrchuw.universalvault.automation.network.S2CPatternsSyncPayload;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.gui.base.AbstractVaultScreen;
import com.mrchuw.universalvault.gui.base.VaultTab;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.gui.modal.PatternConfigModal;
import com.mrchuw.universalvault.gui.modal.QuickCraftModal;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import com.mrchuw.universalvault.storage.ItemKey;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
 *///?}
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import javax.annotation.Nonnull;

public class LibraryScreen extends AbstractVaultScreen<LibraryScreen.PatternEntry> {

    public record PatternEntry(
            VaultPattern pattern,
            long currentStock,
            String displayName,
            String modId,
            Set<Identifier> tags,
            List<Component> tooltip
    ) implements GridEntry {
        @Override public ItemKey key() { return pattern.output(); }
    }

    public LibraryScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override protected VaultTab currentTab() { return VaultTab.LIBRARY; }

    @Override protected VaultSortMode defaultSortMode() { return VaultSortMode.NAME_ASC; }

    @Override protected void sendSyncRequest() {
        Platform.INSTANCE.sendToServer(C2SRequestPatternsSyncPayload.forMenu());
    }

    @Override protected int compareEntries(PatternEntry a, PatternEntry b) {
        return switch (sortMode) {
            case COUNT_DESC -> Integer.compare(b.pattern().priority(), a.pattern().priority());
            case COUNT_ASC  -> Integer.compare(a.pattern().priority(), b.pattern().priority());
            case NAME_ASC   -> a.displayName().compareToIgnoreCase(b.displayName());
            case NAME_DESC  -> b.displayName().compareToIgnoreCase(a.displayName());
            case MOD -> {
                int c = a.modId().compareToIgnoreCase(b.modId());
                if (c != 0) yield c;
                yield a.displayName().compareToIgnoreCase(b.displayName());
            }
        };
    }

    @Override
    //? if >=26.1 {
    protected void drawCellBackground(GuiGraphicsExtractor g, PatternEntry entry, int cellX, int cellY) {
    //?} else {
    /*protected void drawCellBackground(GuiGraphics g, PatternEntry entry, int cellX, int cellY) {
     *///?}
        drawSlotBackground(g, cellX, cellY,
                patternBgColor(entry.pattern(), entry.currentStock()));
    }

    @Override
    //? if >=26.1 {
    protected void drawEntryExtra(GuiGraphicsExtractor g, PatternEntry entry, int cellX, int cellY) {
    //?} else {
    /*protected void drawEntryExtra(GuiGraphics g, PatternEntry entry, int cellX, int cellY) {
     *///?}
        ItemStack stack = entry.pattern().output().toStack(1);
        String label = String.valueOf(entry.currentStock());
        //? if >=26.1 {
        g.itemDecorations(this.font, stack, cellX + 1, cellY + 1, label);
        //?} else {
        /*g.renderItemDecorations(this.font, stack, cellX + 1, cellY + 1, label);
         *///?}
    }

    @Override
    protected boolean onGridClick(PatternEntry entry, int gridIndex, int button, boolean shift) {
        ItemStack carried = this.menu.getCarried();

        if (button == InputConstants.MOUSE_BUTTON_LEFT && !carried.isEmpty()) {
            VaultPattern held = carried.get(com.mrchuw.universalvault.registry.ModRegistry
                    .ENCODED_PATTERN_DATA.get());
            if (held != null) {
                Platform.INSTANCE.sendToServer(
                        new com.mrchuw.universalvault.automation.network.C2SImportPatternPayload(held));
                return true;
            }
            return true;
        }

        if (entry == null) return true;
        UUID pid = entry.pattern().patternId();

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            Platform.INSTANCE.sendToServer(new C2SPatternActionPayload(
                    pid, C2SPatternActionPayload.Action.WITHDRAW));
            return true;
        }

        if (shift && button == InputConstants.MOUSE_BUTTON_LEFT) {
            Platform.INSTANCE.sendToServer(new C2SPatternActionPayload(
                    pid, C2SPatternActionPayload.Action.WITHDRAW_TO_INVENTORY));
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_RIGHT && !shift) {
            Platform.INSTANCE.sendToServer(new C2SPatternActionPayload(
                    pid, C2SPatternActionPayload.Action.PAUSE_TOGGLE));
            return true;
        }

        if (button == InputConstants.MOUSE_BUTTON_MIDDLE) {
            Minecraft mc = Minecraft.getInstance();
            //? if neoforge && <26.2 {
            /*mc.pushGuiLayer(new PatternConfigModal(this, entry.pattern(), this.menu.getOwnerUUID()));
             *///?} elif neoforge {
            mc.gui.pushScreenLayer(new PatternConfigModal(this, entry.pattern(), this.menu.getOwnerUUID()));
             //?} elif <26.2 {
            /*Minecraft.getInstance().setScreen(new PatternConfigModal(this, entry.pattern(), this.menu.getOwnerUUID()));
             *///?} else {
            /*Minecraft.getInstance().gui.setScreen(new PatternConfigModal(this, entry.pattern(), this.menu.getOwnerUUID()));
            *///?}
            return true;
        }
        return false;
    }

    public void onPatternsSync(S2CPatternsSyncPayload payload) {
        List<PatternEntry> parsed = new ArrayList<>(payload.entries().size());
        Minecraft client = Minecraft.getInstance();

        for (S2CPatternsSyncPayload.Entry e : payload.entries()) {
            VaultPattern p = e.pattern();
            ItemStack stack = p.output().toStack(1);
            if (stack.isEmpty()) continue;

            String displayName = stack.getHoverName().getString();
            Identifier regKey = p.output().itemId();
            String modId = regKey.getNamespace();

            Item item = p.output().resolveItem();
            Set<Identifier> tags = item == null
                    ? Set.of()
                    : BuiltInRegistries.ITEM.wrapAsHolder(item).tags()
                    .map(TagKey::location).collect(Collectors.toSet());

            List<Component> tooltip = stack.getTooltipLines(
                    Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);

            parsed.add(new PatternEntry(p, e.currentStock(), displayName, modId, tags, tooltip));
        }

        setEntries(parsed);
    }

    public void onVaultSync(S2CVaultSyncPayload payload) {
        onPatternsSync(new S2CPatternsSyncPayload(payload.patterns()));
    }

    @Override
    public boolean mouseClicked(@Nonnull net.minecraft.client.input.MouseButtonEvent event,
                                boolean doubleClick) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();


        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && event.hasShiftDown()
                && !isInsideGrid(event.x(), event.y())) {
            //? if fabric || <=26.1.1 {
            /*net.minecraft.world.inventory.Slot slot = this.hoveredSlot;
            *///?} else {
            net.minecraft.world.inventory.Slot slot = this.getHoveredSlot();
             //?}
            if (slot != null && slot.hasItem()) {
                VaultPattern p = slot.getItem().get(
                        com.mrchuw.universalvault.registry.ModRegistry.ENCODED_PATTERN_DATA.get());
                if (p != null) {
                    Platform.INSTANCE.sendToServer(
                            new com.mrchuw.universalvault.automation.network.C2SImportPatternPayload(p));
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
}