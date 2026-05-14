package com.knoxhack.echonetcore.client;

import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.network.EchoNetDebug;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public final class EchoNetClientActions {
    private EchoNetClientActions() {
    }

    public static void sendServerboundAction(CustomPacketPayload payload) {
        trySendServerboundAction(payload);
    }

    public static boolean trySendServerboundAction(CustomPacketPayload payload) {
        if (payload == null) {
            return false;
        }
        try {
            ClientPacketDistributor.sendToServer(payload);
            EchoNetDebug.emit(payload.type().id(), EchoPacketDirection.SERVERBOUND,
                    EchoPacketKind.SERVERBOUND_ACTION, "", true, "sent");
            return true;
        } catch (UnsupportedOperationException | IllegalStateException exception) {
            EchoNetDebug.emit(payload.type().id(), EchoPacketDirection.SERVERBOUND,
                    EchoPacketKind.SERVERBOUND_ACTION, "", false, exception.getMessage());
            return false;
        }
    }
}
