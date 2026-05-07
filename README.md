# ECHO Mod Stack

The ECHO stack is a post-Gridfall survival saga for Minecraft 26.1.2 on NeoForge. The public release is built from `echocore`, the shared `echoterminal`, the main `echoashfallprotocol` campaign, and five addon chapters: `echoorbitalremnants`, `echostationfall`, `echonexusprotocol`, `echoindustrialnexus`, and `echoblackboxprotocol`.

ECHO: Ashfall Protocol starts in a compact armored drop pod with ECHO-7, a damaged emergency operator that is useful before it is comforting. The opening is no longer a wide crash capsule: the current `20x10x20` pod is a small starter outpost with a clear spawn bay, bed nook, ECHO control core, labeled lockers, ramp, struts, side windows, roof beacons, and discoverable first-night supplies.

The world is not just hostile. It reacts. Radiation mutates after sustained severe exposure, dirty water is an emergency fallback, toxic air drains filters only in hazard pockets, machines need FE power and maintenance, factions remember what you do, and the final Nexus choice now resolves through a full Restore, Destroy, or Control finale.

## Current Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.1.0` | Shared ECHO service registry, profile ledger, diagnostics, hazards, route records, factions, rewards, terminal placement, and Nexus campaign mirrors. |
| `echoterminal` | `1.1.0` | Common terminal shell with Command Deck, What Now, Mission Graph, Route Records, Faction Atlas, Vitals, Reward Inbox, Archives, Baseline, and Addons surfaces. |
| `echoashfallprotocol` | `1.3.0` | Main Earth survival campaign: drop pod start, wasteland systems, factions/NPCs, guardians, Prime Relays, Nexus warfront, and finale. |
| `echoorbitalremnants` | `1.5.0` | Post-Nexus orbital continuation: launch chain, route worlds, ECHO-0, orbital factions, route records, diagnostics, and support caches. |
| `echostationfall` | `1.1.0` | Station ECHO horror chapter with station power, panic pressure, crew logs, station route state, and terminal handoff. |
| `echonexusprotocol` | `1.0.0` | Nexus corruption chapter for charge control, stabilized fields, memory recovery, matter rewriting, and Core-state escalation. |
| `echoindustrialnexus` | `0.1.0` | Industrial automation chapter for Thermal Flux, rusted machines, automated filters, salvage processing, and factory recovery. |
| `echoblackboxprotocol` | `1.0.0` | Late-game Blackbox finale with memory fragments, archive dungeons, hostile recordings, boss proofs, and final outcome directives. |

## Core Features

- **Expedition survival:** slow hydration loss, emergency dirty water, zone-based toxic air, radiation, mutations, filter cartridges, RadAway, gas masks, scrubbers, and field medicine.
- **Scrap economy:** debris salvage, substrate harvesting, hand recycling, thermal burning, ore grinding, isotope refining, and machine chains.
- **Recovered biome goods:** ruined worlds still provide paper, dyes, clay, flowers, mushrooms, cactus, sugar cane, bamboo, ocean salvage, ice, and animal goods through recipes, POI caches, seed vaults, and faction imports.
- **Power restoration:** micro generators, battery banks, power nodes, thermal arrays, deep-core mining, and the Nexus grid.
- **Mission progression:** ECHO-7 keeps the required route practical through podfall survival, biological adaptation, geological extraction, buried guardian nodes, Prime Relay warfront prep, grid restoration, and the irreversible Nexus choice.
- **Signal Leads:** optional recon records explain crash telemetry, region identity, factions, drone memory, guardians, Nexus context, and ECHO-0 quarantine without blocking the main route or spoiling final outcomes.
- **Factions and intel:** 10 Echo Core Ashfall factions use reputation, contacts, contracts, POI affinity, patrol pressure, services, dossiers, and drone reconnaissance.
- **World exploration:** scanner-led POI routes across wasteland biomes, toxic swamps, ruined cities, radiation zones, crash scars, cryogenic ruins, Nexus scars, faction hubs, procedural landmarks, and underground guardian arenas with visible surface entrances.
- **ECHO terminal:** Command Deck, What Now, Mission Graph, Protocol Roadmap, Signal Leads, Route Map, POI Atlas, Route Records, Field Archive, Survival Index, Faction Atlas, Baseline, Vitals, Companion Link, Reward Inbox, Nexus Core, and ORBITAL channels collect lore, objectives, telemetry, faction reports, route state, and optional expansion status.

## Addon Chapter Chain

The release stack ships the full chapter chain. ECHO: Ashfall Protocol remains the entry campaign; ECHO: Orbital Remnants opens after any Ashfall Nexus choice; Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol extend the shared ECHO state through their own route, machine, archive, and terminal surfaces.

Addons communicate through `echocore` and `echoterminal` instead of reaching into another chapter's save data. The shared terminal navigation profile API is public addon-facing surface: `TerminalNavigationProfile`, `TerminalNavigationProfiles`, and `TerminalNavigationSection`.

## Quick Start

1. Start a fresh world and secure the compact drop pod. Spawn is at template `(9, 3, 12)` with a clear bay, and the bed is at `(5, 3, 10)` / `(5, 3, 11)`.
2. Open visible pod lockers for the scanner, water, filters, rations, meds, bottles, torches, basic weapon support, campfire/chest support, and salvage.
3. Craft an early weapon, use the ramp/door to scout safely, shelter before night, and follow ECHO-7 mission prompts.
4. Build the Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
5. Use the Portable Signal Scanner to find a POI, read its route/hazard/prep report, then open Route Map -> POI Atlas to compare that scanner profile against the concrete template signals it can represent.
6. Clear the nine biome guardians by scanning surface entrances and descending into their buried Gridfall nodes.
7. Wake the Nexus campaign, scan six Prime Relays, resolve three relays, survive the Core countermeasure siege, then restore enough grid infrastructure to reach the Nexus Core and choose Restore, Destroy, or Control.
8. Use ORBITAL / Orbital Command to begin the post-Nexus quarantine expansion, then smoke the Stationfall, Nexus, Industrial, and Blackbox chapter entries from the shared terminal.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+

Official release stack:

- `echocore` `1.1.0` - required shared API and service mod.
- `echoterminal` `1.1.0` - shared ECHO Terminal addon.
- `echoashfallprotocol` `1.3.0` - main gameplay mod.
- `echoorbitalremnants` `1.5.0` - post-Nexus orbital expansion.
- `echostationfall` `1.1.0` - Station ECHO horror chapter.
- `echonexusprotocol` `1.0.0` - Nexus corruption chapter.
- `echoindustrialnexus` `0.1.0` - industrial automation chapter.
- `echoblackboxprotocol` `1.0.0` - late-game Blackbox finale.

Build all release jars from the workspace root:

```powershell
.\gradlew.bat buildEchoWorkspace
```

Run the full release verification gate:

```powershell
.\gradlew.bat verifyEchoRelease --warning-mode all
```

Copy the verified jars into the local CurseForge profile:

```powershell
.\gradlew.bat copyEchoJarsToModpack
```

The canonical local release profile is `C:\Users\Ivan\curseforge\minecraft\Instances\Axes of Tomorrow\mods`. Override the destination with `-PechoModpackModsDir="C:/path/to/mods"` only for ad hoc testing.

Release artifacts:

- `build/libs/echoashfallprotocol-1.3.0.jar`
- `core/echocore/build/libs/echocore-1.1.0.jar`
- `addons/echoterminal/build/libs/echoterminal-1.1.0.jar`
- `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.5.0.jar`
- `addons/echostationfall/build/libs/echostationfall-1.1.0.jar`
- `addons/echonexusprotocol/build/libs/echonexusprotocol-1.0.0.jar`
- `addons/echoindustrialnexus/build/libs/echoindustrialnexus-0.1.0.jar`
- `addons/echoblackboxprotocol/build/libs/echoblackboxprotocol-1.0.0.jar`

Verification commands:

```powershell
python -m pip install -r tools\requirements.txt
python tools\validate_resources.py
python tools\validate_gameplay_data.py
.\gradlew.bat verifyEchoRelease --warning-mode all
```

Expected verification result: every required GameTest task reports clean completion across Core, Terminal, Ashfall, Orbital, Stationfall, Nexus, Industrial, and Blackbox. If `checkEchoModJarSet` fails, run `.\gradlew.bat copyEchoJarsToModpack` first so the `Axes of Tomorrow` profile contains exactly one current jar for every ECHO module.
`validate_resources.py` also runs the release-polish checks for mojibake, stale terminal/drone references, plural structure resource paths, placeholder markers, and uppercase real resource namespaces.

## ECHO Core Integration Contract

Addons should communicate through `echocore` instead of reaching directly into another chapter's save data. Current shared services cover pack mode/profile state, progress ledgers, diagnostics, hazard telemetry, route records, faction definitions/profiles/standing/contracts/actions, POI affinity, NPC dialogue roles, recovery hooks, terminal placement, terminal reward storage, intel mirroring, and Nexus path/campaign status. Providers are expected to be tolerant: duplicate IDs are ignored, failed providers are logged, and the owning mod remains responsible for validating actions and rewards server-side.

Terminal addons should register navigation through the public `echoterminal` profile types rather than special-casing the screen. A chapter owns its actions, rewards, and persistence; Terminal owns presentation and routing.

## Release Operations

`tools/echo-release-terminal` is a private local release-ops dashboard. It is useful for QA state and release drafting, but it is not part of the published mod artifact set. Keep it buildable with `npm.cmd run build` before public release.

## Documentation

- `MODPACK_OVERVIEW.md` - full systems, mechanics, factions, progression, and lore overview.
- `LORE_BIBLE.md` - shared tactical-eerie canon and writing rules for missions, archives, docs, and addon chapters.
- `GETTING_STARTED.md` - player-facing walkthrough from drop pod to Nexus choice.
- `PROCEDURAL_STRUCTURES.md` - POI and structure generation reference.

## 1.3.0 Full Stack Smoke Checklist

Start a fresh world, keep the default ECHO: Ashfall Protocol worldgen, and test the first night without using vanilla forests as your main route. The intended opening is debris, ruined vegetation, dead/charred trees, pod salvage, and ECHO-7 mission guidance.

What to test first:

- First 10 minutes: compact pod spawn, visible lockers, sticks/fiber, ruined planks, first weapon, shelter, clean water, emergency dirty water, and ECHO terminal guidance.
- First machine loop: Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
- Resource recovery: JEI custom machine categories, recovered biome goods, healthy sapling rarity, and route-specific POI cache identity.
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

- **ECHO addon chapters:** Orbital Remnants, Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol are included in the public release stack and surface through ECHO Core plus ECHO Terminal.
- **Multiplayer:** supported; Nexus decisions are shared through the world state.
- **Recipe viewers:** JEI support is optional and includes custom ECHO categories for hardcoded machine/process recipes. Normal crafting/smelting recipes still appear through vanilla recipe data.
