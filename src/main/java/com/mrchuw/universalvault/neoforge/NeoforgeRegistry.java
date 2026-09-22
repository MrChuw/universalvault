package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.block.VaultIOBlock;
import com.mrchuw.universalvault.block.entity.VaultIOBlockEntity;
import com.mrchuw.universalvault.gui.menu.VaultFilterMenu;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.item.VaultIOBlockItem;
import com.mrchuw.universalvault.item.VaultRemoteItem;
import com.mrchuw.universalvault.registry.ModRegistry;
import com.mrchuw.universalvault.storage.VaultIOData;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NeoforgeRegistry {

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UniversalVault.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UniversalVault.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, UniversalVault.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, UniversalVault.MOD_ID);
    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, UniversalVault.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UniversalVault.MOD_ID);

    public static void register(IEventBus modBus) {
        var vaultMenu = MENUS.register("vault_menu",
                () -> IMenuTypeExtension.create(VaultMenu::new));
        var vaultFilterMenu = MENUS.register("vault_filter_menu",
                () -> IMenuTypeExtension.create(VaultFilterMenu::new));

        var vaultIo = BLOCKS.register("vault_io", id ->
                new VaultIOBlock(BlockBehaviour.Properties.of()
                        .setId(ResourceKey.create(Registries.BLOCK, id))
                        .strength(5.0F, 6.0F)
                        .requiresCorrectToolForDrops()));

        // Inside factory lambdas, calling .get() is deferred until registration time:
        var vaultIoItem = ITEMS.register("vault_io", id ->
                new VaultIOBlockItem(vaultIo.get(),
                        new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));

        var vaultRemote = ITEMS.register("vault_remote", id ->
                new VaultRemoteItem(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .stacksTo(1)));

        var vaultIoBe = BLOCK_ENTITIES.register("vault_io",
                () -> new BlockEntityType<>(VaultIOBlockEntity::new, Set.of(vaultIo.get())));

        var vaultIoData = DATA_COMPONENTS.register("vault_io_data",
                () -> DataComponentType.<VaultIOData>builder()
                        .persistent(VaultIOData.CODEC)
                        .networkSynchronized(VaultIOData.STREAM_CODEC)
                        .build());

        TABS.register("universal_vault", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.universal_vault"))
                .icon(() -> new ItemStack(vaultRemote.get()))
                .displayItems((p, out) -> {
                    out.accept(vaultIoItem.get());
                    out.accept(vaultRemote.get());
                })
                .build());

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        DATA_COMPONENTS.register(modBus);
        TABS.register(modBus);

        ModRegistry.VAULT_MENU = (Supplier) vaultMenu;
        ModRegistry.VAULT_FILTER_MENU = (Supplier) vaultFilterMenu;
        ModRegistry.VAULT_IO = (Supplier) vaultIo;
        ModRegistry.VAULT_IO_BLOCK_ITEM = (Supplier) vaultIoItem;
        ModRegistry.VAULT_REMOTE = (Supplier) vaultRemote;
        ModRegistry.VAULT_IO_BLOCK_ENTITY = (Supplier) vaultIoBe;
        ModRegistry.VAULT_IO_DATA = (Supplier) vaultIoData;
    }
}
//?}