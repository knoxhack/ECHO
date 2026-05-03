# ECHO: ASHFALL PROTOCOL

ECHO: ASHFALL PROTOCOL is a post-Gridfall survival modpack experience for Minecraft 26.1.2 on NeoForge. You wake in a ruined drop pod with ECHO-7, a damaged emergency AI, and rebuild enough power, filtration, medicine, weapons, and faction trust to reach the Nexus Core.

The world is not just hostile. It reacts. Radiation mutates after sustained severe exposure, dirty water is an emergency fallback, toxic air drains filters only in hazard pockets, machines need FE power and maintenance, factions remember what you do, and the final Nexus choice now resolves through a full Restore, Destroy, or Control finale.

## Core Features

- **Expedition survival:** slow hydration loss, emergency dirty water, zone-based toxic air, radiation, mutations, filter cartridges, RadAway, gas masks, scrubbers, and field medicine.
- **Scrap economy:** debris salvage, substrate harvesting, hand recycling, thermal burning, ore grinding, isotope refining, and machine chains.
- **Recovered biome goods:** ruined worlds still provide paper, dyes, clay, flowers, mushrooms, cactus, sugar cane, bamboo, ocean salvage, ice, and animal goods through recipes, POI caches, seed vaults, and faction imports.
- **Power restoration:** micro generators, battery banks, power nodes, thermal arrays, deep-core mining, and the Nexus grid.
- **Mission progression:** ECHO-7 guides the player through crash survival, biological adaptation, geological extraction, buried guardian nodes, grid restoration, and the irreversible Nexus choice.
- **Signal Leads:** optional lore and recon records explain crash telemetry, region identity, factions, drone memory, guardians, Nexus context, and ECHO-0 quarantine without blocking the main route.
- **Factions and intel:** Remnants, Salvagers, and Mutant-aligned threats use reputation, patrols, raids, quests, diplomacy, dossiers, and drone reconnaissance.
- **World exploration:** scanner-led POI routes across wasteland biomes, toxic swamps, ruined cities, radiation zones, crash scars, cryogenic ruins, Nexus scars, faction hubs, procedural landmarks, and underground guardian arenas with visible surface entrances.
- **ECHO terminal:** Command Deck, Protocol Roadmap, Signal Leads, Route Map, Field Archive, Survival Index, Baseline, Vitals Scan, Companion Link, Nexus Core, and ORBITAL channels collect lore, objectives, telemetry, faction reports, and optional expansion status.

## Optional Endgame Expansion

If `echoorbitalremnants` is installed, ECHO: Orbital Remnants unlocks after any ECHO: Ashfall Protocol Nexus choice. Before that choice, orbital calibration is locked. After the choice, the ECHO terminal opens the ORBITAL channels: Orbital Command, Route Survey, and ECHO-0 Records.

The addon is optional. ECHO: Ashfall Protocol can run without Orbital Remnants installed.

## Quick Start

1. Start a fresh world and secure the crashed drop pod area.
2. Scavenge debris for scrap metal, wire, circuits, and plastic.
3. Craft an early weapon, shelter before night, and follow ECHO-7 mission prompts.
4. Build the Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
5. Use the Portable Signal Scanner to find a POI, read its route/hazard/prep report, then push into that site for schematic fragments, faction contact, substrate resources, and power-node clues.
6. Clear the nine biome guardians by scanning surface entrances and descending into their buried Gridfall nodes.
7. Restore enough grid infrastructure to reach the Nexus Core and choose Restore, Destroy, or Control.
8. If Orbital Remnants is installed, use ORBITAL / Orbital Command to begin the post-Nexus expansion.

## Requirements

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+

Official release stack:

- `echocore` - required shared API and service mod.
- `echoashfallprotocol` - required main gameplay mod.
- `echoterminal` - required shared ECHO Terminal addon.
- `echoorbitalremnants` - optional post-Nexus expansion.

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

Override the destination with `-PechoModpackModsDir="C:/path/to/mods"` if your local profile uses a different folder.

Release artifacts:

- `build/libs/echoashfallprotocol-1.2.0.jar`
- `core/echocore/build/libs/echocore-1.0.0.jar`
- `addons/echoterminal/build/libs/echoterminal-1.0.0.jar`
- `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.4.0.jar`

Verification commands:

```powershell
python -m pip install -r tools\requirements.txt
python tools\validate_resources.py
python tools\validate_gameplay_data.py
.\gradlew.bat verifyEchoRelease --warning-mode all
```

Expected GameTest totals after `verifyEchoRelease`: Core task `2`, Terminal task `5`, Ashfall root stack `43`, and Orbital task `32`.
`validate_resources.py` also runs the release-polish checks for mojibake, stale terminal/drone references, plural structure resource paths, placeholder markers, and uppercase real resource namespaces.

## Documentation

- `MODPACK_OVERVIEW.md` - full systems, mechanics, factions, progression, and lore overview.
- `LORE_BIBLE.md` - shared tactical-eerie canon and writing rules for missions, archives, docs, and addon chapters.
- `GETTING_STARTED.md` - player-facing walkthrough from drop pod to Nexus choice.
- `PROCEDURAL_STRUCTURES.md` - POI and structure generation reference.

## 1.2.0 Full Endgame Smoke Checklist

Start a fresh world, keep the default ECHO: Ashfall Protocol worldgen, and test the first night without using vanilla forests as your main route. The intended opening is debris, ruined vegetation, dead/charred trees, pod salvage, and ECHO-7 mission guidance.

What to test first:

- First 10 minutes: sticks/fiber, ruined planks, first weapon, shelter, clean water, emergency dirty water, and ECHO terminal guidance.
- First machine loop: Hand Recycler, Micro Generator, Filter Workbench, Water Purifier, and Battery Bank.
- Resource recovery: JEI custom machine categories, recovered biome goods, healthy sapling rarity, and route-specific POI cache identity.
- Scanner loop: Portable Signal Scanner reports the actual site, hazard profile, prep kit, reward track, distance, direction, and field-log status.
- Factions and drones: Scout Drone fallback, ECHO companion repair/modes, faction standing, trader rewards, raids, and intel reports.
- Nexus path: buried guardian nodes, Power Nodes, final choice, path objectives, Archives arena entry/return, Warden defeat, final epilogue, and optional Orbital Remnants unlock if the addon is installed.

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

- **ECHO: Orbital Remnants:** optional post-Nexus addon chapter.
- **Multiplayer:** supported; Nexus decisions are shared through the world state.
- **Recipe viewers:** JEI support is optional and includes custom ECHO categories for hardcoded machine/process recipes. Normal crafting/smelting recipes still appear through vanilla recipe data.
