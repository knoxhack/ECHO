# ECHO: Armory 1.2.0 - Route Kit Readiness

Status: feature implementation prepared in `addons/echoarmory`. This is an Armory-only release note and does not change stack-wide version truth.

## Highlights

- Adds shared route-kit readiness reports with `READY`, `STAGED`, `MISSING`, and `LOCKED` states.
- Extends Armory loadout datapacks with optional `requiredProtections` for toxic, radiation, cold, heat, and fracture thresholds.
- Keeps legacy `minProtection` compatible by treating it as fracture protection when `requiredProtections` is absent.
- Uses readiness reports in Core route records, diagnostics, hazard telemetry, Terminal actions, Loadout Terminal binding, Logistics dispatch, and MissionCore side ops.
- Updates bundled Toxic Breach, Fracture Guardian, and Orbital Assault kits to express route-specific hazard needs.

## Compatibility

- Public Armory version: `1.2.0`.
- No save migration.
- No new required cross-addon API.
- Optional Terminal, Logistics Network, and MissionCore integrations remain guarded.
- No new items, entities, art assets, packets, or Core API changes.

## Verification

Armory compile, targeted build, GameTests, and all-addon workspace build pass:

```powershell
.\gradlew.bat :echoarmory:compileJava --warning-mode all
.\gradlew.bat :echoarmory:build --warning-mode all
.\gradlew.bat --no-configuration-cache :echoarmory:runGameTestServer --warning-mode all
.\gradlew.bat -PechoAddonSet=all buildEchoWorkspace --warning-mode all
```

Root `validateEchoResources` passes. Root `validateEchoGameplayData` is currently blocked by existing Terminal source-token checks.

Validated root resource gate:

```powershell
.\gradlew.bat validateEchoResources --warning-mode all
```

Remaining blocked gate:

```powershell
.\gradlew.bat validateEchoGameplayData --warning-mode all
```
