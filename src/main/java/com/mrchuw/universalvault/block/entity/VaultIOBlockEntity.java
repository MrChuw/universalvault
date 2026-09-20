package com.mrchuw.universalvault.block.entity;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class VaultIOBlockEntity extends BlockEntity {

    public static final int FILTER_SLOTS = 9;

    private UUID targetVaultUUID = UniversalVault.GLOBAL_VAULT_UUID;
    private long lastToggleTime = 0;
    private final NonNullList<ItemStack> filters =
            NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);

    public VaultIOBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.VAULT_IO_BLOCK_ENTITY.get(), pos, state);
    }

    public VaultStorage getStorage() {
        if (this.level != null && !this.level.isClientSide()) {
            return VaultManager.getVault(this.level, this.targetVaultUUID);
        }
        return null;
    }

    public UUID getTargetVaultUUID() {
        return targetVaultUUID;
    }

    public void setTargetVaultUUID(UUID targetVaultUUID) {
        this.targetVaultUUID = targetVaultUUID;
        this.setChangedAndSync();
    }

    public void cycleTarget(Player player) {
        long now = System.currentTimeMillis();
        if (now - lastToggleTime < 100) return;
        lastToggleTime = now;

        if (targetVaultUUID.equals(UniversalVault.GLOBAL_VAULT_UUID)) {
            setTargetVaultUUID(player.getUUID());
        } else {
            setTargetVaultUUID(UniversalVault.GLOBAL_VAULT_UUID);
        }

        if (player instanceof ServerPlayer sp
                && UniversalVaultConfig.CONFIG.announceTargetOnCycle.get()) {
            String targetName = targetVaultUUID.equals(UniversalVault.GLOBAL_VAULT_UUID)
                    ? Component.translatable("gui.universal_vault.target_global").getString()
                    : player.getName().getString();
            sp.sendSystemMessage(
                    Component.translatable("gui.universal_vault.target_changed", targetName));
        }
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        output.store("TargetVault", UUIDUtil.CODEC, this.targetVaultUUID);
        ContainerHelper.saveAllItems(output, this.filters);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        input.read("TargetVault", UUIDUtil.CODEC).ifPresent(uuid -> this.targetVaultUUID = uuid);
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

    /**
     * Applies the filter rule according to the configured mode.
     * If no filter is configured, everything passes.
     */
    public boolean matchesFilter(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!hasAnyFilter()) return true;

        boolean whitelist = UniversalVaultConfig.CONFIG.defaultFilterMode.get()
                == UniversalVaultConfig.FilterMode.WHITELIST;

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