package com.mrchuw.universalvault.fabric;

//? fabric {
/*import com.mrchuw.universalvault.automation.network.*;
import com.mrchuw.universalvault.gui.menu.VaultMenu;
import com.mrchuw.universalvault.gui.screen.PatternEncodeScreen;
import com.mrchuw.universalvault.network.ClientPacketHandler;
import com.mrchuw.universalvault.network.ServerHandlers;
import com.mrchuw.universalvault.network.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;

import net.minecraft.server.level.ServerPlayer;


import static com.mrchuw.universalvault.network.ClientPacketHandler.handleRequestCraftResult;

public final class FabricNetwork {

    private FabricNetwork() {}

    public static void register() {
        //? if <26.1 {
        /^PayloadTypeRegistry<RegistryFriendlyByteBuf> c2s = PayloadTypeRegistry.playC2S();
        PayloadTypeRegistry<RegistryFriendlyByteBuf> s2c = PayloadTypeRegistry.playS2C();
        ^///?} else {
        PayloadTypeRegistry<RegistryFriendlyByteBuf> c2s = PayloadTypeRegistry.serverboundPlay();
        PayloadTypeRegistry<RegistryFriendlyByteBuf> s2c = PayloadTypeRegistry.clientboundPlay();
        //?}
        c2s.register(C2SVaultActionPayload.TYPE, C2SVaultActionPayload.STREAM_CODEC);
        c2s.register(C2SVaultOpenPayload.TYPE, C2SVaultOpenPayload.STREAM_CODEC);
        c2s.register(C2SRequestSyncPayload.TYPE, C2SRequestSyncPayload.STREAM_CODEC);
        c2s.register(C2SEncodePatternPayload.TYPE, C2SEncodePatternPayload.STREAM_CODEC);
        c2s.register(C2SPatternUpdatePayload.TYPE, C2SPatternUpdatePayload.STREAM_CODEC);
        c2s.register(C2SPatternActionPayload.TYPE, C2SPatternActionPayload.STREAM_CODEC);
        c2s.register(C2SRequestPatternsSyncPayload.TYPE, C2SRequestPatternsSyncPayload.STREAM_CODEC);
        c2s.register(C2SRequestJobsSyncPayload.TYPE, C2SRequestJobsSyncPayload.STREAM_CODEC);
        c2s.register(C2SCancelJobPayload.TYPE, C2SCancelJobPayload.STREAM_CODEC);
        c2s.register(C2SNodeConfigPayload.TYPE, C2SNodeConfigPayload.STREAM_CODEC);
        c2s.register(C2SImportPatternPayload.TYPE, C2SImportPatternPayload.STREAM_CODEC);
        c2s.register(C2SRequestCraftResultPayload.TYPE, C2SRequestCraftResultPayload.STREAM_CODEC);
        c2s.register(C2SQuickCraftPayload.TYPE, C2SQuickCraftPayload.STREAM_CODEC);

        s2c.register(S2CVaultSyncPayload.TYPE, S2CVaultSyncPayload.STREAM_CODEC);
        s2c.register(S2CPatternsSyncPayload.TYPE, S2CPatternsSyncPayload.STREAM_CODEC);
        s2c.register(S2CJobsSyncPayload.TYPE, S2CJobsSyncPayload.STREAM_CODEC);
        s2c.register(S2CNodeConfigSyncPayload.TYPE, S2CNodeConfigSyncPayload.STREAM_CODEC);
        s2c.register(S2CUpdateCraftResultPayload.TYPE, S2CUpdateCraftResultPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                C2SVaultActionPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerHandlers.handleAction(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SVaultOpenPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerHandlers.handleOpen(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestSyncPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() -> {
                    ServerPlayer sp = ctx.player();
                    if (sp.containerMenu instanceof VaultMenu menu) {
                        menu.syncVaultData(sp);
                    }
                }));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SEncodePatternPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleEncodePattern(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SPatternUpdatePayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handlePatternUpdate(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SPatternActionPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handlePatternAction(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestPatternsSyncPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handlePatternsRequest(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestJobsSyncPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleJobsRequest(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SCancelJobPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleCancelJob(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SNodeConfigPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleNodeConfig(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SImportPatternPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleImportPattern(ctx.player(), payload)));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SRequestCraftResultPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        handleRequestCraftResult(payload, ctx.player())));

        ServerPlayNetworking.registerGlobalReceiver(
                C2SQuickCraftPayload.TYPE,
                (payload, ctx) -> ctx.server().execute(() ->
                        ServerAutomationHandlers.handleQuickCraft(ctx.player(), payload)));
    }

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(
                S2CVaultSyncPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        ClientPacketHandler.handleSync(payload)));

        ClientPlayNetworking.registerGlobalReceiver(
                S2CPatternsSyncPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handlePatternsSync(payload)));

        ClientPlayNetworking.registerGlobalReceiver(
                S2CJobsSyncPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handleJobsSync(payload)));

        ClientPlayNetworking.registerGlobalReceiver(
                S2CNodeConfigSyncPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        com.mrchuw.universalvault.automation.network.ClientPacketHandler
                                .handleNodeConfigSync(payload)));

        ClientPlayNetworking.registerGlobalReceiver(
                S2CUpdateCraftResultPayload.TYPE,
                (payload, ctx) -> ctx.client().execute(() ->
                        PatternEncodeScreen.handleServerResult(payload.result())));
    }
}
*///?}
