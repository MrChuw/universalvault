package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultClientConfig;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

public class NeoforgePlatformImpl implements Platform {

    private VaultConfig config;
    private VaultClientConfig clientConfig;

    void setConfig(VaultConfig c) { this.config = c; }
    void setClientConfig(VaultClientConfig c) { this.clientConfig = c; }

    @Override public boolean isModLoaded(String modid) { return ModList.get().isLoaded(modid); }
    @Override public String loader() { return "neoforge"; }
    @Override public VaultConfig config() { return config; }
    @Override public VaultClientConfig clientConfig() { return clientConfig; }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void openVaultMenu(ServerPlayer player, UUID targetVaultUuid, Component title) {
        player.openMenu(
                new SimpleMenuProvider(
                        (id, inv, p) -> new VaultMenu(id, inv, targetVaultUuid),
                        title
                ),
                buf -> buf.writeUUID(targetVaultUuid)
        );
    }

    @Override
    public void openVaultFilterMenu(ServerPlayer player, VaultIOBlockEntity ioBe, BlockPos pos, Component title) {
        player.openMenu(
                new SimpleMenuProvider(
                        (id, inv, p) -> new VaultFilterMenu(id, inv, ioBe),
                        title
                ),
                buf -> buf.writeBlockPos(pos)
        );
    }
}
//?}