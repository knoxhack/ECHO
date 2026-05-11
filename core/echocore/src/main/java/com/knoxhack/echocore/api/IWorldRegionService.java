package com.knoxhack.echocore.api;

import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IWorldRegionService extends IRegionService, IHazardService, IWorldMarkerService, IStructureDiscoveryService {
    boolean discoverRegion(ServerPlayer player, Identifier regionId, WorldDiscoverySource source);

    Optional<WorldRegionInstance> currentRegion(Player player);

    void tickPlayer(ServerPlayer player);
}
