package com.mrchuw.universalvault.automation.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.CraftJob;
import com.mrchuw.universalvault.storage.ItemKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record S2CJobsSyncPayload(List<Entry> entries) implements CustomPacketPayload {

    public static final Type<S2CJobsSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "jobs_sync"));

    public record Entry(
            UUID jobId,
            UUID patternId,
            ItemKey topOutput,
            String origin,
            String state,
            long requested,
            long completed,
            int totalSlices,
            int activeSlices
    ) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                UUIDUtil.CODEC.fieldOf("job").forGetter(Entry::jobId),
                UUIDUtil.CODEC.fieldOf("pattern").forGetter(Entry::patternId),
                ItemKey.CODEC.fieldOf("output").forGetter(Entry::topOutput),
                Codec.STRING.fieldOf("origin").forGetter(Entry::origin),
                Codec.STRING.fieldOf("state").forGetter(Entry::state),
                Codec.LONG.fieldOf("requested").forGetter(Entry::requested),
                Codec.LONG.fieldOf("completed").forGetter(Entry::completed),
                Codec.INT.fieldOf("slices").forGetter(Entry::totalSlices),
                Codec.INT.fieldOf("active").forGetter(Entry::activeSlices)
        ).apply(inst, Entry::new));

        public static Entry of(CraftJob job) {
            int active = 0;
            for (var s : job.slices()) {
                if (s.state() == com.mrchuw.universalvault.automation.pattern.JobSlice.State.ACTIVE) {
                    active++;
                }
            }
            return new Entry(
                    job.jobId(), job.patternId(), job.topOutput(),
                    job.origin().name(), job.state().name(),
                    job.requestedThisCycle(), job.completedThisCycle(),
                    job.slices().size(), active);
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CJobsSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public S2CJobsSyncPayload decode(RegistryFriendlyByteBuf buf) {
                    int n = buf.readVarInt();
                    List<Entry> out = new ArrayList<>(n);
                    for (int i = 0; i < n; i++) {
                        CompoundTag tag = buf.readNbt();
                        if (tag == null) continue;
                        out.add(Entry.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow());
                    }
                    return new S2CJobsSyncPayload(out);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, S2CJobsSyncPayload p) {
                    buf.writeVarInt(p.entries().size());
                    for (Entry e : p.entries()) {
                        CompoundTag tag = (CompoundTag) Entry.CODEC
                                .encodeStart(NbtOps.INSTANCE, e).getOrThrow();
                        buf.writeNbt(tag);
                    }
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}