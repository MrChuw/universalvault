package com.mrchuw.universalvault.automation.network;

import com.mojang.serialization.Codec;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.automation.recipe.RecipeView;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SEncodePatternPayload(
        RecipeView recipe,
        long min,
        long max,
        int batch,
        int priority,
        VaultPattern.TriggerMode triggerMode,
        VaultPattern.PushMode pushMode,
        VaultPattern.Modifiers modifiers,
        Optional<java.util.UUID> existingPatternId,
        Destination destination
) implements CustomPacketPayload {

    public enum Destination { SLOT, INVENTORY, LIBRARY }

    public static final Type<C2SEncodePatternPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "encode_pattern"));

    private record Wire(
            RecipeView recipe,
            long min, long max, int batch, int priority,
            String trigger, String push, VaultPattern.Modifiers modifiers,
            Optional<java.util.UUID> existingPatternId,
            String destination
    ) {
        static final Codec<Wire> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.create(inst -> inst.group(
                RecipeView.CODEC.fieldOf("recipe").forGetter(Wire::recipe),
                Codec.LONG.optionalFieldOf("min", 0L).forGetter(Wire::min),
                Codec.LONG.optionalFieldOf("max", 0L).forGetter(Wire::max),
                Codec.INT.optionalFieldOf("batch", 0).forGetter(Wire::batch),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(Wire::priority),
                Codec.STRING.optionalFieldOf("trigger", "BATCH_HYSTERESIS").forGetter(Wire::trigger),
                Codec.STRING.optionalFieldOf("push", "PUSH_ALL").forGetter(Wire::push),
                VaultPattern.Modifiers.CODEC.optionalFieldOf("modifiers", VaultPattern.Modifiers.DEFAULT)
                        .forGetter(Wire::modifiers),
                net.minecraft.core.UUIDUtil.CODEC.optionalFieldOf("existing")
                        .forGetter(Wire::existingPatternId),
                Codec.STRING.optionalFieldOf("destination", "SLOT")
                        .forGetter(Wire::destination)
        ).apply(inst, Wire::new));
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SEncodePatternPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SEncodePatternPayload decode(RegistryFriendlyByteBuf buf) {
                    CompoundTag tag = buf.readNbt();
                    if (tag == null) throw new IllegalStateException("missing encode payload");
                    Wire w = Wire.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
                    return new C2SEncodePatternPayload(
                            w.recipe(), w.min(), w.max(), w.batch(), w.priority(),
                            VaultPattern.TriggerMode.valueOf(w.trigger()),
                            VaultPattern.PushMode.valueOf(w.push()),
                            w.modifiers(), w.existingPatternId(),
                            Destination.valueOf(w.destination()));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, C2SEncodePatternPayload p) {
                    Wire w = new Wire(p.recipe(), p.min(), p.max(), p.batch(), p.priority(),
                            p.triggerMode().name(), p.pushMode().name(),
                            p.modifiers(), p.existingPatternId(),
                            p.destination().name());
                    CompoundTag tag = (CompoundTag) Wire.CODEC
                            .encodeStart(NbtOps.INSTANCE, w).getOrThrow();
                    buf.writeNbt(tag);
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}