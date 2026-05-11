package com.knoxhack.echocore.api.mission;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class NoOpMissionService implements IMissionService {
    public static final NoOpMissionService INSTANCE = new NoOpMissionService();

    private NoOpMissionService() {
    }

    @Override
    public boolean available() {
        return false;
    }

    @Override
    public void registerChapter(String source, MissionChapterDefinition chapter) {
    }

    @Override
    public void registerMission(String source, MissionDefinition mission) {
    }

    @Override
    public Optional<MissionChapterDefinition> chapter(Identifier chapterId) {
        return Optional.empty();
    }

    @Override
    public Optional<MissionDefinition> missionDefinition(Identifier missionId) {
        return Optional.empty();
    }

    @Override
    public List<MissionChapterDefinition> chapters() {
        return List.of();
    }

    @Override
    public List<MissionDefinition> missionDefinitions() {
        return List.of();
    }

    @Override
    public List<IMissionProgressView> missions(Player player) {
        return List.of();
    }

    @Override
    public List<IMissionProgressView> missions(Player player, Identifier chapterId) {
        return List.of();
    }

    @Override
    public Optional<IMissionProgressView> mission(Player player, Identifier missionId) {
        return Optional.empty();
    }

    @Override
    public boolean startMission(ServerPlayer player, Identifier missionId) {
        return false;
    }

    @Override
    public boolean completeMission(ServerPlayer player, Identifier missionId) {
        return false;
    }

    @Override
    public boolean claimReward(ServerPlayer player, Identifier missionId) {
        return false;
    }

    @Override
    public boolean handleAction(ServerPlayer player, Identifier missionId, String actionId) {
        return false;
    }

    @Override
    public boolean recordObjective(
            ServerPlayer player,
            MissionObjectiveType type,
            Identifier target,
            int amount,
            Map<String, String> context) {
        return false;
    }

    @Override
    public String debugState(Player player, Identifier missionId) {
        return "MissionCore unavailable.";
    }
}
