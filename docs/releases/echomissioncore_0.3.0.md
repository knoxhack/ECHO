# ECHO: MissionCore 0.3.0

MissionCore 0.3.0 is the direct runtime hook hardening release. It keeps
MissionCore as framework infrastructure while reducing reliance on legacy Terminal
provider snapshot rules: migrated addons now record real server-side gameplay events
through `EchoCoreServices.recordMissionObjective(...)`.

## Hook Coverage

- Reclamation, Industrial, Convoy, Orbital, Nexus, Blackbox, and Stationfall keep
  their 0.2.0 `*MissionCoreIntegration` adapters for import, mirror, and custom
  action compatibility.
- New common-side hook helpers record restoration, scans, production, routes,
  orbital milestones, Nexus path events, Blackbox decode/combat milestones, and
  Stationfall section objectives where stable gameplay completion points exist.
- Mission ids remain the existing `TerminalMissionDefinition.id()` values.
- Objective targets follow `<addon>:mission/<legacy_mission>/<objective_key>`.
- Hook context maps include `source`, `legacy_mission`, and one gameplay detail such
  as `route`, `machine`, `region`, or `action`.
- `/echomission validate` reports each known migrated source as `direct-hooks`,
  `adapter-state`, or `mixed`.

## Compatibility Notes

- MissionCore storage remains canonical for new mission state.
- Legacy addon stores remain readable and mirrored for old saves and older UI state.
- Terminal remains optional and display-only; direct hooks work without Terminal
  loaded.
- RenderCore, NetCore, DataCore, and WorldCore are not required by MissionCore
  0.3.0.
- Adapter-state fallback remains supported where a clean gameplay hook is not yet
  available.

## Test Commands

```powershell
.\gradlew.bat :echocore:compileJava --no-configuration-cache --no-daemon
.\gradlew.bat :echomissioncore:compileJava --no-configuration-cache --no-daemon
.\gradlew.bat :echoagriculturereclamation:compileJava :echoindustrialnexus:compileJava :echoconvoyprotocol:compileJava :echoorbitalremnants:compileJava :echonexusprotocol:compileJava :echoblackboxprotocol:compileJava :echostationfall:compileJava --no-configuration-cache --no-daemon
.\gradlew.bat :echomissioncore:runGameTestServer --no-configuration-cache --no-daemon
.\gradlew.bat compileJava buildEchoWorkspace --no-configuration-cache --no-daemon
```

## Release Manifest Expectations

- `addons/echomissioncore/gradle.properties` reports `mod_version=0.3.0`.
- The release jar remains a framework module requiring only `echocore`.
- The release includes MissionCore lang, sample data, README, release notes, addon
  registration documentation, direct hook helpers, and validation tests.
- No MissionCore gameplay blocks, items, entities, textures, recipes, or loot tables
  are expected for this release.
