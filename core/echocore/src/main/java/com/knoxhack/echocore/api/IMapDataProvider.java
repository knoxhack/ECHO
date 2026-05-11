package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Optional provider surface for addons that want to contribute HoloMap layers or markers.
 */
public interface IMapDataProvider {
    Identifier providerId();

    default List<IMapLayer> layers(Player player) {
        return List.of();
    }

    default List<IMapMarker> markers(Player player) {
        return List.of();
    }

    default boolean refresh(ServerPlayer player, String reason) {
        return false;
    }
}
