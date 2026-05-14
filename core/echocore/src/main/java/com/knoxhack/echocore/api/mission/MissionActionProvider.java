package com.knoxhack.echocore.api.mission;

import java.util.List;
import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MissionActionProvider {
    MissionActionProvider NONE = (player, mission, status, completeNow) -> List.of();

    List<MissionActionView> actions(Player player, MissionDefinition mission, MissionStatus status, boolean completeNow);
}
