package com.knoxhack.echopowergrid.api;

import java.util.UUID;

public record PowerGridSnapshot(
    UUID networkId,
    long totalGeneration,
    long totalDemand,
    long totalStored,
    long totalCapacity,
    EchoGridState state,
    EchoPowerQuality quality,
    int nodeCount
) {
    public boolean isPowered() {
        return state != EchoGridState.OFFLINE && state != EchoGridState.TRIPPED && state != EchoGridState.EMERGENCY;
    }

    public long availablePower() {
        return Math.max(0, totalGeneration - totalDemand);
    }
}
