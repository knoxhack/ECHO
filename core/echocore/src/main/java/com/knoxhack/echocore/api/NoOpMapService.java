package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Safe fallback when no map implementation is installed.
 */
public final class NoOpMapService implements IMapMarkerService {
    public static final NoOpMapService INSTANCE = new NoOpMapService();

    private NoOpMapService() {
    }

    @Override
    public boolean registerProvider(IMapDataProvider provider) {
        return false;
    }

    @Override
    public List<IMapLayer> layers(Player player) {
        return List.of();
    }

    @Override
    public List<IMapMarker> markers(Player player) {
        return List.of();
    }

    @Override
    public boolean refresh(ServerPlayer player, String reason) {
        return false;
    }
}
