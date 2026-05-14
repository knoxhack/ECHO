package com.knoxhack.echocore.api.mission;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MissionActionHandler {
    MissionActionHandler NONE = (player, mission, actionId) -> false;

    boolean handle(ServerPlayer player, MissionDefinition mission, String actionId);
}
