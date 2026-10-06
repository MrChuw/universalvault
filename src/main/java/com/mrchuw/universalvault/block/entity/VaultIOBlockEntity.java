package com.mrchuw.universalvault.block.entity;

import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import javax.annotation.Nonnull;

public class VaultIOBlockEntity extends BlockEntity {

    public static final int FILTER_SLOTS = 9;

    private static final UUID NO_OWNER = new UUID(0L, 0L);

    private UUID ownerUUID = NO_OWNER;
    private final NonNullList<ItemStack> filters =
            NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);

    public VaultIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.VAULT_IO_BLOCK_ENTITY.get(), pos, state);
    }

    public UUID getOwnerUUID() { return ownerUUID; }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid != null ? uuid : NO_OWNER;
        this.setChangedAndSync();
    }

    public boolean hasOwner() {
        return !NO_OWNER.equals(ownerUUID);
    }

    public VaultStorage getStorage() {
        if (this.level != null && !this.level.isClientSide() && hasOwner()) {
            return VaultManager.getVault(this.level, this.ownerUUID);
        }
        return null;
    }

    @Override
    protected void saveAdditional(@Nonnull ValueOutput output) {
        super.saveAdditional(output);
        output.store("OwnerUUID", UUIDUtil.CODEC, this.ownerUUID);
        ContainerHelper.saveAllItems(output, this.filters);
    }

    @Override
    protected void loadAdditional(@Nonnull ValueInput input) {
        super.loadAdditional(input);
        input.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(u -> this.ownerUUID = u);
        ContainerHelper.loadAllItems(input, this.filters);
    }

    private void setChangedAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEntityChanged(this.worldPosition);
        }
    }

    public ItemStack getFilter(int index) {
        if (index < 0 || index >= FILTER_SLOTS) return ItemStack.EMPTY;
        return filters.get(index);
    }

    public void setFilter(int index, ItemStack stack) {
        if (index < 0 || index >= FILTER_SLOTS) return;
        filters.set(index, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
        this.setChangedAndSync();
    }

    public boolean hasAnyFilter() {
        for (ItemStack s : filters) if (!s.isEmpty()) return true;
        return false;
    }

    public boolean matchesFilter(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!hasAnyFilter()) return true;

        boolean whitelist = VaultConfig.get().defaultFilterMode() == VaultConfig.FilterMode.WHITELIST;

        boolean matched = false;
        for (ItemStack filter : filters) {
            if (filter.isEmpty()) continue;
            if (ItemStack.isSameItemSameComponents(stack, filter)) {
                matched = true;
                break;
            }
        }
        return whitelist == matched;
    }
}
