package com.mrchuw.universalvault.neoforge;

//? neoforge {
/*import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.config.VaultConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

@Mod(UniversalVault.MOD_ID)
public class NeoforgeEntrypoint {

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final NeoforgeConfig SERVER_CONFIG;
    public static final NeoforgeClientConfig CLIENT_CONFIG;

    static {
        var serverPair = new ModConfigSpec.Builder().configure(NeoforgeConfig::new);
        SERVER_CONFIG = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();

        var clientPair = new ModConfigSpec.Builder().configure(NeoforgeClientConfig::new);
        CLIENT_CONFIG = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();
    }

    public NeoforgeEntrypoint(IEventBus modBus, ModContainer container) {
        UniversalVault.init();

        // Registra a config no Platform
        NeoforgePlatformImpl impl = (NeoforgePlatformImpl) com.mrchuw.universalvault.Platform.INSTANCE;
        impl.setConfig(SERVER_CONFIG);
        impl.setClientConfig(CLIENT_CONFIG);

        NeoforgeRegistry.register(modBus);
        NeoforgeNetwork.register(modBus);
        NeoforgeEvents.register(modBus);

        container.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, CLIENT_SPEC);
    }
}
*///?}
