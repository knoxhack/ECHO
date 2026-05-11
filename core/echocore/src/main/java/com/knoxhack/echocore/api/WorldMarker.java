package com.knoxhack.echocore.api;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record WorldMarker(
        Identifier id,
        Identifier regionId,
        WorldMarkerType type,
        String displayName,
        String summary,
        ResourceKey<Level> dimension,
        BlockPos pos,
        int radius,
        boolean discovered,
        long updatedGameTime) {
    public WorldMarker {
        Objects.requireNonNull(id, "id");
        type = type == null ? WorldMarkerType.STRUCTURE : type;
        displayName = displayName == null || displayName.isBlank() ? id.toString() : displayName.strip();
        summary = summary == null ? "" : summary.strip();
        dimension = dimension == null ? Level.OVERWORLD : dimension;
        pos = pos == null ? BlockPos.ZERO : pos.immutable();
        radius = Math.max(1, radius);
    }

    public WorldMarker discovered(boolean value) {
        return new WorldMarker(id, regionId, type, displayName, summary, dimension, pos, radius, value, updatedGameTime);
    }
}
