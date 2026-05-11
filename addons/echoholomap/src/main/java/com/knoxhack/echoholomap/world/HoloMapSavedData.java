package com.knoxhack.echoholomap.world;

import com.knoxhack.echocore.api.EchoMapMarker;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echoholomap.EchoHoloMap;
import com.knoxhack.echoholomap.HoloMapIds;
import com.knoxhack.echoholomap.map.HoloMapLayers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class HoloMapSavedData extends SavedData {
    private static final Codec<StoredMarker> MARKER_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(StoredMarker::id),
            Codec.STRING.fieldOf("layer").forGetter(StoredMarker::layer),
            Codec.STRING.optionalFieldOf("kind", IMapMarker.MarkerKind.DRONE_SCAN.name()).forGetter(StoredMarker::kind),
            Codec.STRING.optionalFieldOf("state", IMapMarker.MarkerState.DISCOVERED.name()).forGetter(StoredMarker::state),
            Codec.STRING.optionalFieldOf("title", "").forGetter(StoredMarker::title),
            Codec.STRING.optionalFieldOf("summary", "").forGetter(StoredMarker::summary),
            Codec.STRING.optionalFieldOf("dimension", Level.OVERWORLD.identifier().toString()).forGetter(StoredMarker::dimension),
            Codec.DOUBLE.optionalFieldOf("x", 0.0D).forGetter(StoredMarker::x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0D).forGetter(StoredMarker::y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0D).forGetter(StoredMarker::z),
            Codec.FLOAT.optionalFieldOf("radius", 24.0F).forGetter(StoredMarker::radius),
            Codec.BOOL.optionalFieldOf("precise", true).forGetter(StoredMarker::precise)
    ).apply(instance, StoredMarker::new));

    public static final Codec<HoloMapSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MARKER_CODEC.listOf().optionalFieldOf("debug_markers", List.of()).forGetter(HoloMapSavedData::storedMarkers)
    ).apply(instance, HoloMapSavedData::new));

    public static final SavedDataType<HoloMapSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, "holomap"),
            HoloMapSavedData::new,
            CODEC);

    private final Map<String, EchoMapMarker> debugMarkers = new LinkedHashMap<>();

    public HoloMapSavedData() {
    }

    private HoloMapSavedData(List<StoredMarker> markers) {
        for (StoredMarker stored : markers) {
            EchoMapMarker marker = stored.toMarker();
            if (marker != null) {
                debugMarkers.put(marker.id().toString(), marker);
            }
        }
    }

    public static HoloMapSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public EchoMapMarker addDebugMarker(ServerPlayer player, Identifier layerId) {
        if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        Identifier safeLayer = layerId == null ? HoloMapIds.DRONES_SCANS : layerId;
        BlockPos pos = player.blockPosition();
        Identifier id = Identifier.fromNamespaceAndPath(EchoHoloMap.MODID,
                "debug/" + safeLayer.getPath().replace("layer/", "") + "/" + UUID.randomUUID());
        EchoMapMarker marker = new EchoMapMarker(
                id,
                safeLayer,
                HoloMapIds.DEBUG_SOURCE,
                kindForLayer(safeLayer),
                IMapMarker.MarkerState.DISCOVERED,
                "Debug " + HoloMapLayers.fallbackLayer(safeLayer).title(),
                "Permission-gated test marker generated at the operator position.",
                player.level().dimension(),
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                32.0F,
                null,
                null,
                -1,
                true);
        debugMarkers.put(marker.id().toString(), marker);
        setDirty();
        return marker;
    }

    public int clearDebugMarkers() {
        int count = debugMarkers.size();
        if (count > 0) {
            debugMarkers.clear();
            setDirty();
        }
        return count;
    }

    public List<EchoMapMarker> debugMarkers(Level level) {
        ResourceKey<Level> dimension = level == null ? Level.OVERWORLD : level.dimension();
        return debugMarkers.values().stream()
                .filter(marker -> marker.dimension().equals(dimension))
                .toList();
    }

    private List<StoredMarker> storedMarkers() {
        return debugMarkers.values().stream().map(StoredMarker::from).toList();
    }

    private static IMapMarker.MarkerKind kindForLayer(Identifier layerId) {
        if (HoloMapIds.CRASH_SITES.equals(layerId)) {
            return IMapMarker.MarkerKind.CRASH_SITE;
        }
        if (HoloMapIds.ROUTES.equals(layerId)) {
            return IMapMarker.MarkerKind.ROUTE;
        }
        if (HoloMapIds.HAZARDS.equals(layerId)) {
            return IMapMarker.MarkerKind.HAZARD;
        }
        if (HoloMapIds.MISSIONS.equals(layerId)) {
            return IMapMarker.MarkerKind.MISSION;
        }
        if (HoloMapIds.BASES_OUTPOSTS.equals(layerId)) {
            return IMapMarker.MarkerKind.BASE_OUTPOST;
        }
        if (HoloMapIds.ORBITAL_SCANS.equals(layerId)) {
            return IMapMarker.MarkerKind.ORBITAL_SCAN;
        }
        if (HoloMapIds.NEXUS_ANOMALY.equals(layerId)) {
            return IMapMarker.MarkerKind.NEXUS_ANOMALY;
        }
        return IMapMarker.MarkerKind.DRONE_SCAN;
    }

    private record StoredMarker(String id, String layer, String kind, String state, String title, String summary,
            String dimension, double x, double y, double z, float radius, boolean precise) {
        private static StoredMarker from(EchoMapMarker marker) {
            return new StoredMarker(
                    marker.id().toString(),
                    marker.layerId().toString(),
                    marker.kind().name(),
                    marker.state().name(),
                    marker.title(),
                    marker.summary(),
                    marker.dimension().identifier().toString(),
                    marker.x(),
                    marker.y(),
                    marker.z(),
                    marker.radius(),
                    marker.precise());
        }

        private EchoMapMarker toMarker() {
            Identifier id = Identifier.tryParse(this.id);
            Identifier layer = Identifier.tryParse(this.layer);
            if (id == null || layer == null) {
                return null;
            }
            Identifier dimensionId = Identifier.tryParse(dimension);
            ResourceKey<Level> dimensionKey = dimensionId == null
                    ? Level.OVERWORLD
                    : ResourceKey.create(Registries.DIMENSION, dimensionId);
            return new EchoMapMarker(id, layer, HoloMapIds.DEBUG_SOURCE,
                    enumValue(kind, IMapMarker.MarkerKind.DRONE_SCAN),
                    enumValue(state, IMapMarker.MarkerState.DISCOVERED),
                    title, summary, dimensionKey, x, y, z, radius, null, null, -1, precise);
        }

        private static <T extends Enum<T>> T enumValue(String value, T fallback) {
            try {
                return Enum.valueOf(fallback.getDeclaringClass(), value == null ? "" : value);
            } catch (IllegalArgumentException exception) {
                return fallback;
            }
        }
    }
}
