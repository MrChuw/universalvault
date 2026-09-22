package com.mrchuw.universalvault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalVault {

    public static final String MOD_ID = "universal_vault";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final java.util.UUID GLOBAL_VAULT_UUID = new java.util.UUID(0L, 0L);

    public static final String VERSION   = /*$ mod_version*/ "1.0.0";
    public static final String MINECRAFT = /*$ minecraft*/   "26.3";

    public static void init() {
        LOGGER.info("Initializing {} {} for Minecraft {} on {}",
                MOD_ID, VERSION, MINECRAFT, Platform.INSTANCE.loader());
    }
}