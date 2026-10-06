package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SPatternActionPayload(
        UUID patternId,
        Action action
) implements CustomPacketPayload {

    public enum Action { PAUSE_TOGGLE, REMOVE, CANCEL_JOB, WITHDRAW, WITHDRAW_TO_INVENTORY }

    public static final Type<C2SPatternActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "pattern_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SPatternActionPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SPatternActionPayload decode(RegistryFriendlyByteBuf buf) {
                    UUID id = buf.readUUID();
                    Action a = Action.values()[buf.readVarInt()];
                    return new C2SPatternActionPayload(id, a);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, C2SPatternActionPayload p) {
                    buf.writeUUID(p.patternId());
                    buf.writeVarInt(p.action().ordinal());
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
