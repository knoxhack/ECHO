# ECHO: Agriculture Reclamation 0.3.1 Patch Changelog

## Pollinator Drone Stabilization

- Hardened dock-bound Pollinator Drone lookup so overlapping entity queries cannot double-count the same drone.
- Duplicate cleanup keeps the oldest valid bound drone for a dock and recalls extras deterministically.
- Drone save/load continues to preserve home dock, current target, cooldown, status, and service count.
- Pollinator service remains greenhouse-gated: outdoor crops and Hydroponic Trays without an established zone receive no drone growth service.

## Diagnostic Polish

- Greenhouse Controller, Pollinator Dock, Ecology Scanner, FIELD > Reclamation, and greenhouse mission text use clearer drone/service target wording.
- Stale saved greenhouse profiles now call out missing controllers or removed support structures instead of implying a live safe zone.
- Existing drone machine rule keys and defaults remain unchanged.

## Validation Targets

- `.\gradlew.bat :echoagriculturereclamation:build --warning-mode all`
- `.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateReleaseArtifacts printReleaseManifest --warning-mode all`

## Compatibility Notes

- This is a stabilization patch and bumps Agriculture Reclamation from `0.3.0` to `0.3.1`.
- No new crops, blocks, items, entities, machine rule keys, data components, recipes, or saved-data fields are added.
- Existing `greenhouseSafety`, `greenhouseZones`, and optional drone profile fields remain readable.
