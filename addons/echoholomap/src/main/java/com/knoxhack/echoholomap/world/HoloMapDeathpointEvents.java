package com.knoxhack.echoholomap.world;

import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.network.HoloMapSync;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class HoloMapDeathpointEvents {
    private HoloMapDeathpointEvents() {
    }

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!deathpointsEnabled() || !(event.getEntity() instanceof ServerPlayer player)
                || player.level().getServer() == null) {
            return;
        }
        HoloMapWaypointSavedData.get(player.level().getServer())
                .recordDeathpoint(player, maxDeathpointsPerPlayer());
        HoloMapSync.send(player);
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HoloMapSync.send(player);
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            HoloMapSync.send(player);
        }
    }

    public static boolean deathpointsEnabled() {
        try {
            return Config.DEATHPOINTS_ENABLED.get();
        } catch (RuntimeException exception) {
            return true;
        }
    }

    public static int maxDeathpointsPerPlayer() {
        try {
            return Math.max(0, Math.min(128, Config.DEATHPOINTS_MAX_PER_PLAYER.get()));
        } catch (RuntimeException exception) {
            return 10;
        }
    }
}
