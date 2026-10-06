package com.mrchuw.universalvault.network;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.tracking.AdaptiveVelocityTracker;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.storage.ItemKey;
import com.mrchuw.universalvault.storage.VaultManager;
import com.mrchuw.universalvault.storage.VaultStorage;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class VaultActionHandler {

    private VaultActionHandler() {}

    public static void handle(ServerPlayer player, C2SVaultActionPayload payload) {
        if (!(player.containerMenu instanceof VaultMenu vaultMenu)) return;

        UUID owner = vaultMenu.getOwnerUUID();
        VaultStorage storage = VaultManager.getVault(player.level(), owner);
        if (storage == null) return;

        ItemKey key = payload.targetKey();
        if (key != null && !key.isResolved()) return;

        long totalExtracted = 0;

        switch (payload.actionType()) {

            case EXTRACT -> {
                if (key == null) return;
                int amount = Math.min(payload.amount(), 64);
                ItemStack sample = key.toStack(1);
                int maxStack = sample.getMaxStackSize();

                int space = 0;
                for (ItemStack invStack : player.getInventory().getNonEquipmentItems()) {
                    if (invStack.isEmpty()) space += maxStack;
                    else if (ItemStack.isSameItemSameComponents(invStack, sample)) space +=
                            maxStack - invStack.getCount();
                    if (space >= amount) break;
                }
                int toExtract = Math.min(amount, space);
                if (toExtract > 0) {
                    ItemStack extracted = storage.extract(key, toExtract, false);
                    if (!extracted.isEmpty()) {
                        player.getInventory().add(extracted);
                        totalExtracted += extracted.getCount();
                    }
                }
            }
            case PICKUP -> {
                if (key == null) return;
                int amount = Math.clamp(payload.amount(), 1, 64);
                totalExtracted += pickupToCarried(player, storage, key, amount);
            }
            case PICKUP_ALL -> {
                if (key == null) return;
                if (!player.containerMenu.getCarried().isEmpty()) return;
                long available = storage.getAmount(key);
                if (available <= 0) return;
                int maxStack = maxStackFor(key);
                totalExtracted += pickupToCarried(player, storage, key, (int) Math.min(available, maxStack));
            }
            case PICKUP_HALF -> {
                if (key == null) return;
                if (!player.containerMenu.getCarried().isEmpty()) return;
                long available = storage.getAmount(key);
                if (available <= 0) return;
                int maxStack = maxStackFor(key);
                totalExtracted += pickupToCarried(player, storage, key, halfAmount(available, maxStack));
            }

            case QUICK_MOVE -> {
                if (key == null) return;
                long available = storage.getAmount(key);
                if (available <= 0) return;
                int maxStack = maxStackFor(key);
                totalExtracted += moveToPlayerInventory(player, storage, key, (int) Math.min(available, maxStack));
            }
            case QUICK_MOVE_HALF -> {
                if (key == null) return;
                long available = storage.getAmount(key);
                if (available <= 0) return;
                int maxStack = maxStackFor(key);
                totalExtracted += moveToPlayerInventory(player, storage, key, halfAmount(available, maxStack));
            }
            case DEPOSIT_ALL -> {
                ItemStack carried = player.containerMenu.getCarried();
                if (carried.isEmpty()) return;
                insertAndShrinkCarried(player, storage, carried, carried.copy());
            }
            case DEPOSIT_ONE -> {
                ItemStack carried = player.containerMenu.getCarried();
                if (carried.isEmpty()) return;
                insertAndShrinkCarried(player, storage, carried, carried.copyWithCount(1));
            }
            case DROP_ONE -> {
                if (key == null) return;
                ItemStack extracted = storage.extract(key, 1, false);
                if (!extracted.isEmpty()) {
                    //? if <=26.2 {
                    /*player.drop(extracted, false);
                    *///?} else {
                    player.drop(extracted, false, net.minecraft.util.Prediction.PREDICTED);
                     //?}
                    totalExtracted += extracted.getCount();
                }
            }
            case DROP_STACK -> {
                if (key == null) return;
                long available = storage.getAmount(key);
                if (available <= 0) return;
                int maxStack = maxStackFor(key);
                int toDrop = (int) Math.min(available, maxStack);
                ItemStack extracted = storage.extract(key, toDrop, false);
                if (!extracted.isEmpty()) {
                    //? if <=26.2 {
                    /*player.drop(extracted, false);
                    *///?} else {
                    player.drop(extracted, false, net.minecraft.util.Prediction.PREDICTED);
                     //?}
                    totalExtracted += extracted.getCount();
                }

            }
            case DEPOSIT_HELD -> {
                ItemStack carried = player.containerMenu.getCarried();
                if (carried.isEmpty()) break;
                int amountToDeposit = payload.amount();
                if (amountToDeposit <= 0 || amountToDeposit > carried.getCount())
                    amountToDeposit = carried.getCount();
                insertAndShrinkCarried(player, storage, carried, carried.copyWithCount(amountToDeposit));
            }
            case DEPOSIT_SLOT -> {
                int slotIndex = payload.slotIndex();
                if (slotIndex < 0 || slotIndex >= player.getInventory().getContainerSize()) return;
                ItemStack stack = player.getInventory().getItem(slotIndex);
                if (stack.isEmpty()) return;
                long inserted = storage.insert(stack, false);
                if (inserted > 0) {
                    stack.shrink((int) inserted);
                    player.getInventory().setItem(slotIndex, stack);
                    player.containerMenu.slotsChanged(player.getInventory());
                }
            }
            case CONSUME_CARRIED -> {
                ItemStack carried = player.containerMenu.getCarried();
                if (!carried.isEmpty()) {
                    carried.shrink(1);
                    player.containerMenu.setCarried(carried);
                }
            }
            case GIVE_TO_CARRIED -> {
                if (key == null) return;
                ItemStack sample = key.toStack(1);
                ItemStack carried = player.containerMenu.getCarried();
                if (carried.isEmpty()) {
                    player.containerMenu.setCarried(sample);
                } else if (ItemStack.isSameItemSameComponents(carried, sample)
                        && carried.getCount() < carried.getMaxStackSize()) {
                    carried.grow(1);
                    player.containerMenu.setCarried(carried);
                } else {
                    //? if <=26.2 {
                    /*player.drop(sample, false);
                     *///?} else {
                    player.drop(sample, false, net.minecraft.util.Prediction.PREDICTED);
                    //?}
                }
            }
        }

        player.containerMenu.broadcastChanges();
        vaultMenu.syncVaultData(player);

        if (totalExtracted > 0 && player.level() instanceof ServerLevel serverLevel) {
            AdaptiveVelocityTracker.recordPlayerExtract(serverLevel, totalExtracted);
        }
    }

    private static int maxStackFor(ItemKey key) {
        return key.toStack(1).getMaxStackSize();
    }

    private static int halfAmount(long available, int maxStack) {
        long half = (available + 1) / 2;
        return half > maxStack ? (maxStack + 1) / 2 : (int) half;
    }


    private static int pickupToCarried(ServerPlayer player, VaultStorage storage, ItemKey key, int amount) {
        ItemStack extracted = storage.extract(key, amount, false);
        if (!extracted.isEmpty()) {
            player.containerMenu.setCarried(extracted);
            return extracted.getCount();
        }
        return 0;
    }

    private static int moveToPlayerInventory(ServerPlayer player, VaultStorage storage, ItemKey key, int amount) {
        ItemStack extracted = storage.extract(key, amount, false);
        if (!extracted.isEmpty()) {
            ItemStack remaining = extracted.copy();
            player.getInventory().add(remaining);
            if (!remaining.isEmpty()) storage.insert(remaining, false);
            return extracted.getCount();
        }
        return 0;
    }

    private static void insertAndShrinkCarried(ServerPlayer player, VaultStorage storage,
                                               ItemStack carried, ItemStack toInsert) {
        long inserted = storage.insert(toInsert, false);
        if (inserted > 0) {
            carried.shrink((int) inserted);
            player.containerMenu.setCarried(carried);
        }
    }

}