package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.ClientPacketHandler;
import com.mrchuw.universalvault.network.ServerHandlers;
import com.mrchuw.universalvault.network.payload.C2SRequestSyncPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class FabricNetwork {

    private FabricNetwork() {}

    public static void register() {
        //? if <26.1 {
        /^PayloadTypeRegistry.playC2S().register(
                C2SVaultActionPayload.TYPE,
                C2SVaultActionPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                C2SVaultOpenPayload.TYPE,
                C2SVaultOpenPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.playC2S().register(
                C2SRequestSyncPayload.TYPE,
                C2SRequestSyncPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.playS2C().register(
                S2CVaultSyncPayload.TYPE,
                S2CVaultSyncPayload.STREAM_CODEC
        );
        ^///?} else {
        PayloadTypeRegistry.serverboundPlay().register(
                C2SVaultActionPayload.TYPE,
                C2SVaultActionPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                C2SVaultOpenPayload.TYPE,
                C2SVaultOpenPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.serverboundPlay().register(
                C2SRequestSyncPayload.TYPE,
                C2SRequestSyncPayload.STREAM_CODEC
        );
        PayloadTypeRegistry.clientboundPlay().register(
                S2CVaultSyncPayload.TYPE,
                S2CVaultSyncPayload.STREAM_CODEC
        );
        //?}

        // ---- Receivers server-side -------------------------------------------

        ServerPlayNetworking.registerGlobalReceiver(
                C2SVaultActionPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerHandlers.handleAction(ctx.player(), payload)
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                C2SVaultOpenPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerHandlers.handleOpen(ctx.player(), payload)
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestSyncPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() -> {
                    ServerPlayer sp = ctx.player();
                    if (sp.containerMenu instanceof VaultMenu menu) {
                        menu.syncVaultData(sp);
                    }
                })
        );
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                S2CVaultSyncPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        ClientPacketHandler.handleSync(payload)
                )
        );
    }
}
*///?}