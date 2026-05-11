package com.knoxhack.echocore.api.mission;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MissionCompletionHandler {
    MissionCompletionHandler NONE = (player, mission) -> {
    };

    void onCompleted(ServerPlayer player, MissionDefinition mission);
}
