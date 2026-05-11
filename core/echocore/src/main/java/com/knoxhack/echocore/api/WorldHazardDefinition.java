package com.knoxhack.echocore.api;

import java.util.Objects;
import net.minecraft.resources.Identifier;

public record WorldHazardDefinition(
        Identifier id,
        String displayName,
        String summary,
        int defaultSeverity,
        boolean ticking) {
    public WorldHazardDefinition {
        Objects.requireNonNull(id, "id");
        displayName = displayName == null || displayName.isBlank() ? id.toString() : displayName.strip();
        summary = summary == null ? "" : summary.strip();
        defaultSeverity = Math.max(0, Math.min(100, defaultSeverity));
    }
}
