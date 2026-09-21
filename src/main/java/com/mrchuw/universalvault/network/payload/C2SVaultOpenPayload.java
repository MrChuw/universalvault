package com.mrchuw.universalvault.network.payload;

import com.mrchuw.universalvault.UniversalVault;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record C2SVaultOpenPayload(UUID targetVaultUUID) implements CustomPacketPayload {
    public static final Type<C2SVaultOpenPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_open")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SVaultOpenPayload> STREAM_CODEC =
        CustomPacketPayload.codec(C2SVaultOpenPayload::write, C2SVaultOpenPayload::new);

    public C2SVaultOpenPayload(RegistryFriendlyByteBuf buf) {
        this(buf.readUUID());
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUUID(this.targetVaultUUID);
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
