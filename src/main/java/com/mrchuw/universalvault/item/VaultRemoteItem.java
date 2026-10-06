package com.mrchuw.universalvault.item;

import com.mrchuw.universalvault.Platform;
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
    public @Nonnull InteractionResult use(@Nonnull Level level,
                                          @Nonnull Player player,
                                          @Nonnull InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            Platform.INSTANCE.openVaultMenu(
                    sp,
                    sp.getUUID(),
                    Component.translatable("gui.universal_vault.personal_title"));
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
