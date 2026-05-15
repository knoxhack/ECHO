# ECHO Mod Stack

Ashfall is the modpack. ECHO is the first-party ecosystem that powers it. `ECHO: Ashfall Protocol` is the main campaign addon, built as the root `echoashfallprotocol` artifact.

The ECHO stack is a post-Gridfall survival saga for Minecraft 26.1.2 on NeoForge. The simplified public stack can be described as Core, Terminal, Ashfall Protocol, Blockworks, Orbital, Agriculture, Stationfall, Nexus, Industrial, and Blackbox. The build-truth Gradle `all` stack also includes NetCore, RuntimeGuard, ThemeCore, PlayerCore, MissionCore, DataCore, WorldCore, SignalOS, SignalOS Example, RenderCore, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

Compatibility history token for docs validation: `1.2.0`.

ECHO: Ashfall Protocol starts in a compact armored drop pod with ECHO-7, a damaged emergency operator that is useful before it is comforting. The opening is now a `16x9x16` Blockworks-heavy drop pod with a clear spawn bay, emergency bunk, ECHO control core, Echo crates/cache, ramp, struts, side windows, roof beacons, and discoverable first-night supplies.

The world is not just hostile. It reacts. Radiation mutates after sustained severe exposure, dirty water is an emergency fallback, toxic air drains filters only in hazard pockets, machines need FE power and maintenance, factions remember what you do, and the final Nexus choice now resolves through a full Restore, Destroy, or Control finale.

## Current Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.2.0` | Shared ECHO service registry, profile ledger, diagnostics, hazards, route records, factions, rewards, terminal placement, and Nexus campaign mirrors. |
| `echonetcore` | `1.2.0` | Shared packet registration, sync, action validation, and debug network contracts. |
| `echoruntimeguard` | `1.2.0` | Shared TPS/FPS pressure monitoring, runtime budgets, smart tick hints, and performance diagnostics. |
| `echothemecore` | `1.2.0` | Shared visual/theme/UI skin service for ECHO modules and vanilla surfaces. |
| `echoplayercore` | `1.2.0` | Player utility commands, homes, back, spawn, random teleport, and travel QoL. |
| `echomissioncore` | `1.2.0` | Shared mission, objective, progression, reward, and Terminal feed engine. |
| `echodatacore` | `1.2.0` | Shared persistent player, world, and team progression data. |
| `echoworldcore` | `1.2.0` | Shared world regions, markers, hazards, discoveries, and world event contracts. |
| `echoterminal` | `1.2.0` | Common terminal shell with Command Deck, What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, Baseline, and Addons surfaces. |
| `signalos` | `1.2.0` | Reusable terminal/content framework for chapters, missions, archives, rewards, diagnostics, JSON content, and the soft KubeJS bridge. |
| `signalosexample` | `1.2.0` | Example-only SignalOS addon for Java, JSON, diagnostics, rewards, archives, and KubeJS-friendly integration patterns. |
| `echorendercore` | `1.2.0` | Shared advanced visual-state, animation-profile, particle-profile, preview, composition, and renderer helper layer for polished ECHO/Ashfall assets. |
| `echoashfallprotocol` | `1.2.0` | Main Earth survival campaign: drop pod start, wasteland systems, factions/NPCs, guardians, Prime Relays, Nexus warfront, and finale. |
| `echoorbitalremnants` | `1.2.0` | Post-Nexus orbital continuation: launch chain, route worlds, ECHO-0, orbital factions, route records, diagnostics, and support caches. |
| `echoagriculturereclamation` | `1.2.0` | Field ecology recovery chapter for recovered seed capsules, contaminated soils, hydroponics, greenhouse zones, pollinator drones, gene stabilization, and chunk-local restoration. |
| `echostationfall` | `1.2.0` | Station ECHO horror chapter with station power, panic pressure, crew logs, station route state, and terminal handoff. |
| `echonexusprotocol` | `1.2.0` | Nexus corruption chapter for charge control, smarter field-map risk planning, stabilized fields, memory recovery, matter rewriting, and Core-state escalation. |
| `echoindustrialnexus` | `1.2.0` | Industrial automation chapter for Thermal Flux, rusted machines, automated filters, salvage processing, MultiblockCore factory ops, and factory command. |
| `echologisticsnetwork` | `1.2.0` | Supply crates, labels, loadouts, drone delivery docks, remote requests, faction depots, external endpoints, courier persistence, and operations dashboards. |
| `echoconvoyprotocol` | `1.2.0` | Ruined-Earth vehicles, multiblock depots, cargo/fuel logistics, deterministic Field Ops, HoloMap routes, recovery signals, and convoy operations. |
| `echoholomap` | `1.2.0` | Terminal-integrated command map for regions, routes, hazards, scans, missions, and addon markers. |
| `echoindex` | `1.2.0` | Shared item, recipe, usage, and archive index for Terminal-facing reference surfaces. |
| `echoarmory` | `1.2.0` | Modular weapons, armor, modules, energy recharge, faction locks, Terminal hooks, and Logistics hooks. |
| `echolens` | `1.2.0` | Smart scanner HUD with local inspection, server-assisted Deep Scan, inventory privacy, and addon context. |
| `echomultiblockcore` | `1.2.0` | Shared data-driven multiblock validation, runtime, robotics, workcell, and scanner/map/terminal contracts. |
| `echoblockworks` | `1.2.0` | First-party decorative, structural, themed block families, palette kits, and rare showcase ruin palettes for ECHO builds. |
| `echoblackboxprotocol` | `1.2.0` | Late-game Blackbox finale with memory fragments, archive dungeons, hostile recordings, boss proofs, and final outcome directives. |

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

1. Start a fresh world and secure the compact drop pod. Spawn is at template `(8, 3, 10)` with a clear bay, and the emergency bunk is at `(4, 3, 7)` / `(4, 3, 8)`.
2. Open visible Echo crates/cache for the scanner, water, filters, rations, meds, bottles, torches, basic weapon support, campfire support, and salvage.
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

- `echocore` `1.2.0` - required shared API and service mod.
- `echonetcore` `1.2.0` - shared packet bridge and network diagnostics.
- `echoruntimeguard` `1.2.0` - shared runtime budgets, lag diagnostics, and smart tick hints.
- `echothemecore` `1.2.0` - shared visual/theme/UI skin service.
- `echoplayercore` `1.2.0` - player utility commands, homes, back, spawn, random teleport, and travel QoL.
- `echomissioncore` `1.2.0` - shared mission service and objective registry.
- `echodatacore` `1.2.0` - shared persistent player/world/team data service.
- `echoworldcore` `1.2.0` - shared world region, hazard, and marker service.
- `echoterminal` `1.2.0` - shared ECHO Terminal addon.
- `signalos` `1.2.0` - reusable terminal/content framework.
- `signalosexample` `1.2.0` - example-only SignalOS addon.
- `echorendercore` `1.2.0` - shared advanced visual-state, preview, composition, and renderer profile support.
- `echoashfallprotocol` `1.2.0` - main campaign addon for the Ashfall modpack.
- `echoorbitalremnants` `1.2.0` - post-Nexus orbital expansion.
- `echoagriculturereclamation` `1.2.0` - field agriculture, pollinator drone, and ecology recovery chapter.
- `echostationfall` `1.2.0` - Station ECHO horror chapter.
- `echonexusprotocol` `1.2.0` - Nexus corruption chapter.
- `echoindustrialnexus` `1.2.0` - industrial automation chapter.
- `echologisticsnetwork` `1.2.0` - logistics, storage, loadouts, external endpoints, delivery, and operations dashboard chapter.
- `echoconvoyprotocol` `1.2.0` - ruined-Earth vehicles, multiblock depots, cargo/fuel logistics, Field Ops lifecycles, and HoloMap convoy routes chapter.
- `echoholomap` `1.2.0` - Terminal-integrated command map and marker registry.
- `echoindex` `1.2.0` - shared item, recipe, usage, and archive index.
- `echoarmory` `1.2.0` - combat, gear, modules, and loadout support chapter.
- `echolens` `1.2.0` - smart scanner HUD with server-assisted Deep Scan and addon-context inspection layer.
- `echomultiblockcore` `1.2.0` - shared multiblock validation, runtime, and robotics framework.
- `echoblockworks` `1.2.0` - themed block family, decoration catalog, palette kit, and rare showcase site palette module.
- `echoblackboxprotocol` `1.2.0` - late-game Blackbox finale.

Build all release jars from the workspace root:

```powershell
.\gradlew.bat build -PechoAddonSet=all
```

Run the full release verification gate:

```powershell
.\gradlew.bat verifyEchoRelease --warning-mode all
```

Copy the verified jars into a local CurseForge or launcher profile only when doing local modpack QA:

```powershell
.\gradlew.bat -PechoModpackModsDir="C:/path/to/Ashfall/mods" copyEchoJarsToModpack verifyEchoModpackProfile
```

`verifyEchoRelease` is the pure repo gate and does not require a local modpack profile. `copyEchoJarsToModpack`, `checkEchoModJarSet`, and `verifyEchoModpackProfile` require `-PechoModpackModsDir="C:/path/to/Ashfall/mods"`; no historical default path is treated as the Ashfall target.

Release artifacts:

- `build/libs/echoashfallprotocol-1.2.0.jar`
- `core/echocore/build/libs/echocore-1.2.0.jar`
- `addons/echonetcore/build/libs/echonetcore-1.2.0.jar`
- `addons/echoruntimeguard/build/libs/echoruntimeguard-1.2.0.jar`
- `addons/echomissioncore/build/libs/echomissioncore-1.2.0.jar`
- `addons/echodatacore/build/libs/echodatacore-1.2.0.jar`
- `addons/echoworldcore/build/libs/echoworldcore-1.2.0.jar`
- `addons/echoterminal/build/libs/echoterminal-1.2.0.jar`
- `addons/echosignalos/build/libs/signalos-1.2.0.jar`
- `addons/signalosexample/build/libs/signalosexample-1.2.0.jar`
- `addons/echorendercore/build/libs/echorendercore-1.2.0.jar`
- `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.2.0.jar`
- `addons/echoagriculturereclamation/build/libs/echoagriculturereclamation-1.2.0.jar`
- `addons/echostationfall/build/libs/echostationfall-1.2.0.jar`
- `addons/echonexusprotocol/build/libs/echonexusprotocol-1.2.0.jar`
- `addons/echoindustrialnexus/build/libs/echoindustrialnexus-1.2.0.jar`
- `addons/echologisticsnetwork/build/libs/echologisticsnetwork-1.2.0.jar`
- `addons/echoconvoyprotocol/build/libs/echoconvoyprotocol-1.2.0.jar`
- `addons/echoholomap/build/libs/echoholomap-1.2.0.jar`
- `addons/echoindex/build/libs/echoindex-1.2.0.jar`
- `addons/echoarmory/build/libs/echoarmory-1.2.0.jar`
- `addons/echolens/build/libs/echolens-1.2.0.jar`
- `addons/echomultiblockcore/build/libs/echomultiblockcore-1.2.0.jar`
- `addons/echoblockworks/build/libs/echoblockworks-1.2.0.jar`
- `addons/echoblackboxprotocol/build/libs/echoblackboxprotocol-1.2.0.jar`

Verification commands:

```powershell
python -m pip install -r tools\requirements.txt
python tools\validate_resources.py --addon-set all
python tools\validate_gameplay_data.py
.\gradlew.bat build -PechoAddonSet=all
```

Expected verification result: every required build and GameTest task reports clean completion across Core, NetCore, MissionCore, DataCore, WorldCore, Terminal, SignalOS, SignalOS Example, ECHO: Ashfall Protocol, Orbital, Agriculture Reclamation, Stationfall, Nexus, Industrial, Logistics, Convoy, HoloMap, Armory, and Blackbox. Run `verifyEchoModpackProfile` only after the local modpack destination is configured for Ashfall.
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
- `docs/releases/echo_stack_1.2.0_rendercore_terminal_minor.md` - 1.2.0 stack release notes for Terminal route placement and RenderCore V18 screen chrome.
- `docs/releases/echo_stack_1.2.0_orbital_terminal_polish.md` - 1.2.0 stack release notes for Orbital, Terminal route placement, and Agriculture optional route polish.
- `docs/releases/agriculture_reclamation_1.2.0.md` - Agriculture Reclamation 1.2.0 Terminal route-polish notes and migration statement.
- `docs/releases/echo_stack_1.1.3_patch_alignment.md` - 1.1.3 patch version alignment notes and verification.
- `docs/releases/echo_stack_1.1.3_patch_hardening.md` - 1.1.3 patch hardening notes and verification.
- `docs/releases/echo_stack_1.1.2_release_gate_hardening.md` - 1.1.2 deterministic release-gate hardening notes and verification.
- `docs/releases/echo_stack_1.1.1_cyberglass_default.md` - 1.1.1 CyberGlass default certification release notes and focused verification.
- `docs/releases/echo_stack_1.1.0_index_recipe_coverage.md` - 1.1.0 Index recipe coverage release notes and focused verification.
- `docs/releases/ashfall_1.0.0_smoke_test.md` - Ashfall first-world release smoke checklist and gate log.

## Version Contract

Releases follow an explicit version contract so tags, module versions, and release names stay aligned:

- Git tags must use `v<major>.<minor>.<patch>` with optional prerelease suffixes (for example `v1.0.1-beta.1`).
- The numeric part of the tag must match each module's release version for that cut.
- GitHub release names should reuse the exact tag value for traceability.

See `docs/release_process.md` for the full release workflow and manifest checks.

## 1.2.0 Full Stack Smoke Checklist

Start a fresh world, keep the default ECHO: Ashfall Protocol worldgen, and test the first night without using vanilla forests as your main route. The intended opening is debris, ruined vegetation, dead/charred trees, pod salvage, and ECHO-7 mission guidance.

What to test first:

- First 10 minutes: compact pod spawn, visible lockers, sticks/fiber, ruined planks, first weapon, shelter, clean water, emergency dirty water, and ECHO terminal guidance.
- First machine loop: Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
- Resource recovery: JEI custom machine categories, Terminal Recipe Index coverage, recovered biome goods, healthy sapling rarity, and route-specific POI cache identity.
- Agriculture Reclamation: seed capsule recovery, mature-only crop drops, soil purifier no-op safety, hydroponic persistence, gene stabilization, greenhouse zone scans, Pollinator Drone service, and ecology scanner restoration pressure.
- Scanner loop: Portable Signal Scanner reports the actual site, hazard profile, prep kit, reward track, distance, direction, and field-log status.
- Terminal loop: What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, and Addons should agree with the owning chapter state.
- Factions and drones: Scout Drone fallback, ECHO companion repair/modes, faction NPC dialogue, contracts, standing, trader rewards, raids, and intel reports.
- Nexus path: buried guardian nodes, Prime Relays, Core countermeasure siege, Power Nodes, final choice, path objectives, Archives arena entry/return, Warden defeat, final epilogue, Orbital unlock, and Stationfall/Nexus/Industrial/Blackbox chapter entry visibility.

Known 1.2.0 watchpoints:

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

## Newly Active Service Addons (Audit Pass)

The explicit public stack also includes these active service addons introduced after the main 1.2.0 docs table: `echopowergrid` `1.2.0`, `echosoundcore` `1.2.0`, `echotutorialcore` `1.2.0`, `echorelictech` `1.2.0`, and `echoweathercore` `1.2.0`. They are included in `settings.gradle` and are tracked in `docs/reports/ECHO_ECOSYSTEM_AUDIT.md` with honest partial/blocked notes.
