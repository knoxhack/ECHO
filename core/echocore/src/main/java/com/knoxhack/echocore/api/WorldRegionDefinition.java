package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.resources.Identifier;

public record WorldRegionDefinition(
        Identifier id,
        WorldRegionType type,
        String displayName,
        String summary,
        List<Identifier> biomeIds,
        List<Identifier> biomeTags,
        List<Identifier> structureIds,
        List<Identifier> hazardIds,
        Identifier discoveryId,
        int radius,
        Identifier renderProfileId,
        Identifier audioProfileId,
        int sortOrder) {
    public WorldRegionDefinition {
        Objects.requireNonNull(id, "id");
        type = type == null ? WorldRegionType.ANOMALY_ZONE : type;
        displayName = clean(displayName, readable(id.getPath()));
        summary = clean(summary, "Shared world region.");
        biomeIds = cleanList(biomeIds);
        biomeTags = cleanList(biomeTags);
        structureIds = cleanList(structureIds);
        hazardIds = cleanList(hazardIds);
        discoveryId = discoveryId == null ? id : discoveryId;
        radius = Math.max(16, radius);
    }

    public boolean biomeBacked() {
        return !biomeIds.isEmpty() || !biomeTags.isEmpty();
    }

    public boolean matchesStructure(Identifier structureId) {
        return structureId != null && structureIds.contains(structureId);
    }

    private static List<Identifier> cleanList(List<Identifier> values) {
        return List.copyOf(values == null ? List.of() : values.stream().filter(Objects::nonNull).toList());
    }

    private static String clean(String value, String fallback) {
        String cleaned = value == null ? "" : value.strip();
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private static String readable(String value) {
        String[] parts = clean(value, "region").replace('/', '_').split("_+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.isEmpty() ? "Region" : builder.toString();
    }
}
