# ECHO: Agriculture Reclamation 0.2.0 Draft Changelog

## Greenhouse Zones

- Added saved greenhouse zone profiles established by Greenhouse Controller scans.
- Saved profiles preserve score, support/enclosure scoring, seal/roof/floor status, active and idle Pollinator Dock counts, crop target count, controller position, and scan time.
- Crop growth, Hydroponic Tray growth and harvests, Ecology Scanner restoration pulses, Terminal status, and greenhouse diagnostics now use greenhouse context instead of a raw live score.
- Saved zones are checked against the current controller structure before they grant safety, so removed or degraded structures cannot keep over-crediting an old scan.
- Poor established zones apply soft penalties only inside that saved zone:
  - Safe zones keep existing greenhouse growth/yield/restoration behavior.
  - Strained zones apply a small growth and seed-safety penalty.
  - Unsafe zones apply a larger growth penalty, lower effective seed safety, and slightly reduced restoration gain.
  - Unregistered outdoor growth keeps the existing early-route behavior.

## Diagnostics

- Greenhouse Controller feedback now reports zone quality plus enclosure, glass, filter, and dock details.
- Pollinator Dock feedback reports whether it is serving crop/tray targets and shows the current zone quality.
- Ecology Scanner and FIELD > Reclamation reports now include greenhouse zone quality and clearer next actions.
- Terminal greenhouse mission detail now calls out saved zone state and the next missing requirement.

## Validation Targets

- `.\gradlew.bat :echoagriculturereclamation:build --warning-mode all`
- `.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`

## Compatibility Notes

- No Pollinator Drone entity is added in 0.2.0.
- Greenhouses remain soft systems rather than strict pass/fail multiblocks.
- Existing worlds remain readable because the old `greenhouseSafety` saved-data field is preserved and the new `greenhouseZones` field is optional.
