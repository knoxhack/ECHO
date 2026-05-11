package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record WorldHazardSnapshot(
        List<Identifier> regionIds,
        List<Identifier> hazardIds,
        int severity,
        boolean safeZone,
        String summary) {
    public WorldHazardSnapshot {
        regionIds = List.copyOf(regionIds == null ? List.of() : regionIds.stream().filter(Objects::nonNull).toList());
        hazardIds = List.copyOf(hazardIds == null ? List.of() : hazardIds.stream().filter(Objects::nonNull).toList());
        severity = Math.max(0, Math.min(100, severity));
        safeZone = safeZone || hazardIds.isEmpty() || severity <= 0;
        summary = summary == null || summary.isBlank()
                ? (safeZone ? "No active shared world hazard." : "Shared world hazard active.")
                : summary.strip();
    }

    public static WorldHazardSnapshot nominal() {
        return new WorldHazardSnapshot(List.of(), List.of(), 0, true, "No active shared world hazard.");
    }
}
