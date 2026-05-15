package com.knoxhack.echopowergrid.network;

import com.knoxhack.echonetcore.api.EchoNetPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, PowerGridNetworkSummaryPacket.TYPE,
                PowerGridNetworkSummaryPacket.CODEC,
                (packet, player, context) -> handleClient("handle", packet));
    }

    private static void handleClient(String method, Object packet) {
        try {
            Class.forName("com.knoxhack.echopowergrid.client.PowerGridClientState")
                    .getMethod(method, packet.getClass())
                    .invoke(null, packet);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
