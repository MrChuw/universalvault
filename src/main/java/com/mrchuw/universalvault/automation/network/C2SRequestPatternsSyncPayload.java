package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SRequestPatternsSyncPayload(Optional<BlockPos> nodePos)
        implements CustomPacketPayload {

    public static final Type<C2SRequestPatternsSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "request_patterns_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestPatternsSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(BlockPos.STREAM_CODEC),
                    C2SRequestPatternsSyncPayload::nodePos,
                    C2SRequestPatternsSyncPayload::new
            );

    public static C2SRequestPatternsSyncPayload forMenu() {
        return new C2SRequestPatternsSyncPayload(Optional.empty());
    }

    public static C2SRequestPatternsSyncPayload forNode(BlockPos pos) {
        return new C2SRequestPatternsSyncPayload(Optional.of(pos));
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}