package com.mrchuw.universalvault.registry;

import com.mrchuw.universalvault.UniversalVault;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, UniversalVault.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> UNIVERSAL_VAULT_TAB =
            CREATIVE_MODE_TABS.register("universal_vault", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.universal_vault"))
                    .icon(() -> new ItemStack(ModRegistry.VAULT_REMOTE.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModRegistry.VAULT_IO_BLOCK_ITEM.get());
                        output.accept(ModRegistry.VAULT_REMOTE.get());
                    })
                    .build()
            );

    public static void register(IEventBus modBus) {
        CREATIVE_MODE_TABS.register(modBus);
    }
}