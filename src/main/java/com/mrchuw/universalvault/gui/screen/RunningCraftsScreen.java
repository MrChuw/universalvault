package com.mrchuw.universalvault.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SCancelJobPayload;
import com.mrchuw.universalvault.automation.network.C2SRequestJobsSyncPayload;
import com.mrchuw.universalvault.automation.network.S2CJobsSyncPayload;
import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.gui.base.AbstractVaultScreen;
import com.mrchuw.universalvault.gui.base.VaultTab;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class RunningCraftsScreen extends AbstractVaultScreen<RunningCraftsScreen.JobEntry> {

    public record JobEntry(
            UUID jobId,
            ItemKey output,
            long requested,
            long completed,
            String origin,
            int activeSlices,
            String displayName,
            String modId,
            Set<Identifier> tags,
            List<Component> tooltip
    ) implements GridEntry {
        @Override public ItemKey key() { return output; }
    }

    public RunningCraftsScreen(VaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override protected VaultTab currentTab() { return VaultTab.CRAFTS; }

    @Override protected VaultSortMode defaultSortMode() { return VaultSortMode.COUNT_DESC; }

    @Override protected void sendSyncRequest() {
        Platform.INSTANCE.sendToServer(new C2SRequestJobsSyncPayload());
    }

    @Override protected int compareEntries(JobEntry a, JobEntry b) {
        return switch (sortMode) {
            case COUNT_DESC -> Long.compare(b.requested(), a.requested());
            case COUNT_ASC  -> Long.compare(a.requested(), b.requested());
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
    protected void drawCellBackground(GuiGraphicsExtractor g, JobEntry entry, int cellX, int cellY) {
    //?} else {
    /*protected void drawCellBackground(GuiGraphics g, JobEntry entry, int cellX, int cellY) {
     *///?}
        int bg;
        if (entry.completed() >= entry.requested()) {
            bg = BG_STATUS_SATISFIED;
        } else if (entry.activeSlices() > 0) {
            bg = BG_STATUS_PROCESSING;
        } else {
            bg = BG_STATUS_BLOCKED;
        }
        drawSlotBackground(g, cellX, cellY, bg);
    }

    @Override
    //? if >=26.1 {
    protected void drawEntryExtra(GuiGraphicsExtractor g, JobEntry entry, int cellX, int cellY) {
    //?} else {
    /*protected void drawEntryExtra(GuiGraphics g, JobEntry entry, int cellX, int cellY) {
     *///?}
        ItemStack stack = entry.output().toStack(1);
        String label;
        if (entry.completed() >= entry.requested()) {
            label = String.valueOf(entry.completed());
        } else if (entry.activeSlices() > 0) {
            label = entry.completed() + "/" + entry.requested();
        } else {
            // Bloqueado: nada despachado. Mostra com "!" pra diferenciar.
            label = "!" + entry.completed();
        }
        //? if >=26.1 {
        g.itemDecorations(this.font, stack, cellX + 1, cellY + 1, label);
        //?} else {
        /*g.renderItemDecorations(this.font, stack, cellX + 1, cellY + 1, label);
         *///?}
    }

    @Override
    protected boolean onGridClick(JobEntry entry, int gridIndex, int button, boolean shift) {
        if (entry == null) return true;
        if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            Platform.INSTANCE.sendToServer(new C2SCancelJobPayload(entry.jobId()));
            return true;
        }
        return false;
    }

    public void onPatternsSync(com.mrchuw.universalvault.automation.network.S2CPatternsSyncPayload payload) {}

    public void onJobsSync(S2CJobsSyncPayload payload) {
        List<JobEntry> parsed = new ArrayList<>(payload.entries().size());
        Minecraft client = Minecraft.getInstance();

        for (S2CJobsSyncPayload.Entry e : payload.entries()) {
            ItemStack stack = e.topOutput().toStack(1);
            if (stack.isEmpty()) continue;

            String displayName = stack.getHoverName().getString();
            Identifier regKey = e.topOutput().itemId();
            String modId = regKey.getNamespace();

            Item item = e.topOutput().resolveItem();
            Set<Identifier> tags = item == null
                    ? Set.of()
                    : BuiltInRegistries.ITEM.wrapAsHolder(item).tags()
                    .map(TagKey::location).collect(Collectors.toSet());

            List<Component> tooltip = stack.getTooltipLines(
                    Item.TooltipContext.EMPTY, client.player, TooltipFlag.NORMAL);

            parsed.add(new JobEntry(
                    e.jobId(), e.topOutput(), e.requested(), e.completed(),
                    e.origin(), e.activeSlices(),
                    displayName, modId, tags, tooltip));
        }
        setEntries(parsed);
    }

    @Override
    public boolean mouseClicked(@Nonnull net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        this.lastMouseX = event.x();
        this.lastMouseY = event.y();
        return super.mouseClicked(event, doubleClick);
    }
}
