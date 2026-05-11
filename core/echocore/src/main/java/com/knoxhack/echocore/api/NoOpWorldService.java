package com.knoxhack.echocore.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class NoOpWorldService implements IWorldRegionService {
    public static final NoOpWorldService INSTANCE = new NoOpWorldService();

    private NoOpWorldService() {
    }

    @Override
    public boolean registerRegionDefinition(WorldRegionDefinition definition) {
        return false;
    }

    @Override
    public List<WorldRegionDefinition> regionDefinitions() {
        return List.of();
    }

    @Override
    public Optional<WorldRegionDefinition> regionDefinition(Identifier id) {
        return Optional.empty();
    }

    @Override
    public List<WorldRegionInstance> nearbyRegions(Level level, BlockPos pos, int radius) {
        return List.of();
    }

    @Override
    public List<WorldRegionInstance> activeRegions(Player player) {
        return List.of();
    }

    @Override
    public boolean registerHazardDefinition(WorldHazardDefinition definition) {
        return false;
    }

    @Override
    public List<WorldHazardDefinition> hazardDefinitions() {
        return List.of();
    }

    @Override
    public Optional<WorldHazardDefinition> hazardDefinition(Identifier id) {
        return Optional.empty();
    }

    @Override
    public WorldHazardSnapshot hazardSnapshot(Player player) {
        return WorldHazardSnapshot.nominal();
    }

    @Override
    public WorldMarker revealMarker(ServerPlayer player, WorldMarker marker) {
        return marker == null ? null : marker;
    }

    @Override
    public WorldMarker revealMarker(Level level, WorldMarker marker) {
        return marker == null ? null : marker;
    }

    @Override
    public List<WorldMarker> nearbyMarkers(Level level, BlockPos pos, int radius) {
        return List.of();
    }

    @Override
    public List<WorldMarker> markers(Player player) {
        return List.of();
    }

    @Override
    public List<String> validateMarkers(Level level) {
        return List.of();
    }

    @Override
    public boolean recordStructureScan(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary) {
        return false;
    }

    @Override
    public boolean recordStructureEntry(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary) {
        return false;
    }

    @Override
    public boolean hasDiscoveredRegion(Player player, Identifier regionId) {
        return false;
    }

    @Override
    public Set<Identifier> discoveredRegions(Player player) {
        return Set.of();
    }

    @Override
    public boolean discoverRegion(ServerPlayer player, Identifier regionId, WorldDiscoverySource source) {
        return false;
    }

    @Override
    public Optional<WorldRegionInstance> currentRegion(Player player) {
        return Optional.empty();
    }

    @Override
    public void tickPlayer(ServerPlayer player) {
    }
}
