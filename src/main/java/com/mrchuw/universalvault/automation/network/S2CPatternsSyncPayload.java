package com.mrchuw.universalvault.automation.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record S2CPatternsSyncPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final Type<S2CPatternsSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "patterns_sync"));

    public record Entry(VaultPattern pattern, long currentStock) {

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                VaultPattern.CODEC.fieldOf("pattern").forGetter(Entry::pattern),
                Codec.LONG.optionalFieldOf("stock", 0L).forGetter(Entry::currentStock)
        ).apply(inst, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC =
                new StreamCodec<>() {
                    @Override
                    public Entry decode(RegistryFriendlyByteBuf buf) {
                        CompoundTag tag = buf.readNbt();
                        if (tag == null) return null;
                        return Entry.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                    }

                    @Override
                    public void encode(RegistryFriendlyByteBuf buf, Entry e) {
                        CompoundTag tag = (CompoundTag) Entry.CODEC
                                .encodeStart(NbtOps.INSTANCE, e).getOrThrow();
                        buf.writeNbt(tag);
                    }
                };
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPatternsSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public S2CPatternsSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    int n = buf.readVarInt();
                    List<Entry> out = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        Entry e = Entry.STREAM_CODEC.decode(buf);
                        if (e != null) out.add(e);
                    }
                    return new S2CPatternsSyncPayload(out);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, S2CPatternsSyncPayload p) {
                    buf.writeVarInt(p.entries().size());
                    for (Entry e : p.entries()) {
                        Entry.STREAM_CODEC.encode(buf, e);
                    }
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
