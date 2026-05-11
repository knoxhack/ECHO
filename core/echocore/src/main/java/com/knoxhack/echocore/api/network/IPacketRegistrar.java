package com.knoxhack.echocore.api.network;

public interface IPacketRegistrar {
    IPacketRegistrar NOOP = new IPacketRegistrar() {
        @Override
        public String protocolVersion() {
            return "";
        }

        @Override
        public boolean optionalPackets() {
            return true;
        }
    };

    String protocolVersion();

    boolean optionalPackets();
}
