package com.knoxhack.echocore.api.network;

@FunctionalInterface
public interface PacketDebugHook {
    void onPacket(EchoPacketDebugEvent event);
}
