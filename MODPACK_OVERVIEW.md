# ECHO Modpack Overview

ECHO is a post-Gridfall survival saga for Minecraft 26.1.2 on NeoForge. ECHO: Ashfall Protocol is the main terrestrial campaign, and the broader ECHO ecosystem now spans the shared Core and Terminal layers plus the Orbital Remnants, Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol addon chapters.

The current full-stack release set is `echocore` `1.1.0`, `echoterminal` `1.1.0`, `echoashfallprotocol` `1.3.0`, `echoorbitalremnants` `1.5.0`, `echostationfall` `1.1.0`, `echonexusprotocol` `1.0.0`, `echoindustrialnexus` `0.1.0`, and `echoblackboxprotocol` `1.0.0`.

Ashfall starts with a compact `20x10x20` armored drop pod, a damaged ECHO-7 AI, and a dead world full of toxic pockets, emergency water choices, unstable radiation, failed machines, faction pressure, and the still-running Nexus Core.

The shared lore tone is tactical eerie: ECHO-7 gives direct field instructions while the records underneath imply a wider Gridfall quarantine story. Mission wording, archive records, addon chapters, and optional Signal Leads all follow that same field-canon voice.

## Current Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.1.0` | Shared service layer for pack mode, profile state, progress ledgers, diagnostics, hazards, route records, factions, POI affinity, terminal reward storage, archives, intel mirrors, terminal placement, and Nexus campaign state. |
| `echoterminal` | `1.1.0` | Shared ECHO terminal shell, mission browser, chapter navigation, archive surfaces, reward inbox, diagnostics, and addon-facing navigation/profile API. |
| `echoashfallprotocol` | `1.3.0` | Main ruined-Earth survival campaign with the compact drop pod, hazards, machines, factions, guardians, Prime Relay warfront, Nexus choice, Warden finale, and addon handoff. |
| `echoorbitalremnants` | `1.5.0` | Post-Nexus orbital route chain from Earth calibration through launch, Low Orbit, Station Network, Moon, Mars, Europa, Saturn, Titan, Deep Space Protocol, ECHO-0, surveys, faction contracts, and final network seal. |
| `echostationfall` | `1.1.0` | Station ECHO horror chapter with station boarding, nine section power/log recovery, oxygen/pressure/panic telemetry, AI override, Station Mother, and Blackbox handoff. |
| `echonexusprotocol` | `1.0.0` | Chapter IV Nexus corruption and memory chapter with Nexus Charge, field stabilization, corrupted biomes, Core access, matter rewriting, the Nexus Guardian, and Restore/Control/Destroy/Merge path commitment. |
| `echoindustrialnexus` | `0.1.0` | Industrial automation chapter with Thermal Flux, ducts, factories, machine heat, scrubber safe zones, filter automation, hybrid Nexus processing, POIs, and the Furnace Warden. |
| `echoblackboxprotocol` | `1.0.0` | Late-game memory finale with typed Blackbox fragments, archive dungeons, memory stability, hostile recordings, boss proofs, Nexus Core Access Key, Truth Engine, and final directives. |

## Core Loop

1. Survive the crash site.
2. Loot the visible pod lockers, secure the bed/ramp, then scavenge debris, ruined vegetation, dead trees, and ruined machinery.
3. Build the first powered machine loop.
4. Stabilize water, power, shelter, and expedition recovery tools.
5. Explore POIs for substrates, schematics, faction intel, and power-node leads.
6. Clear the nine buried biome guardian nodes that anchor the ruined regions.
7. Wake the Nexus campaign, scan Prime Relays, resolve the warfront, and restore enough grid infrastructure to access the Nexus Core.
8. Make the irreversible Nexus choice.
9. Finish the chosen Restore, Destroy, or Control branch through the Pre-Fall Archives, The Warden, and the final epilogue.
10. Continue through the release addon chain: Orbital Remnants, Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol.

## ECHO Ecosystem

The ecosystem is split by ownership rather than by one giant mod owning every save field.

ECHO Core owns shared contracts: pack mode, player profile state, progress milestones, diagnostics, hazard telemetry, route records, faction definitions/profiles/contracts/actions, POI affinity, NPC dialogue roles, terminal placement, terminal reward storage, archive unlocks, intel mirrors, and Nexus path/campaign mirrors. Addons publish into those services and keep their own detailed state.

ECHO Terminal owns presentation: Command Deck, What Now, mission graph, route records, archives, faction atlas, vitals, reward inbox, addon chapter tabs, and mission browser interactions. Addons register terminal chapters through public APIs such as `TerminalNavigationProfile`, `TerminalNavigationProfiles`, `TerminalNavigationSection`, `TerminalMissionProvider`, and terminal action handlers.

Chapter handoffs are milestone driven:

| Handoff | Trigger |
|---|---|
| Ashfall -> Orbital Remnants | Any Ashfall Nexus path unlocks Earth orbital calibration unless Orbital is running standalone. |
| Orbital Remnants -> Stationfall | Station coordinates or the restored Station Network opens Station ECHO boarding. |
| Stationfall -> Nexus Protocol | `stationfall:blackbox_recovered` opens the Nexus Protocol chain. |
| Nexus Protocol -> Blackbox Protocol | Nexus memory/Core escalation exposes deeper Blackbox evidence, Core keys, and final path context. |
| Industrial Nexus | Runs as infrastructure support across the stack, feeding filters, factory recovery, Thermal Flux, hybrid materials, and late-game machine safety. |

The Gradle workspace supports a small beta addon set and the full stack. `-PechoAddonSet=beta` includes Terminal and Orbital Remnants; `-PechoAddonSet=all` includes Stationfall, Nexus Protocol, Industrial Nexus, and Blackbox Protocol as well.

Release verification is also part of the ecosystem. `buildEchoWorkspace` builds the selected stack, `verifyEchoRelease` runs resource validation, gameplay-data validation, POI checks, runtime-log checks, jar-set checks, builds, and GameTests, and `copyEchoJarsToModpack` copies the selected jars into the local `Axes of Tomorrow` CurseForge profile. The private `tools/echo-release-terminal` dashboard supports release drafting and QA state, but it is not shipped as a mod artifact.

## Progression

ECHO: Ashfall Protocol uses ECHO-7 missions as the main progression spine.

The shared terminal also exposes **Signal Leads**: optional lore and recon records derived from existing progress. They explain crash telemetry, worldgen surface identity, POI signals, the POI Field Atlas, faction contact, drone memory, guardian nodes, Nexus choice context, and the ECHO-0 quarantine thread without blocking the main route or changing rewards.

ECHO Core is the integration layer beneath that surface. It publishes pack mode, profile state, diagnostics, hazard telemetry, route records, reward inbox support, faction profiles/contracts/actions, POI affinity, terminal placement, intel mirrors, and Nexus campaign state so every chapter can share context without owning another chapter's save data.

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
| 9 | P9 AFTERMATH PROTOCOL | Live with the choice, finish path objectives, and unlock the release addon chain. |

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

POIs include drop pods, survivor caches, wreck fields, bio labs, data centers, subway stations, military vaults, reactor ruins, relay sites, faction villages, industrial shells, scavenger camps, cryogenic ruins, and Nexus structures. Guardian POIs use visible surface entrances such as hatches, bunker doors, sinkholes, shafts, and breaches, then route the player into underground procedural control nodes with access tunnels, loot rooms, hazard pockets, defenders, and a final guardian chamber.

## Factions and Intel

The wasteland has three major social pressures:

| Faction | Identity | Typical Rewards |
|---|---|---|
| Remnant Collective | Order, restoration, power-grid discipline. | Safer routes, tech support, discounts. |
| Salvager Guild | Trade, salvage rights, opportunistic logistics. | Better trades, cargo help, rare materials. |
| Mutant Front | Adaptation, mutation, anti-old-world survival. | Mutation support, safe zones, biological advantages. |

Reputation affects hostility, trade, patrol behavior, faction quests, village safety, and diplomacy. Factions can drift through alliance, truce, tension, skirmish, or open war. Raids and patrols make faction choices visible in the world.

ECHO intelligence is gathered through drones, faction proximity, faction NPC conversations, accepted contracts, POI affinity, intercepted transmissions, dossiers, historical records, and tactical reports. The ECHO terminal exposes these through Field Archive, Route Map, Survival Index, Faction Atlas, and faction-related panels.

Faction NPCs use ECHO Core roles and dialogue snapshots. Ashfall registers Remnant, Salvager, and Mutant-side field contacts; Orbital Remnants mirrors Orbital Remnant, Void Salvagers, and Nexus Choir standings after the post-Nexus handoff.

## ECHO-7 Terminal

The ECHO terminal is the player-facing command surface.

- **PROTOCOL:** Command Deck, What Now, Mission Graph, Protocol Roadmap, and Signal Leads for active route control, required objectives, diagnostics, and optional recon.
- **FIELD:** Route Map, POI Atlas, Route Records, Field Archive, Faction Atlas, Survival Index, and Baseline for routes, records, template recognition, faction state, recipes, and recovered Minecraft tasks.
- **SYSTEMS:** Vitals, Companion Link, and Reward Inbox for hazard telemetry, mutation/research state, drone commands, and terminal-stored rewards.
- **NEXUS:** Nexus Core for Prime Relay warfront prep, Core countermeasure siege readiness, final path interface, and permanent choice confirmation.
- **ORBITAL and ADDONS:** Orbital Command, Route Survey, ECHO-0 Records, Stationfall station state, Nexus Protocol research state, Industrial recovery records, Blackbox archives, route records, diagnostics, support caches, and addon faction standings.

The terminal should answer one practical question at all times: what should I stabilize, craft, scan, repair, or explore next?

Addon terminal navigation is treated as public API in `echoterminal`: `TerminalNavigationProfile`, `TerminalNavigationProfiles`, and `TerminalNavigationSection` define chapter sections without making an addon reach into another chapter's UI internals.

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

Once the guardian chain is clear, the current warfront pass adds a final readiness layer: wake the Nexus campaign, scan six Prime Relays, resolve three relays by stabilizing/severing/overriding them, survive the Core countermeasure siege, and prepare the chosen path. Once that chain and enough grid infrastructure are restored, the player chooses one path:

- **Restore:** rebuild the grid and pursue stabilization.
- **Destroy:** end the Core permanently and free the ruins from its control.
- **Control:** seize the system and accept the cost of command.

The choice is intended to be irreversible and becomes the narrative gate for Orbital Remnants and the later addon chain.

## Release Addon Chapters

The release addon chain is now documented as one ECHO ecosystem, but each chapter still owns its world state, rewards, bosses, and route actions.

### Orbital Remnants

ECHO: Orbital Remnants becomes the ORBITAL / ECHO-0 Route Chain after the Nexus decision.

- Before any Nexus choice, Earth orbital calibration is locked by the quarantine handoff unless the pack is running in Orbital standalone mode.
- After any Nexus choice, Orbital Command calibrates contact and continues through Earth launch recovery, Emergency Rocket assembly, Low Earth Orbit, Station Relay repairs, Lunar Scar, Mars Ash Basin, Europa Cryo Ocean, Saturn Ring Graveyard, Titan Methane Shelf, Deep Space Protocol, ECHO-0, surveys, faction contracts, and final network seal.
- The route now includes Saturn and Titan transfer windows, Saturn Ring Relays, Titan Methane Pumps, Saturn Ring Fragments, Saturn Relay Lenses, Titan Methane Cells, Titan Survey Cores, Saturn Relay Sentinels, and Titan Methane Stalkers.
- The route world set now covers Orbital Debris Field, Lunar Scar Zone, Mars Ash Basin, Europa Cryo Ocean, Saturn Ring Graveyard, Titan Methane Shelf, and Nexus Anomaly Belt.
- The ECHO terminal displays ECHO-0 Route Chain objectives, launch readiness, rocket status, route surveys, route records, field records, support caches, suit telemetry, station power, orbital faction standings, and final network seal state.
- Orbital factions include Orbital Remnants, Void Salvagers, and Nexus Choir. The current route requires three faction proof contracts before the final seal.
- Orbital Remnants progress is not merged into the main Ashfall turn-in system; it is rendered as owned ORBITAL channels and mirrored through ECHO Core where shared UI needs it.

Orbital gameplay adds pressure suit gear, oxygen cells/canisters, sealant patches, radiation/thermal/scanner suit modules, launch infrastructure, fuel/oxygen/heat-shield machines, station relay nodes, planetary route keys, survey markers, faction relay hubs, faction vendor kiosks, orbital weapons, and a route-long ECHO-0 quarantine story.

### Stationfall

ECHO: Stationfall follows the orbital signal into Station ECHO.

- Boarding opens through the Orbital station gate, restored coordinates, or Station Access Card.
- The station dimension contains nine sections: Docking Ring, Crew Quarters, Hydroponics Bay, Medical Wing, Engineering Deck, Data Core, Observation Deck, Containment Wing, and Command Module.
- The station loop tracks section power, doors, crew logs, oxygen, pressure, Signal Panic, AI override state, boss state, return point, and blackbox recovery.
- Station Batteries restore or overload section power. Crew Log Terminals unlock section archive records. The Data Core yields the AI Override Chip. The Command Console starts the Station Mother finale.
- Items and blocks include Station Access Card, Pressure Seal Kit, Emergency Oxygen Pack, Station Battery, Hull Cutter, Crew Log Tablet, AI Override Chip/Core, Signal Panic Dampener, Stationfall Blackbox, Station Power Node, Pressure Door, Damaged Airlock, Hull Breach, Crew Log Terminal, Data Core Terminal, Command Console, Cracked Observation Glass, Corrupted Hydroponic Growth, and Containment Pod.
- Threats include Hollow Crewman, EVA Stalker, Medical Husk, Hydroponic Growth, Maintenance Drone, Screaming Signal, Station Mimic, Suit Without a Body, and Station Mother.

Stationfall's final blackbox recovery records `stationfall:blackbox_recovered`, unlocks Station Mother and Blackbox archive entries, and becomes the direct gate for Nexus Protocol.

### Nexus Protocol

ECHO: Nexus Protocol opens after Stationfall's blackbox handoff or development unlock.

- The terminal chapter is NEXUS PROTOCOL: The Signal Beneath, Dirty Charge, Stabilize the Camp, The Tower Still Speaks, Deleted History, Quarantine Failed, The Monolith Remembers, Reality Forge, The Core Door, and What Rebuilds the World.
- Systems include Nexus Charge, Nexus player data, scan counts, research unlocks, used gear/machines, Blackbox fragments, monolith activation, Warden defeat, Core entry, final choice state, and world-level ending state.
- Machines and blocks include Nexus Recycler, Nexus Charge Tank, Corruption Filter, Nexus Field Stabilizer, Nexus Infuser, Memory Decoder, Reality Forge, Corruption Reactor, Protocol Seal, Reality Tear, Blackbox Plate, Blackbox Monolith Core, Core Brick, Core Glass Block, Signal Glass, Static Fluid Block, and Nexus Crystal Cluster.
- Gear and materials include Nexus Shards, Stable Nexus Core, Blackbox Fragments, Corrupted Ferrite, Static Fluid, White Signal Bark, Nexus Gel, Reality Dust, Field/Filter Membranes, Core Glass, Signal Wire, Stabilized Alloy, Echo Crystal Dust, Clean Resonance Battery, Memory Shards, Data Fragments, Reactor Core, Core Access Key, Core Key Assembly, Nexus Scanner Visor, Nexus Pickaxe, Signal Blade, Reality Anchor, Purity Charge, Collapse Charge, and Nexus armor.
- Worldgen adds the Nexus dimension plus Fractured Wasteland, Nexus Scar, Static Basin, Blackbox Forest, and Core Exclusion Zone with structures such as Abandoned Nexus Field Station, Signal Relay Tower, Data Vault, Corruption Containment Lab, Blackbox Monolith, and Nexus Core Chamber.
- Threats include Nexus Husk, Data Wraith, Static Crawler, Core Soldier, Archive Seeker, Corruption Warden, and Nexus Guardian.

Nexus Protocol can commit Restore, Control, Destroy, or Merge. Ashfall remains the first irreversible Nexus choice, while later chapters expose deeper Core-state interpretations and consequences.

### Industrial Nexus

ECHO: Industrial Nexus is the infrastructure and automation expansion.

- The chapter tracks factory scans, Thermal Flux generated, machine/duct/controller/scrubber counts, hot machines, stored flux, safe zones, scrubber modes, overheat events, Nexus-thermal warnings, POI locators, world progress, claimed caches, and Furnace Warden defeat.
- The mission chain is Reclaim Power, Grind the Wasteland, Filters for Survival, Dense Alloy, Control the Heat, Clean the Camp, Reactor Waste, Hybrid Warning, Factory Controller Online, and Production Survived.
- Thermal Flux infrastructure includes Scrap Dynamo, Thermal Array, Geothermal Pump, Reactor Heat Exchanger, Solar Concentrator, Static Heat Exchanger, Flux Capacitor Bank, Reinforced/Stabilized/Hybrid/Core Flux Banks, Copper/Reinforced/Stabilized/Hybrid/Core Flux Ducts, and the Flux Multimeter.
- Factory automation includes Salvage Shredder, Ore Grinder, Alloy Kiln, Substrate Grinder, Filter Press, Component Assembler, Industrial Recycler, Nexus-Thermal Injector, Fluid Refiner, Water Purifier, Corruption Safe Recycler, Reality Furnace, Factory Controller, item ducts, smart/vacuum/Nexus-safe ducts, and rusted/reinforced/pressurized/shielded/static pipes.
- Heat and safety tools include Thermal Wrench, Emergency Coolant Pack, Coolant Cells, Heat Sink Upgrades, Radiation Shielding Upgrades, Nexus Stabilizer Upgrades, Emergency Shutdown Modules, Industrial Scrubber modes, and Industrial Exo armor.
- POI support includes Rusted Factory Complexes, Abandoned Thermal Plants, Geothermal Drill Sites, Reactor Cooling Stations, and Nexus Heat Exchanger Ruins.
- The Furnace Warden is the current boss and drops/proves the late industrial thermal core route.

Industrial Nexus also manufactures cross-chapter support parts: gas mask filters, emergency oxygen filters, pressure components, launch frames, station batteries, pressure seal kits, hull repair foam, AI override casing, memory stabilizer casing, blackbox decoder cooling, protocol extractor coils, and Core key assemblies.

### Blackbox Protocol

ECHO: Blackbox Protocol is the late-game memory finale.

- Typed memory fragments are Personal, ECHO, Security, Command, Core, and Deleted. Decoding fragments creates matching Memory Records, lowers memory stability, and raises false signal pressure.
- The mission chain is Decode the Blackbox, Blackbox Vault, False ECHO and Command Remnant, Assemble Core Access, Nexus Guardian, and Truth Engine Choice.
- Archive dungeons are Blackbox Vault, Command Bunker, Memory Labyrinth, Core Access Temple, and Nexus Core Chamber. Each uses monolith blocks and dimension handoffs rather than ordinary overworld POIs.
- Machines and blocks include Blackbox Decoder, Memory Projector, Archive Terminal, Core Key Assembler, Truth Engine, Memory Stabilizer, Protocol Extractor, Vault/Bunker/Labyrinth/Temple/Core Chamber Monoliths, Black Metal Block, Corrupted Ferrite Block, Core Brick, and Signal Glass.
- Bosses and threats include Archive Husk, Security ECHO, Memory Parasite, Blackbox Sentinel, False ECHO, Command Remnant, and Nexus Guardian.
- Core access requires typed logs, boss proofs, ECHO Identity Fragment, Command Key, Protocol Extractor Schematic, Core Access Key Matrix, and the Nexus Core Access Key.
- Final directives are Restore, Control, Destroy, and Merge. The Truth Engine applies the chosen ending state; Merge is the secret route that requires deeper deleted-log and boss proof.

Lore continuity: Gridfall breaks Earth, the pod fell from Station ECHO, ECHO-7 survives as a damaged field operator under ECHO-0's old authority, the Nexus choice reopens deep protocols, Orbital Remnants proves the quarantine can be challenged, Stationfall reveals the station did not merely die, Nexus Protocol exposes Core instructions, Industrial Nexus rebuilds infrastructure under heat and corruption pressure, and Blackbox Protocol asks who gets to author the final memory.

## Full Stack Smoke Notes

Recommended fresh-world checks:

- First-night survival inside the compact pod without relying on vanilla forests.
- JEI visibility for custom machine/process recipes.
- ECHO terminal guidance across Command Deck, What Now, Mission Graph, Protocol Roadmap, Signal Leads, Route Map, POI Atlas, Route Records, Survival Index, Faction Atlas, Vitals, Companion Link, Reward Inbox, Nexus Core, ORBITAL, and addon chapter surfaces.
- Drone repair/mode commands and Scout Drone fallback behavior.
- Faction NPC dialogue/contracts, faction standings, trader rewards, POI cache identity, scanner route profiles, underground guardian routes, Prime Relay warfront prep, Nexus path objectives, Warden reward-once behavior, post-Nexus Orbital presentation, and addon route entry visibility.
- Orbital route checks: Earth calibration, launch readiness, staged Emergency Rocket, Low Orbit, Station Relay repairs, Moon/Mars/Europa/Saturn/Titan route unlocks, Deep Space Protocol, ECHO-0, 21 survey records, three faction contracts, and final network seal.
- Stationfall checks: station boarding/return, nine section power states, crew logs, Signal Panic telemetry, Data Core AI override, Station Mother fight, Stationfall Blackbox recovery, and Nexus Protocol unlock.
- Nexus Protocol checks: Stationfall blackbox gate, Nexus Scanner Visor, Nexus Recycler, Corruption Filter, Nexus Field Stabilizer, Memory Decoder, Blackbox Monolith, Corruption Warden, Reality Forge, Core Access Key, Nexus Guardian, and Restore/Control/Destroy/Merge commit actions.
- Industrial checks: Thermal Flux generation, factory scans, duct networks, filter automation, scrubber safe zones, heat/overload handling, Nexus-thermal warning, Industrial POI locator hints, and Furnace Warden reward state.
- Blackbox checks: typed fragment decoding, memory stability/false signal pressure, Vault/Bunker/Labyrinth/Temple/Core Chamber monolith access, False ECHO, Command Remnant, Nexus Core Access Key, Nexus Guardian, Truth Engine directives, and support cache claims.
- Release-ops checks: `-PechoAddonSet=all` workspace build, resource validators, gameplay-data validator, runtime-log scan, jar-set check, and copy to the local `Axes of Tomorrow` modpack profile only after stale jars are removed.

Known constraints:

- New resource and POI distribution is most reliable in new chunks.
- The default workspace addon set is `beta`; use `-PechoAddonSet=all` when testing the full ecosystem.
- Drone control is terminal/direct-interaction based; the old standalone drone menu path is intentionally not exposed.
- Audio polish currently favors built-in Minecraft sound events over bundled custom sound files.
