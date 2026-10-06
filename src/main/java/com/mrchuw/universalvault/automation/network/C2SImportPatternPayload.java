package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SImportPatternPayload(VaultPattern pattern) implements CustomPacketPayload {

    public static final Type<C2SImportPatternPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "import_pattern"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SImportPatternPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SImportPatternPayload decode(RegistryFriendlyByteBuf buf) {
                    CompoundTag tag = buf.readNbt();
                    if (tag == null) throw new IllegalStateException("missing pattern");
                    VaultPattern p = VaultPattern.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                    return new C2SImportPatternPayload(p);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, C2SImportPatternPayload p) {
                    CompoundTag tag = (CompoundTag) VaultPattern.CODEC
                            .encodeStart(NbtOps.INSTANCE, p.pattern()).getOrThrow();
                    buf.writeNbt(tag);
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}