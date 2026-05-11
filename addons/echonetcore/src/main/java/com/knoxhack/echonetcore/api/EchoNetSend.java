package com.knoxhack.echonetcore.api;

import com.knoxhack.echocore.api.network.EchoPacketDirection;
import com.knoxhack.echocore.api.network.EchoPacketKind;
import com.knoxhack.echonetcore.network.EchoNetDebug;
import java.util.Collection;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class EchoNetSend {
    private EchoNetSend() {
    }

    public static boolean toPlayer(ServerPlayer player, CustomPacketPayload payload) {
        return toPlayer(player, payload, EchoPacketKind.CLIENTBOUND_SYNC);
    }

    public static boolean toPlayer(ServerPlayer player, CustomPacketPayload payload, EchoPacketKind kind) {
        if (player == null || payload == null) {
            return false;
        }
        try {
            PacketDistributor.sendToPlayer(player, payload);
            EchoNetDebug.emit(payload.type().id(), EchoPacketDirection.CLIENTBOUND,
                    kind == null ? EchoPacketKind.CLIENTBOUND_SYNC : kind,
                    player.getScoreboardName(), true, "sent");
            return true;
        } catch (UnsupportedOperationException | IllegalStateException exception) {
            EchoNetDebug.emit(payload.type().id(), EchoPacketDirection.CLIENTBOUND,
                    kind == null ? EchoPacketKind.CLIENTBOUND_SYNC : kind,
                    player.getScoreboardName(), false, exception.getMessage());
            return false;
        }
    }

    public static int toPlayers(Collection<ServerPlayer> players, CustomPacketPayload payload, EchoPacketKind kind) {
        if (players == null || payload == null) {
            return 0;
        }
        int sent = 0;
        for (ServerPlayer player : players) {
            if (toPlayer(player, payload, kind)) {
                sent++;
            }
        }
        return sent;
    }

    public static int toPlayers(Collection<ServerPlayer> players, CustomPacketPayload payload) {
        return toPlayers(players, payload, EchoPacketKind.CLIENTBOUND_SYNC);
    }
}
