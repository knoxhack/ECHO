package com.knoxhack.echocore.api.network;

public interface INetworkService {
    INetworkBridge bridge();

    IPacketRegistrar packetRegistrar();

    PacketDebugHooks debugHooks();

    default boolean active() {
        return true;
    }
}
