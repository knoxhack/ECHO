# ECHO: Agriculture Reclamation 0.3.0 Draft Changelog

## Pollinator Drone System

- Added the `pollinator_drone` entity as a dock-bound Agriculture service drone.
- Pollinator Drone Dock normal use deploys or reports the bound drone; sneak-use recalls bound drones.
- Duplicate drones for the same dock are recalled so each dock owns one active service drone.
- Drones save and load their home dock, current target, cooldown, status, and service count.
- Drones hover near their dock, visit crop and Hydroponic Tray targets, and discard themselves if their dock is removed.

## Greenhouse-Gated Service

- Pollinator service only grants growth support inside an established greenhouse zone.
- Safe zones receive the full configured service bonus.
- Strained and unsafe established zones receive reduced service while keeping the existing soft greenhouse penalties.
- Outdoor crops and trays without an established greenhouse zone keep the existing early-route behavior and receive no drone service bonus.
- Drone service uses the same soil compatibility, seed profile, greenhouse context, and nutrient rules as normal crop and tray growth.

## Diagnostics

- Greenhouse profiles now preserve deployed drone count and service target count in addition to 0.2.0 zone details.
- Greenhouse Controller, Pollinator Dock, Ecology Scanner, FIELD > Reclamation reports, and greenhouse mission detail include drone service diagnostics.
- Machine rules now expose `pollinatorDroneServiceRadius`, `pollinatorDroneHomeRadius`, `pollinatorDroneServiceTicks`, and `pollinatorDroneGrowthBonus`.

## Validation Targets

- `.\gradlew.bat :echoagriculturereclamation:build --warning-mode all`
- `.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`

## Compatibility Notes

- This is a feature release and bumps Agriculture Reclamation from `0.2.0` to `0.3.0`.
- Existing saved `greenhouseSafety` and `greenhouseZones` data remains readable; new drone profile fields are optional.
- Existing 0.2.0 greenhouse scoring remains soft and does not require drones to reach safe-zone quality.
- This release adds only Agriculture's Pollinator Drone, not the broader ECHO companion scout or Logistics delivery drone systems.
