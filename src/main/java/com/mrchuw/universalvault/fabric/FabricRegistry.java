package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.block.VaultIOBlock;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.item.VaultIOBlockItem;
import com.mrchuw.universalvault.item.VaultRemoteItem;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.Set;

//? if <26.2 {
/^import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
^///?}

//? if <26.1 {
/^import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
^///?} else {
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
//?}

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FabricRegistry {

    public static void register() {
        Identifier vaultIoId = Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_io");
        Identifier vaultRemoteId = Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_remote");

        // ---- Block -----------------------------------------------------------
        //? if >=1.21.10 {
        ResourceKey<Block> vaultIoBlockKey = ResourceKey.create(Registries.BLOCK, vaultIoId);
        BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of()
                .setId(vaultIoBlockKey)
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops();
        Block vaultIo = Registry.register(
                BuiltInRegistries.BLOCK,
                vaultIoBlockKey,
                new VaultIOBlock(blockProperties)
        );
        //?} else {
        /^BlockBehaviour.Properties blockProperties = BlockBehaviour.Properties.of()
                .strength(5.0F, 6.0F)
                .requiresCorrectToolForDrops();
        Block vaultIo = Registry.register(
                BuiltInRegistries.BLOCK,
                vaultIoId,
                new VaultIOBlock(blockProperties)
        );
        ^///?}

        ModRegistry.VAULT_IO = () -> vaultIo;

        // ---- Items -----------------------------------------------------------
        //? if >=1.21.10 {
        ResourceKey<Item> vaultIoItemKey = ResourceKey.create(Registries.ITEM, vaultIoId);
        Item.Properties vaultIoItemProps = new Item.Properties().setId(vaultIoItemKey);
        VaultIOBlockItem vaultIoItem = new VaultIOBlockItem(vaultIo, vaultIoItemProps);
        Registry.register(BuiltInRegistries.ITEM, vaultIoItemKey, vaultIoItem);

        ResourceKey<Item> vaultRemoteItemKey = ResourceKey.create(Registries.ITEM, vaultRemoteId);
        Item.Properties vaultRemoteProps = new Item.Properties().setId(vaultRemoteItemKey).stacksTo(1);
        VaultRemoteItem vaultRemote = new VaultRemoteItem(vaultRemoteProps);
        Registry.register(BuiltInRegistries.ITEM, vaultRemoteItemKey, vaultRemote);
        //?} else {
        /^VaultIOBlockItem vaultIoItem = new VaultIOBlockItem(vaultIo, new Item.Properties());
        Registry.register(BuiltInRegistries.ITEM, vaultIoId, vaultIoItem);

        VaultRemoteItem vaultRemote = new VaultRemoteItem(new Item.Properties().stacksTo(1));
        Registry.register(BuiltInRegistries.ITEM, vaultRemoteId, vaultRemote);
        ^///?}

        ModRegistry.VAULT_IO_BLOCK_ITEM = () -> vaultIoItem;
        ModRegistry.VAULT_REMOTE = () -> vaultRemote;

        // ---- Block entity type -----------------------------------------------
        BlockEntityType<VaultIOBlockEntity> beType = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                vaultIoId,
                //? if <26.2 {
                /^FabricBlockEntityTypeBuilder.create(VaultIOBlockEntity::new, vaultIo).build()
                ^///?} else {
                new BlockEntityType<>(VaultIOBlockEntity::new, Set.of(vaultIo))
                 //?}
        );
        ModRegistry.VAULT_IO_BLOCK_ENTITY = () -> beType;

        // ---- Data component ---------------------------------------------------
        DataComponentType<VaultIOData> dataComp = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_io_data"),
                DataComponentType.<VaultIOData>builder()
                        .persistent(VaultIOData.CODEC)
                        .networkSynchronized(VaultIOData.STREAM_CODEC)
                        .build()
        );
        ModRegistry.VAULT_IO_DATA = () -> dataComp;

        // ---- Menu types ------------------------------------------------------
        MenuType<VaultMenu> vaultMenu = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_menu"),
                //? if <26.1 {
                /^new ExtendedScreenHandlerType<>(
                        VaultMenu::new,
                        net.minecraft.core.UUIDUtil.STREAM_CODEC
                )
                ^///?} else {
                new ExtendedMenuType<>(
                        VaultMenu::new,
                        net.minecraft.core.UUIDUtil.STREAM_CODEC
                )
                //?}
        );
        ModRegistry.VAULT_MENU = () -> vaultMenu;

        MenuType<VaultFilterMenu> vaultFilterMenu = Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "vault_filter_menu"),
                //? if <26.1 {
                /^new ExtendedScreenHandlerType<>(
                        VaultFilterMenu::new,
                        net.minecraft.network.codec.StreamCodec.of(
                                (buf, pos) -> buf.writeBlockPos(pos),
                                buf -> buf.readBlockPos()
                        )
                )
                ^///?} else {
                new ExtendedMenuType<>(
                        VaultFilterMenu::new,
                        net.minecraft.network.codec.StreamCodec.of(
                                (buf, pos) -> buf.writeBlockPos(pos),
                                buf -> buf.readBlockPos()
                        )
                )
                //?}
        );
        ModRegistry.VAULT_FILTER_MENU = () -> vaultFilterMenu;

        // ---- Creative tab ----------------------------------------------------
        //? if <26.1 {
        /^CreativeModeTab tab = FabricItemGroup.builder()
                ^///?} else {
                CreativeModeTab tab = FabricCreativeModeTab.builder()
                 //?}
                .title(Component.translatable("itemGroup.universal_vault"))
                .icon(() -> new ItemStack(vaultRemote))
                .displayItems((params, output) -> {
                    output.accept(new ItemStack(vaultIoItem));
                    output.accept(new ItemStack(vaultRemote));
                })
                .build();

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(UniversalVault.MOD_ID, "universal_vault"),
                tab
        );

        ItemStorage.SIDED.registerForBlockEntity(
                (be, direction) -> new FabricItemHandler(be),
                beType
        );
    }
}
*///?}