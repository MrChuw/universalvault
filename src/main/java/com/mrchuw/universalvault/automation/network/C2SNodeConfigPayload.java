package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SNodeConfigPayload(
        BlockPos nodePos,
        Set<Direction> pullSides,
        Map<UUID, Boolean> patternOverrides,
        Set<Direction> bindSides
) implements CustomPacketPayload {

    public static final Type<C2SNodeConfigPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "node_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SNodeConfigPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public C2SNodeConfigPayload decode(RegistryFriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();

                    int nPull = buf.readVarInt();
                    Set<Direction> pull = EnumSet.noneOf(Direction.class);
                    for (int i = 0; i < nPull; i++) pull.add(Direction.values()[buf.readVarInt()]);

                    int nOverrides = buf.readVarInt();
                    Map<UUID, Boolean> overrides = new HashMap<>(nOverrides);
                    for (int i = 0; i < nOverrides; i++) {
                        overrides.put(buf.readUUID(), buf.readBoolean());
                    }

                    int nBind = buf.readVarInt();
                    Set<Direction> bind = EnumSet.noneOf(Direction.class);
                    for (int i = 0; i < nBind; i++) bind.add(Direction.values()[buf.readVarInt()]);

                    return new C2SNodeConfigPayload(pos, pull, overrides, bind);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, C2SNodeConfigPayload p) {
                    buf.writeBlockPos(p.nodePos());

                    List<Direction> pull = new ArrayList<>(p.pullSides());
                    buf.writeVarInt(pull.size());
                    for (Direction d : pull) buf.writeVarInt(d.ordinal());

                    buf.writeVarInt(p.patternOverrides().size());
                    for (Map.Entry<UUID, Boolean> e : p.patternOverrides().entrySet()) {
                        buf.writeUUID(e.getKey());
                        buf.writeBoolean(e.getValue());
                    }

                    List<Direction> bind = new ArrayList<>(p.bindSides());
                    buf.writeVarInt(bind.size());
                    for (Direction d : bind) buf.writeVarInt(d.ordinal());
                }
            };

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
