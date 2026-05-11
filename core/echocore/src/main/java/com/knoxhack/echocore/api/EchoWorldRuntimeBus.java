package com.knoxhack.echocore.api;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;

public final class EchoWorldRuntimeBus {
    private static final CopyOnWriteArrayList<Consumer<RegionEntered>> REGION_ENTERED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<RegionDiscovered>> REGION_DISCOVERED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<RegionScanned>> REGION_SCANNED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<MarkerRevealed>> MARKER_REVEALED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<HazardChanged>> HAZARD_CHANGED = new CopyOnWriteArrayList<>();

    private EchoWorldRuntimeBus() {
    }

    public static void onRegionEntered(Consumer<RegionEntered> listener) {
        add(REGION_ENTERED, listener);
    }

    public static void onRegionDiscovered(Consumer<RegionDiscovered> listener) {
        add(REGION_DISCOVERED, listener);
    }

    public static void onRegionScanned(Consumer<RegionScanned> listener) {
        add(REGION_SCANNED, listener);
    }

    public static void onMarkerRevealed(Consumer<MarkerRevealed> listener) {
        add(MARKER_REVEALED, listener);
    }

    public static void onHazardChanged(Consumer<HazardChanged> listener) {
        add(HAZARD_CHANGED, listener);
    }

    public static void fireRegionEntered(RegionEntered event) {
        fire(REGION_ENTERED, event);
    }

    public static void fireRegionDiscovered(RegionDiscovered event) {
        fire(REGION_DISCOVERED, event);
    }

    public static void fireRegionScanned(RegionScanned event) {
        fire(REGION_SCANNED, event);
    }

    public static void fireMarkerRevealed(MarkerRevealed event) {
        fire(MARKER_REVEALED, event);
    }

    public static void fireHazardChanged(HazardChanged event) {
        fire(HAZARD_CHANGED, event);
    }

    public static void clearForTests() {
        REGION_ENTERED.clear();
        REGION_DISCOVERED.clear();
        REGION_SCANNED.clear();
        MARKER_REVEALED.clear();
        HAZARD_CHANGED.clear();
    }

    private static <T> void add(CopyOnWriteArrayList<Consumer<T>> listeners, Consumer<T> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private static <T> void fire(CopyOnWriteArrayList<Consumer<T>> listeners, T event) {
        if (event == null) {
            return;
        }
        for (Consumer<T> listener : listeners) {
            listener.accept(event);
        }
    }

    public record RegionEntered(ServerPlayer player, WorldRegionInstance region) {
    }

    public record RegionDiscovered(ServerPlayer player, WorldRegionInstance region,
            WorldDiscoverySource source, boolean firstDiscovery) {
    }

    public record RegionScanned(ServerPlayer player, WorldRegionInstance region, WorldMarker marker) {
    }

    public record MarkerRevealed(ServerPlayer player, WorldMarker marker) {
    }

    public record HazardChanged(ServerPlayer player, WorldHazardSnapshot previous, WorldHazardSnapshot current) {
    }
}
