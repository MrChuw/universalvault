package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.handler.ItemAutomationHandler;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.config.VaultClientConfig;
import com.mrchuw.universalvault.config.VaultConfig;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;

//? if <26.1 {
/^import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
^///?} else {
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
 //?}

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
        player.openMenu(createExtendedMenu(title, targetVaultUuid, (syncId, inv, p) -> new VaultMenu(syncId, inv, targetVaultUuid)));
    }

    @Override
    public void openVaultFilterMenu(ServerPlayer player, VaultIOBlockEntity ioBe, BlockPos pos, Component title) {
        player.openMenu(createExtendedMenu(title, pos, (syncId, inv, p) -> new VaultFilterMenu(syncId, inv, ioBe)));
    }

    @Override
    public ItemAutomationHandler findItemHandler(Level level, BlockPos pos, Direction side) {
        Storage<ItemVariant> h = ItemStorage.SIDED.find(level, pos, null);
        if (h == null && side != null) h = ItemStorage.SIDED.find(level, pos, side);
        return h == null ? null : new FabricItemAutomationHandler(h);
    }

    private static <T> MenuProvider createExtendedMenu(Component title, T data, MenuConstructor constructor) {
        //? if <26.1 {
        /^return new ExtendedScreenHandlerFactory<T>() {
            @Override public T getScreenOpeningData(ServerPlayer player) { return data; }
            @Override public Component getDisplayName() { return title; }
            @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) { return constructor.createMenu(syncId, inv, p); }
        };
        ^///?} else {
        return new ExtendedMenuProvider<T>() {
            @Override public T getScreenOpeningData(ServerPlayer player) { return data; }
            @Override public Component getDisplayName() { return title; }
            @Override public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) { return constructor.createMenu(syncId, inv, p); }
        };
        //?}
    }
}
*///?}
