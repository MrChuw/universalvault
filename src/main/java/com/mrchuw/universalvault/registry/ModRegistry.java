package com.mrchuw.universalvault.registry;

import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.block.VaultIOBlock;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.item.VaultIOBlockItem;
import com.mrchuw.universalvault.item.VaultRemoteItem;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.Set;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UniversalVault.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UniversalVault.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE,
            UniversalVault.MOD_ID
    );
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(
            Registries.MENU,
            UniversalVault.MOD_ID
    );
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE,
            UniversalVault.MOD_ID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<VaultMenu>> VAULT_MENU = MENUS.register("vault_menu", () ->
            IMenuTypeExtension.create(VaultMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<VaultFilterMenu>> VAULT_FILTER_MENU =
            MENUS.register("vault_filter_menu", () ->
                    IMenuTypeExtension.create(VaultFilterMenu::new)
            );

    public static final DeferredHolder<Block, VaultIOBlock> VAULT_IO = BLOCKS.register("vault_io", id ->
            new VaultIOBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, id))
                    .strength(5.0F, 6.0F)
                    .requiresCorrectToolForDrops())
    );

    public static final DeferredHolder<Item, VaultIOBlockItem> VAULT_IO_BLOCK_ITEM =
            ITEMS.register("vault_io", id ->
                    new VaultIOBlockItem(
                            VAULT_IO.get(),
                            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))
                    )
            );

    public static final DeferredHolder<Item, VaultRemoteItem> VAULT_REMOTE = ITEMS.register("vault_remote", id ->
            new VaultRemoteItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1))
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<VaultIOBlockEntity>> VAULT_IO_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("vault_io", () ->
                    new BlockEntityType<>(VaultIOBlockEntity::new, Set.of(VAULT_IO.get()))
            );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<VaultIOData>> VAULT_IO_DATA =
            DATA_COMPONENTS.register("vault_io_data", () ->
                    DataComponentType.<VaultIOData>builder()
                            .persistent(VaultIOData.CODEC)
                            .networkSynchronized(VaultIOData.STREAM_CODEC)
                            .build()
            );

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        DATA_COMPONENTS.register(modBus);
    }
}