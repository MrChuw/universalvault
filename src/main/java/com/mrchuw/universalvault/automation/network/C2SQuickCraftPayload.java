package com.mrchuw.universalvault.automation.network;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.storage.ItemKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import javax.annotation.Nonnull;

public record C2SQuickCraftPayload(
        ItemKey item,
        int amount
) implements CustomPacketPayload {

    public static final Type<C2SQuickCraftPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "quick_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SQuickCraftPayload> STREAM_CODEC =
            StreamCodec.composite(
                    com.mrchuw.universalvault.network.payload.C2SVaultActionPayload.ITEM_KEY_STREAM_CODEC,
                    C2SQuickCraftPayload::item,
                    net.minecraft.network.codec.ByteBufCodecs.VAR_INT,
                    C2SQuickCraftPayload::amount,
                    C2SQuickCraftPayload::new
            );

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}