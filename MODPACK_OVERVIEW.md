# ECHO: ASHFALL PROTOCOL - Modpack Overview

ECHO: ASHFALL PROTOCOL is a post-apocalyptic survival overhaul for Minecraft 26.1.2 on NeoForge. It starts with a ruined drop pod, a damaged ECHO-7 AI, and a dead world full of toxic pockets, emergency water choices, unstable radiation, failed machines, hostile factions, and the still-running Nexus Core.

The shared lore tone is tactical eerie: ECHO-7 gives direct field instructions while the records underneath imply a wider Gridfall quarantine story. `LORE_BIBLE.md` is the source of truth for mission wording, archive records, addon chapters, and future optional Signal Leads.

## Core Loop

1. Survive the crash site.
2. Scavenge debris, ruined vegetation, dead trees, and ruined machinery.
3. Build the first powered machine loop.
4. Stabilize water, power, shelter, and expedition recovery tools.
5. Explore POIs for substrates, schematics, faction intel, and power-node leads.
6. Clear the nine buried biome guardian nodes that anchor the ruined regions.
7. Restore enough grid infrastructure to access the Nexus Core.
8. Make the irreversible Nexus choice.
9. Finish the chosen Restore, Destroy, or Control branch through the Pre-Fall Archives, The Warden, and the final epilogue.
10. Continue into optional Orbital Remnants content if the addon is installed.

## Progression

ECHO: Ashfall Protocol uses ECHO-7 missions as the main progression spine.

The shared terminal also exposes **Signal Leads**: optional lore and recon records derived from existing progress. They explain crash telemetry, worldgen surface identity, POI signals, the POI Field Atlas, faction contact, drone memory, guardian nodes, Nexus choice context, and the ECHO-0 quarantine thread without blocking the main route or changing rewards.

| Phase | Name | Focus |
|---|---|---|
| 1 | P1 PODFALL | Secure shelter, water, and first tools near the pod. |
| 2 | P2 OUTPOST SURVIVAL | Turn the crash site into a lit, supplied first outpost. |
| 3 | P3 LIFE SUPPORT | Build the first powered base: recycler, generator, purifier, battery, pipes, heat, and expedition supplies. |
| 4 | P4 SIGNAL CONTACT | Contact factions, build research, scan POIs, prepare expeditions, and repair drone scouting. |
| 5 | P5 BIOHAZARD ADAPTATION | Manage radiation, mutation, medicine, scrubbers, and contaminated salvage. |
| 6 | P6 DEEP EXTRACTION | Process substrates and dense materials into advanced machinery and weapons. |
| 7 | P7 GRID RESTORATION | Deploy scanners, drones, power nodes, workshops, relays, and clear buried guardian nodes. |
| 8 | P8 NEXUS DECISION | Reach the Nexus Core after the Nexus Scar Avatar falls and choose Restore, Destroy, or Control. |
| 9 | P9 AFTERMATH PROTOCOL | Live with the choice, finish path objectives, and unlock optional orbital expansion content. |

## Survival Systems

### Hazard Zones

ECHO: Ashfall Protocol treats environmental danger as hazard zones instead of scattered one-off effects. Toxic Swamps and Industrial Ruins are toxic-air zones, Radiation Zones accumulate radiation, Cryogenic Ruins drain body heat through ColdData, and Nexus Scars combine radiation, toxic pressure, and mutation instability.

Crash and ruined starter biomes remain mostly safe except for local hazard blocks, so the opening loop is survivable.

### Toxic Air

Gas masks and filter cartridges protect against toxic swamps, industrial ruins, and marked high-particulate source blocks. They are expedition gear, not a permanent oxygen tax.

- Basic filters give early hazard-pocket coverage.
- Advanced filters last longer under harsh conditions.
- Elite filters degrade much more slowly and protect against the worst air.
- Atmospheric Scrubbers create real safe pockets, block toxic air, accelerate radiation decay, and slow cold loss.

### Radiation

Radiation comes from blocks, mobs, reactor ruins, radiation biomes, Nexus scars, and radiation storms. Sustained severe radiation causes negative effects and can trigger mutation events.

Counters include RadAway, hazmat, storm shelter, scrubber pockets, radiation-cleansing machines, and careful route planning.

### Hydration

Dirty water is an emergency fallback with brief nausea; clean water is the planned route supply. The Water Purifier converts Dirty Water Bottles into Clean Water Bottles, and low hydration slows, weakens, and eventually harms the player.

### Mutations

Radiation and Mutagen Vials can rewrite the player. Mutations may grant useful traits such as night vision, faster scavenging, regeneration, or radiation resistance, but stacking too many mutations adds side effects like visual distortion, self-damage, mob aggression, hunger drain, and weakness. The Field Med Bay helps cure side effects.

## Machines and Automation

The scrap economy turns ruined-world junk into survival infrastructure.

Early crafting does not require a healthy vanilla forest. Players can recover sticks and plant fiber from ash bushes, dry grass, burnt grass, wasteland grass, reeds, mutated leaves, and debris. Dead Wood Logs and Charred Wood Logs drop by hand and craft into 4 Oak Planks, and Mutated Saplings provide a renewable but contaminated tree route on ruined soils. Healthy vanilla saplings are rare restoration finds from labs, data centers, survivor caches, Remnant/Salvager caches, and positive Salvager trader access.

Vanilla biome resources are explicit recovery rewards instead of normal-biome dependencies. Wasteland Reeds craft into paper, Irradiated Cactus crafts into Green Dye, toxic substrate processing yields clay, and cryogenic stone can be broken down into snowballs. Seed Vaults, Bio Labs, Data Centers, Remnant caches, Salvager posts, sewers, subways, abandoned mines, cryogenic ruins, and Mutant sanctuaries now cover desert, jungle, swamp, ocean, flower, cold-biome, cave, and animal-economy resources without restoring vanilla biome generation.

| Missing Vanilla Ecology | Primary ECHO Source |
|---|---|
| Sugar cane, cactus, bamboo, cocoa, crop seeds | Seed Vaults, Bio Labs, Data Centers, Salvager imports. |
| Flowers, dyes, mushrooms, moss, honey samples | Seed Vaults, Bio Labs, Remnant restoration caches, Mutant sanctuaries. |
| Clay, vines, lily pads, mangrove samples | Sewers, toxic wetland salvage, contaminated soil processing. |
| Kelp, seagrass, coral, prismarine, ink sacs, nautilus shells | Salvager ocean salvage caches and trader imports. |
| Snowballs, ice, packed ice, blue ice | Cryogenic ruins and cryogenic fractured stone. |
| Wool, eggs, milk, animal goods | Remnant restoration caches and survival salvage. |

| System | Role |
|---|---|
| Hand Recycler | Converts scrap into useful early components. |
| Micro Generator | First local power source. |
| Thermal Burner | Smelts and processes with fuel. |
| Water Purifier | Converts dirty water into clean water. |
| Filter Workbench | Crafts and supports filter cartridges. |
| Scrap Press | Compresses scrap into denser materials. |
| Ore Grinder | Processes rubble, slagstone, aggregate, shale, and crash slag. |
| Isotope Refiner | Refines advanced radioactive and trace materials. |
| Battery Bank | Stores power for larger bases. |
| Factory Controller | Monitors machine networks. |
| Item Pipe | Moves items between nearby machines. |
| Deep Core Miner | Late-game resource extraction. |

Machines depend on power distribution and may interact with adjacency, controller, pipe, or maintenance systems depending on the block.

JEI support is optional but recommended. ECHO: Ashfall Protocol registers custom JEI categories for hardcoded machine behavior such as recycling, purification, burning, generation, pressing, grinding, isotope refining, radiation cleansing, crystalline synthesis, and deep-core mining. Locked schematic outputs stay visible with lock text so players can see what branch or machine tier they are moving toward.

## World and Exploration

ECHO: Ashfall Protocol replaces peaceful overworld assumptions with ruined ecosystems and sparse but valuable POIs. The Portable Signal Scanner is the exploration spine: it reports the actual site, route, hazard profile, prep kit, objective, reward track, distance, direction, and field-log state. The Route Map POI Atlas then groups all concrete template signals under those scanner profiles so players can learn the difference between camps, wrecks, hubs, labs, vaults, and landmarks without changing progression.

- **Crash zones:** violent debris fields and starting-resource pressure.
- **Wastelands:** open terrain, low cover, scrap, and early danger.
- **Toxic swamps:** high air pressure, spores, dirty water, and filter drain.
- **Ruined cities:** vertical ambushes, salvage caches, and old-world tech.
- **Radiation zones:** isotope risk, reactor ruins, and mutation pressure.
- **Cryogenic ruins:** cold-tech remnants and specialized threats.
- **Nexus scars:** late-game anomalies tied to the Core.

POIs include drop pods, survivor caches, wreck fields, bio labs, data centers, subway stations, military vaults, reactor ruins, relay sites, faction villages, industrial shells, scavenger camps, cryogenic ruins, and Nexus structures. Guardian POIs use visible surface entrances such as hatches, bunker doors, sinkholes, shafts, and breaches, then route the player into underground procedural arenas with access tunnels, loot rooms, hazard pockets, defenders, and a boss chamber.

## Factions and Intel

The wasteland has three major social pressures:

| Faction | Identity | Typical Rewards |
|---|---|---|
| Remnant Collective | Order, restoration, power-grid discipline. | Safer routes, tech support, discounts. |
| Salvager Guild | Trade, salvage rights, opportunistic logistics. | Better trades, cargo help, rare materials. |
| Mutant Front | Adaptation, mutation, anti-old-world survival. | Mutation support, safe zones, biological advantages. |

Reputation affects hostility, trade, patrol behavior, faction quests, village safety, and diplomacy. Factions can drift through alliance, truce, tension, skirmish, or open war. Raids and patrols make faction choices visible in the world.

ECHO intelligence is gathered through drones, faction proximity, intercepted transmissions, dossiers, historical records, and tactical reports. The ECHO terminal exposes these through Field Archive, Route Map, Survival Index, and faction-related panels.

## ECHO-7 Terminal

The ECHO terminal is the player-facing command surface.

- **PROTOCOL:** Command Deck, Protocol Roadmap, and Signal Leads for active route control, required objectives, and optional recon.
- **FIELD:** Route Map, POI Atlas, Field Archive, Survival Index, and Baseline for routes, records, template recognition, recipes, and recovered Minecraft tasks.
- **SYSTEMS:** Vitals Scan and Companion Link for hazard telemetry, mutation/research state, and drone commands.
- **NEXUS:** Nexus Core for the final path interface and permanent choice confirmation.
- **ORBITAL:** Orbital Command, Route Survey, and ECHO-0 Records when the optional addon is installed.

The terminal should answer one practical question at all times: what should I stabilize, craft, scan, repair, or explore next?

## Drone System

The ECHO companion drone grows from a damaged helper into a tactical extension of the terminal.

| Mode | Repair Requirement | Role |
|---|---:|---|
| Follow | 25% | Follows and lights the player. |
| Scout | 50% | Patrols, detects threats, gathers intel. |
| Combat | 50% | Engages enemies with faction-aware targeting. |
| Scavenge | 75% | Helps collect debris and drops. |
| Patrol | 100% | Defends an area autonomously. |

Combat abilities include suppression, target marking, and faction-aware prioritization. Scout behavior can discover villages, intercept radio chatter, and add recon reports to the terminal.

## Research

Schematic Fragments unlock advanced perks across power, mechanical, biological, defensive, and communications branches.

Implemented perk themes include weapon damage, health regeneration, machine efficiency, radiation resistance, trade prices, and faster looting. Fragments are primarily recovered from POI loot and are meant to reward exploration.

Elite Salvager reputation unlocks a Rare Tech Schematic trade. Four generic Schematic Fragments can be exchanged for one Rare Tech Schematic, which decodes at a Research Lab into the first missing schematic branch in enum order plus 75 RP. If every branch is already unlocked, it archives for 125 RP instead.

## Nexus Endgame

The Nexus Core is the final ECHO: Ashfall Protocol decision point. Before the Core route opens, all nine biome guardians must be defeated. They are buried Gridfall control nodes: Wasteland Sentinel, Plains Warlord, City Ruin Stalker, Industrial Juggernaut, Toxic Hive Matriarch, Crash Zone Colossus, Radiation Behemoth, Cryogenic Overseer, and Nexus Scar Avatar.

Once the guardian chain and enough grid infrastructure are restored, the player chooses one path:

- **Restore:** rebuild the grid and pursue stabilization.
- **Destroy:** end the Core permanently and free the ruins from its control.
- **Control:** seize the system and accept the cost of command.

The choice is intended to be irreversible and becomes the narrative gate for optional Orbital Remnants integration.

## Optional ECHO: Orbital Remnants Addon

When ECHO: Orbital Remnants is installed with ECHO: Ashfall Protocol, it becomes the ORBITAL / ECHO-0 Route Chain after the Nexus decision.

- Before any Nexus choice, Earth orbital calibration is locked by the quarantine handoff.
- After any Nexus choice, Orbital Command can calibrate contact and continue the launch chain.
- The ECHO terminal displays ECHO-0 Route Chain objectives, field records, Survival Index references, route flags, suit telemetry, station power, and orbital faction standings.
- Orbital Remnants progress is not merged into the main Ashfall turn-in system; it is rendered as owned ORBITAL channels.

Lore continuity: Gridfall breaks Earth, the pod fell from Station ECHO, ECHO-7 survives as a damaged field operator under ECHO-0's old authority, the Nexus choice reopens deep protocols, and Orbital Remnants follows the signal back into a quarantine system that still thinks silence is safety.

## 1.2.0 Full Endgame Smoke Notes

Recommended fresh-world checks:

- First-night survival without relying on vanilla forests.
- JEI visibility for custom machine/process recipes.
- ECHO terminal guidance across Command Deck, Protocol Roadmap, Signal Leads, Route Map, Survival Index, Vitals Scan, Companion Link, Nexus Core, and ORBITAL.
- Drone repair/mode commands and Scout Drone fallback behavior.
- Faction standings, trader rewards, POI cache identity, scanner route profiles, underground guardian routes, Nexus path objectives, Warden reward-once behavior, and post-Nexus Orbital Remnants presentation.

Known constraints:

- New resource and POI distribution is most reliable in new chunks.
- Drone control is terminal/direct-interaction based; the old standalone drone menu path is intentionally not exposed.
- Audio polish currently favors built-in Minecraft sound events over bundled custom sound files.
