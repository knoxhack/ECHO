package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
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
