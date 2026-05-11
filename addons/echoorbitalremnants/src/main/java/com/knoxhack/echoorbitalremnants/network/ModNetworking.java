package com.knoxhack.echoorbitalremnants.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import com.knoxhack.echonetcore.api.EchoRateLimitPolicy;
import com.knoxhack.echoorbitalremnants.item.EchoTerminalItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, OpenEchoTerminalPayload.TYPE, OpenEchoTerminalPayload.STREAM_CODEC);
        registrar.playToClient(OrbitalEventVisualPayload.TYPE, OrbitalEventVisualPayload.STREAM_CODEC);
        EchoNetPayloads.serverboundAction(registrar, EchoTerminalActionPayload.TYPE, EchoTerminalActionPayload.STREAM_CODEC,
                EchoRateLimitPolicy.of(10, "orbital_terminal_action"), ModNetworking::handleTerminalAction);
    }

    private static void handleTerminalAction(EchoTerminalActionPayload payload, ServerPlayer player,
            net.neoforged.neoforge.network.handling.IPayloadContext context) {
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
