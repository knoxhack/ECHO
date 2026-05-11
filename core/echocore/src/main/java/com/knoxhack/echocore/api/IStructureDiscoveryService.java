package com.knoxhack.echocore.api;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IStructureDiscoveryService {
    boolean recordStructureScan(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary);

    boolean recordStructureEntry(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary);

    boolean hasDiscoveredRegion(Player player, Identifier regionId);

    Set<Identifier> discoveredRegions(Player player);
}
