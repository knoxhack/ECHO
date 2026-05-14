# ECHO Modpack Overview

Ashfall is the modpack. ECHO is the first-party ecosystem. ECHO: Ashfall Protocol is the main terrestrial campaign addon and root `echoashfallprotocol` artifact.

Build-truth validation token: `1.1.0`.

ECHO is a post-Gridfall survival saga for Minecraft 26.1.2 on NeoForge. The simplified public stack can be described as Core, Terminal, Ashfall Protocol, Orbital, Agriculture, Stationfall, Nexus, Industrial, and Blackbox. The full Gradle `all` stack also includes NetCore, RuntimeGuard, ThemeCore, PlayerCore, MissionCore, DataCore, WorldCore, SignalOS, SignalOS Example, RenderCore, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

The current full-stack release set is `echocore` `1.0.0`, `echonetcore` `1.0.0`, `echoruntimeguard` `1.0.0`, `echothemecore` `0.2.0`, `echoplayercore` `0.1.0`, `echomissioncore` `1.0.0`, `echodatacore` `1.0.0`, `echoworldcore` `1.0.0`, `echoterminal` `1.0.0`, `signalos` `1.0.0`, `signalosexample` `1.0.0`, `echorendercore` `1.0.0`, `echoashfallprotocol` `1.0.0`, `echoorbitalremnants` `1.0.0`, `echonexusprotocol` `1.0.0`, `echoagriculturereclamation` `1.0.0`, `echostationfall` `1.0.0`, `echoblackboxprotocol` `1.0.0`, `echoindustrialnexus` `1.0.0`, `echologisticsnetwork` `1.0.0`, `echoconvoyprotocol` `1.0.0`, `echoholomap` `1.0.0`, `echoindex` `1.0.0`, `echoarmory` `1.0.0`, `echolens` `1.0.0`, `echomultiblockcore` `1.0.0`, and `echoblockworks` `1.0.0`.

Ashfall starts with a compact `20x10x20` armored drop pod, a damaged ECHO-7 AI, and a dead world full of toxic pockets, emergency water choices, unstable radiation, failed machines, faction pressure, and the still-running Nexus Core.

The shared lore tone is tactical eerie: ECHO-7 gives direct field instructions while the records underneath imply a wider Gridfall quarantine story. Mission wording, archive records, addon chapters, and optional Signal Leads all follow that same field-canon voice.

## Current Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.0.0` | Shared service layer for pack mode, profile state, progress ledgers, diagnostics, hazards, route records, factions, POI affinity, terminal reward storage, archives, intel mirrors, terminal placement, and Nexus campaign state. |
| `echonetcore` | `1.0.0` | Shared packet bridge, sync helpers, packet diagnostics, and server action validation. |
| `echoruntimeguard` | `1.0.0` | Shared TPS/FPS pressure monitoring, runtime budgets, smart tick hints, and performance diagnostics. |
| `echothemecore` | `0.2.0` | Shared visual/theme/UI skin service for ECHO modules and vanilla surfaces. |
| `echoplayercore` | `0.1.0` | Player utility commands, homes, back, spawn, random teleport, and travel QoL. |
| `echomissioncore` | `1.0.0` | Shared mission service, objective registry, progression state, and reward contracts. |
| `echodatacore` | `1.0.0` | Shared persistent player, world, and team data service. |
| `echoworldcore` | `1.0.0` | Shared world regions, markers, hazards, structure discoveries, and world event contracts. |
| `echoterminal` | `1.0.0` | Shared ECHO terminal shell, mission browser, chapter navigation, archive surfaces, reward inbox, diagnostics, and addon-facing navigation/profile API. |
| `signalos` | `1.0.0` | Reusable terminal/content framework for chapters, missions, archives, rewards, diagnostics, JSON loading, validation, and soft KubeJS bridge integration. |
| `signalosexample` | `1.0.0` | Example-only SignalOS addon demonstrating Java registration, datapack JSON, diagnostics, rewards, archives, and KubeJS-friendly content. |
| `echorendercore` | `1.0.0` | Shared visual-state, animation-profile, particle-profile, preview, composition, and renderer helper layer for advanced ECHO/Ashfall assets. |
| `echoashfallprotocol` | `1.0.0` | Main ruined-Earth survival campaign with the compact drop pod, hazards, machines, factions, guardians, Prime Relay warfront, Nexus choice, Warden finale, and addon handoff. |
| `echoorbitalremnants` | `1.0.0` | Post-Nexus orbital route chain from Earth calibration through launch, Low Orbit, Station Network, Moon, Mars, Europa, Saturn, Titan, Deep Space Protocol, ECHO-0, surveys, faction contracts, and final network seal. |
| `echoagriculturereclamation` | `1.0.0` | Field agriculture recovery with recovered seeds, contaminated soils, hydroponics, greenhouse zones, Pollinator Drone service, gene stabilization, bio-reactor support, and chunk-local restoration scores. |
| `echostationfall` | `1.0.0` | Station ECHO horror chapter with station boarding, nine section power/log recovery, oxygen/pressure/panic telemetry, AI override, Station Mother, and Blackbox handoff. |
| `echonexusprotocol` | `1.0.0` | Chapter IV Nexus corruption and memory chapter with Nexus Charge, smarter field-map risk planning, field stabilization, corrupted biomes, Core access, matter rewriting, the Nexus Guardian, and Restore/Control/Destroy/Merge path commitment. |
| `echoindustrialnexus` | `1.0.0` | Industrial automation chapter with Thermal Flux, ducts, MultiblockCore factories, Factory Command dashboards, machine heat, scrubber safe zones, filter automation, hybrid Nexus processing, POIs, and the Furnace Warden. |
| `echologisticsnetwork` | `1.0.0` | Supply crates, labels, loadouts, external endpoints, drone delivery docks, remote requests, faction depots, courier persistence, Terminal integration, and operations dashboards. |
| `echoconvoyprotocol` | `1.0.0` | Ruined-Earth vehicles, multiblock depots, cargo/fuel logistics, deterministic Field Ops, HoloMap routes, roadside contracts, recovery signals, and travel hazards. |
| `echoholomap` | `1.0.0` | Terminal-integrated command map for regions, routes, hazards, scans, missions, and addon markers. |
| `echoindex` | `1.0.0` | Shared item, recipe, usage, and archive index for Terminal-facing reference surfaces. |
| `echoarmory` | `1.0.0` | Modular weapons, armor, workstations, modules, energy recharge, faction locks, Terminal hooks, and Logistics loadout hooks. |
| `echolens` | `1.0.0` | Smart scanner HUD with local inspection, server-assisted Deep Scan, inventory privacy, and addon context. |
| `echomultiblockcore` | `1.0.0` | Shared data-driven multiblock validation, runtime, robotics, workcell, and scanner/map/terminal contracts. |
| `echoblockworks` | `1.0.0` | First-party decorative, structural, and themed block families for ECHO builds and ruins. |
| `echoblackboxprotocol` | `1.0.0` | Late-game memory finale with typed Blackbox fragments, archive dungeons, memory stability, hostile recordings, boss proofs, Nexus Core Access Key, Truth Engine, and final directives. |

## Core Loop

1. Survive the crash site.
2. Loot the visible pod lockers, secure the bed/ramp, then scavenge debris, ruined vegetation, dead trees, and ruined machinery.
3. Build the first powered machine loop.
4. Stabilize water, power, shelter, and expedition recovery tools.
5. Explore POIs for substrates, schematics, faction intel, and power-node leads.
6. Clear the eight active buried biome guardian nodes that anchor the ruined regions.
7. Wake the Nexus campaign, scan Prime Relays, resolve the warfront, and restore enough grid infrastructure to access the Nexus Core.
8. Make the irreversible Nexus choice.
9. Finish the chosen Restore, Destroy, or Control branch through the Pre-Fall Archives, The Warden, and the final epilogue.
10. Continue through the release addon chain: Orbital Remnants, Agriculture Reclamation, Stationfall, Nexus Protocol, Industrial Nexus, Logistics Network, Convoy Protocol, Armory, and Blackbox Protocol.

## ECHO Ecosystem

The ecosystem is split by ownership rather than by one giant mod owning every save field.

ECHO Core owns shared contracts: pack mode, player profile state, progress milestones, diagnostics, hazard telemetry, route records, faction definitions/profiles/contracts/actions, POI affinity, NPC dialogue roles, terminal placement, terminal reward storage, archive unlocks, intel mirrors, and Nexus path/campaign mirrors. Addons publish into those services and keep their own detailed state.

ECHO Terminal owns presentation: Command Deck, What Now, mission graph, route records, Recipe Index, transient provider-backed mission HUD notices, archives, faction atlas, vitals, reward inbox, addon chapter tabs, and mission browser interactions. Addons register terminal chapters through public APIs such as `TerminalNavigationProfile`, `TerminalNavigationProfiles`, `TerminalNavigationSection`, `TerminalMissionProvider`, terminal action handlers, and the `TerminalRecipeProvider` recipe surface.

Chapter handoffs are milestone driven:

| Handoff | Trigger |
|---|---|
| Ashfall -> Orbital Remnants | Any Ashfall Nexus path unlocks Earth orbital calibration unless Orbital is running standalone. |
| Ashfall ecology -> Agriculture Reclamation | Recovered seed capsules, Bio Labs, Data Centers, Radwarden caches, Crashbreak imports, toxic salvage, and cryogenic ruins feed the FIELD > Reclamation loop. |
| Orbital Remnants -> Stationfall | Station coordinates or the restored Station Network opens Station ECHO boarding. |
| Stationfall -> Nexus Protocol | `stationfall:blackbox_recovered` opens the Nexus Protocol chain. |
| Nexus Protocol -> Blackbox Protocol | Nexus memory/Core escalation exposes deeper Blackbox evidence, Core keys, and final path context. |
| Industrial Nexus | Runs as infrastructure support across the stack, feeding filters, factory recovery, Thermal Flux, hybrid materials, and late-game machine safety. |

The Gradle workspace supports a small beta addon set and the full stack. `-PechoAddonSet=beta` includes NetCore, RuntimeGuard, ThemeCore, PlayerCore, Terminal, MissionCore, DataCore, SignalOS, SignalOS Example, RenderCore, Orbital Remnants, Nexus Protocol, Agriculture Reclamation, WorldCore, MultiblockCore, and Blockworks, plus Core and ECHO: Ashfall Protocol. `-PechoAddonSet=all` adds Stationfall, Blackbox Protocol, Industrial Nexus, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

Release verification is also part of the ecosystem. `build -PechoAddonSet=all` is the required full-stack build command for this workspace pass. `verifyEchoRelease` also runs resource validation, gameplay-data validation, POI checks, runtime-log checks, jar-set checks, builds, and GameTests, but should only be used after `echoModpackModsDir` is configured for the intended Ashfall profile. The private `tools/echo-release-terminal` dashboard supports release drafting and QA state, but it is not shipped as a mod artifact.

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

Early crafting does not require a healthy vanilla forest. Players can recover sticks and plant fiber from ash bushes, dry grass, burnt grass, wasteland grass, reeds, mutated leaves, and debris. Dead Wood Logs and Charred Wood Logs drop by hand and craft into 4 Oak Planks, and Mutated Saplings provide a renewable but contaminated tree route on ruined soils. Healthy vanilla saplings are rare restoration finds from labs, data centers, survivor caches, Radwarden/Crashbreak caches, and positive Crashbreak trader access.

Vanilla biome resources are explicit recovery rewards instead of normal-biome dependencies. Wasteland Reeds craft into paper, Irradiated Cactus crafts into Green Dye, toxic substrate processing yields clay, and cryogenic stone can be broken down into snowballs. Seed Vaults, Bio Labs, Data Centers, Radwarden caches, Crashbreak posts, sewers, subways, abandoned mines, cryogenic ruins, and Sporebound sanctuaries now cover desert, jungle, swamp, ocean, flower, cold-biome, cave, and animal-economy resources without restoring vanilla biome generation.

| Missing Vanilla Ecology | Primary ECHO Source |
|---|---|
| Sugar cane, cactus, bamboo, cocoa, crop seeds | Seed Vaults, Bio Labs, Data Centers, Crashbreak imports. |
| Flowers, dyes, mushrooms, moss, honey samples | Seed Vaults, Bio Labs, Radwarden restoration caches, Sporebound sanctuaries. |
| Clay, vines, lily pads, mangrove samples | Sewers, toxic wetland salvage, contaminated soil processing. |
| Kelp, seagrass, coral, prismarine, ink sacs, nautilus shells | Crashbreak ocean salvage caches and trader imports. |
| Snowballs, ice, packed ice, blue ice | Cryogenic ruins and cryogenic fractured stone. |
| Wool, eggs, milk, animal goods | Radwarden restoration caches and survival salvage. |

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

JEI support is optional but recommended. ECHO: Ashfall Protocol registers custom JEI categories for hardcoded machine behavior such as recycling, purification, burning, generation, pressing, grinding, isotope refining, radiation cleansing, crystalline synthesis, and deep-core mining. Those hardcoded recipe notes now share the same Ashfall item-info catalog and schematic gating text used by the ECHO Terminal Recipe Index, so locked outputs stay visible with branch or machine-tier guidance in both surfaces.

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
| Radwarden Compact | Containment crews, patrol discipline, reactor cleanup, and cold-route survival law. | Safer routes, exposure cleanup, defensive supplies, containment intelligence. |
| Crashbreak Salvage | Crash-site crews, route builders, depot pragmatists, and machine recovery brokers. | Salvage contracts, cargo help, rare materials, fabrication support. |
| Sporebound Sanctum | Biological recovery, adaptation doctrine, Nexus anomaly interpretation, and risky field medicine. | Filtration support, biological samples, anomaly readings, late-route survival supplies. |

Reputation affects hostility, trade, patrol behavior, faction quests, village safety, and diplomacy. Factions can drift through alliance, truce, tension, skirmish, or open war. Raids and patrols make faction choices visible in the world.

ECHO intelligence is gathered through drones, faction proximity, faction NPC conversations, accepted contracts, POI affinity, intercepted transmissions, dossiers, historical records, and tactical reports. The ECHO terminal exposes these through Field Archive, Route Map, Survival Index, Faction Atlas, and faction-related panels.

Faction NPCs use ECHO Core roles and dialogue snapshots. Ashfall registers three active factions: Radwarden Compact, Crashbreak Salvage, and Sporebound Sanctum. Orbital lanes mirror into those same three standings after the post-Nexus handoff.

## ECHO-7 Terminal

The ECHO terminal is the player-facing command surface.

- **Command:** Command Deck and What Now for active route control, blockers, diagnostics, and next actions.
- **Progress:** Mission Graph, Protocol Roadmap, main survival route, chapter hubs, and addon mission browsers for required objectives and chapter entry points.
- **Intel:** Route Map, POI Atlas, Route Records, Recipe Index, Field Archive, Faction Atlas, Survival Index, Baseline, Nexus Core, Orbital Command, Route Survey, ECHO-0 Records, Stationfall state, Nexus Protocol research, Industrial recovery records, and Blackbox archives.
- **System:** Vitals, Companion Link, Reward Inbox, and terminal settings for hazard telemetry, mutation/research state, drone commands, support caches, and presentation state.

The terminal should answer one practical question at all times: what should I stabilize, craft, scan, repair, or explore next?

Addon terminal navigation is treated as public API in `echoterminal`: `TerminalNavigationProfile`, `TerminalNavigationProfiles`, and `TerminalNavigationSection` define Command, Progress, Intel, and System ownership without making an addon reach into another chapter's UI internals. Recipe visibility uses the companion recipe API: `TerminalRecipeProvider`, `TerminalRecipeRegistry`, `TerminalRecipeCategory`, `TerminalRecipeEntry`, `TerminalRecipeSlot`, `TerminalRecipeNote`, and `TerminalRecipeSnapshot`. Legacy chrome group fallbacks exist only to keep old tabs visible.

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

Elite Crashbreak reputation unlocks a Rare Tech Schematic trade. Four generic Schematic Fragments can be exchanged for one Rare Tech Schematic, which decodes at a Research Lab into the first missing schematic branch in enum order plus 75 RP. If every branch is already unlocked, it archives for 125 RP instead.

## Nexus Endgame

The Nexus Core is the final ECHO: Ashfall Protocol decision point. Before the Core route opens, all eight active biome guardians must be defeated. They are buried Gridfall control nodes with faction ownership: Radwarden tracks Plains Warlord, Radiation Behemoth, and Cryogenic Overseer; Crashbreak tracks City Ruin Stalker, Industrial Juggernaut, and Crash Zone Colossus; Sporebound tracks Toxic Hive Matriarch and Nexus Scar Avatar.

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
- Orbital lanes map into Radwarden orbital containment, Crashbreak orbital salvage, and Sporebound anomaly interpretation. The current route requires three faction proof contracts before the final seal.
- Orbital Remnants progress is not merged into the main Ashfall turn-in system; it is rendered as owned ORBITAL channels and mirrored through Ashfall's three ECHO Core factions where shared UI needs it.

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
- Its ECHO Terminal recipe provider reads `echoindustrialnexus:industrial_processing` JSON and surfaces item or tag ingredients, catalysts, byproducts, fluids, Thermal Flux cost/generation, heat, duration, and process notes in the shared Recipe Index.
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
- JEI and Terminal Recipe Index visibility for custom machine/process recipes.
- ECHO terminal guidance across Command, Progress, Intel, and System sections, including Command Deck, What Now, Mission Graph, Protocol Roadmap, Signal Leads, Route Map, POI Atlas, Recipe Index, Route Records, Survival Index, Faction Atlas, Vitals, Companion Link, Reward Inbox, Nexus Core, ORBITAL, and addon chapter surfaces.
- Drone repair/mode commands and Scout Drone fallback behavior.
- Faction NPC dialogue/contracts, faction standings, trader rewards, POI cache identity, scanner route profiles, underground guardian routes, Prime Relay warfront prep, Nexus path objectives, Warden reward-once behavior, post-Nexus Orbital presentation, and addon route entry visibility.
- Orbital route checks: Earth calibration, launch readiness, staged Emergency Rocket, Low Orbit, Station Relay repairs, Moon/Mars/Europa/Saturn/Titan route unlocks, Deep Space Protocol, ECHO-0, 21 survey records, three faction contracts, and final network seal.
- Agriculture Reclamation checks: seed capsule recovery, profiled planting, hydroponic growth persistence, Bio-Reactor and Compost Recycler outputs, gene stabilization, greenhouse zone scoring, Pollinator Drone service, Ashfall/restoration soil compatibility, and chunk-local restoration pressure.
- Stationfall checks: station boarding/return, nine section power states, crew logs, Signal Panic telemetry, Data Core AI override, Station Mother fight, Stationfall Blackbox recovery, and Nexus Protocol unlock.
- Nexus Protocol checks: Stationfall blackbox gate, Nexus Scanner Visor, Nexus Recycler, Corruption Filter, Nexus Field Stabilizer, Memory Decoder, Blackbox Monolith, Corruption Warden, Reality Forge, Core Access Key, Nexus Guardian, and Restore/Control/Destroy/Merge commit actions.
- Industrial checks: Thermal Flux generation, factory scans, duct networks, filter automation, scrubber safe zones, heat/overload handling, Nexus-thermal warning, Industrial POI locator hints, and Furnace Warden reward state.
- Blackbox checks: typed fragment decoding, memory stability/false signal pressure, Vault/Bunker/Labyrinth/Temple/Core Chamber monolith access, False ECHO, Command Remnant, Nexus Core Access Key, Nexus Guardian, Truth Engine directives, and support cache claims.
- Release-ops checks: `-PechoAddonSet=all` workspace build, resource validators, gameplay-data validator, runtime-log scan, jar-set check, and copy only to an explicitly configured Ashfall modpack profile after stale jars are removed.

Known constraints:

- New resource and POI distribution is most reliable in new chunks.
- The current release docs describe the full stack; use `-PechoAddonSet=beta` only for the smaller shared-core/Ashfall route build, and `-PechoAddonSet=all` when testing the full ecosystem.
- Drone control is terminal/direct-interaction based; the old standalone drone menu path is intentionally not exposed.
- Audio polish currently favors built-in Minecraft sound events over bundled custom sound files.

## Newly Active Service Addons (Audit Pass)

Build truth now includes `echopowergrid` `0.1.0`, `echosoundcore` `0.1.0`, `echotutorialcore` `0.1.0`, `echorelictech` `0.2.0-beta`, and `echoweathercore` `0.1.0` in the Gradle `all` stack. These modules are active but still have partial integration/audio/tutorial/weather surfaces documented in the ecosystem audit reports.
