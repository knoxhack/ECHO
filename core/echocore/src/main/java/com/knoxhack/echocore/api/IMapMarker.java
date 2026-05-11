package com.knoxhack.echocore.api;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * Public marker contract for Terminal-compatible ECHO maps.
 */
public interface IMapMarker {
    Identifier id();

    Identifier layerId();

    Identifier sourceId();

    MarkerKind kind();

    MarkerState state();

    String title();

    String summary();

    ResourceKey<Level> dimension();

    double x();

    double y();

    double z();

    float radius();

    Identifier icon();

    Identifier routeId();

    int routeOrder();

    boolean precise();

    enum MarkerKind {
        CRASH_SITE,
        ROUTE,
        HAZARD,
        MISSION,
        BASE_OUTPOST,
        ORBITAL_SCAN,
        NEXUS_ANOMALY,
        DRONE_SCAN,
        REGION,
        GENERIC
    }

    enum MarkerState {
        HIDDEN,
        LOCKED,
        DISCOVERED,
        CHECKED
    }
}
