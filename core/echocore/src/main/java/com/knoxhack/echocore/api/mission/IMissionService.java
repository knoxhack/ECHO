package com.knoxhack.echocore.api.mission;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public interface IMissionService extends IMissionRegistry {
    boolean available();

    List<IMissionProgressView> missions(Player player);

    List<IMissionProgressView> missions(Player player, Identifier chapterId);

    Optional<IMissionProgressView> mission(Player player, Identifier missionId);

    boolean startMission(ServerPlayer player, Identifier missionId);

    boolean completeMission(ServerPlayer player, Identifier missionId);

    boolean claimReward(ServerPlayer player, Identifier missionId);

    boolean handleAction(ServerPlayer player, Identifier missionId, String actionId);

    boolean recordObjective(
            ServerPlayer player,
            MissionObjectiveType type,
            Identifier target,
            int amount,
            Map<String, String> context);

    default void registerHookCoverage(String source, Identifier missionId, Identifier objectiveTarget) {
    }

    default Map<String, String> missionHookCoverageBySource() {
        return Map.of();
    }

    String debugState(Player player, Identifier missionId);
}
