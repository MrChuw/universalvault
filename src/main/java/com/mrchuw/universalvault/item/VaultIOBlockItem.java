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
import org.jspecify.annotations.NonNull;

public class VaultIOBlockItem extends BlockItem {

    public VaultIOBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player,
                                          @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift + right-click in the air = clear configuration
        if (player.isShiftKeyDown() && stack.has(ModRegistry.VAULT_IO_DATA.get())) {
            if (!level.isClientSide()) {
                stack.remove(ModRegistry.VAULT_IO_DATA.get());
                player.sendOverlayMessage(
                        Component.translatable("gui.universal_vault.cleared"));
            }
            return InteractionResult.SUCCESS;
        }

        // No shift: let BlockItem try to place the block
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(@NonNull ItemStack stack,
                                Item.TooltipContext context,
                                @NonNull TooltipDisplay display,
                                @NonNull Consumer<Component> tooltip,
                                @NonNull TooltipFlag flag) {
        VaultIOData data = stack.get(ModRegistry.VAULT_IO_DATA.get());
        if (data != null) {
            tooltip.accept(Component.translatable("item.universal_vault.vault_io.configured"));
            tooltip.accept(Component.translatable("item.universal_vault.vault_io.configured.hint"));
        }
    }
}