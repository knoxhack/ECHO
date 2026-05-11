# ECHO: Agriculture Reclamation

Agriculture Reclamation is the ECHO field recovery chapter for ruined-world farming. It is a standalone NeoForge addon with mod id `echoagriculturereclamation`, package `com.knoxhack.echoagriculturereclamation`, and version `0.1.1`.

## Production Status

- Build and resources are wired into the beta and full ECHO stacks.
- The player route is usable in survival: recover a profiled seed, purify soil or use a Hydroponic Tray, grow and harvest crops, produce Bio-Gel or nutrient mix, stabilize genes, scan greenhouse safety, and raise chunk-local restoration pressure.
- Restoration stays local to blocks and chunks. It does not rewrite biome ids or restore vanilla ecology for free.
- Terminal and Core integration publish FIELD > Reclamation metrics, route records, diagnostics, recovery cache support, and six route milestones.
- Cross-addon compatibility is optional and registry-id based for Ashfall ruined soils, Restoration Project-style soils, Nexus restore alignment, and ECHO faction preferences.

## Player Smoke Route

1. Recover a `Recovered Seed Capsule` from ECHO ruin loot, or craft one from wheat seeds, bone meal, a glass bottle, and copper.
2. Use the `Seed Vault Terminal` or direct capsule use to get a profiled `Contaminated Seed`.
3. Plant the profiled seed on dirt, grass, farmland, or compatible reclamation soil, or insert it into a `Hydroponic Tray`.
4. Grow and harvest at least one crop output.
5. Craft a `Bio-Reactor` with `Soil Nutrient Mix`, then convert any crop matter into `Bio-Gel`.
6. Craft and use `Gene Stabilizer` with a contaminated seed plus `Gene Sample` or `Bio-Gel`.
7. Build a sealed greenhouse with glass, filters, dock support, trays, and controller scan.
8. Mature restoration crops and scan ecology until local soil conversion pressure is visible.

## Crop Utility Notes

- `Medicinal Aloe` feeds the Bio-Reactor for Bio-Gel and can award Ashfall bandage output when that addon is loaded.
- `Signal Fungus` is a stronger Bio-Reactor input and also composts into extra nutrient mix.
- `Cryo Moss` converts to Bio-Gel plus Purification Enzyme in the Bio-Reactor or extra nutrient mix in the Compost Recycler.
- `Filter Reed` composts into extra nutrient mix, can award Ashfall plant fiber when present, and crafts into paper for Spore Filter recovery.
- `Nexus Orchid` converts to Bio-Gel plus Gene Sample and can award Nexus gel when Nexus Protocol is loaded.

## Validation

Run from the workspace root:

```powershell
.\gradlew.bat :echoagriculturereclamation:build --warning-mode all
.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all
.\gradlew.bat -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all
.\gradlew.bat -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all
```

If Python is not on `PATH`, pass `-PechoPythonExecutable="C:/path/to/python.exe"` to the Gradle validation commands.

## Release Notes

- The pollinator dock contributes greenhouse safety when it can service nearby crops or Hydroponic Trays; no drone entity is spawned.
- Greenhouse safety is enclosure-aware: open support helps, but a sealed Greenhouse Glass shell with overhead glass is required for full safe-envelope rating.
- Crop, soil, machine, and progression rules live under `data/echoagriculturereclamation/echoagriculturereclamation`.
- Global seed and gene recovery injections live under NeoForge `data/echoagriculturereclamation/loot_modifiers`.
- Mature crop loot is conservative: immature crops do not drop produce.
- Generic seed items must carry the `seed_profile` data component before planting or tray growth.
- 0.1.1 development adds modest utility outputs for harvested non-food crops without changing Terminal, milestone, or biome-restoration behavior.
- Post-0.1.1 greenhouse polish adds sealed-envelope scoring and clearer Pollinator Dock diagnostics without adding new entities.

The 0.1.0 release PR draft, changelog, and final jar staging checklist live in `docs/releases/agriculture_reclamation_0.1.0.md`; the 0.1.1 crop utility draft notes live in `docs/releases/agriculture_reclamation_0.1.1.md`.
