package com.knoxhack.echocore.api.mission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * Small in-memory registry useful for diagnostics, validation, and GameTest coverage.
 */
public final class InMemoryMissionRegistry implements IMissionRegistry {
    private final Map<Identifier, MissionChapterDefinition> chapters = new LinkedHashMap<>();
    private final Map<Identifier, MissionDefinition> missions = new LinkedHashMap<>();

    @Override
    public void registerChapter(String source, MissionChapterDefinition chapter) {
        if (chapter != null) {
            chapters.put(chapter.id(), chapter);
        }
    }

    @Override
    public void registerMission(String source, MissionDefinition mission) {
        if (mission != null) {
            missions.put(mission.id(), mission);
        }
    }

    @Override
    public Optional<MissionChapterDefinition> chapter(Identifier chapterId) {
        return Optional.ofNullable(chapters.get(chapterId));
    }

    @Override
    public Optional<MissionDefinition> missionDefinition(Identifier missionId) {
        return Optional.ofNullable(missions.get(missionId));
    }

    @Override
    public List<MissionChapterDefinition> chapters() {
        return new ArrayList<>(chapters.values());
    }

    @Override
    public List<MissionDefinition> missionDefinitions() {
        return new ArrayList<>(missions.values());
    }
}
