package com.mrchuw.universalvault.registry;

import com.mrchuw.universalvault.block.VaultIOBlock;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.item.VaultIOBlockItem;
import com.mrchuw.universalvault.item.VaultRemoteItem;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModRegistry {

    public static Supplier<MenuType<VaultMenu>> VAULT_MENU;
    public static Supplier<MenuType<VaultFilterMenu>> VAULT_FILTER_MENU;
    public static Supplier<Block> VAULT_IO;
    public static Supplier<Item> VAULT_IO_BLOCK_ITEM;
    public static Supplier<Item> VAULT_REMOTE;
    public static Supplier<BlockEntityType<VaultIOBlockEntity>> VAULT_IO_BLOCK_ENTITY;
    public static Supplier<DataComponentType<VaultIOData>> VAULT_IO_DATA;

    private ModRegistry() {}
}