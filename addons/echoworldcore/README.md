# ECHO: WorldCore

WorldCore is a foundation module for the ECHO ecosystem. It does not own
Ashfall world generation, Convoy routes, Orbital debris generation, Nexus
structures, or any player craftable content. It provides the shared vocabulary
and runtime services that those systems use to describe the same world safely.

## What WorldCore Provides

- Region definitions and active region lookup.
- Hazard definitions and current hazard snapshots.
- Persistent world markers for structures, crash sites, routes, debris, outposts,
  and anomalies.
- Per-player region discovery through ECHO Core discovery data plus WorldCore
  SavedData.
- Runtime bus events for region enter/discover/scan, marker reveal, and hazard
  changes.
- Optional Terminal status and HoloMap feed support through ECHO Core services.
- Permission-gated `/echoworld` validation and inspection commands.
- RenderCore profile resources for every built-in region and hazard category.
- Forward-compatible AudioCore ambience profile resources for built-in region
  `audioProfileId` values.

## Public Services

Use ECHO Core accessors instead of depending on the implementation class:

- `EchoCoreServices.worldRegions()`
- `EchoCoreServices.regionService()`
- `EchoCoreServices.hazardService()`
- `EchoCoreServices.worldMarkerService()`
- `EchoCoreServices.structureDiscoveryService()`

When WorldCore is absent, these resolve to `NoOpWorldService`, so optional
integrations can call them safely.

## Built-In Integrations

- Ashfall scanner discoveries are recorded as WorldCore structure markers.
- Convoy route start, checkpoint, and destination events create route markers.
- Orbital recovery and debris sites create persistent orbital markers.
- HoloMap consumes WorldCore regions, markers, and hazards through Core services.
- Terminal shows active regions, marker counts, hazard summary, validation state,
  and a HoloMap link when HoloMap is installed.
- DataCore subscribes to WorldCore runtime events and stores last region, marker,
  discovery, and hazard summary keys when DataCore is installed.
- MissionCore subscribes to WorldCore runtime events and records matching
  `enter_region`, `discover_structure`, and custom objective progress.

## Commands

All commands require operator/game-master permission and
`debug.commandsEnabled=true`.

- `/echoworld validate`
- `/echoworld list [region|hazard|all]`
- `/echoworld nearby [radius]`
- `/echoworld markers [radius]`
- `/echoworld hazard`
- `/echoworld reveal <region_id>`

## Configuration

Common config keys:

- `runtime.playerScanIntervalTicks`
- `runtime.activeRegionRadius`
- `runtime.markerQueryRadiusCap`
- `debug.commandsEnabled`

Defaults preserve the initial WorldCore behavior.
