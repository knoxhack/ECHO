package com.knoxhack.echocore.api;

import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Immutable default implementation of {@link IMapMarker}.
 */
public record EchoMapMarker(
        Identifier id,
        Identifier layerId,
        Identifier sourceId,
        MarkerKind kind,
        MarkerState state,
        String title,
        String summary,
        ResourceKey<Level> dimension,
        double x,
        double y,
        double z,
        float radius,
        Identifier icon,
        Identifier routeId,
        int routeOrder,
        boolean precise) implements IMapMarker {
    public EchoMapMarker {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(layerId, "layerId");
        sourceId = sourceId == null ? id : sourceId;
        kind = kind == null ? MarkerKind.GENERIC : kind;
        state = state == null ? MarkerState.DISCOVERED : state;
        title = title == null || title.isBlank() ? id.getPath() : title.strip();
        summary = summary == null ? "" : summary.strip();
        dimension = dimension == null ? Level.OVERWORLD : dimension;
        radius = Math.max(0.0F, radius);
    }
}
