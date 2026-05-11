package com.knoxhack.echoholomap.map;

import com.knoxhack.echocore.api.IMapDataProvider;
import com.knoxhack.echocore.api.IMapLayer;
import com.knoxhack.echocore.api.IMapMarker;
import com.knoxhack.echocore.api.IMapMarkerService;
import com.knoxhack.echoholomap.EchoHoloMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class HoloMapService implements IMapMarkerService {
    public static final HoloMapService INSTANCE = new HoloMapService();

    private final List<IMapDataProvider> providers = new CopyOnWriteArrayList<>();

    private HoloMapService() {
    }

    public void registerBuiltins() {
        registerProvider(BuiltinMapDataProvider.INSTANCE);
    }

    @Override
    public boolean registerProvider(IMapDataProvider provider) {
        if (provider == null) {
            return false;
        }
        Identifier providerId = safeProviderId(provider);
        if (providerId == null) {
            return false;
        }
        for (IMapDataProvider existing : providers) {
            Identifier existingId = safeProviderId(existing);
            if (providerId.equals(existingId)) {
                return false;
            }
        }
        providers.add(provider);
        return true;
    }

    @Override
    public List<IMapLayer> layers(Player player) {
        Map<Identifier, IMapLayer> layers = new LinkedHashMap<>();
        for (IMapLayer layer : HoloMapLayers.REQUIRED) {
            layers.put(layer.id(), layer);
        }
        for (IMapDataProvider provider : providers) {
            for (IMapLayer layer : safeLayers(provider, player)) {
                if (layer == null || layer.id() == null) {
                    continue;
                }
                layers.putIfAbsent(layer.id(), layer);
            }
        }
        return layers.values().stream()
                .sorted(Comparator.comparingInt(IMapLayer::sortOrder)
                        .thenComparing(layer -> layer.id().toString()))
                .toList();
    }

    @Override
    public List<IMapMarker> markers(Player player) {
        Map<Identifier, IMapMarker> markers = new LinkedHashMap<>();
        for (IMapDataProvider provider : providers) {
            for (IMapMarker marker : safeMarkers(provider, player)) {
                if (marker == null || marker.id() == null || marker.layerId() == null) {
                    continue;
                }
                IMapMarker existing = markers.putIfAbsent(marker.id(), marker);
                if (existing != null && !existing.equals(marker)) {
                    EchoHoloMap.LOGGER.debug("Duplicate HoloMap marker id {} from provider {} ignored.",
                            marker.id(), safeProviderId(provider));
                }
            }
        }
        return markers.values().stream()
                .sorted(Comparator.comparing((IMapMarker marker) -> marker.layerId().toString())
                        .thenComparing(marker -> marker.state().ordinal())
                        .thenComparing(IMapMarker::title)
                        .thenComparing(marker -> marker.id().toString()))
                .toList();
    }

    @Override
    public boolean refresh(ServerPlayer player, String reason) {
        boolean refreshed = false;
        for (IMapDataProvider provider : providers) {
            try {
                refreshed |= provider.refresh(player, reason == null ? "" : reason);
            } catch (RuntimeException exception) {
                EchoHoloMap.LOGGER.warn("HoloMap provider {} failed while refreshing.",
                        safeProviderId(provider), exception);
            }
        }
        return refreshed;
    }

    @Override
    public int providerCount() {
        return providers.size();
    }

    public void clearForTests() {
        providers.clear();
    }

    private static List<IMapLayer> safeLayers(IMapDataProvider provider, Player player) {
        try {
            List<IMapLayer> layers = provider.layers(player);
            return layers == null ? List.of() : layers;
        } catch (RuntimeException exception) {
            EchoHoloMap.LOGGER.warn("HoloMap provider {} failed while listing layers.",
                    safeProviderId(provider), exception);
            return List.of();
        }
    }

    private static List<IMapMarker> safeMarkers(IMapDataProvider provider, Player player) {
        try {
            List<IMapMarker> markers = provider.markers(player);
            return markers == null ? List.of() : markers;
        } catch (RuntimeException exception) {
            EchoHoloMap.LOGGER.warn("HoloMap provider {} failed while listing markers.",
                    safeProviderId(provider), exception);
            return List.of();
        }
    }

    private static Identifier safeProviderId(IMapDataProvider provider) {
        try {
            return provider == null ? null : provider.providerId();
        } catch (RuntimeException exception) {
            EchoHoloMap.LOGGER.warn("HoloMap provider id lookup failed.", exception);
            return null;
        }
    }
}
