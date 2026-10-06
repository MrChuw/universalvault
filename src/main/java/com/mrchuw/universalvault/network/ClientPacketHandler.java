package com.mrchuw.universalvault.network;

import com.mrchuw.universalvault.Platform;
import com.mrchuw.universalvault.automation.network.C2SRequestCraftResultPayload;
import com.mrchuw.universalvault.automation.network.S2CUpdateCraftResultPayload;
import com.mrchuw.universalvault.gui.screen.LibraryScreen;
import com.mrchuw.universalvault.gui.screen.VaultScreen;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;

public class ClientPacketHandler {

    private ClientPacketHandler() {}

    public static void handleSync(S2CVaultSyncPayload payload) {
        //? if >=26.2 {
        var screen = Minecraft.getInstance().gui.screen();
        //?} else {
        /*var screen = Minecraft.getInstance().screen;
         *///?}
        if (screen instanceof VaultScreen vs) {
            vs.onVaultSync(payload);
        } else if (screen instanceof LibraryScreen ls) {
            ls.onVaultSync(payload);
        }
    }

    public static void handleRequestCraftResult(C2SRequestCraftResultPayload payload, ServerPlayer player) {
        ServerLevel level = player.level();
        CraftingInput input = CraftingInput.of(3, 3, payload.items());

        ItemStack result = ItemStack.EMPTY;
        if (!input.isEmpty()) {
            var opt = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level);
            if (opt.isPresent()) {
                //? if <26.1 {
                /*result = opt.get().value().assemble(input, level.registryAccess());
                 *///?} else {
                result = opt.get().value().assemble(input);
                //?}
            }
        }

        Platform.INSTANCE.sendToPlayer(player, new S2CUpdateCraftResultPayload(result));
    }
}