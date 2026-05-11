package com.knoxhack.echoholomap.network;

public final class HoloMapClientState {
    private static volatile HoloMapSnapshotPacket snapshot = HoloMapSnapshotPacket.empty();

    private HoloMapClientState() {
    }

    public static void apply(HoloMapSnapshotPacket packet) {
        snapshot = packet == null ? HoloMapSnapshotPacket.empty() : packet;
    }

    public static HoloMapSnapshotPacket snapshot() {
        return snapshot;
    }
}
