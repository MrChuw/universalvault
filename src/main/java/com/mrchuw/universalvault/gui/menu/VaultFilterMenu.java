package com.mrchuw.universalvault.gui.menu;

import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.registry.ModRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
//? if >=26.1 {
import net.minecraft.world.inventory.ContainerInput;
//?} else {
/*import net.minecraft.world.inventory.ClickType;
 *///?}
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultFilterMenu extends AbstractContainerMenu {

    public static final int FILTER_SLOTS = 9;
    public static final int FILTER_X = 8;
    public static final int FILTER_Y = 20;
    public static final int PLAYER_INV_Y = 84;
    public static final int HOTBAR_Y = 142;

    private final VaultIOBlockEntity blockEntity;
    private final VaultFilterContainer filterContainer;

    public VaultFilterMenu(int id, Inventory playerInv, VaultIOBlockEntity be) {
        super(ModRegistry.VAULT_FILTER_MENU.get(), id);
        this.blockEntity = be;
        this.filterContainer = new VaultFilterContainer(be);

        for (int i = 0; i < FILTER_SLOTS; i++) {
            this.addSlot(new GhostSlot(filterContainer, i, FILTER_X + i * 18, FILTER_Y));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = col + row * 9 + 9;
                this.addSlot(new Slot(playerInv, index, 8 + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    public VaultFilterMenu(int id, Inventory playerInv, FriendlyByteBuf buf) {
        this(id, playerInv, resolveBlockEntity(playerInv, buf));
    }

    private static VaultIOBlockEntity resolveBlockEntity(Inventory playerInv, FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
        if (be instanceof VaultIOBlockEntity v) return v;
        throw new IllegalStateException("Missing VaultIOBlockEntity at " + pos);
    }

    public VaultIOBlockEntity getBlockEntity() { return blockEntity; }

    //? if >=26.1 {
    @Override
    public void clicked(int slotId, int button, @Nonnull ContainerInput containerInput, @Nonnull Player player) {
    //?} else {
    /*@Override
    public void clicked(int slotId, int button, @Nonnull ClickType containerInput, @Nonnull Player player) {
    *///?}
        if (slotId >= 0 && slotId < FILTER_SLOTS) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty()) {
                filterContainer.setItem(slotId, carried.copyWithCount(1));
            } else {
                filterContainer.setItem(slotId, ItemStack.EMPTY);
            }
            broadcastChanges();
            return;
        }
        super.clicked(slotId, button, containerInput, player);
    }

    @Override
    public @Nonnull ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return blockEntity != null
                && !blockEntity.isRemoved()
                && player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }
}