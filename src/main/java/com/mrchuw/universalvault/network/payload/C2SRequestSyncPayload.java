package com.mrchuw.universalvault.network.payload;

import com.mrchuw.universalvault.UniversalVault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record C2SRequestSyncPayload() implements CustomPacketPayload {
    public static final Type<C2SRequestSyncPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "request_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestSyncPayload> STREAM_CODEC = StreamCodec.ofMember(
        (p, buf) -> {},
        buf -> new C2SRequestSyncPayload()
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
