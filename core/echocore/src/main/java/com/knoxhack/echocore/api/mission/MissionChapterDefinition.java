package com.knoxhack.echocore.api.mission;

import net.minecraft.resources.Identifier;

public record MissionChapterDefinition(
        Identifier id,
        String title,
        String summary,
        int order,
        int accentColor) {
    public MissionChapterDefinition {
        if (id == null) {
            throw new IllegalArgumentException("Mission chapter id cannot be null.");
        }
        title = title == null || title.isBlank() ? id.getPath() : title;
        summary = summary == null ? "" : summary;
        accentColor = accentColor == 0 ? 0x55FFDD : accentColor;
    }
}
