package com.mrchuw.universalvault.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mrchuw.universalvault.UniversalVault;
import com.mrchuw.universalvault.gui.VaultMenuHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = UniversalVault.MOD_ID)
public class VaultCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("vault")
                .executes(VaultCommand::openGlobalVault)
                .then(Commands.literal("global").executes(VaultCommand::openGlobalVault))
                .then(Commands.literal("personal").executes(VaultCommand::openPersonalVault))
                .then(
                    Commands.literal("player")
                        .requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                    .executes(VaultCommand::openPlayerVault)
                        )
                )
        );
    }

    private static int openGlobalVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VaultMenuHelper.openVault(
                player,
                UniversalVault.GLOBAL_VAULT_UUID,
                Component.translatable("gui.universal_vault.global_title")
        );
        return 1;
    }

    private static int openPersonalVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        VaultMenuHelper.openVault(
                player,
                player.getUUID(),
                Component.translatable("gui.universal_vault.personal_title")
        );
        return 1;
    }

    private static int openPlayerVault(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer caller = ctx.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        VaultMenuHelper.openVault(
                caller,
                target.getUUID(),
                Component.translatable("gui.universal_vault.player_vault_title", target.getScoreboardName())
        );
        return 1;
    }
}
