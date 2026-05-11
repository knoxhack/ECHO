package com.knoxhack.echocore.api;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IWorldMarkerService {
    WorldMarker revealMarker(ServerPlayer player, WorldMarker marker);

    WorldMarker revealMarker(Level level, WorldMarker marker);

    List<WorldMarker> nearbyMarkers(Level level, BlockPos pos, int radius);

    List<WorldMarker> markers(Player player);

    List<String> validateMarkers(Level level);
}
