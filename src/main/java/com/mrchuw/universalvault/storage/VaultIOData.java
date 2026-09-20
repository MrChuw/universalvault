package com.mrchuw.universalvault.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record VaultIOData(UUID targetVault, List<ItemStack> filters) {

    public static final Codec<VaultIOData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.fieldOf("target").forGetter(VaultIOData::targetVault),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("filters").forGetter(VaultIOData::filters)
    ).apply(inst, VaultIOData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, VaultIOData> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC, VaultIOData::targetVault,
                    ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), VaultIOData::filters,
                    VaultIOData::new
            );
}
