package com.knoxhack.echoholomap.network;

import com.knoxhack.echocore.api.EchoCoreServices;
import com.knoxhack.echocore.api.IMapLayer;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echonetcore.api.EchoPayloadCodecs;
import com.knoxhack.echoholomap.Config;
import com.knoxhack.echoholomap.EchoHoloMap;
import com.knoxhack.echoholomap.map.HoloMapLayers;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record HoloMapSnapshotPacket(
        List<LayerData> layers,
        List<MarkerData> markers,
        String statusLine,
        long gameTime) implements CustomPacketPayload {
    private static final int MAX_LAYERS = 32;
    private static final int MAX_MARKERS_PACKET = 2048;
    private static final int MAX_TEXT = 240;

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EchoHoloMap.MODID, "snapshot");
    public static final Type<HoloMapSnapshotPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, HoloMapSnapshotPacket> CODEC =
            StreamCodec.of(HoloMapSnapshotPacket::write, HoloMapSnapshotPacket::read);

    public HoloMapSnapshotPacket {
        layers = copyLayers(layers);
        markers = copyMarkers(markers);
        statusLine = safe(statusLine, "HoloMap awaiting field sync.");
        gameTime = Math.max(0L, gameTime);
    }

    public static HoloMapSnapshotPacket empty() {
        return new HoloMapSnapshotPacket(List.of(), List.of(),
                "HoloMap offline. Press SYNC after Terminal handshake.", 0L);
    }

    public static HoloMapSnapshotPacket from(ServerPlayer player) {
        if (player == null) {
            return empty();
        }
        EchoCoreServices.refreshMapMarkers(player, "snapshot");
        Map<Identifier, LayerData> layerMap = new LinkedHashMap<>();
        for (IMapLayer layer : EchoCoreServices.mapLayers(player)) {
            if (layer != null && layer.id() != null) {
                layerMap.putIfAbsent(layer.id(), LayerData.from(layer));
            }
        }
        List<MarkerData> markerData = new ArrayList<>();
        boolean showHidden = showHiddenMarkers();
        int maxMarkers = maxMarkers();
        for (IMapMarker marker : EchoCoreServices.mapMarkers(player)) {
            if (marker == null || marker.id() == null || marker.layerId() == null) {
                continue;
            }
            if (marker.state() == IMapMarker.MarkerState.HIDDEN && !showHidden) {
                continue;
            }
            layerMap.putIfAbsent(marker.layerId(), LayerData.from(HoloMapLayers.fallbackLayer(marker.layerId())));
            markerData.add(MarkerData.from(marker));
            if (markerData.size() >= maxMarkers) {
                break;
            }
        }
        List<LayerData> layers = layerMap.values().stream()
                .sorted(Comparator.comparingInt(LayerData::sortOrder)
                        .thenComparing(layer -> layer.id().toString()))
                .limit(MAX_LAYERS)
                .toList();
        String status = "Providers " + EchoCoreServices.mapMarkerService().providerCount()
                + " / layers " + layers.size()
                + " / markers " + markerData.size()
                + " / t+" + player.level().getGameTime();
        return new HoloMapSnapshotPacket(layers, markerData, status, player.level().getGameTime());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static void write(RegistryFriendlyByteBuf buffer, HoloMapSnapshotPacket packet) {
        buffer.writeVarInt(packet.layers().size());
        for (LayerData layer : packet.layers()) {
            EchoPayloadCodecs.writeIdentifier(buffer, layer.id());
            buffer.writeUtf(layer.title(), MAX_TEXT);
            buffer.writeVarInt(layer.sortOrder());
            buffer.writeInt(layer.color());
            buffer.writeBoolean(layer.visibleByDefault());
        }
        buffer.writeVarInt(packet.markers().size());
        for (MarkerData marker : packet.markers()) {
            EchoPayloadCodecs.writeIdentifier(buffer, marker.id());
            EchoPayloadCodecs.writeIdentifier(buffer, marker.layerId());
            EchoPayloadCodecs.writeIdentifier(buffer, marker.sourceId());
            buffer.writeEnum(marker.kind());
            buffer.writeEnum(marker.state());
            buffer.writeUtf(marker.title(), MAX_TEXT);
            buffer.writeUtf(marker.summary(), MAX_TEXT);
            buffer.writeUtf(marker.dimension(), EchoPayloadCodecs.ID);
            buffer.writeDouble(marker.x());
            buffer.writeDouble(marker.y());
            buffer.writeDouble(marker.z());
            buffer.writeFloat(marker.radius());
            buffer.writeUtf(marker.routeId(), EchoPayloadCodecs.ID);
            buffer.writeVarInt(marker.routeOrder());
            buffer.writeBoolean(marker.precise());
        }
        buffer.writeUtf(packet.statusLine(), MAX_TEXT);
        buffer.writeVarLong(packet.gameTime());
    }

    private static HoloMapSnapshotPacket read(RegistryFriendlyByteBuf buffer) {
        int layerCount = Math.max(0, Math.min(MAX_LAYERS, buffer.readVarInt()));
        List<LayerData> layers = new ArrayList<>();
        for (int i = 0; i < layerCount; i++) {
            layers.add(new LayerData(
                    EchoPayloadCodecs.readIdentifier(buffer),
                    buffer.readUtf(MAX_TEXT),
                    buffer.readVarInt(),
                    buffer.readInt(),
                    buffer.readBoolean()));
        }
        int markerCount = Math.max(0, Math.min(MAX_MARKERS_PACKET, buffer.readVarInt()));
        List<MarkerData> markers = new ArrayList<>();
        for (int i = 0; i < markerCount; i++) {
            markers.add(new MarkerData(
                    EchoPayloadCodecs.readIdentifier(buffer),
                    EchoPayloadCodecs.readIdentifier(buffer),
                    EchoPayloadCodecs.readIdentifier(buffer),
                    buffer.readEnum(IMapMarker.MarkerKind.class),
                    buffer.readEnum(IMapMarker.MarkerState.class),
                    buffer.readUtf(MAX_TEXT),
                    buffer.readUtf(MAX_TEXT),
                    buffer.readUtf(EchoPayloadCodecs.ID),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readDouble(),
                    buffer.readFloat(),
                    buffer.readUtf(EchoPayloadCodecs.ID),
                    buffer.readVarInt(),
                    buffer.readBoolean()));
        }
        return new HoloMapSnapshotPacket(layers, markers, buffer.readUtf(MAX_TEXT), buffer.readVarLong());
    }

    private static List<LayerData> copyLayers(List<LayerData> layers) {
        if (layers == null || layers.isEmpty()) {
            return List.of();
        }
        return layers.stream()
                .filter(layer -> layer != null && layer.id() != null)
                .limit(MAX_LAYERS)
                .toList();
    }

    private static List<MarkerData> copyMarkers(List<MarkerData> markers) {
        if (markers == null || markers.isEmpty()) {
            return List.of();
        }
        return markers.stream()
                .filter(marker -> marker != null && marker.id() != null && marker.layerId() != null)
                .limit(MAX_MARKERS_PACKET)
                .toList();
    }

    private static int maxMarkers() {
        try {
            return Math.max(32, Math.min(MAX_MARKERS_PACKET, Config.MAX_MARKERS.get()));
        } catch (RuntimeException exception) {
            return 384;
        }
    }

    private static boolean showHiddenMarkers() {
        try {
            return Config.SHOW_HIDDEN_MARKERS.get();
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    public record LayerData(Identifier id, String title, int sortOrder, int color, boolean visibleByDefault) {
        public LayerData {
            title = safe(title, id == null ? "Layer" : id.getPath());
            color = color == 0 ? 0xFF66E8FF : color;
        }

        public static LayerData from(IMapLayer layer) {
            return new LayerData(layer.id(), layer.title(), layer.sortOrder(), layer.color(), layer.visibleByDefault());
        }
    }

    public record MarkerData(
            Identifier id,
            Identifier layerId,
            Identifier sourceId,
            IMapMarker.MarkerKind kind,
            IMapMarker.MarkerState state,
            String title,
            String summary,
            String dimension,
            double x,
            double y,
            double z,
            float radius,
            String routeId,
            int routeOrder,
            boolean precise) {
        public MarkerData {
            sourceId = sourceId == null ? id : sourceId;
            kind = kind == null ? IMapMarker.MarkerKind.GENERIC : kind;
            state = state == null ? IMapMarker.MarkerState.DISCOVERED : state;
            title = safe(title, id == null ? "Marker" : id.getPath());
            summary = safe(summary, "");
            dimension = safe(dimension, "minecraft:overworld");
            radius = Math.max(0.0F, radius);
            routeId = safe(routeId, "");
        }

        public static MarkerData from(IMapMarker marker) {
            return new MarkerData(
                    marker.id(),
                    marker.layerId(),
                    marker.sourceId(),
                    marker.kind(),
                    marker.state(),
                    marker.title(),
                    marker.summary(),
                    marker.dimension().identifier().toString(),
                    marker.x(),
                    marker.y(),
                    marker.z(),
                    marker.radius(),
                    marker.routeId() == null ? "" : marker.routeId().toString(),
                    marker.routeOrder(),
                    marker.precise());
        }
    }
}
