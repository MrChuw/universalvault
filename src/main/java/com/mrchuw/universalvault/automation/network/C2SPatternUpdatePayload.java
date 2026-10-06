package com.mrchuw.universalvault.automation.network;

import com.mojang.serialization.Codec;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SPatternUpdatePayload(
        UUID owner,
        Optional<VaultPattern> pattern
) implements CustomPacketPayload {

    private static final Codec<VaultPattern> CODEC = VaultPattern.CODEC;

    public static final Type<C2SPatternUpdatePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "pattern_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPatternUpdatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SPatternUpdatePayload decode(RegistryFriendlyByteBuf buf) {
                    UUID owner = buf.readUUID();
                    boolean has = buf.readBoolean();
                    VaultPattern pattern = null;
                    if (has) {
                        CompoundTag tag = buf.readNbt();
                        if (tag != null) {
                            pattern = CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                        }
                    }
                    return new C2SPatternUpdatePayload(owner, Optional.ofNullable(pattern));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, C2SPatternUpdatePayload p) {
                    buf.writeUUID(p.owner());
                    if (p.pattern().isPresent()) {
                        buf.writeBoolean(true);
                        CompoundTag tag = (CompoundTag) CODEC
                                .encodeStart(NbtOps.INSTANCE, p.pattern().get())
                                .getOrThrow();
                        buf.writeNbt(tag);
                    } else {
                        buf.writeBoolean(false);
                    }
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}