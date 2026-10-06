package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SCancelJobPayload(UUID jobId) implements CustomPacketPayload {

    public static final Type<C2SCancelJobPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "cancel_job"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SCancelJobPayload> STREAM_CODEC =
            CustomPacketPayload.codec(
                    (p, buf) -> buf.writeUUID(p.jobId()),
                    buf -> new C2SCancelJobPayload(buf.readUUID()));

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}