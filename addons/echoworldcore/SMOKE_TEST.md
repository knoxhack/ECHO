# ECHO: WorldCore Smoke Test

## Required Setup

Run with at least:

- `echocore`
- `echonetcore`
- `echoworldcore`
- `echoterminal` for Terminal status checks

Optional but recommended:

- `echoashfallprotocol`
- `echoorbitalremnants`
- `echoconvoyprotocol`
- `echoholomap`

## Test Steps

1. Start a dedicated server or integrated server with WorldCore enabled.
2. Run `/echoworld validate`.
   - Expected: validation passes and reports region, hazard, and marker counts.
3. Run `/echoworld list region`.
   - Expected: Ashfall, Orbital, Convoy, secure outpost, and Nexus definitions
     are listed.
4. Run `/echoworld list hazard`.
   - Expected: salvage debris, toxic air, radiation, cryo cold, Nexus anomaly,
     orbital exposure, convoy threat, and secure zone are listed.
5. Run `/echoworld reveal crash_zone_wasteland`.
   - Expected: the region is discovered through the normal Core discovery path.
6. Open ECHO Terminal and inspect WorldCore in the addons/status view.
   - Expected: active region count, marker count, hazard state, validation state,
     and HoloMap link when HoloMap is installed.
7. With Ashfall installed, scan a POI.
   - Expected: `/echoworld markers 512` shows a structure marker.
8. With Convoy installed, start or advance a route.
   - Expected: route start/checkpoint/destination markers appear.
9. With Orbital installed, seed recovery/debris sites.
   - Expected: orbital debris/recovery markers appear in WorldCore and HoloMap.
10. With HoloMap installed, open Terminal HoloMap.
    - Expected: WorldCore markers use distinct crash, route, hazard, outpost,
      orbital, and anomaly styling.
11. With DataCore and MissionCore installed, inspect DataCore debug keys and a
    mission objective with matching `enter_region`, `discover_structure`, or
    custom WorldCore targets.
    - Expected: DataCore records last region/marker/hazard state and MissionCore
      progresses matching objectives from WorldCore runtime bus events.
12. With RenderCore installed, reload client resources.
    - Expected: WorldCore region and hazard visual profiles load as normal
      RenderCore resources. AudioCore profile resources remain inert unless an
      AudioCore-compatible addon is installed.

## Expected Safety

- Missing optional addons must not crash.
- Dedicated server must not load client-only classes.
- Region discovery and markers persist after world reload.
- Debug commands are permission-gated and can be disabled through config.
