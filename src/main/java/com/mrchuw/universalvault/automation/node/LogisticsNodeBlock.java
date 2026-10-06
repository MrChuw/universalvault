package com.mrchuw.universalvault.automation.node;

import com.mojang.serialization.MapCodec;
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.S2CNodeConfigSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LogisticsNodeBlock extends BaseEntityBlock {

    public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

    //? if <=26.2 {
    /*public static final MapCodec<LogisticsNodeBlock> CODEC = simpleCodec(LogisticsNodeBlock::new);
    *///?}

    public LogisticsNodeBlock(Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(ACTIVE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new LogisticsNodeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof LogisticsNodeBlockEntity node
                    && lvl instanceof net.minecraft.server.level.ServerLevel sl) {
                node.serverTick(sl);
            }
        };
    }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state,
                            @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof LogisticsNodeBlockEntity node)) return;

        if (placer instanceof Player p) {
            node.setOwnerUUID(p.getUUID());
        }
        node.bindAdjacentStations();
    }

    @Override
    protected @Nonnull InteractionResult useItemOn(
            @Nonnull ItemStack stack,
            @Nonnull BlockState state,
            @Nonnull Level level,
            @Nonnull BlockPos pos,
            @Nonnull Player player,
            @Nonnull InteractionHand hand,
            @Nonnull BlockHitResult hit) {

        if (!(level.getBlockEntity(pos) instanceof LogisticsNodeBlockEntity node)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (player instanceof ServerPlayer sp) {
            Platform.INSTANCE.sendToPlayer(sp, new S2CNodeConfigSyncPayload(
                    pos,
                    node.getPullSides(),
                    node.getPatternOverrides(),
                    node.getBindSides()));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void neighborChanged(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos,
                                   @Nonnull Block block, @Nullable Orientation orientation,
                                   boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level.isClientSide()) return;
        if (!(level.getBlockEntity(pos) instanceof LogisticsNodeBlockEntity node)) return;
        node.bindAdjacentStations();
    }
}
