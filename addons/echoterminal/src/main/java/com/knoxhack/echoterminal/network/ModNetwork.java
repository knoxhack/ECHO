package com.knoxhack.echoterminal.network;

import com.knoxhack.echoterminal.api.TerminalActionRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(TerminalActionPacket.TYPE, TerminalActionPacket.CODEC, ModNetwork::handleTerminalAction);
    }

    private static void handleTerminalAction(TerminalActionPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!TerminalActionRegistry.handle(player, packet.tabId(), packet.actionId(), packet.payload())) {
            player.sendSystemMessage(Component.literal("[ECHO-7] Unknown terminal action."), true);
        }
    }
}
