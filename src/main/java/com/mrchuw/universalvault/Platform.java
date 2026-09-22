package com.mrchuw.universalvault;

import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultClientConfig;
import com.mrchuw.universalvault.config.VaultConfig;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

public interface Platform {

    //? if fabric {
    /*Platform INSTANCE = new com.mrchuw.universalvault.fabric.FabricPlatformImpl();
    *///?} else if neoforge {
    Platform INSTANCE = new com.mrchuw.universalvault.neoforge.NeoforgePlatformImpl();
     //?}

    boolean isModLoaded(String modid);
    String loader();

    VaultConfig config();
    VaultClientConfig clientConfig();

    void sendToServer(CustomPacketPayload payload);
    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void openVaultMenu(ServerPlayer player, UUID targetVaultUuid, Component title);
    void openVaultFilterMenu(ServerPlayer player, VaultIOBlockEntity ioBe, BlockPos pos, Component title);
}