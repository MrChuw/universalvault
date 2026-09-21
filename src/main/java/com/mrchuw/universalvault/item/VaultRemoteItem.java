package com.mrchuw.universalvault.item;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import javax.annotation.Nonnull;

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

            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new VaultMenu(id, inv, target),
                            title
                    ),
                    buf -> buf.writeUUID(target)
            );
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
