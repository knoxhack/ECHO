package com.knoxhack.echodatacore.network;

import com.knoxhack.echodatacore.DataCoreDataService;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String VERSION = "1";

    private ModNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION).optional();
        registrar.playToClient(DataCoreSyncPacket.TYPE, DataCoreSyncPacket.CODEC, ModNetwork::handleDataSync);
    }

    private static void handleDataSync(DataCoreSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> DataCoreDataService.INSTANCE.applyClientSync(packet));
    }
}