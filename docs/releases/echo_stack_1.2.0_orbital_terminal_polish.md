# ECHO Stack 1.2.0 - Orbital Terminal Polish

ECHO Stack `1.2.0` is a public full-stack release led by Orbital Remnants Terminal polish and Agriculture Reclamation route readiness. It keeps Orbital and Agriculture gameplay and save contracts stable while aligning the shared Survival Route, MissionCore import path, public module versions, and release documentation around the same route placement.

## Highlights

- Root Ashfall, ECHO Core, and every public addon included by `echoAddonSet=all` declare `1.2.0`.
- Orbital Remnants publishes explicit Survival Route placement for its Terminal mission provider:
  - Phase 06: Earth calibration, launch chain, Low Orbit, and station network.
  - Phase 07: Moon, Mars, Europa, Saturn, Titan, and Deep Space Protocol.
  - Phase 08: ECHO-0.
  - Phase 09: survey network, faction outposts, and final network seal.
- MissionCore's Terminal projection uses the same Orbital phase policy when Orbital missions are imported through the shared mission service.
- Agriculture Reclamation publishes FIELD > Reclamation missions as optional Phase 02 Survival Route side leads while retaining ownership of its actions, rewards, support caches, and detailed diagnostics.
- Terminal finalizes explicit route-placement metadata with `main`, `optional`, `reference`, and `hidden` records; the aggregate route honors hidden placement and sorts by explicit phase and order before fallback inference.
- RenderCore remains optional for Orbital visuals; the tinted renderer fallback remains the no-RenderCore path.
- No new Orbital or Agriculture blocks, items, entities, dimensions, packets, recipes, schemas, data components, save fields, or save migrations are part of this release.

## Compatibility

- Public stack version: `1.2.0`.
- Intended release tag and GitHub release name: `v1.2.0`.
- No new public Orbital API.
- Existing Terminal `TerminalMissionProvider.routePlacement(...)` and `TerminalMissionRoutePlacement` are the placement contract.
- Existing optional dependency conventions and version floors remain unchanged unless a module already raised its own floor for earlier 1.2.0 work.

## Automated Verification

Run targeted Orbital checks:

```powershell
.\gradlew.bat :echoorbitalremnants:compileJava
.\gradlew.bat :echoorbitalremnants:runGameTestServer
```

Run targeted Terminal and Agriculture checks:

```powershell
.\gradlew.bat :echoterminal:build --warning-mode all
.\gradlew.bat :echoagriculturereclamation:build --warning-mode all
.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all
.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all
```

Run all-stack release gates:

```powershell
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
.\gradlew.bat -PechoAddonSet=all verifyEchoRelease --warning-mode all
```

## Current Workspace Evidence

- `.\gradlew.bat :echoorbitalremnants:compileJava` passed.
- `.\gradlew.bat :echoorbitalremnants:runGameTestServer` passed with the Orbital route-placement GameTest coverage included.
- `.\gradlew.bat --no-daemon --max-workers=1 :echoterminal:build :echoagriculturereclamation:build --warning-mode all` passed with corrected `1.2.0` metadata.
- `.\gradlew.bat --no-daemon --max-workers=1 :echoterminal:runGameTestServer :echoagriculturereclamation:runGameTestServer --warning-mode all` passed with corrected `1.2.0` metadata.
- `.\gradlew.bat --no-daemon --max-workers=1 -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all` passed.
- `.\gradlew.bat --no-daemon --max-workers=1 -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all` passed and printed every public stack artifact at `1.2.0`.
- `.\gradlew.bat --no-daemon --max-workers=1 -PechoAddonSet=all verifyEchoRelease --warning-mode all` is blocked in the root GameTest server: 249 tests ran and 42 required tests failed, primarily from `echotutorialcore:sync_progress` mock-player packet sends plus existing PowerGrid, Industrial, Nexus, HoloMap, and Ashfall assertions.
- On Windows redirected Gradle output, the all-stack gates were run serially with `--max-workers=1` after concurrent Gradle runs caused transient class-output and jar-output races.
