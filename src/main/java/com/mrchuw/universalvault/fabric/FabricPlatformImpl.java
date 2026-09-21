package com.mrchuw.universalvault.fabric;

//? fabric {
import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.config.VaultClientConfig;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import net.minecraft.core.BlockPos;

//? if <26.1 {
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
//?} else {
/*import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
*///?}

public class FabricPlatformImpl implements Platform {

    private final VaultConfig config = new FabricConfig();
    private final VaultClientConfig clientConfig = new FabricClientConfig();

    @Override public boolean isModLoaded(String modid) { return FabricLoader.getInstance().isModLoaded(modid); }
    @Override public String loader() { return "fabric"; }
    @Override public VaultConfig config() { return config; }
    @Override public VaultClientConfig clientConfig() { return clientConfig; }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void openVaultMenu(ServerPlayer player, UUID targetVaultUuid, Component title) {
        //? if <26.1 {
        player.openMenu(new ExtendedScreenHandlerFactory<UUID>() {
            @Override
            public UUID getScreenOpeningData(ServerPlayer player) {
                return targetVaultUuid;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return new VaultMenu(syncId, inv, targetVaultUuid);
            }
        });
        //?} else {
        /*player.openMenu(new ExtendedMenuProvider<UUID>() {
            @Override
            public UUID getScreenOpeningData(ServerPlayer player) {
                return targetVaultUuid;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return new VaultMenu(syncId, inv, targetVaultUuid);
            }
        });
        *///?}
    }

    @Override
    public void openVaultFilterMenu(ServerPlayer player, VaultIOBlockEntity ioBe, BlockPos pos, Component title) {
        //? if <26.1 {
        player.openMenu(new ExtendedScreenHandlerFactory<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return new VaultFilterMenu(syncId, inv, ioBe);
            }
        });
        //?} else {
        /*player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override
            public BlockPos getScreenOpeningData(ServerPlayer player) {
                return pos;
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                return new VaultFilterMenu(syncId, inv, ioBe);
            }
        });
        *///?}
    }
}
//?}