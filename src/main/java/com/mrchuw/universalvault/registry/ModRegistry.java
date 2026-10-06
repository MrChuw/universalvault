package com.mrchuw.universalvault.registry;

import com.mrchuw.universalvault.automation.node.LogisticsNodeBlockEntity;
import com.mrchuw.universalvault.automation.pattern.VaultPattern;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.storage.VaultIOData;

import java.util.UUID;
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

    public static Supplier<Block> LOGISTICS_NODE;
    public static Supplier<Item> LOGISTICS_NODE_ITEM;
    public static Supplier<BlockEntityType<LogisticsNodeBlockEntity>> LOGISTICS_NODE_BLOCK_ENTITY;

    public static Supplier<Item> ENCODED_PATTERN;
    public static Supplier<DataComponentType<VaultPattern>> ENCODED_PATTERN_DATA;
    private ModRegistry() {}
}