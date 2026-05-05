package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.endgame.NexusCampaignActions;
import com.knoxhack.echoashfallprotocol.endgame.NexusChoiceService;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Handles /nexus [restore|destroy|control] commands for the Nexus Core endgame.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class NexusCommandHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("nexus")
                        .then(Commands.literal("status")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.sendStatus(player) ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.literal("awaken")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.awakenCore(player) ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.literal("scan")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.scanPrimeRelays(player) ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.literal("encounter")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.handleTerminalAction(player, "encounter")
                                                ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                })
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(ctx -> {
                                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                                String type = StringArgumentType.getString(ctx, "type");
                                                return NexusCampaignActions.startRelayEncounter(player, type)
                                                        ? Command.SINGLE_SUCCESS : 0;
                                            }
                                            return 0;
                                        })))
                        .then(Commands.literal("relay")
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .executes(ctx -> {
                                            if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                                String action = StringArgumentType.getString(ctx, "action");
                                                return NexusCampaignActions.resolveNextRelay(player, action)
                                                        ? Command.SINGLE_SUCCESS : 0;
                                            }
                                            return 0;
                                        }))
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .then(Commands.argument("action", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                                        String type = StringArgumentType.getString(ctx, "type");
                                                        String action = StringArgumentType.getString(ctx, "action");
                                                        return NexusCampaignActions.resolveRelay(player, type, action)
                                                                ? Command.SINGLE_SUCCESS : 0;
                                                    }
                                                    return 0;
                                                }))))
                        .then(Commands.literal("siege")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.surviveCoreCountermeasure(player)
                                                ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.literal("operation")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.completePathOperation(player)
                                                ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.literal("finale")
                                .executes(ctx -> {
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return NexusCampaignActions.completeFinale(player) ? Command.SINGLE_SUCCESS : 0;
                                    }
                                    return 0;
                                }))
                        .then(Commands.argument("choice", StringArgumentType.word())
                                .executes(ctx -> {
                                    String choice = StringArgumentType.getString(ctx, "choice");
                                    if (ctx.getSource().getEntity() instanceof ServerPlayer player) {
                                        return handleNexusChoice(player, choice);
                                    }
                                    return 0;
                                }))
        );
    }

    private static int handleNexusChoice(ServerPlayer player, String choiceStr) {
        return NexusChoiceService.applyChoice(player, choiceStr) ? Command.SINGLE_SUCCESS : 0;
    }
}
