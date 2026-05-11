# ECHO Mod Stack

Ashfall is the modpack. ECHO is the first-party ecosystem that powers it. `ECHO: Ashfall Protocol` is the main campaign addon, built as the root `echoashfallprotocol` artifact.

The ECHO stack is a post-Gridfall survival saga for Minecraft 26.1.2 on NeoForge. The simplified public stack can be described as Core, Terminal, Ashfall Protocol, Orbital, Agriculture, Stationfall, Nexus, Industrial, and Blackbox. The build-truth Gradle `all` stack also includes NetCore, MissionCore, DataCore, WorldCore, SignalOS, SignalOS Example, RenderCore, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

ECHO: Ashfall Protocol starts in a compact armored drop pod with ECHO-7, a damaged emergency operator that is useful before it is comforting. The opening is no longer a wide crash capsule: the current `20x10x20` pod is a small starter outpost with a clear spawn bay, bed nook, ECHO control core, labeled lockers, ramp, struts, side windows, roof beacons, and discoverable first-night supplies.

The world is not just hostile. It reacts. Radiation mutates after sustained severe exposure, dirty water is an emergency fallback, toxic air drains filters only in hazard pockets, machines need FE power and maintenance, factions remember what you do, and the final Nexus choice now resolves through a full Restore, Destroy, or Control finale.

## Current Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.1.0` | Shared ECHO service registry, profile ledger, diagnostics, hazards, route records, factions, rewards, terminal placement, and Nexus campaign mirrors. |
| `echonetcore` | `0.1.0` | Shared packet registration, sync, action validation, and debug network contracts. |
| `echomissioncore` | `0.1.0` | Shared mission, objective, progression, reward, and Terminal feed engine. |
| `echodatacore` | `1.0.0` | Shared persistent player, world, and team progression data. |
| `echoworldcore` | `0.1.0` | Shared world regions, markers, hazards, discoveries, and world event contracts. |
| `echoterminal` | `1.1.0` | Common terminal shell with Command Deck, What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, Baseline, and Addons surfaces. |
| `signalos` | `0.1.0` | Reusable terminal/content framework for chapters, missions, archives, rewards, diagnostics, JSON content, and the soft KubeJS bridge. |
| `signalosexample` | `0.1.0` | Example-only SignalOS addon for Java, JSON, diagnostics, rewards, archives, and KubeJS-friendly integration patterns. |
| `echorendercore` | `0.5.0` | Shared advanced visual-state, animation-profile, particle-profile, and renderer helper layer for polished ECHO/Ashfall assets. |
| `echoashfallprotocol` | `1.3.0` | Main Earth survival campaign: drop pod start, wasteland systems, factions/NPCs, guardians, Prime Relays, Nexus warfront, and finale. |
| `echoorbitalremnants` | `1.5.0` | Post-Nexus orbital continuation: launch chain, route worlds, ECHO-0, orbital factions, route records, diagnostics, and support caches. |
| `echoagriculturereclamation` | `0.1.1` | Field ecology recovery chapter for recovered seed capsules, contaminated soils, hydroponics, greenhouses, gene stabilization, and chunk-local restoration. |
| `echostationfall` | `1.1.0` | Station ECHO horror chapter with station power, panic pressure, crew logs, station route state, and terminal handoff. |
| `echonexusprotocol` | `1.0.0` | Nexus corruption chapter for charge control, stabilized fields, memory recovery, matter rewriting, and Core-state escalation. |
| `echoindustrialnexus` | `0.1.0` | Industrial automation chapter for Thermal Flux, rusted machines, automated filters, salvage processing, and factory recovery. |
| `echologisticsnetwork` | `0.1.0` | Supply crates, labels, loadouts, drone delivery docks, remote requests, faction depots, and courier persistence. |
| `echoconvoyprotocol` | `0.1.0` | Ruined-Earth vehicles, fuel, cargo, roadside contracts, checkpoint gates, and Convoy terminal routes. |
| `echoholomap` | `0.1.0` | Terminal-integrated command map for regions, routes, hazards, scans, missions, and addon markers. |
| `echoindex` | `0.1.0` | Shared item, recipe, usage, and archive index for Terminal-facing reference surfaces. |
| `echoarmory` | `0.1.0` | Modular weapons, armor, modules, energy recharge, faction locks, Terminal hooks, and Logistics hooks. |
| `echolens` | `0.1.0` | Smart scanner HUD for blocks, entities, fluids, machines, inventory privacy, and addon context. |
| `echoblackboxprotocol` | `1.0.0` | Late-game Blackbox finale with memory fragments, archive dungeons, hostile recordings, boss proofs, and final outcome directives. |

## Core Features

- **Expedition survival:** slow hydration loss, emergency dirty water, zone-based toxic air, radiation, mutations, filter cartridges, RadAway, gas masks, scrubbers, and field medicine.
- **Scrap economy:** debris salvage, substrate harvesting, hand recycling, thermal burning, ore grinding, isotope refining, and machine chains.
- **Recovered biome goods:** ruined worlds still provide paper, dyes, clay, flowers, mushrooms, cactus, sugar cane, bamboo, ocean salvage, ice, animal goods, and new Agriculture Reclamation crops through recipes, POI caches, seed vaults, hydroponics, and faction imports.
- **Power restoration:** micro generators, battery banks, power nodes, thermal arrays, deep-core mining, and the Nexus grid.
- **Mission progression:** ECHO-7 keeps the required route practical through podfall survival, biological adaptation, geological extraction, buried guardian nodes, Prime Relay warfront prep, grid restoration, and the irreversible Nexus choice.
- **Signal Leads:** optional recon records explain crash telemetry, region identity, factions, drone memory, guardians, Nexus context, and ECHO-0 quarantine without blocking the main route or spoiling final outcomes.
- **Factions and intel:** Radwarden Compact, Crashbreak Salvage, and Sporebound Sanctum are the three Echo Core Ashfall factions for reputation, contacts, contracts, POI affinity, patrol pressure, services, dossiers, and drone reconnaissance.
- **World exploration:** scanner-led POI routes across wasteland biomes, toxic swamps, ruined cities, radiation zones, crash scars, cryogenic ruins, Nexus scars, faction hubs, procedural landmarks, and underground guardian arenas with visible surface entrances.
- **ECHO terminal:** Command Deck, What Now, Mission Graph, Protocol Roadmap, Signal Leads, Route Map, POI Atlas, Route Records, Recipe Index, Field Archive, Survival Index, Faction Atlas, Baseline, Vitals, Companion Link, Reward Inbox, Nexus Core, and ORBITAL channels collect lore, objectives, telemetry, recipes, faction reports, route state, and optional expansion status.

## Addon Chapter Chain

The release stack ships the full chapter chain. ECHO: Ashfall Protocol remains the entry campaign; ECHO: Orbital Remnants opens after any Ashfall Nexus choice; Agriculture Reclamation can run alongside beta field recovery; Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol extend the shared ECHO state through their own route, machine, archive, and terminal surfaces.

Addons communicate through `echocore` and `echoterminal` instead of reaching into another chapter's save data. The shared terminal navigation profile API is public addon-facing surface: `TerminalNavigationProfile`, `TerminalNavigationProfiles`, and `TerminalNavigationSection`; the shell organizes pages into Command, Progress, Intel, and System. Recipe-aware addons can also publish process data through the Terminal recipe provider API.

## Quick Start

1. Start a fresh world and secure the compact drop pod. Spawn is at template `(9, 3, 12)` with a clear bay, and the bed is at `(5, 3, 10)` / `(5, 3, 11)`.
2. Open visible pod lockers for the scanner, water, filters, rations, meds, bottles, torches, basic weapon support, campfire/chest support, and salvage.
3. Craft an early weapon, use the ramp/door to scout safely, shelter before night, and follow ECHO-7 mission prompts.
4. Build the Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
5. Use the Portable Signal Scanner to find a POI, read its route/hazard/prep report, then open Route Map -> POI Atlas to compare that scanner profile against the concrete template signals it can represent.
6. Clear the eight active biome guardians by scanning surface entrances and descending into their buried Gridfall nodes; each guardian reports back into Radwarden, Crashbreak, or Sporebound faction memory.
7. Wake the Nexus campaign, scan six Prime Relays, resolve three relays, survive the Core countermeasure siege, then restore enough grid infrastructure to reach the Nexus Core and choose Restore, Destroy, or Control.
8. Use FIELD > Reclamation to recover seeds and stabilize a small food route, then use ORBITAL / Orbital Command to begin the post-Nexus quarantine expansion and surface the Stationfall, Nexus, Industrial, and Blackbox chapter entries from the shared terminal.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+

Build-truth Gradle `all` stack:

- `echocore` `1.1.0` - required shared API and service mod.
- `echonetcore` `0.1.0` - shared packet bridge and network diagnostics.
- `echomissioncore` `0.1.0` - shared mission service and objective registry.
- `echodatacore` `1.0.0` - shared persistent player/world/team data service.
- `echoworldcore` `0.1.0` - shared world region, hazard, and marker service.
- `echoterminal` `1.1.0` - shared ECHO Terminal addon.
- `signalos` `0.1.0` - reusable terminal/content framework.
- `signalosexample` `0.1.0` - example-only SignalOS addon.
- `echorendercore` `0.5.0` - shared advanced visual-state and renderer profile support.
- `echoashfallprotocol` `1.3.0` - main campaign addon for the Ashfall modpack.
- `echoorbitalremnants` `1.5.0` - post-Nexus orbital expansion.
- `echoagriculturereclamation` `0.1.1` - field agriculture and ecology recovery chapter.
- `echostationfall` `1.1.0` - Station ECHO horror chapter.
- `echonexusprotocol` `1.0.0` - Nexus corruption chapter.
- `echoindustrialnexus` `0.1.0` - industrial automation chapter.
- `echologisticsnetwork` `0.1.0` - logistics, storage, loadouts, and delivery chapter.
- `echoconvoyprotocol` `0.1.0` - ruined-Earth vehicles and cargo routes chapter.
- `echoholomap` `0.1.0` - Terminal-integrated command map and marker registry.
- `echoindex` `0.1.0` - shared item, recipe, usage, and archive index.
- `echoarmory` `0.1.0` - combat, gear, modules, and loadout support chapter.
- `echolens` `0.1.0` - smart scanner HUD and addon-context inspection layer.
- `echoblackboxprotocol` `1.0.0` - late-game Blackbox finale.

Build all release jars from the workspace root:

```powershell
.\gradlew.bat build -PechoAddonSet=all
```

Run the full release verification gate:

```powershell
.\gradlew.bat verifyEchoRelease --warning-mode all
```

Copy the verified jars into the local CurseForge profile:

```powershell
.\gradlew.bat copyEchoJarsToModpack
```

Set `-PechoModpackModsDir="C:/path/to/Ashfall/mods"` before copying jars into a local pack profile. Do not treat any historical default path as the Ashfall target unless it has been explicitly configured for that workspace.

Release artifacts:

- `build/libs/echoashfallprotocol-1.3.0.jar`
- `core/echocore/build/libs/echocore-1.1.0.jar`
- `addons/echonetcore/build/libs/echonetcore-0.1.0.jar`
- `addons/echomissioncore/build/libs/echomissioncore-0.1.0.jar`
- `addons/echodatacore/build/libs/echodatacore-1.0.0.jar`
- `addons/echoworldcore/build/libs/echoworldcore-0.1.0.jar`
- `addons/echoterminal/build/libs/echoterminal-1.1.0.jar`
- `addons/echosignalos/build/libs/signalos-0.1.0.jar`
- `addons/signalosexample/build/libs/signalosexample-0.1.0.jar`
- `addons/echorendercore/build/libs/echorendercore-0.5.0.jar`
- `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.5.0.jar`
- `addons/echoagriculturereclamation/build/libs/echoagriculturereclamation-0.1.1.jar`
- `addons/echostationfall/build/libs/echostationfall-1.1.0.jar`
- `addons/echonexusprotocol/build/libs/echonexusprotocol-1.0.0.jar`
- `addons/echoindustrialnexus/build/libs/echoindustrialnexus-0.1.0.jar`
- `addons/echologisticsnetwork/build/libs/echologisticsnetwork-0.1.0.jar`
- `addons/echoconvoyprotocol/build/libs/echoconvoyprotocol-0.1.0.jar`
- `addons/echoholomap/build/libs/echoholomap-0.1.0.jar`
- `addons/echoindex/build/libs/echoindex-0.1.0.jar`
- `addons/echoarmory/build/libs/echoarmory-0.1.0.jar`
- `addons/echolens/build/libs/echolens-0.1.0.jar`
- `addons/echoblackboxprotocol/build/libs/echoblackboxprotocol-1.0.0.jar`

Verification commands:

```powershell
python -m pip install -r tools\requirements.txt
python tools\validate_resources.py --addon-set all
python tools\validate_gameplay_data.py
.\gradlew.bat build -PechoAddonSet=all
```

Expected verification result: every required build and GameTest task reports clean completion across Core, NetCore, MissionCore, DataCore, WorldCore, Terminal, SignalOS, SignalOS Example, ECHO: Ashfall Protocol, Orbital, Agriculture Reclamation, Stationfall, Nexus, Industrial, Logistics, Convoy, HoloMap, Armory, and Blackbox. Run `verifyEchoRelease` only after the local modpack destination is configured for Ashfall.
`validate_resources.py` also runs the release-polish checks for mojibake, stale terminal/drone references, plural structure resource paths, placeholder markers, and uppercase real resource namespaces.

## ECHO Core Integration Contract

Addons should communicate through `echocore` instead of reaching directly into another chapter's save data. Current shared services cover pack mode/profile state, progress ledgers, diagnostics, hazard telemetry, route records, faction definitions/profiles/standing/contracts/actions, POI affinity, NPC dialogue roles, recovery hooks, terminal placement, terminal reward storage, intel mirroring, and Nexus path/campaign status. Providers are expected to be tolerant: duplicate IDs are ignored, failed providers are logged, and the owning mod remains responsible for validating actions and rewards server-side.

Terminal addons should register navigation through the public `echoterminal` profile types rather than special-casing the screen. Explicit `TerminalNavigationProfile` registration is the chapter ownership contract; older chrome group fallbacks exist only as compatibility. Recipe-aware addons should register `TerminalRecipeProvider` implementations with `TerminalRecipeRegistry`, using `TerminalRecipeCategory`, `TerminalRecipeEntry`, `TerminalRecipeSlot`, `TerminalRecipeNote`, and `TerminalRecipeSnapshot` so the shared Recipe Index can search outputs, uses, machine slots, catalysts, info notes, and locked schematic hints. A chapter owns its actions, rewards, recipe authority, and persistence; Terminal owns presentation and routing.

## Release Operations

`tools/echo-release-terminal` is a private local release-ops dashboard. It is useful for QA state and release drafting, but it is not part of the published mod artifact set. Keep it buildable with `npm.cmd run build` before public release.

## Documentation

- `MODPACK_OVERVIEW.md` - full systems, mechanics, factions, progression, and lore overview.
- `LORE_BIBLE.md` - shared tactical-eerie canon and writing rules for missions, archives, docs, and addon chapters.
- `GETTING_STARTED.md` - player-facing walkthrough from drop pod to Nexus choice.
- `PROCEDURAL_STRUCTURES.md` - POI and structure generation reference.
- `docs/release_process.md` - release checklist, version contract, and CI release artifact expectations.
- `docs/releases/ashfall_1.3.0_smoke_test.md` - Ashfall first-world release smoke checklist and gate log.

## Version Contract

Releases follow an explicit version contract so tags, module versions, and release names stay aligned:

- Git tags must use `v<major>.<minor>.<patch>` with optional prerelease suffixes (for example `v1.3.1-beta.1`).
- The numeric part of the tag must match each module's release version for that cut.
- GitHub release names should reuse the exact tag value for traceability.

See `docs/release_process.md` for the full release workflow and manifest checks.

## 1.3.0 Full Stack Smoke Checklist

Start a fresh world, keep the default ECHO: Ashfall Protocol worldgen, and test the first night without using vanilla forests as your main route. The intended opening is debris, ruined vegetation, dead/charred trees, pod salvage, and ECHO-7 mission guidance.

What to test first:

- First 10 minutes: compact pod spawn, visible lockers, sticks/fiber, ruined planks, first weapon, shelter, clean water, emergency dirty water, and ECHO terminal guidance.
- First machine loop: Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
- Resource recovery: JEI custom machine categories, Terminal Recipe Index coverage, recovered biome goods, healthy sapling rarity, and route-specific POI cache identity.
- Agriculture Reclamation: seed capsule recovery, mature-only crop drops, soil purifier no-op safety, hydroponic persistence, gene stabilization, greenhouse safety scan, and ecology scanner restoration pressure.
- Scanner loop: Portable Signal Scanner reports the actual site, hazard profile, prep kit, reward track, distance, direction, and field-log status.
- Terminal loop: What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, and Addons should agree with the owning chapter state.
- Factions and drones: Scout Drone fallback, ECHO companion repair/modes, faction NPC dialogue, contracts, standing, trader rewards, raids, and intel reports.
- Nexus path: buried guardian nodes, Prime Relays, Core countermeasure siege, Power Nodes, final choice, path objectives, Archives arena entry/return, Warden defeat, final epilogue, Orbital unlock, and Stationfall/Nexus/Industrial/Blackbox chapter entry visibility.

Known 1.3.0 watchpoints:

- Old worlds may not contain the newest POI/resource distribution until new chunks generate, but old POI progress ids are normalized through compatibility aliases.
- Old worlds may lose legacy Ashfall-owned terminal blocks; the supported terminal block is now `echoterminal:echo_terminal`.
- The standalone drone menu path is intentionally not exposed; drone control is through the ECHO terminal and direct drone interaction.
- Some audio cues intentionally reuse vanilla sound events in this release.

Bug report format:

```text
Version / mod list:
World age and seed:
What you expected:
What happened:
Steps to reproduce:
Screenshots or crash report:
Coordinates / biome / POI:
```

## Compatibility

- **ECHO addon chapters:** Orbital Remnants, Agriculture Reclamation, Stationfall, Nexus Protocol, Industrial Nexus, Logistics Network, Convoy Protocol, Armory, and Blackbox Protocol are included in the full Gradle stack and surface through ECHO Core plus ECHO Terminal.
- **Multiplayer:** supported; Nexus decisions are shared through the world state.
- **Recipe viewers:** JEI support is optional and includes custom ECHO categories for hardcoded Ashfall machine/process recipes. ECHO Terminal also includes a provider-backed Recipe Index with searchable ECHO items, Recipes/Uses modes, category filters, item detail panes, process notes, and locked schematic hints. Normal crafting/smelting recipes still appear through vanilla recipe data.
