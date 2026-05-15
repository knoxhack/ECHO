# ECHO: Nexus Protocol 1.2.0 - Gameplay Polish

`echonexusprotocol` 1.2.0 is an addon-only Chapter IV Nexus gameplay polish release. It intentionally does not make the rest of the ECHO stack a public 1.2.0 release.

## Highlights

- Nexus metadata now reports `1.2.0` for the addon-specific artifact set.
- Optional ECHO Terminal integration now requires the route-placement API floor used by the shared Survival Route.
- The Nexus Terminal route is mapped into late shared route phases: early and middle Nexus work in phase 7, Core and ending work in phase 8.
- Mission guidance, Field Map recovery advice, machine feedback, boss telegraphs, and route-critical reward pacing are tuned around the existing end-to-end Nexus route.

## Compatibility

- Addon version: `echonexusprotocol` 1.2.0.
- Suggested addon tag: `echonexusprotocol-v1.2.0`.
- The public stack version contract is intentionally bypassed for this addon-only release; the rest of the ECHO stack may remain `1.1.3`.
- Registry IDs are kept stable for existing Nexus blocks, items, entities, missions, recipes, loot tables, dimensions, and structures.
- Field Map telemetry remains the current 5x5 sync shape.

## Verification Plan

Run Nexus and release gates:

```powershell
python tools/validate_resources.py --addon-set beta
python tools/validate_resources.py --addon-set all
python tools/validate_gameplay_data.py
.\gradlew.bat :echonexusprotocol:runGameTestServer -PechoAddonSet=beta --warning-mode all --no-configuration-cache
.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
```

Manual smoke remains required before publication:

- First scan, starter cache, Recycler, Stabilizer, Filter, Field Map, Memory Decoder, Warden, Monolith, Reality Forge, Core entry and return, Guardian, final path, and quit/reload persistence.

## Current Workspace Evidence

- Passed `.\gradlew.bat :echonexusprotocol:compileJava -PechoAddonSet=beta --warning-mode all --no-configuration-cache`.
- Passed JSON parsing for the tuned Nexus chest and boss loot tables.
- Passed `python tools\validate_gameplay_data.py`.
- Passed `.\gradlew.bat :echonexusprotocol:runGameTestServer -PechoAddonSet=beta --warning-mode all --no-configuration-cache`; all 35 required Nexus GameTests passed.
- Blocked `python tools\validate_resources.py --addon-set beta` and `--addon-set all` on unrelated Industrial Terminal art: missing `echoindustrialnexus/mission/nexus_furnace_array.png` icon and hero textures.
- Blocked beta workspace build on unrelated Terminal test compile failure: `addons\echoterminal\src\main\java\com\knoxhack\echoterminal\test\ModGameTests.java:1297` cannot resolve `PlacedMissionProvider`.
- Full all-addon build, release artifact validation, and manual client smoke remain outstanding after the unrelated gate blockers above.
