package com.knoxhack.echopowergrid.api;

import com.knoxhack.echopowergrid.grid.PowerNetworkManager;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class EchoPowerGridApi {
    private EchoPowerGridApi() {}

    public static Optional<EchoPowerNetwork> getNetwork(Level level, BlockPos pos) {
        if (level == null || pos == null) return Optional.empty();
        return Optional.ofNullable(PowerNetworkManager.get(level).getNetworkAt(pos));
    }

    public static Optional<EchoEnergyStorage> getEnergyStorage(Level level, BlockPos pos) {
        if (level == null || pos == null) return Optional.empty();
        return PowerNetworkManager.get(level).getEnergyStorageAt(pos);
    }

    public static boolean isPowered(Level level, BlockPos pos) {
        return getNetwork(level, pos).map(n -> n.state != EchoGridState.OFFLINE && n.state != EchoGridState.TRIPPED).orElse(false);
    }

    public static long getAvailablePower(Level level, BlockPos pos) {
        return getNetwork(level, pos).map(EchoPowerNetwork::toSnapshot).map(PowerGridSnapshot::availablePower).orElse(0L);
    }

    public static boolean requestPower(Level level, BlockPos pos, long epPerTick) {
        if (level == null || pos == null) return false;
        return PowerNetworkManager.get(level).requestPower(pos, epPerTick);
    }

    public static void markNetworkDirty(Level level, BlockPos pos) {
        if (level == null || pos == null) return;
        PowerNetworkManager.get(level).markDirty(pos);
    }

    public static PowerGridSnapshot getSnapshot(Level level, BlockPos pos) {
        return getNetwork(level, pos).map(EchoPowerNetwork::toSnapshot).orElse(
            new PowerGridSnapshot(new java.util.UUID(0, 0), 0, 0, 0, 0, EchoGridState.OFFLINE, EchoPowerQuality.STABLE, 0)
        );
    }
}
