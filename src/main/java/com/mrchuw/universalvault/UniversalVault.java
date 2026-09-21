package com.mrchuw.universalvault;

import com.mrchuw.universalvault.config.UniversalVaultClientConfig;
import com.mrchuw.universalvault.config.UniversalVaultConfig;
import com.mrchuw.universalvault.network.VaultNetwork;
import com.mrchuw.universalvault.registry.ModCreativeTabs;
import com.mrchuw.universalvault.registry.ModRegistry;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(UniversalVault.MOD_ID)
public class UniversalVault {

    public static final String MOD_ID = "universal_vault";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final UUID GLOBAL_VAULT_UUID = new UUID(0L, 0L);

    public static final String VERSION    = /*$ mod_version*/ "1.0.0";
    public static final String MINECRAFT  = /*$ minecraft*/   "26.3";

    public UniversalVault(IEventBus modBus, ModContainer container) {
        LOGGER.info("Initializing {} {} for Minecraft {}", MOD_ID, VERSION, MINECRAFT);

        ModRegistry.register(modBus);
        ModCreativeTabs.register(modBus);
        VaultNetwork.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, UniversalVaultConfig.SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, UniversalVaultClientConfig.SPEC);

    }

    public static Identifier id(String namespace, String path) {
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new Identifier(namespace, path);
         *///?}
    }
}