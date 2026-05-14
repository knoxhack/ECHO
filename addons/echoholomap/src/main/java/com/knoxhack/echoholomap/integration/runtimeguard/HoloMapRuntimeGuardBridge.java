package com.knoxhack.echoholomap.integration.runtimeguard;

import com.knoxhack.echoholomap.EchoHoloMap;
import com.knoxhack.echoruntimeguard.api.NetworkPriority;
import com.knoxhack.echoruntimeguard.api.RuntimeGuardServices;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class HoloMapRuntimeGuardBridge {
    private HoloMapRuntimeGuardBridge() {
    }

    public static boolean shouldRefreshMarkers(ServerPlayer player, String reason) {
        if (isPlayerRequested(reason)) {
            return true;
        }
        return RuntimeGuardServices.integrations().shouldRefreshHoloMapMarkers(
                player == null ? null : player.blockPosition());
    }

    public static int refreshIntervalTicks(int fallback) {
        return Math.max(Math.max(1, fallback),
                RuntimeGuardServices.integrations().getHoloMapRefreshIntervalTicks());
    }

    public static void recordSync(String channelPath, int estimatedBytes, String priorityName) {
        NetworkPriority priority = priority(priorityName);
        RuntimeGuardServices.network().recordSend(
                Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, channelPath),
                estimatedBytes,
                priority);
    }

    private static boolean isPlayerRequested(String reason) {
        if (reason == null || reason.isBlank()) {
            return false;
        }
        String normalized = reason.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("manual")
                || normalized.contains("command")
                || normalized.contains("button")
                || normalized.contains("player")
                || normalized.contains("test");
    }

    private static NetworkPriority priority(String name) {
        if (name == null || name.isBlank()) {
            return NetworkPriority.BACKGROUND_SYNC;
        }
        try {
            return NetworkPriority.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return NetworkPriority.BACKGROUND_SYNC;
        }
    }
}
