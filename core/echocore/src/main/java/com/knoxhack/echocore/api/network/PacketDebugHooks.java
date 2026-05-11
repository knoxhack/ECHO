package com.knoxhack.echocore.api.network;

public interface PacketDebugHooks {
    PacketDebugHooks NOOP = new PacketDebugHooks() {
        @Override
        public void add(PacketDebugHook hook) {
        }

        @Override
        public void remove(PacketDebugHook hook) {
        }

        @Override
        public void emit(EchoPacketDebugEvent event) {
        }
    };

    void add(PacketDebugHook hook);

    void remove(PacketDebugHook hook);

    void emit(EchoPacketDebugEvent event);
}
