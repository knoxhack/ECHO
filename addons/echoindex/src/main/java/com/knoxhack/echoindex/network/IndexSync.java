package com.knoxhack.echoindex.network;

import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.api.EchoNetSend;
import com.knoxhack.echoindex.service.IndexDiscoveryStore;
import net.minecraft.server.level.ServerPlayer;

public final class IndexSync {
    private IndexSync() {
    }

    public static boolean send(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return EchoNetSend.toPlayer(player,
                new IndexStateSyncPacket(IndexDiscoveryStore.INSTANCE.syncTag(player)),
                EchoPacketKind.CLIENTBOUND_SYNC);
    }
}
