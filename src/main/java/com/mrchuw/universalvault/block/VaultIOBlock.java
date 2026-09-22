package com.mrchuw.universalvault.block;


import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
//? if <=26.2 {
/*import net.minecraft.world.level.block.RenderShape;
import com.mojang.serialization.MapCodec;
*///?}
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class VaultIOBlock extends BaseEntityBlock {

    //? if <=26.2 {
    /*public static final MapCodec<VaultIOBlock> CODEC = simpleCodec(VaultIOBlock::new);

     *///?}

    public VaultIOBlock(Properties properties) {
        super(properties);
    }

    //? if <=26.2 {
    /*@Override
    protected @Nonnull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nonnull RenderShape getRenderShape(@Nonnull BlockState state) {
        return RenderShape.MODEL;
    }

    *///?}

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new VaultIOBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nullable LivingEntity placer,
            @Nonnull ItemStack stack
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
            //? if <=26.2 {
            /*@Nonnull Level level,
            @Nonnull Player player,
            *///?} else {
            @Nonnull ServerLevel level,
            @Nonnull ServerPlayer player,
            //?}
            @Nonnull BlockPos pos,
            @Nonnull BlockState state,
            @Nullable BlockEntity blockEntity,
            @Nonnull ItemStack tool
    ) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        //? if <=26.2 {
        /*if (level.isClientSide()) return;
         *///?}
        dropWithEmbeddedData(level, pos, blockEntity);
    }

    private void dropWithEmbeddedData(Level level, BlockPos pos, @Nullable BlockEntity blockEntity) {
        // Custom drop carrying the embedded data
        ItemStack drop = new ItemStack(this);
        if (blockEntity instanceof VaultIOBlockEntity be) {
            List<ItemStack> filters = new ArrayList<>();
            for (int i = 0; i < VaultIOBlockEntity.FILTER_SLOTS; i++) {
                filters.add(be.getFilter(i).copy());
            }
            drop.set(ModRegistry.VAULT_IO_DATA.get(), new VaultIOData(be.getTargetVaultUUID(), filters));
        }
        Block.popResource(level, pos, drop);
    }

    @Override
    protected @Nonnull InteractionResult useItemOn(
            @Nonnull ItemStack stack,
            @Nonnull BlockState state,
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull Player player,
            @Nonnull InteractionHand hand,
            @Nonnull BlockHitResult hit
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
            Platform.INSTANCE.openVaultFilterMenu(
                    sp,
                    ioBe,
                    pos,
                    Component.translatable("gui.universal_vault.filter_title")
            );
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
