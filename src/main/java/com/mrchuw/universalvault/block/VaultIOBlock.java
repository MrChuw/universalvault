package com.mrchuw.universalvault.block;

import com.mojang.serialization.MapCodec;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class VaultIOBlock extends BaseEntityBlock {

    public static final MapCodec<VaultIOBlock> CODEC = simpleCodec(VaultIOBlock::new);

    public VaultIOBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return new VaultIOBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            @NonNull Level level,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable LivingEntity placer,
            @NonNull ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;

        if (level.getBlockEntity(pos) instanceof VaultIOBlockEntity be) {
            VaultIOData data = stack.get(ModRegistry.VAULT_IO_DATA.get());
            if (data != null) {
                be.setTargetVaultUUID(data.targetVault());
                for (int i = 0; i < VaultIOBlockEntity.FILTER_SLOTS; i++) {
                    ItemStack filter = i < data.filters().size() ? data.filters().get(i) : ItemStack.EMPTY;
                    be.setFilter(i, filter);
                }
            } else {
                be.setChanged();
            }
        }
    }

    @Override
    public void playerDestroy(
            @NonNull Level level,
            @NonNull Player player,
            @NonNull BlockPos pos,
            @NonNull BlockState state,
            @Nullable BlockEntity blockEntity,
            @NonNull ItemStack tool
    ) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);

        if (level.isClientSide()) return;

        // Custom drop carrying the embedded data — replaces the loot table's default drop
        ItemStack drop = new ItemStack(this);
        if (blockEntity instanceof VaultIOBlockEntity be) {
            List<ItemStack> filters = new ArrayList<>();
            for (int i = 0; i < VaultIOBlockEntity.FILTER_SLOTS; i++) {
                filters.add(be.getFilter(i).copy());
            }
            drop.set(ModRegistry.VAULT_IO_DATA.get(),
                    new VaultIOData(be.getTargetVaultUUID(), filters));
        }
        Block.popResource(level, pos, drop);
    }

    @Override
    protected @NonNull InteractionResult useItemOn(
            @NonNull ItemStack stack, @NonNull BlockState state, @NonNull Level level,
            @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand,
            @NonNull BlockHitResult hit
    ) {
        if (!(level.getBlockEntity(pos) instanceof VaultIOBlockEntity ioBe)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            ioBe.cycleTarget(player);
            return InteractionResult.SUCCESS_SERVER;
        }

        if (player instanceof ServerPlayer sp) {
            sp.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) -> new VaultFilterMenu(id, inv, ioBe),
                            Component.translatable("gui.universal_vault.filter_title")
                    ),
                    buf -> buf.writeBlockPos(pos)
            );
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
