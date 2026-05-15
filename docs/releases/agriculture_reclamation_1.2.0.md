# ECHO: Agriculture Reclamation 1.2.0 - Terminal Route Polish

Agriculture Reclamation `1.2.0` is a stack-aligned release-polish cut focused on Terminal Survival Route integration and release readiness. It is not an Agriculture gameplay expansion.

## Highlights

- Agriculture missions publish `TerminalMissionRole.OPTIONAL` plus explicit `TerminalMissionRoutePlacement.optional(2, missionOrder)` placement.
- Terminal shows Agriculture as optional Phase 02 side leads in the aggregate Survival Route.
- FIELD > Reclamation remains the owner of Agriculture actions, rewards, support caches, and detailed diagnostics.
- Reflective Terminal registration is hardened so stale optional Terminal installs fail closed instead of crashing the Agriculture addon load path.

## Compatibility

- Public stack version: `1.2.0`.
- Required ECHO Core floor remains `[1.1.0,)`.
- Optional sibling dependency floors remain `[1.0.0,)`; Terminal route placement activates when the current Terminal API is present.
- No new Agriculture blocks, items, entities, recipes, packets, save fields, data components, datapack schemas, or gameplay migrations are part of this release.

## Verification

```powershell
.\gradlew.bat :echoterminal:build --warning-mode all
.\gradlew.bat :echoagriculturereclamation:build --warning-mode all
.\gradlew.bat :echoterminal:runGameTestServer --warning-mode all
.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all
.\gradlew.bat -PechoAddonSet=all verifyEchoRelease --warning-mode all
```

## Migration

No migration is required for existing Agriculture worlds. The 1.2.0 changes affect Terminal presentation, route placement metadata, release documentation, and optional-integration hardening only.
