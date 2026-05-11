package com.knoxhack.echocore.api.network;

import net.minecraft.resources.Identifier;

public record EchoPacketDebugEvent(
        Identifier payloadId,
        EchoPacketDirection direction,
        EchoPacketKind kind,
        String playerName,
        boolean accepted,
        String detail) {
    public EchoPacketDebugEvent {
        playerName = playerName == null ? "" : playerName;
        detail = detail == null ? "" : detail;
    }
}
