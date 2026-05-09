package com.knoxhack.signalos.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class SignalOsTerminalSync {
    private SignalOsTerminalSync() {
    }

    public static void send(ServerPlayer player) {
        if (player != null) {
            PacketDistributor.sendToPlayer(player, SignalOsTerminalStatePacket.from(player));
        }
    }
}
