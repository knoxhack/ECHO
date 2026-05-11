package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface IRegionService {
    boolean registerRegionDefinition(WorldRegionDefinition definition);

    List<WorldRegionDefinition> regionDefinitions();

    Optional<WorldRegionDefinition> regionDefinition(Identifier id);

    List<WorldRegionInstance> nearbyRegions(Level level, BlockPos pos, int radius);

    List<WorldRegionInstance> activeRegions(Player player);
}
