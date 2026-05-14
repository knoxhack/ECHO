package com.knoxhack.echoindex.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echoindex.EchoIndex;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class IndexMissionHooks {
    private IndexMissionHooks() {
    }

    public static void registerCoverage() {
        register("open_search_entry", "open");
        register("inspect_recipe_source", "recipe");
        register("follow_source_note", "source");
    }

    public static void recordOpenSearch(ServerPlayer player, Identifier entryId) {
        record(player, "open_search_entry", "open", MissionObjectiveType.UNLOCK_RESEARCH, "entry", detail(entryId, "sync"));
    }

    public static void recordRecipeInspect(ServerPlayer player, Identifier recipeId) {
        record(player, "inspect_recipe_source", "recipe", MissionObjectiveType.UNLOCK_RESEARCH, "recipe", detail(recipeId, "recipe"));
    }

    public static void recordSourceNote(ServerPlayer player, Identifier sourceId) {
        record(player, "follow_source_note", "source", MissionObjectiveType.UNLOCK_RESEARCH, "source", detail(sourceId, "source"));
    }

    private static void register(String missionPath, String objectiveKey) {
        Identifier mission = mission(missionPath);
        EchoCoreServices.registerMissionHookCoverage(
                EchoIndex.MODID,
                mission,
                MissionHookTargets.objectiveTarget(EchoIndex.MODID, mission, objectiveKey));
    }

    private static void record(
            ServerPlayer player,
            String missionPath,
            String objectiveKey,
            MissionObjectiveType type,
            String detailKey,
            String detail) {
        if (player == null) {
            return;
        }
        Identifier mission = mission(missionPath);
        EchoCoreServices.recordMissionObjective(
                player,
                type,
                MissionHookTargets.objectiveTarget(EchoIndex.MODID, mission, objectiveKey),
                1,
                MissionHookTargets.context(EchoIndex.MODID, mission, detailKey, detail));
    }

    private static String detail(Identifier id, String fallback) {
        return id == null ? fallback : id.toString();
    }

    private static Identifier mission(String path) {
        return Identifier.fromNamespaceAndPath(EchoIndex.MODID, path);
    }
}
