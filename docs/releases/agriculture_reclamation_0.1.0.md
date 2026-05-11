# ECHO: Agriculture Reclamation 0.1.0 Release

Release owner: Agriculture Reclamation
Mod id: `echoagriculturereclamation`
Artifact: `addons/echoagriculturereclamation/build/libs/echoagriculturereclamation-0.1.0.jar`
Target stack: ECHO beta and full release stacks

## Pull Request Draft

Title:

```text
Release ECHO: Agriculture Reclamation 0.1.0
```

Body:

```markdown
## Summary
- Adds ECHO: Agriculture Reclamation as a first-class optional addon chapter.
- Ships the ruined-world farming loop: recovered seed capsules, seed profiles, soil purification, hydroponic growth, gene stabilization, greenhouse safety, machine processing, and chunk-local restoration pressure.
- Wires FIELD > Reclamation into ECHO Terminal and publishes Agriculture route records, diagnostics, recovery cache support, and milestones through ECHO Core.
- Adds optional cross-addon compatibility for Ashfall ruined soils, Restoration Project-style soil ids, Nexus restore alignment, and ECHO faction-biased seed recovery.

## Player Route
1. Recover a Recovered Seed Capsule from ECHO ruin loot, or craft one from wheat seeds, bone meal, a glass bottle, and copper.
2. Identify a profiled Contaminated Seed through the Seed Vault Terminal or direct capsule use.
3. Plant it on dirt, grass, farmland, or compatible reclamation soil, or grow it in a Hydroponic Tray.
4. Harvest food or restoration crops.
5. Craft a Bio-Reactor with Soil Nutrient Mix and process any crop matter into Bio-Gel.
6. Craft and use the Gene Stabilizer with a contaminated seed plus Bio-Gel or Gene Sample.
7. Build greenhouse support and scan FIELD > Reclamation.
8. Mature restoration crops and scan ecology until local soil conversion pressure improves the chunk.

## Release Notes
- Restoration is block and chunk local only; the addon does not rewrite biome ids.
- Immature crops do not drop produce, preventing crop-break food bypasses.
- Generic seeds require a seed_profile data component before planting or hydroponic growth.
- Hydroponic trays preserve crop identity, contamination, stability, nutrient buffer, and growth state.
- Machine transactions reject invalid input without consuming catalysts or route resources.
- Pollinator Drone Dock is greenhouse support only in 0.1.0; no drone entity is spawned.
- Greenhouse safety is nearby-block scoring, not a strict enclosure simulation.

## Validation
- `.\gradlew.bat :echoagriculturereclamation:build --warning-mode all`
- `.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all`
- `.\gradlew.bat -PechoPythonExecutable="C:/Users/hacko/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe" -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all`
- `.\gradlew.bat -PechoPythonExecutable="C:/Users/hacko/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe" -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`

## GameTest Evidence
- Agriculture batch: 21 focused tests registered under `echoagriculturereclamation:agriculture_reclamation`.
- Latest focused Agriculture run should cover recipe-chain playability, Core recovery once-only behavior, and player progress NBT round trip.

## Compatibility Notes
- Required: `echocore`.
- Optional runtime integration: `echoterminal`, `echoashfallprotocol`, `echoorbitalremnants`, `echonexusprotocol`, `echoindustrialnexus`, Stationfall/Blackbox runtime context when present.
- The addon remains technically optional and communicates through registry ids, ModList checks, and ECHO Core services.
```

## Changelog

### Added

- New addon module `addons/echoagriculturereclamation`.
- Seven reclamation soil states: Dead, Contaminated, Irradiated, Toxic Mud, Purified, Stabilized, and Restored.
- Eleven crop routes: Ash Wheat, Hardroot, Glow Beans, Radleaf, Mutant Berries, Cryo Moss, Clean Corn, Medicinal Aloe, Filter Reed, Nexus Orchid, and Signal Fungus.
- Seed profile data component for crop id, contamination tier, and stability.
- Recovered Seed Capsule, Contaminated Seed, Stabilized Seed, Gene Sample, Soil Nutrient Mix, Purification Enzyme, and Bio-Gel.
- Hydroponic Tray, Seed Vault Terminal, Soil Purifier, Gene Stabilizer, Bio-Reactor, Greenhouse Controller, Pollinator Drone Dock, Spore Filter, Compost Recycler, Ecology Scanner, and Greenhouse Glass.
- Data-driven crop, soil, machine, and progression rule files under `data/echoagriculturereclamation/echoagriculturereclamation`.
- Global loot modifiers for seed capsules and gene samples across ruined-world recovery sources, packaged under NeoForge `loot_modifiers`.
- Standalone first-capsule crafting route using wheat seeds, bone meal, glass bottle, and copper.
- FIELD > Reclamation terminal page, report action, scan action, and six mission milestones.
- ECHO Core chapter, route records, diagnostics, recovery cache, and milestone recording.
- Ashfall/Restoration Project soil compatibility by registry id.
- Optional faction-biased seed recovery for Radwarden, Crashbreak, and Sporebound preference paths.
- Focused GameTests for seed recovery, standalone recipe-chain playability, soil conversion, hydroponics, greenhouse scoring, stabilization, Terminal metrics and one-time rewards, Core recovery once-only behavior, player/saved-data and block-entity persistence, loot modifier packaging, restoration pressure, Core records, cross-addon soil mapping, milestones, faction bias, and main loop regression.

### Changed

- Crop loot is mature-only, so immature crops cannot bypass survival progression.
- Food crop values are modest and survival-safe.
- Hydroponic growth, purifier pass size, and seed recovery stability are tuned for the ruined-world route.
- Machine and seed feedback now gives clear success, no-op, and invalid-input messages.
- Root release docs include Agriculture Reclamation smoke coverage and milestone handoff ids.

### Known Constraints

- Pollinator Drone Dock contributes greenhouse safety only.
- Greenhouse validation uses nearby support scoring rather than enclosure geometry.
- Medicine, fiber, industrial, and deeper crop-category outputs are reserved for future balance/content passes.
- Restoration improves local Agriculture/Ashfall-compatible blocks; vanilla biome restoration is not part of 0.1.0.

## Final Modpack Jar Staging Checklist

Use this checklist from a clean working tree after the PR branch is ready.

1. Build Agriculture directly.
   - Command: `.\gradlew.bat :echoagriculturereclamation:build --warning-mode all`
   - Expected jar: `addons/echoagriculturereclamation/build/libs/echoagriculturereclamation-0.1.0.jar`

2. Run Agriculture GameTests.
   - Command: `.\gradlew.bat :echoagriculturereclamation:runGameTestServer --warning-mode all`
   - Expected result: all required tests pass; Agriculture batch reports no failures.

3. Validate and build beta stack.
   - Command: `.\gradlew.bat -PechoPythonExecutable="C:/path/to/python.exe" -PechoAddonSet=beta validateEchoResources buildEchoWorkspace --warning-mode all`
   - Expected result: resource validation passed and `buildEchoWorkspace` successful.

4. Validate and build full stack.
   - Command: `.\gradlew.bat -PechoPythonExecutable="C:/path/to/python.exe" -PechoAddonSet=all validateEchoResources buildEchoWorkspace --warning-mode all`
   - Expected result: resource validation passed and `buildEchoWorkspace` successful.

5. Stage jars into the local modpack profile.
   - Command: `.\gradlew.bat -PechoAddonSet=all copyEchoJarsToModpack`
   - Expected Agriculture jar in profile: `echoagriculturereclamation-0.1.0.jar`
   - Expected rule: exactly one current jar per ECHO module; remove stale older Agriculture jars before launch if the copy task reports duplicates.

6. Launch the modpack profile.
   - Confirm the mod list shows `ECHO: Agriculture Reclamation 0.1.0`.
   - Confirm `FIELD > Reclamation` appears when ECHO Terminal is installed.
   - Confirm a `Recovered Seed Capsule` can be crafted or found through ECHO ruin loot, then becomes a profiled seed.
   - Confirm a Hydroponic Tray can accept that seed and preserve status after save/reload.

7. Run final manual smoke.
   - Purify one local soil patch.
   - Grow or tray-grow one crop to mature output.
   - Process crop matter through Bio-Reactor or Compost Recycler.
   - Stabilize one contaminated seed.
   - Scan greenhouse safety and ecology.
   - Confirm restoration score changes without biome rewrites.

8. Preserve release evidence.
   - Attach Gradle command output or CI links to the PR.
   - Include the Agriculture GameTest summary.
   - Include a screenshot or short note for FIELD > Reclamation visibility if doing a public release post.
