package com.knoxhack.signalos.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.signalos.api.TerminalActionRegistry;
import com.knoxhack.signalos.client.SignalOsClientState;
import com.knoxhack.signalos.service.SignalOsRackActions;
import com.knoxhack.signalos.service.SignalOsTerminalServices;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, SignalOsTerminalStatePacket.TYPE, SignalOsTerminalStatePacket.CODEC,
                (packet, player, context) -> SignalOsClientState.apply(packet));
        EchoNetPayloads.serverboundAction(registrar, SignalOsOpenTerminalPacket.TYPE, SignalOsOpenTerminalPacket.CODEC,
                EchoRateLimitPolicy.of(10, "open_terminal"), ModNetwork::handleOpenTerminal);
        EchoNetPayloads.serverboundAction(registrar, SignalOsActionPacket.TYPE, SignalOsActionPacket.CODEC,
                EchoRateLimitPolicy.of(4, "terminal_action"), ModNetwork::handleTerminalAction);
        EchoNetPayloads.serverboundAction(registrar, SignalOsRackActionPacket.TYPE, SignalOsRackActionPacket.CODEC,
                EchoRateLimitPolicy.of(4, "rack_action"), ModNetwork::handleRackAction);
    }

    private static void handleOpenTerminal(SignalOsOpenTerminalPacket packet, ServerPlayer player,
            IPayloadContext context) {
        SignalOsTerminalServices.openRemoteTerminal(player);
    }

    private static void handleTerminalAction(SignalOsActionPacket packet, ServerPlayer player,
            IPayloadContext context) {
        if (!TerminalActionRegistry.handle(player, packet.pageId(), packet.actionId(), packet.payload())) {
            player.sendSystemMessage(Component.literal("[SignalOS] Unknown terminal action."), true);
        }
        SignalOsTerminalSync.send(player);
    }

    private static void handleRackAction(SignalOsRackActionPacket packet, ServerPlayer player,
            IPayloadContext context) {
        if (!SignalOsRackActions.handle(player, packet)) {
            player.sendSystemMessage(Component.literal("[SignalOS] Unknown rack action."), true);
        }
        SignalOsTerminalSync.send(player);
    }
}
