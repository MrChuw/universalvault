package com.mrchuw.universalvault.util;

//? if fabric {
/*import net.fabricmc.loader.api.FabricLoader;
*///? } elif neoforge {
import net.neoforged.fml.loading.FMLEnvironment;
 //?}

public class DevEnvironment {

    public static boolean isDev() {
        //? if fabric {
        /*return FabricLoader.getInstance().isDevelopmentEnvironment();
        *///? } elif neoforge {
        return !FMLEnvironment.isProduction();
         //?}
    }
}