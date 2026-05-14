package com.knoxhack.echoindex.integration;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.mission.IMissionRegistry;
import com.knoxhack.echocore.api.mission.MissionChapterDefinition;
import com.knoxhack.echocore.api.mission.MissionDefinition;
import com.knoxhack.echocore.api.mission.MissionHookTargets;
import com.knoxhack.echocore.api.mission.MissionKind;
import com.knoxhack.echocore.api.mission.MissionObjectiveType;
import com.knoxhack.echocore.api.mission.MissionRewardClaimMode;
import com.knoxhack.echocore.api.mission.ObjectiveDefinition;
import com.knoxhack.echocore.api.mission.RewardDefinition;
import com.knoxhack.echoindex.EchoIndex;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class IndexMissionCoreIntegration {
    private static final Identifier CHAPTER = id("index");

    private IndexMissionCoreIntegration() {
    }

    public static void register() {
        EchoCoreServices.registerMissionContent(EchoIndex.MODID, IndexMissionCoreIntegration::registerContent);
        IndexMissionHooks.registerCoverage();
    }

    public static void registerContent(IMissionRegistry registry) {
        registry.registerChapter(EchoIndex.MODID, new MissionChapterDefinition(
                CHAPTER,
                "Index Side Ops",
                "Search, track, inspect, and follow shared recipe and source records.",
                76,
                0xFFAEEA6A));
        registerMission(registry, "open_search_entry", "open", MissionObjectiveType.UNLOCK_RESEARCH,
                "Open Search Entry", "Open or search the shared Index feed.",
                "Index search state is now synced.",
                new ItemStack(Items.BOOK), 0, "Open or search Index", new ItemStack(Items.PAPER, 4));
        registerMission(registry, "inspect_recipe_source", "recipe", MissionObjectiveType.UNLOCK_RESEARCH,
                "Inspect Recipe Source", "Pin, transfer, or inspect a tracked recipe card.",
                "The recipe source is now ready for operator use.",
                new ItemStack(Items.CRAFTING_TABLE), 1, "Inspect an Index recipe", new ItemStack(Items.BOOK, 1));
        registerMission(registry, "follow_source_note", "source", MissionObjectiveType.UNLOCK_RESEARCH,
                "Follow Source Note", "Follow an addon source note or read a linked Index entry.",
                "Addon source notes are now reachable from the archive.",
                new ItemStack(Items.WRITABLE_BOOK), 2, "Follow a source note", new ItemStack(Items.EXPERIENCE_BOTTLE, 1));
    }

    private static void registerMission(
            IMissionRegistry registry,
            String missionPath,
            String objectiveKey,
            MissionObjectiveType type,
            String title,
            String briefing,
            String fieldGuide,
            ItemStack icon,
            int order,
            String objectiveLabel,
            ItemStack reward) {
        Identifier mission = id(missionPath);
        Identifier target = MissionHookTargets.objectiveTarget(EchoIndex.MODID, mission, objectiveKey);
        registry.registerMission(EchoIndex.MODID, MissionDefinition.builder(mission, CHAPTER)
                .phase("index_side_ops", "Index Side Ops", 0, order)
                .text(title, briefing, fieldGuide)
                .category("Index", "Side Op")
                .icon(icon)
                .kind(MissionKind.SIDE_OP)
                .objective(new ObjectiveDefinition(
                        id(missionPath + "/" + objectiveKey),
                        type,
                        objectiveLabel,
                        "",
                        icon,
                        1,
                        false,
                        Map.of("target", target.toString())))
                .reward(RewardDefinition.item(id(missionPath + "/reward"), MissionRewardClaimMode.CLAIMABLE, reward))
                .build());
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(EchoIndex.MODID, path);
    }
}
