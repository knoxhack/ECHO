package com.knoxhack.echocore.api.network;

public final class NoOpNetworkService implements INetworkService {
    public static final NoOpNetworkService INSTANCE = new NoOpNetworkService();

    private NoOpNetworkService() {
    }

    @Override
    public INetworkBridge bridge() {
        return INetworkBridge.NOOP;
    }

    @Override
    public IPacketRegistrar packetRegistrar() {
        return IPacketRegistrar.NOOP;
    }

    @Override
    public PacketDebugHooks debugHooks() {
        return PacketDebugHooks.NOOP;
    }

    @Override
    public boolean active() {
        return false;
    }
}
