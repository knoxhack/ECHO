package com.knoxhack.echorelictech.integration.worldcore;

import com.knoxhack.echorelictech.EchoRelicTech;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class RelicTechWorldCoreIntegration {
    public static void register() {
        EchoRelicTech.LOGGER.info("ECHO WorldCore integration loaded for RelicTech.");
    }

    public static void emitVaultDiscovery(ServerPlayer player, Identifier vaultId, BlockPos pos) {
        try {
            Class<?> serviceClass = Class.forName("com.knoxhack.echoworldcore.service.WorldRegionService");
            Object instance = serviceClass.getField("INSTANCE").get(null);
            java.lang.reflect.Method recordScan = serviceClass.getMethod("recordStructureScan", ServerPlayer.class, Identifier.class, BlockPos.class, String.class, String.class);
            recordScan.invoke(instance, player, vaultId, pos, "Relic Vault", "Pre-Gridfall research vault discovered.");
        } catch (Exception | LinkageError ignored) {}
    }

    public static void emitRelicFailureHazard(ServerPlayer player, BlockPos pos) {
        try {
            Class<?> busClass = Class.forName("com.knoxhack.echocore.api.EchoWorldRuntimeBus");
            Class<?> hazardClass = Class.forName("com.knoxhack.echocore.api.EchoWorldRuntimeBus$HazardChanged");
            // Fire generic hazard changed event if available
        } catch (Exception | LinkageError ignored) {}
    }
}
