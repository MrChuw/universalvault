package com.mrchuw.universalvault.config;

import com.mrchuw.universalvault.client.VaultSortMode;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class UniversalVaultClientConfig {

    public static final UniversalVaultClientConfig CONFIG;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.EnumValue<VaultSortMode> sortMode;

    private UniversalVaultClientConfig(ModConfigSpec.Builder builder) {
        builder.comment("Universal Vault — client preferences").push("client");

        sortMode = builder
                .comment("Default sort mode for the vault screen.",
                        "Click the sort button in the GUI to change it.")
                .defineEnum("sortMode", VaultSortMode.COUNT_DESC);

        builder.pop();
    }

    static {
        Pair<UniversalVaultClientConfig, ModConfigSpec> pair =
                new ModConfigSpec.Builder().configure(UniversalVaultClientConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }
}