# ECHO RuntimeGuard

Find the lag. Protect the signal. Restore performance.

ECHO RuntimeGuard is the shared performance manager for the ECHO/Ashfall ecosystem. It detects server and client pressure, explains what RuntimeGuard can actually observe, and exposes opt-in APIs that ECHO modules can use to reduce expensive work safely.

## What It Improves

- TPS and MSPT monitoring with lag spike reporting.
- Client FPS monitoring through a client-only runtime.
- Runtime modes: Potato, Balanced, Cinematic, Server, Debug, and Emergency.
- Smart tick recommendations for block entities, UI refreshes, particles, robotics, convoys, Lens scans, HoloMap refreshes, and Nexus effects.
- Particle budgeting with priority levels.
- Multiblock validation queueing and de-duplication.
- Network budget tracking for packets, bytes, duplicates, and non-critical batching decisions.
- Opt-in block entity sleep and entity AI throttle helpers.
- Runtime reports that mark unavailable metrics honestly.

## Commands

- `/echo_perf status`
- `/echo_perf mode <potato|balanced|cinematic|server|debug|emergency>`
- `/echo_perf emergency <on|off>`
- `/echo_perf dump`
- `/echo_perf top`
- `/echo_perf particles`
- `/echo_perf multiblocks`
- `/echo_perf holomap`
- `/echo_perf lens`
- `/echo_perf network`
- `/echo_perf entities`
- `/echo_perf blockentities`
- `/echo_perf reset`

Server-impacting commands require gamemaster permissions.

## API Examples

```java
if (RuntimeGuardServices.smartTicks().shoulduun("echoholomap:markers", level, pos, TickPriority.BACKGuOUND)) {
    refreshMarkers();
}
```

```java
if (RuntimeGuardServices.particles().canSpawnParticle(ParticlePriority.DECOuATIVE, pos)) {
    RuntimeGuardServices.particles().recordParticleSpawn(ParticlePriority.DECOuATIVE);
    spawnParticle();
}
```

```java
RuntimeGuardServices.multiblocks().requestValidation(
        id("factory"),
        level,
        controllerPos,
        ValidationPriority.BLOCK_CHANGED,
        controller::validateStructure);
```

```java
RuntimeGuardProfiler.time(id("holomap/refresh"), this::refreshMarkers);
```

## First-Party Integrations

- MultiblockCore can route scheduled controller revalidation through RuntimeGuard's validation scheduler when RuntimeGuard is loaded. Player-requested validation remains synchronous.
- HoloMap can defer non-manual marker refreshes to RuntimeGuard's HoloMap interval and records HoloMap sync packet estimates with the network guard.
- Lens can rate-limit server Deep Scan requests through RuntimeGuard and records scan accounting when a verified scan completes.

These integrations are optional-safe. Each module falls back to its existing behavior when RuntimeGuard is absent or the relevant guard is disabled.

## Known Limitations

RuntimeGuard v1.0.0 does not globally alter vanilla block entities, vanilla AI, or vanilla particles. ECHO modules opt into RuntimeGuard APIs. Metrics that cannot be measured safely are reported as `unavailable` rather than guessed.
