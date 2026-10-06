package com.mrchuw.universalvault.automation.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record C2SRequestCraftResultPayload(List<ItemStack> items) implements CustomPacketPayload {
    public static final Type<C2SRequestCraftResultPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("universalvault", "request_craft_result"));

    public static final StreamCodec<ByteBuf, C2SRequestCraftResultPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.items.size());
                for (ItemStack stack : payload.items) {
                    ItemStack.OPTIONAL_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, stack);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<ItemStack> items = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    items.add(ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf));
                }
                return new C2SRequestCraftResultPayload(items);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}