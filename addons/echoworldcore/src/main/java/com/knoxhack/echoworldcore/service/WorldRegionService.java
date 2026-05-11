package com.knoxhack.echoworldcore.service;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.EchoWorldRuntimeBus;
import com.knoxhack.echocore.api.IWorldRegionService;
import com.knoxhack.echocore.api.WorldDiscoverySource;
import com.knoxhack.echocore.api.WorldHazardDefinition;
import com.knoxhack.echocore.api.WorldHazardSnapshot;
import com.knoxhack.echocore.api.WorldMarker;
import com.knoxhack.echocore.api.WorldMarkerType;
import com.knoxhack.echocore.api.WorldRegionDefinition;
import com.knoxhack.echocore.api.WorldRegionInstance;
import com.knoxhack.echocore.api.WorldRegionType;
import com.knoxhack.echoworldcore.Config;
import com.knoxhack.echoworldcore.EchoWorldCore;
import com.knoxhack.echoworldcore.world.WorldRegionSavedData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public final class WorldRegionService implements IWorldRegionService {
    public static final WorldRegionService INSTANCE = new WorldRegionService();

    private final Map<Identifier, WorldRegionDefinition> baseRegionDefinitions = new ConcurrentHashMap<>();
    private final Map<Identifier, WorldHazardDefinition> baseHazardDefinitions = new ConcurrentHashMap<>();
    private final Map<Identifier, WorldRegionDefinition> dataRegionDefinitions = new ConcurrentHashMap<>();
    private final Map<Identifier, WorldHazardDefinition> dataHazardDefinitions = new ConcurrentHashMap<>();
    private final Map<Identifier, WorldRegionDefinition> regionDefinitions = new ConcurrentHashMap<>();
    private final Map<Identifier, WorldHazardDefinition> hazardDefinitions = new ConcurrentHashMap<>();
    private final Map<UUID, Identifier> lastPrimaryRegion = new ConcurrentHashMap<>();
    private final Map<UUID, WorldHazardSnapshot> lastHazards = new ConcurrentHashMap<>();

    public WorldRegionService() {
    }

    @Override
    public boolean registerRegionDefinition(WorldRegionDefinition definition) {
        if (definition == null) {
            return false;
        }
        boolean added = baseRegionDefinitions.putIfAbsent(definition.id(), definition) == null;
        rebuildDefinitionViews();
        return added;
    }

    @Override
    public List<WorldRegionDefinition> regionDefinitions() {
        return regionDefinitions.values().stream()
                .sorted(Comparator.comparingInt(WorldRegionDefinition::sortOrder)
                        .thenComparing(definition -> definition.id().toString()))
                .toList();
    }

    @Override
    public Optional<WorldRegionDefinition> regionDefinition(Identifier id) {
        return Optional.ofNullable(id == null ? null : regionDefinitions.get(id));
    }

    @Override
    public List<WorldRegionInstance> nearbyRegions(Level level, BlockPos pos, int radius) {
        if (level == null || pos == null) {
            return List.of();
        }
        int safeRadius = safeRadius(radius);
        LinkedHashMap<Identifier, WorldRegionInstance> regions = new LinkedHashMap<>();
        for (WorldRegionDefinition definition : regionDefinitions()) {
            if (definition.biomeBacked() && matchesBiome(level, pos, definition)) {
                WorldRegionInstance instance = instanceForDefinition(level, pos, definition, null);
                regions.put(instance.definitionId(), instance);
            }
        }
        for (WorldMarker marker : nearbyMarkers(level, pos, safeRadius)) {
            WorldRegionInstance instance = instanceForMarker(marker, null);
            regions.putIfAbsent(instance.definitionId(), instance);
        }
        return List.copyOf(regions.values());
    }

    @Override
    public List<WorldRegionInstance> activeRegions(Player player) {
        if (player == null) {
            return List.of();
        }
        return nearbyRegions(player.level(), player.blockPosition(), Config.activeRegionRadius()).stream()
                .map(instance -> instanceWithDiscovery(player, instance))
                .toList();
    }

    @Override
    public boolean registerHazardDefinition(WorldHazardDefinition definition) {
        if (definition == null) {
            return false;
        }
        boolean added = baseHazardDefinitions.putIfAbsent(definition.id(), definition) == null;
        rebuildDefinitionViews();
        return added;
    }

    @Override
    public List<WorldHazardDefinition> hazardDefinitions() {
        return hazardDefinitions.values().stream()
                .sorted(Comparator.comparing(definition -> definition.id().toString()))
                .toList();
    }

    @Override
    public Optional<WorldHazardDefinition> hazardDefinition(Identifier id) {
        return Optional.ofNullable(id == null ? null : hazardDefinitions.get(id));
    }

    @Override
    public WorldHazardSnapshot hazardSnapshot(Player player) {
        if (player == null) {
            return WorldHazardSnapshot.nominal();
        }
        LinkedHashSet<Identifier> regionIds = new LinkedHashSet<>();
        LinkedHashSet<Identifier> hazardIds = new LinkedHashSet<>();
        int severity = 0;
        for (WorldRegionInstance region : activeRegions(player)) {
            regionIds.add(region.definitionId());
            for (Identifier hazardId : region.hazardIds()) {
                hazardIds.add(hazardId);
                severity = Math.max(severity, hazardDefinition(hazardId)
                        .map(WorldHazardDefinition::defaultSeverity)
                        .orElse(25));
            }
        }
        if (hazardIds.isEmpty()) {
            return WorldHazardSnapshot.nominal();
        }
        String summary = hazardIds.stream()
                .map(id -> hazardDefinition(id).map(WorldHazardDefinition::displayName).orElse(id.toString()))
                .reduce((left, right) -> left + ", " + right)
                .orElse("Shared world hazard active.");
        return new WorldHazardSnapshot(List.copyOf(regionIds), List.copyOf(hazardIds), severity, false, summary);
    }

    @Override
    public WorldMarker revealMarker(ServerPlayer player, WorldMarker marker) {
        if (marker == null) {
            return null;
        }
        WorldMarker revealed = revealMarker(player == null ? null : player.level(), marker);
        if (player != null && revealed != null) {
            if (revealed.regionId() != null && regionDefinitions.containsKey(revealed.regionId())) {
                discoverRegion(player, revealed.regionId(), WorldDiscoverySource.MARKER);
            }
        }
        EchoWorldRuntimeBus.fireMarkerRevealed(new EchoWorldRuntimeBus.MarkerRevealed(player, revealed));
        return revealed;
    }

    @Override
    public WorldMarker revealMarker(Level level, WorldMarker marker) {
        if (marker == null) {
            return null;
        }
        WorldMarker revealed = marker.discovered(true);
        if (level instanceof ServerLevel serverLevel) {
            WorldRegionSavedData.get(serverLevel).saveMarker(revealed);
        }
        return revealed;
    }

    @Override
    public List<WorldMarker> nearbyMarkers(Level level, BlockPos pos, int radius) {
        if (!(level instanceof ServerLevel serverLevel) || pos == null) {
            return List.of();
        }
        int safeRadius = safeRadius(radius);
        long maxDistance = (long) safeRadius * safeRadius;
        return WorldRegionSavedData.get(serverLevel).markers().stream()
                .filter(marker -> marker.dimension().equals(level.dimension()))
                .filter(marker -> marker.pos().distSqr(pos) <= maxDistance)
                .sorted(Comparator.comparingDouble(marker -> marker.pos().distSqr(pos)))
                .toList();
    }

    @Override
    public List<WorldMarker> markers(Player player) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return List.of();
        }
        return WorldRegionSavedData.get(serverLevel).markers().stream()
                .filter(marker -> marker.dimension().equals(player.level().dimension()))
                .toList();
    }

    @Override
    public List<String> validateMarkers(Level level) {
        List<String> warnings = new ArrayList<>();
        if (regionDefinitions.isEmpty()) {
            warnings.add("No WorldCore region definitions are registered.");
        }
        if (hazardDefinitions.isEmpty()) {
            warnings.add("No WorldCore hazard definitions are registered.");
        }
        Map<Identifier, Identifier> discoveryIds = new LinkedHashMap<>();
        for (WorldRegionDefinition definition : regionDefinitions()) {
            if (definition.displayName().isBlank()) {
                warnings.add("Region " + definition.id() + " has a blank display name.");
            }
            if (definition.summary().isBlank()) {
                warnings.add("Region " + definition.id() + " has a blank summary.");
            }
            if (definition.radius() < 16) {
                warnings.add("Region " + definition.id() + " has invalid radius " + definition.radius());
            }
            Identifier previousRegion = discoveryIds.putIfAbsent(definition.discoveryId(), definition.id());
            if (previousRegion != null && !previousRegion.equals(definition.id())) {
                warnings.add("Regions " + previousRegion + " and " + definition.id()
                        + " share discovery id " + definition.discoveryId());
            }
            for (Identifier hazardId : definition.hazardIds()) {
                if (!hazardDefinitions.containsKey(hazardId)) {
                    warnings.add("Region " + definition.id() + " references missing hazard " + hazardId);
                }
            }
        }
        for (WorldHazardDefinition definition : hazardDefinitions()) {
            if (definition.displayName().isBlank()) {
                warnings.add("Hazard " + definition.id() + " has a blank display name.");
            }
            if (definition.summary().isBlank()) {
                warnings.add("Hazard " + definition.id() + " has a blank summary.");
            }
            if (definition.defaultSeverity() < 0 || definition.defaultSeverity() > 100) {
                warnings.add("Hazard " + definition.id() + " has invalid severity " + definition.defaultSeverity());
            }
        }
        if (level instanceof ServerLevel serverLevel) {
            for (WorldMarker marker : WorldRegionSavedData.get(serverLevel).markers()) {
                if (marker.regionId() != null && !regionDefinitions.containsKey(marker.regionId())) {
                    warnings.add("Marker " + marker.id() + " references unknown region " + marker.regionId());
                }
            }
        }
        return List.copyOf(warnings);
    }

    public synchronized void replaceDataDefinitions(Map<Identifier, WorldHazardDefinition> hazards,
            Map<Identifier, WorldRegionDefinition> regions) {
        dataHazardDefinitions.clear();
        dataRegionDefinitions.clear();
        if (hazards != null) {
            dataHazardDefinitions.putAll(hazards);
        }
        if (regions != null) {
            dataRegionDefinitions.putAll(regions);
        }
        rebuildDefinitionViews();
        for (Identifier id : dataHazardDefinitions.keySet()) {
            if (baseHazardDefinitions.containsKey(id)) {
                EchoWorldCore.LOGGER.info("WorldCore data hazard {} overrides bootstrap definition.", id);
            }
        }
        for (Identifier id : dataRegionDefinitions.keySet()) {
            if (baseRegionDefinitions.containsKey(id)) {
                EchoWorldCore.LOGGER.info("WorldCore data region {} overrides bootstrap definition.", id);
            }
        }
    }

    public int dataRegionDefinitionCount() {
        return dataRegionDefinitions.size();
    }

    public int dataHazardDefinitionCount() {
        return dataHazardDefinitions.size();
    }

    @Override
    public boolean recordStructureScan(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary) {
        return recordStructure(player, structureId, pos, displayName, summary, WorldDiscoverySource.SCAN);
    }

    @Override
    public boolean recordStructureEntry(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary) {
        return recordStructure(player, structureId, pos, displayName, summary, WorldDiscoverySource.STRUCTURE);
    }

    @Override
    public boolean hasDiscoveredRegion(Player player, Identifier regionId) {
        return player != null && regionDefinition(regionId)
                .map(definition -> EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId()))
                .orElse(false);
    }

    @Override
    public Set<Identifier> discoveredRegions(Player player) {
        if (player == null) {
            return Set.of();
        }
        LinkedHashSet<Identifier> discovered = new LinkedHashSet<>();
        for (WorldRegionDefinition definition : regionDefinitions()) {
            if (EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId())) {
                discovered.add(definition.id());
            }
        }
        if (player.level() instanceof ServerLevel serverLevel) {
            discovered.addAll(WorldRegionSavedData.get(serverLevel).discoveries(player.getUUID()));
        }
        return Set.copyOf(discovered);
    }

    @Override
    public boolean discoverRegion(ServerPlayer player, Identifier regionId, WorldDiscoverySource source) {
        if (player == null || regionId == null) {
            return false;
        }
        WorldRegionDefinition definition = regionDefinitions.get(regionId);
        if (definition == null) {
            return false;
        }
        boolean already = EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId());
        boolean discovered = EchoCoreServices.discoverFeature(player, definition.discoveryId());
        WorldRegionInstance instance = activeRegions(player).stream()
                .filter(candidate -> candidate.definitionId().equals(regionId))
                .findFirst()
                .orElse(instanceForDefinition(player.level(), player.blockPosition(), definition, player));
        if (player.level() instanceof ServerLevel serverLevel) {
            WorldRegionSavedData.get(serverLevel).recordDiscovery(player.getUUID(), definition.id(),
                    source == null ? WorldDiscoverySource.INTEGRATION : source, player.blockPosition(),
                    serverLevel.getGameTime());
        }
        EchoWorldRuntimeBus.fireRegionDiscovered(new EchoWorldRuntimeBus.RegionDiscovered(
                player, instance, source == null ? WorldDiscoverySource.INTEGRATION : source, !already && discovered));
        return discovered;
    }

    @Override
    public Optional<WorldRegionInstance> currentRegion(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return activeRegions(player).stream().findFirst();
    }

    @Override
    public void tickPlayer(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return;
        }
        List<WorldRegionInstance> active = activeRegions(player);
        if (!active.isEmpty()) {
            WorldRegionInstance primary = active.get(0);
            Identifier previous = lastPrimaryRegion.put(player.getUUID(), primary.definitionId());
            if (!primary.definitionId().equals(previous)) {
                EchoWorldRuntimeBus.fireRegionEntered(new EchoWorldRuntimeBus.RegionEntered(player, primary));
            }
            for (WorldRegionInstance region : active) {
                discoverRegion(player, region.definitionId(), WorldDiscoverySource.ENTER);
            }
        } else {
            lastPrimaryRegion.remove(player.getUUID());
        }
        WorldHazardSnapshot currentHazard = hazardSnapshot(player);
        WorldHazardSnapshot previousHazard = lastHazards.put(player.getUUID(), currentHazard);
        if (previousHazard != null && !previousHazard.equals(currentHazard)) {
            EchoWorldRuntimeBus.fireHazardChanged(new EchoWorldRuntimeBus.HazardChanged(player, previousHazard, currentHazard));
        }
    }

    private boolean recordStructure(ServerPlayer player, Identifier structureId, BlockPos pos,
            String displayName, String summary, WorldDiscoverySource source) {
        if (player == null || structureId == null || pos == null) {
            return false;
        }
        Optional<WorldRegionDefinition> definition = definitionForStructure(structureId);
        Identifier regionId = definition.map(WorldRegionDefinition::id).orElse(structureId);
        WorldMarkerType markerType = definition.map(def -> switch (def.type()) {
            case CRASH_ZONE -> WorldMarkerType.CRASH_SITE;
            case CONVOY_ROUTE -> WorldMarkerType.ROUTE_CHECKPOINT;
            case ORBITAL_DEBRIS_FIELD -> WorldMarkerType.ORBITAL_DEBRIS;
            case SECURE_OUTPOST -> WorldMarkerType.OUTPOST;
            case ANOMALY_ZONE, NEXUS_SCAR -> WorldMarkerType.ANOMALY;
            default -> WorldMarkerType.STRUCTURE;
        }).orElse(WorldMarkerType.STRUCTURE);
        WorldMarker marker = new WorldMarker(markerId(structureId, pos), regionId, markerType,
                displayName, summary, player.level().dimension(), pos, 64, true, player.level().getGameTime());
        revealMarker(player, marker);
        WorldRegionInstance instance = definition
                .map(def -> instanceForDefinition(player.level(), pos, def, player))
                .orElse(new WorldRegionInstance(regionId, regionId, WorldRegionType.ANOMALY_ZONE,
                        displayName, player.level().dimension(), pos, 64, List.of(), true));
        if (definition.isPresent()) {
            discoverRegion(player, regionId, source);
        }
        EchoWorldRuntimeBus.fireRegionScanned(new EchoWorldRuntimeBus.RegionScanned(player, instance, marker));
        return true;
    }

    private Optional<WorldRegionDefinition> definitionForStructure(Identifier structureId) {
        for (WorldRegionDefinition definition : regionDefinitions()) {
            if (definition.matchesStructure(structureId)
                    || definition.id().equals(structureId)
                    || definition.id().getPath().equals(structureId.getPath())) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    private WorldRegionInstance instanceForDefinition(Level level, BlockPos pos,
            WorldRegionDefinition definition, Player player) {
        ChunkPos chunk = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        ResourceKey<Level> dimension = level == null ? Level.OVERWORLD : level.dimension();
        Identifier dimensionId = dimension.identifier();
        Identifier instanceId = Identifier.fromNamespaceAndPath(definition.id().getNamespace(),
                "region/" + definition.id().getPath() + "/" + dimensionId.getNamespace() + "/"
                        + dimensionId.getPath() + "/" + chunk.x() + "_" + chunk.z());
        boolean discovered = player != null && EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId());
        return new WorldRegionInstance(instanceId, definition.id(), definition.type(), definition.displayName(),
                dimension, pos.immutable(), definition.radius(), definition.hazardIds(), discovered);
    }

    private WorldRegionInstance instanceForMarker(WorldMarker marker, Player player) {
        WorldRegionDefinition definition = marker.regionId() == null ? null : regionDefinitions.get(marker.regionId());
        if (definition == null) {
            return new WorldRegionInstance(marker.id(), marker.regionId() == null ? marker.id() : marker.regionId(),
                    WorldRegionType.ANOMALY_ZONE, marker.displayName(), marker.dimension(), marker.pos(),
                    marker.radius(), List.of(), marker.discovered());
        }
        boolean discovered = player != null && EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId());
        return new WorldRegionInstance(marker.id(), definition.id(), definition.type(), definition.displayName(),
                marker.dimension(), marker.pos(), Math.max(marker.radius(), definition.radius()),
                definition.hazardIds(), discovered || marker.discovered());
    }

    private WorldRegionInstance instanceWithDiscovery(Player player, WorldRegionInstance instance) {
        WorldRegionDefinition definition = regionDefinitions.get(instance.definitionId());
        boolean discovered = definition != null && EchoCoreServices.hasDiscoveredFeature(player, definition.discoveryId());
        return new WorldRegionInstance(instance.id(), instance.definitionId(), instance.type(), instance.displayName(),
                instance.dimension(), instance.center(), instance.radius(), instance.hazardIds(), discovered);
    }

    private boolean matchesBiome(Level level, BlockPos pos, WorldRegionDefinition definition) {
        Holder<Biome> biome = level.getBiome(pos);
        Identifier biomeId = biome.unwrapKey().map(key -> key.identifier()).orElse(null);
        if (biomeId != null && definition.biomeIds().contains(biomeId)) {
            return true;
        }
        for (Identifier tagId : definition.biomeTags()) {
            if (biome.is(TagKey.create(Registries.BIOME, tagId))) {
                return true;
            }
        }
        return false;
    }

    private synchronized void rebuildDefinitionViews() {
        hazardDefinitions.clear();
        hazardDefinitions.putAll(baseHazardDefinitions);
        hazardDefinitions.putAll(dataHazardDefinitions);
        regionDefinitions.clear();
        regionDefinitions.putAll(baseRegionDefinitions);
        regionDefinitions.putAll(dataRegionDefinitions);
    }

    private static int safeRadius(int radius) {
        return Math.min(Math.max(1, radius), Config.markerQueryRadiusCap());
    }

    private static Identifier markerId(Identifier source, BlockPos pos) {
        String path = "marker/" + sanitize(source.getPath()) + "/" + pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
        return Identifier.fromNamespaceAndPath(EchoWorldCore.MODID, path);
    }

    private static String sanitize(String value) {
        return value == null ? "unknown" : value.replace(':', '_').replace('\\', '/');
    }
}
