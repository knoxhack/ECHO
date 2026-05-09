package com.knoxhack.signalos.network;

import com.knoxhack.signalos.api.TerminalActionRegistry;
import com.knoxhack.signalos.client.SignalOsClientState;
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
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SignalOsTerminalStatePacket.TYPE, SignalOsTerminalStatePacket.CODEC,
                ModNetwork::handleTerminalState);
        registrar.playToServer(SignalOsOpenTerminalPacket.TYPE, SignalOsOpenTerminalPacket.CODEC,
                ModNetwork::handleOpenTerminal);
        registrar.playToServer(SignalOsActionPacket.TYPE, SignalOsActionPacket.CODEC,
                ModNetwork::handleTerminalAction);
    }

    private static void handleTerminalState(SignalOsTerminalStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> SignalOsClientState.apply(packet));
    }

    private static void handleOpenTerminal(SignalOsOpenTerminalPacket packet, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            SignalOsTerminalServices.openRemoteTerminal(player);
        }
    }

    private static void handleTerminalAction(SignalOsActionPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!TerminalActionRegistry.handle(player, packet.pageId(), packet.actionId(), packet.payload())) {
            player.sendSystemMessage(Component.literal("[SignalOS] Unknown terminal action."), true);
        }
        SignalOsTerminalSync.send(player);
    }
}
