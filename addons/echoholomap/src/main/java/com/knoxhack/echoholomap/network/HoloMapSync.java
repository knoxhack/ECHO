package com.knoxhack.echoholomap.network;

import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echoholomap.EchoHoloMap;
import net.minecraft.server.level.ServerPlayer;

public final class HoloMapSync {
    private HoloMapSync() {
    }

    public static void send(ServerPlayer player) {
        if (player == null) {
            return;
        }
        try {
            EchoNetSend.toPlayer(player, HoloMapSnapshotPacket.from(player), EchoPacketKind.CLIENTBOUND_SYNC);
        } catch (RuntimeException exception) {
            EchoHoloMap.LOGGER.debug("Skipped optional HoloMap sync for {}: {}",
                    player.getName().getString(), exception.getMessage());
        }
    }
}
