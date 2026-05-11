package com.knoxhack.echodatacore.network;

import com.knoxhack.echodatacore.DataCoreDataService;
import com.knoxhack.echonetcore.api.EchoNetPayloads;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = EchoNetPayloads.optional(event);
        EchoNetPayloads.clientboundSync(registrar, DataCoreSyncPacket.TYPE, DataCoreSyncPacket.CODEC,
                (packet, player, context) -> DataCoreDataService.INSTANCE.applyClientSync(packet));
    }
}
