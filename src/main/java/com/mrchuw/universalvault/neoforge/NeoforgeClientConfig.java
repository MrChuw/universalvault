package com.mrchuw.universalvault.neoforge;

//? neoforge {
/*import com.mrchuw.universalvault.client.VaultSortMode;
import com.mrchuw.universalvault.config.VaultClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class NeoforgeClientConfig implements VaultClientConfig {
    public final ModConfigSpec.EnumValue<VaultSortMode> sortMode;

    public NeoforgeClientConfig(ModConfigSpec.Builder b) {
        b.push("client");
        sortMode = b.defineEnum("sortMode", VaultSortMode.COUNT_DESC);
        b.pop();
    }

    @Override public VaultSortMode sortMode() { return sortMode.get(); }
    @Override public void setSortMode(VaultSortMode mode) { sortMode.set(mode); }
}
*///?}