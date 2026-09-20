package com.mrchuw.universalvault.network.payload;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.List;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record S2CVaultSyncPayload(List<Entry> entries) implements CustomPacketPayload {
    public static final Type<S2CVaultSyncPayload> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_sync")
    );

    public record Entry(ItemKey key, long count) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, entry -> entry.key().itemId(),
                DataComponentPatch.STREAM_CODEC, entry -> entry.key().components(),
                ByteBufCodecs.VAR_LONG, Entry::count,
                (id, patch, count) -> new Entry(new ItemKey(id, patch), count)
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CVaultSyncPayload> STREAM_CODEC = StreamCodec.composite(
        Entry.STREAM_CODEC.apply(ByteBufCodecs.list()),
        S2CVaultSyncPayload::entries,
        S2CVaultSyncPayload::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
