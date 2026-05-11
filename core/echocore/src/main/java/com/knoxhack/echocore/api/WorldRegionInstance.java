package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record WorldRegionInstance(
        Identifier id,
        Identifier definitionId,
        WorldRegionType type,
        String displayName,
        ResourceKey<Level> dimension,
        BlockPos center,
        int radius,
        List<Identifier> hazardIds,
        boolean discovered) {
    public WorldRegionInstance {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(definitionId, "definitionId");
        type = type == null ? WorldRegionType.ANOMALY_ZONE : type;
        displayName = displayName == null || displayName.isBlank() ? id.toString() : displayName.strip();
        dimension = dimension == null ? Level.OVERWORLD : dimension;
        center = center == null ? BlockPos.ZERO : center.immutable();
        radius = Math.max(1, radius);
        hazardIds = List.copyOf(hazardIds == null ? List.of() : hazardIds.stream().filter(Objects::nonNull).toList());
    }
}
