package com.knoxhack.echonetcore.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class EchoNetClientActions {
    private EchoNetClientActions() {
    }

    public static void sendServerboundAction(CustomPacketPayload payload) {
        if (payload != null) {
            ClientPacketDistributor.sendToServer(payload);
        }
    }
}
