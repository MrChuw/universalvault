package com.mrchuw.universalvault.automation.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record S2CUpdateCraftResultPayload(ItemStack result) implements CustomPacketPayload {
    public static final Type<S2CUpdateCraftResultPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("universalvault", "update_craft_result"));

    public static final StreamCodec<ByteBuf, S2CUpdateCraftResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, payload.result),
            buf -> new S2CUpdateCraftResultPayload(ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
