package com.knoxhack.echolens.integration.runtimeguard;

import com.knoxhack.echoruntimeguard.api.LensScanType;
import com.knoxhack.echoruntimeguard.api.RuntimeGuardServices;
import net.minecraft.server.level.ServerPlayer;

public final class LensRuntimeGuardBridge {
    private LensRuntimeGuardBridge() {
    }

    public static boolean canRunDeepScan(ServerPlayer player) {
        return RuntimeGuardServices.integrations().canRunLensScan(player, LensScanType.DEEP);
    }

    public static int deepScanBudget(ServerPlayer player, int fallback) {
        return Math.max(1, Math.min(Math.max(1, fallback),
                RuntimeGuardServices.integrations().getDeepScanBudgetPerTick(player)));
    }

    public static void recordDeepScan(ServerPlayer player, int blocksScanned, int entitiesScanned) {
        RuntimeGuardServices.integrations().recordLensScan(player, LensScanType.DEEP, blocksScanned, entitiesScanned);
    }
}
