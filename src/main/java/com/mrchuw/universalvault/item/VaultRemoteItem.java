package com.mrchuw.universalvault.item;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.UniversalVault;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class VaultRemoteItem extends Item {

    public VaultRemoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public @Nonnull InteractionResult use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean global = player.isShiftKeyDown();
            UUID target = global ? UniversalVault.GLOBAL_VAULT_UUID : serverPlayer.getUUID();
            Component title = global
                    ? Component.translatable("gui.universal_vault.global_title")
                    : Component.translatable("gui.universal_vault.personal_title");

            Platform.INSTANCE.openVaultMenu(serverPlayer, target, title);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}