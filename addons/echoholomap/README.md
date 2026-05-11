# ECHO: HoloMap

ECHO: HoloMap adds a Terminal command-map tab for shared world telemetry: crash sites, routes, hazards, missions, bases, orbital scan overlays, anomalies, and drone scan markers. It also discovers a real, per-player terrain map while players move through loaded chunks and renders the same terrain cache in a lightweight minimap HUD.

## Terrain Discovery

HoloMap terrain is owned by the `echoholomap` module, not ECHO Core. The server samples loaded chunks around each player on a configurable interval and stores compact `16x16` biome-colored terrain tiles per player and dimension. The scanner never force-loads chunks and client tile requests can only fetch terrain the server has already discovered for that player.

V1 terrain is biome-colored with simple water, shore, dimension, and height shading. It is intentionally not a block-texture clone of the world renderer. Markers remain Core API data and are drawn over the terrain camera using real world X/Z coordinates.

Client controls:

- Open the existing ECHO Terminal and select `HoloMap`.
- Pan with arrow keys.
- Zoom with mouse wheel, `+`, and `-`.
- Press `CENTER` or `Home`/`C` to recenter on the player.
- Press `J` in-game to toggle the minimap HUD.

## Addon Marker API

Addons should register map data from common setup through ECHO Core:

```java
EchoCoreServices.registerMapDataProvider(new IMapDataProvider() {
    @Override
    public Identifier providerId() {
        return Identifier.fromNamespaceAndPath("exampleaddon", "map_provider");
    }

    @Override
    public List<IMapLayer> layers(Player player) {
        return List.of(new EchoMapLayer(
                Identifier.fromNamespaceAndPath("echoholomap", "layer/crash_sites"),
                "Crash Sites", 10, 0xFFFFA05B, true));
    }

    @Override
    public List<IMapMarker> markers(Player player) {
        return List.of(new EchoMapMarker(
                Identifier.fromNamespaceAndPath("exampleaddon", "marker/crash_alpha"),
                Identifier.fromNamespaceAndPath("echoholomap", "layer/crash_sites"),
                providerId(),
                IMapMarker.MarkerKind.CRASH_SITE,
                IMapMarker.MarkerState.DISCOVERED,
                "Crash Alpha",
                "Recovered debris field with salvage risk.",
                player == null ? Level.OVERWORLD : player.level().dimension(),
                120.5D, 72.0D, -340.5D,
                48.0F,
                null,
                null,
                -1,
                true));
    }
});
```

Convoy routes use ordinary markers with a shared route id and ordered route positions:

```java
Identifier routeId = Identifier.fromNamespaceAndPath("exampleaddon", "route/ember_line");
List<IMapMarker> routeMarkers = List.of(
        new EchoMapMarker(
                Identifier.fromNamespaceAndPath("exampleaddon", "marker/ember_line/start"),
                Identifier.fromNamespaceAndPath("echoholomap", "layer/routes"),
                providerId(),
                IMapMarker.MarkerKind.ROUTE,
                IMapMarker.MarkerState.DISCOVERED,
                "Ember Line Start",
                "Convoy route staging point.",
                Level.OVERWORLD,
                -80.0D, 70.0D, 12.0D,
                0.0F,
                null,
                routeId,
                0,
                true),
        new EchoMapMarker(
                Identifier.fromNamespaceAndPath("exampleaddon", "marker/ember_line/end"),
                Identifier.fromNamespaceAndPath("echoholomap", "layer/routes"),
                providerId(),
                IMapMarker.MarkerKind.ROUTE,
                IMapMarker.MarkerState.DISCOVERED,
                "Ember Line End",
                "Convoy route endpoint.",
                Level.OVERWORLD,
                260.0D, 72.0D, -144.0D,
                0.0F,
                null,
                routeId,
                1,
                true));
```

Locked orbital scans should use safe hint text, an estimated position, and a radius:

```java
IMapMarker lockedScan = new EchoMapMarker(
        Identifier.fromNamespaceAndPath("exampleaddon", "marker/orbital/locked_cache"),
        Identifier.fromNamespaceAndPath("echoholomap", "layer/orbital_scans"),
        providerId(),
        IMapMarker.MarkerKind.ORBITAL_SCAN,
        IMapMarker.MarkerState.LOCKED,
        "Encrypted Orbital Return",
        "Signal exists, but coordinates require additional scan clearance.",
        Level.OVERWORLD,
        512.0D, 96.0D, -768.0D,
        96.0F,
        null,
        null,
        -1,
        false);
```

Required built-in layer ids:

- `echoholomap:layer/crash_sites`
- `echoholomap:layer/routes`
- `echoholomap:layer/hazards`
- `echoholomap:layer/missions`
- `echoholomap:layer/bases_outposts`
- `echoholomap:layer/orbital_scans`
- `echoholomap:layer/nexus_anomaly`
- `echoholomap:layer/drones_scans`

Marker states:

- `HIDDEN`: not shown in normal snapshots.
- `LOCKED`: shown dimmed with hint-safe text.
- `DISCOVERED`: shown as active field intel.
- `CHECKED`: shown as completed/cleared intel.

For orbital or drone scans, set `radius` to the scan footprint and `precise=false` if the position is estimated.

## Debug

Permissioned operators can add test markers in-game:

```text
/echoholomap debug add_marker drones_scans
/echoholomap debug add_marker hazards
/echoholomap debug dump
/echoholomap debug clear_markers
/echoholomap debug scan_terrain 6
/echoholomap debug dump_terrain
/echoholomap debug clear_terrain
```

The Terminal tab also has a `TEST` hook that adds a drone/scan marker when debug markers are enabled.

Terrain debug commands are gated by the same debug config and operator permission as marker debug commands. `scan_terrain` only samples chunks already loaded around the player; it does not reveal remote panned locations.
