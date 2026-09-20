package com.mrchuw.universalvault.network.payload;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.Optional;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public record C2SVaultActionPayload(
        ActionType actionType,
        @Nullable ItemKey targetKey,
        int amount,
        int slotIndex
) implements CustomPacketPayload {
    public static final Type<C2SVaultActionPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_action")
    );

    public enum ActionType {
        EXTRACT,
        DEPOSIT_HELD,
        DEPOSIT_SLOT,
        PICKUP,
        PICKUP_ALL,
        PICKUP_HALF,
        QUICK_MOVE,
        QUICK_MOVE_HALF,
        DEPOSIT_ALL,
        DEPOSIT_ONE,
        DROP_ONE,
        DROP_STACK,
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemKey> ITEM_KEY_STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ItemKey::itemId,
            DataComponentPatch.STREAM_CODEC, ItemKey::components,
            ItemKey::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ItemKey>> OPTIONAL_ITEM_KEY_CODEC =
            ByteBufCodecs.optional(ITEM_KEY_STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SVaultActionPayload> STREAM_CODEC = StreamCodec.ofMember(
            C2SVaultActionPayload::write,
            C2SVaultActionPayload::new
    );

    public C2SVaultActionPayload(RegistryFriendlyByteBuf buf) {
        this(
                buf.readEnum(ActionType.class),
                OPTIONAL_ITEM_KEY_CODEC.decode(buf).orElse(null),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeEnum(actionType);
        OPTIONAL_ITEM_KEY_CODEC.encode(buf, Optional.ofNullable(targetKey));
        buf.writeVarInt(amount);
        buf.writeVarInt(slotIndex);
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
