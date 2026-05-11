package com.knoxhack.echocore.api.mission;

import net.minecraft.world.entity.player.Player;

@FunctionalInterface
public interface MissionCompletionRule {
    MissionCompletionRule NONE = (player, mission) -> false;

    boolean isComplete(Player player, MissionDefinition mission);
}
