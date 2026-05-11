package com.knoxhack.echocore.api.mission;

import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public interface IMissionRegistry {
    void registerChapter(String source, MissionChapterDefinition chapter);

    void registerMission(String source, MissionDefinition mission);

    Optional<MissionChapterDefinition> chapter(Identifier chapterId);

    Optional<MissionDefinition> missionDefinition(Identifier missionId);

    List<MissionChapterDefinition> chapters();

    List<MissionDefinition> missionDefinitions();
}
