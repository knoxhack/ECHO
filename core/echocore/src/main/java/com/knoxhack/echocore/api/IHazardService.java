package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public interface IHazardService {
    boolean registerHazardDefinition(WorldHazardDefinition definition);

    List<WorldHazardDefinition> hazardDefinitions();

    Optional<WorldHazardDefinition> hazardDefinition(Identifier id);

    WorldHazardSnapshot hazardSnapshot(Player player);
}
