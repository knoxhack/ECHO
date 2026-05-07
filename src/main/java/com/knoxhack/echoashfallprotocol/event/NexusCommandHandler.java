package com.knoxhack.echoashfallprotocol.event;

import com.knoxhack.echoashfallprotocol.EchoAshfallProtocol;
import com.knoxhack.echoashfallprotocol.endgame.NexusCampaignActions;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Handles player-visible Nexus status commands.
 */
@EventBusSubscriber(modid = EchoAshfallProtocol.MODID)
public class NexusCommandHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nexus")
                        .executes(ctx -> sendStatus(ctx.getSource()))
                        .then(Commands.literal("status")
                                .executes(ctx -> sendStatus(ctx.getSource())))
        );
    }

    private static int sendStatus(CommandSourceStack source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            return NexusCampaignActions.sendStatus(player) ? Command.SINGLE_SUCCESS : 0;
        }
        return 0;
    }
}
