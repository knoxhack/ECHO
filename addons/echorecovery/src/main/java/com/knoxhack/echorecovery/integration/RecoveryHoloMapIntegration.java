package com.knoxhack.echorecovery.integration;

import com.knoxhack.echoholomap.waypoint.HoloMapWaypoint;
import com.knoxhack.echoholomap.world.HoloMapWaypointSavedData;
import com.knoxhack.echorecovery.EchoRecovery;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public final class RecoveryHoloMapIntegration {
    private RecoveryHoloMapIntegration() {}

    public static void registerCommon() {
        EchoRecovery.LOGGER.info("HoloMap integration registered.");
    }

    public static void onGraveCreated(ServerPlayer player, BlockPos pos) {
        if (player == null || pos == null || player.level().getServer() == null) {
            return;
        }
        try {
            HoloMapWaypointSavedData data = HoloMapWaypointSavedData.get(player.level().getServer());
            String dim = player.level().dimension().identifier().toString();
            HoloMapWaypoint waypoint = HoloMapWaypoint.create(
                HoloMapWaypoint.Scope.PERSONAL,
                player.getUUID(),
                dim,
                pos.getX(), pos.getY(), pos.getZ(),
                "Grave of " + player.getScoreboardName(),
                0xFFFF6666,
                player.level().getServer().overworld().getGameTime()
            );
            data.upsert(player, waypoint, false);
        } catch (Exception e) {
            EchoRecovery.LOGGER.error("Failed to create HoloMap grave waypoint", e);
        }
    }
}
