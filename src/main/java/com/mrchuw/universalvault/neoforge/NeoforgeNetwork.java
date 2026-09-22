package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.network.ClientPacketHandler;
import com.mrchuw.universalvault.network.ServerHandlers;
import com.mrchuw.universalvault.network.payload.C2SRequestSyncPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultActionPayload;
import com.mrchuw.universalvault.network.payload.C2SVaultOpenPayload;
import com.mrchuw.universalvault.network.payload.S2CVaultSyncPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class NeoforgeNetwork {

    private NeoforgeNetwork() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(NeoforgeNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // ---- Client -> Server -------------------------------------------------

        registrar.playToServer(
                C2SVaultActionPayload.TYPE,
                C2SVaultActionPayload.STREAM_CODEC,
                NeoforgeNetwork::handleAction
        );

        registrar.playToServer(
                C2SVaultOpenPayload.TYPE,
                C2SVaultOpenPayload.STREAM_CODEC,
                NeoforgeNetwork::handleOpen
        );

        registrar.playToServer(
                C2SRequestSyncPayload.TYPE,
                C2SRequestSyncPayload.STREAM_CODEC,
                NeoforgeNetwork::handleRequestSync
        );

        // ---- Server -> Client -------------------------------------------------

        registrar.playToClient(
                S2CVaultSyncPayload.TYPE,
                S2CVaultSyncPayload.STREAM_CODEC,
                NeoforgeNetwork::handleSync
        );
    }

    // -------------------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------------------

    private static void handleAction(C2SVaultActionPayload payload, IPayloadContext ctx) {
        // enqueueWork: garante que roda na thread principal do servidor
        ctx.enqueueWork(() -> ServerHandlers.handleAction(ctx.player(), payload));
    }

    private static void handleOpen(C2SVaultOpenPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ServerHandlers.handleOpen(ctx.player(), payload));
    }

    private static void handleRequestSync(C2SRequestSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer sp)) return;
            if (sp.containerMenu instanceof VaultMenu menu) {
                menu.syncVaultData(sp);
            }
        });
    }

    private static void handleSync(S2CVaultSyncPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientPacketHandler.handleSync(payload));
    }
}
//?}