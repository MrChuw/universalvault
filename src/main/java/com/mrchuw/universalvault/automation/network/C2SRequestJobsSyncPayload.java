package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SRequestJobsSyncPayload() implements CustomPacketPayload {

    public static final Type<C2SRequestJobsSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "request_jobs_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestJobsSyncPayload> STREAM_CODEC =
            StreamCodec.ofMember(
                    (p, buf) -> {},
                    buf -> new C2SRequestJobsSyncPayload());

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}