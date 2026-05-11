package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Runtime service used by map UIs to aggregate marker providers safely.
 */
public interface IMapMarkerService {
    boolean registerProvider(IMapDataProvider provider);

    List<IMapLayer> layers(Player player);

    List<IMapMarker> markers(Player player);

    default boolean refresh(ServerPlayer player, String reason) {
        return false;
    }

    default int providerCount() {
        return 0;
    }
}
