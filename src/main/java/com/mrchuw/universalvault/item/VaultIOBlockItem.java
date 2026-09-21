package com.mrchuw.universalvault.item;

import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class VaultIOBlockItem extends BlockItem {

    public VaultIOBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @Nonnull InteractionResult use(@Nonnull Level level, @Nonnull Player player,
                                          @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown() && stack.has(ModRegistry.VAULT_IO_DATA.get())) {
            if (!level.isClientSide()) {
                stack.remove(ModRegistry.VAULT_IO_DATA.get());

                //? if >=26.1 {
                player.sendOverlayMessage(Component.translatable("gui.universal_vault.cleared"));
                 //?} else {
                /*player.displayClientMessage(Component.translatable("gui.universal_vault.cleared"), true);
                *///?}
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack,
                                @Nonnull Item.TooltipContext context,
                                @Nonnull TooltipDisplay display,
                                @Nonnull Consumer<Component> tooltip,
                                @Nonnull TooltipFlag flag) {
        VaultIOData data = stack.get(ModRegistry.VAULT_IO_DATA.get());
        if (data != null) {
            tooltip.accept(Component.translatable("item.universal_vault.vault_io.configured"));
            tooltip.accept(Component.translatable("item.universal_vault.vault_io.configured.hint"));
        }
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}