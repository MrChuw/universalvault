package com.mrchuw.universalvault.neoforge;

//? neoforge {
import com.mrchuw.universalvault.automation.network.*;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.gui.screen.PatternEncodeScreen;
import com.mrchuw.universalvault.network.ClientPacketHandler;
import com.mrchuw.universalvault.network.ServerHandlers;
import com.mrchuw.universalvault.network.payload.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;import static com.mrchuw.universalvault.network.ClientPacketHandler.handleRequestCraftResult;

public final class NeoforgeNetwork {

    private NeoforgeNetwork() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(NeoforgeNetwork::onRegisterPayloads);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");


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


        registrar.playToClient(
                S2CVaultSyncPayload.TYPE,
                S2CVaultSyncPayload.STREAM_CODEC,
                NeoforgeNetwork::handleSync
        );


        registrar.playToServer(
                C2SEncodePatternPayload.TYPE,
                C2SEncodePatternPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleEncodePattern(
                                (ServerPlayer) ctx.player(), payload)));

        registrar.playToServer(
                C2SPatternUpdatePayload.TYPE,
                C2SPatternUpdatePayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handlePatternUpdate(
                                (ServerPlayer) ctx.player(), payload)));

        registrar.playToServer(
                C2SPatternActionPayload.TYPE,
                C2SPatternActionPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handlePatternAction(
                                (ServerPlayer) ctx.player(), payload)));

        registrar.playToServer(
                C2SRequestPatternsSyncPayload.TYPE,
                C2SRequestPatternsSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handlePatternsRequest(
                                (ServerPlayer) ctx.player(), payload)));

        registrar.playToServer(
                C2SRequestJobsSyncPayload.TYPE,
                C2SRequestJobsSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleJobsRequest(
                                (ServerPlayer) ctx.player(), payload)));

        registrar.playToServer(
                C2SCancelJobPayload.TYPE,
                C2SCancelJobPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleCancelJob(
                                (ServerPlayer) ctx.player(), payload)));


        registrar.playToServer(
                C2SNodeConfigPayload.TYPE,
                C2SNodeConfigPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleNodeConfig(
                                (ServerPlayer) ctx.player(), payload)));


        registrar.playToClient(
                S2CPatternsSyncPayload.TYPE,
                S2CPatternsSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handlePatternsSync(payload)));

        registrar.playToClient(
                S2CJobsSyncPayload.TYPE,
                S2CJobsSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handleJobsSync(payload)));

        registrar.playToClient(
                S2CNodeConfigSyncPayload.TYPE,
                S2CNodeConfigSyncPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handleNodeConfigSync(payload)));

        registrar.playToServer(
                C2SImportPatternPayload.TYPE,
                C2SImportPatternPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleImportPattern(
                                (ServerPlayer) ctx.player(), payload)));


        registrar.playToServer(
                C2SRequestCraftResultPayload.TYPE,
                C2SRequestCraftResultPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                            // Chame o método que calcula e devolve a resposta
                            handleRequestCraftResult(payload, player);
                        }
                    });
                }
        );

        registrar.playToClient(
                S2CUpdateCraftResultPayload.TYPE,
                S2CUpdateCraftResultPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        PatternEncodeScreen.handleServerResult(payload.result());
                    });
                }
        );

        registrar.playToServer(
                C2SQuickCraftPayload.TYPE,
                C2SQuickCraftPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(
                        () -> ServerAutomationHandlers.handleQuickCraft(
                                (ServerPlayer) ctx.player(), payload)));
    }

    private static void handleAction(C2SVaultActionPayload payload, IPayloadContext ctx) {
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
