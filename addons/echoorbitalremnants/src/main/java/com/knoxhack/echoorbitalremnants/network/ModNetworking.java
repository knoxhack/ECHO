package com.knoxhack.echoorbitalremnants.network;

import com.knoxhack.echoorbitalremnants.item.EchoTerminalItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(OpenEchoTerminalPayload.TYPE, OpenEchoTerminalPayload.STREAM_CODEC)
                .playToClient(OrbitalEventVisualPayload.TYPE, OrbitalEventVisualPayload.STREAM_CODEC)
                .playToServer(EchoTerminalActionPayload.TYPE, EchoTerminalActionPayload.STREAM_CODEC,
                        ModNetworking::handleTerminalAction);
    }

    private static void handleTerminalAction(EchoTerminalActionPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!EchoTerminalItem.hasTerminal(player)) {
            player.sendSystemMessage(Component.literal("ECHO-7 // Terminal link lost."));
            return;
        }
        if (payload.action() == EchoTerminalActionPayload.Action.SCAN) {
            EchoTerminalItem.performScan(player);
        }
        EchoTerminalItem.openTerminal(player);
    }
}
