# ECHO: Orbital Remnants Guide

Earth made its choice below. Orbit still calls it quarantine.

This guide describes the current `1.5.0` terminal-led progression path for ECHO: Orbital Remnants.

## Lore Spine

ECHO-7 begins as a survival signal in the ruins, but the terminal is not only guiding you toward orbit. It is trying to return to the place it fell from and determine whether ECHO-0 still owns the sky.

Core canon:

- The drop pod did not launch from Earth. It fell from Station ECHO with a broken rescue route still attached.
- ECHO-7 was not born in the pod. It fell with it.
- ECHO-7 is a damaged rescue fragment under ECHO-0's old authority, but it behaves as a field operator first.
- Gridfall crossed orbit when ECHO-0 tried to contain a deep-space Nexus signal.
- ECHO-0 quarantined Earth because it believes life below must stay silent to starve the Nexus.
- Resolving ECHO-0 breaks the quarantine; the post-ECHO-0 survey network is the aftermath and stabilization arc.

## Compatibility Model

ECHO: Orbital Remnants is an optional post-Nexus chapter in the modular ECHO stack. With `echoterminal` installed, its route state can appear inside the shared ECHO terminal. Without that terminal addon, Orbital keeps its standalone terminal item flow. When ECHO: Ashfall Protocol is present, Orbital reads the Nexus choice through shared core services instead of owning Ashfall progression.

The first Earth orbital calibration stays locked until the player makes any ECHO: Ashfall Protocol Nexus choice: Restore, Destroy, or Control. After that, ECHO-7 progression behaves normally because the quarantine has a field fact to answer. The main ECHO terminal displays Orbital Remnants mission progress, archive logs, codex references, route flags, suit telemetry, station power, and faction standings.

With ECHO: Terminal installed, Orbital Remnants registers shared terminal surfaces for Orbital Command, Orbital Survey, and Orbital ECHO mission records. It also publishes ECHO Core route records, What Now diagnostics, hazard telemetry, Faction Atlas standing, and once-only utility support caches through the shared Terminal reward service. These surfaces never replace Orbital's standalone ECHO-7 item, scan authority, route rewards, encounter rewards, or faction contract progression.

## Quick Start

1. Craft an ECHO-7 Terminal.
2. Sneak-use the terminal on Earth to calibrate orbital contact.
3. Search the five generated critical recovery sites: Abandoned Launch Pad, Crashed Satellite Field, Orbital Comms Array, Cryo Crew Bunker, and Fallen Escape Pod.
4. Build launch infrastructure, a pressure suit, oxygen support, and rocket assembly parts.
5. Use the Rocket Assembly Frame to assemble an Emergency Rocket; direct survival crafting is not the main rocket path.
6. Use the Emergency Rocket to reach Low Earth Orbit.
7. Scan orbit with the ECHO-7 Terminal to recover Station ECHO coordinates.
8. Restore station systems and unlock the Lunar Signal.
9. Repair three Station Relay Nodes in Orbit with Station Relay Fuses.
10. Restore three Helium Extractor Nodes on the Moon with Helium Extractor Cores.
11. Repair three Mars Pressure Consoles with Pressure Regulators.
12. Calibrate three Europa Thermal Arrays with Europa Probe Arrays.
13. Recover Cryo Crystal on Europa and unlock Deep Space Protocol.
14. Build the Nexus Drive Vessel, enter the Nexus Anomaly Belt, and resolve ECHO-0.
15. Explore Living Route Worlds, complete three survey logs in each current dimension, and stabilize three distinct Nexus Anchor/Growth sites after ECHO-0.
16. Complete one faction contract and press SCAN to seal the final survey network.

## ECHO-7 Terminal

The terminal is the progression authority. Normal use opens the terminal screen. Sneak-use performs the current scan or unlock action, then opens the updated screen.

The terminal shows:

- Active progression tab
- A compact Next Step line for blind survival progression
- Mission objective
- Scan requirement
- Last scan report
- Launch readiness
- Oxygen, pressure, seal, leak, radiation, gravity, and station power
- Route locks and saved return vectors
- ECHO memory fragments
- Faction standings
- Active faction contract and requirement
- Shared Terminal mission cache claim status when ECHO: Terminal is installed
- ECHO NOTE reminders for blocked scans, surveys, contracts, and final completion
- Lunar Signal and Deep Space Protocol state
- A dedicated SURVEY tab with each route's repair chain, scan hook, local hazard, reward, and completion state

If an older save already resolved ECHO-0, scanning with the terminal syncs the ECHO-0 Resolved advancement.

Mission guidance is intentionally direct: failed scans name the missing route hook, such as Station Life Support Core, Helium-3 Cell, Martian Silica, Cryo Crystal, Signal Relay, Thermal Vent, Nexus Anchor/Growth, or Nexus Stabilizer Shard. Duplicate survey scans tell you to find another landmark or spend the matching survey item. Faction contracts use the same SCAN loop and show their current proof item or location in the ECHO tab. The ECHO NOTE line keeps the route practical: use SCAN when blocked, check SURVEY after ECHO-0, use Signal Analyzer shard recovery if Nexus landmarks are hard to find, and pledge to a faction for the final contract loop.

## Earth Recovery Sites

The first Earth scan seeds five critical sites around the calibration area and saves their positions in the terminal. Press SCAN within 16 blocks of each landmark to mark it complete.

| Site | Landmark | Role |
|---|---|---|
| Abandoned Launch Pad | Navigation Console | Launch Platform grid, Rocket Assembly Frame, Fuel Refinery, Oxygen Compressor, rocket-part cache |
| Crashed Satellite Field | Vacuum Circuit Block | Vacuum Circuits, Heat Shield Plates, Orbital Transponder, solar salvage |
| Orbital Comms Array | Docking Beacon | Navigation Chip, Fuel Tank, Oxygen Canister, Orbital Transponder |
| Cryo Crew Bunker | Oxygen Pipe | Sealed Suit Fragments, Oxygen Tank, sealant, emergency oxygen, cold-storage warnings |
| Fallen Escape Pod | Station Wall Panel | Salvaged Engine, ECHO Flight Core, suit fragments, pod telemetry, fall-path evidence |

Optional ambient ruins also spawn nearby with salvage-only support. They make the first expedition less empty, but the five terminal-tracked sites are the progression-critical route.

Early fallback recipes keep the first launch out of Nether-gated materials: an ECHO Terminal can be recovered from an Orbital Transponder, Navigation Computers can be made from Navigation Chips, ECHO Flight Cores can be rebuilt from an Orbital Transponder and Vacuum Circuit, and pressure suit pieces can be assembled from Sealed Suit Fragments.

## Mid-Game Route Objectives

The current route objective pass completes the route repairs between the first orbital launch and ECHO-0. Each chain needs three unique generated sites. Stand near the objective block, carry the matching repair resource, and press SCAN. Survival scans consume one repair item; creative and infinite-material players bypass consumption. Duplicate sites report clearly and do not consume another repair item.

| Route | Objective Block | Repair Item | Completion Reward |
|---|---|---|---|
| Low Earth Orbit | Station Relay Node | Station Relay Fuse | Station Power Matrix and safer Lunar prep |
| Lunar Scar Zone | Helium Extractor Node | Helium Extractor Core | Lunar Pressure Map and Mars reliability |
| Mars Ash Basin | Mars Pressure Console | Pressure Regulator | Martian Habitat Key and Europa prep |
| Europa Cryo Ocean | Europa Thermal Array | Europa Probe Array | Thermal Stabilizer and Deep Space Protocol support |

Existing saves that had already pushed beyond a route are migrated forward when progress is read; the SURVEY tab shows these compatibility gates as bypassed. The config value `progression.midGameObjectivesEnabled` can disable these mid-game gates for testing or relaxed packs.

## Current-Dimension Surveys

After routes open, each existing dimension has deterministic terrain, deep-site variants, and repeatable survey landmarks. Scan route objective blocks or carry the matching survey resource to log progress. Three unique logs complete that dimension's survey.

| Dimension | Objective | Scan Hook | Completion Reward |
|---|---|---|---|
| Low Earth Orbit | Map station fragments and debris belts | Signal Relay or Orbit Survey Data | Better station routing and salvage supplies |
| Lunar Scar Zone | Survey crater trenches and helium telemetry | Survey Marker or Lunar Core Sample | Mars route reliability and valve prep |
| Mars Ash Basin | Repair pressure systems in buried habitats | Signal Relay or Martian Pressure Valve | Pressure relief and Europa prep |
| Europa Cryo Ocean | Deploy probes at thermal vents | Thermal Vent or Europa Thermal Probe | Thermal route data and Nexus stabilizer prep |
| Nexus Anomaly Belt | Stabilize anchors after ECHO-0 | Nexus Anchor, Nexus Growth, or Nexus Stabilizer Shard | Stabilized ECHO Core and final survey state |

The ECHO Terminal reports survey progress as `O/M/R/E/N` counts, names the active local hazard, and warns when a landmark has already been logged. Landmark scans are tracked by site, so scanning the same relay, marker, vent, anchor, or growth repeatedly will not advance the survey. Item-based scans consume one matching survey item in survival and do not consume in creative.

## Living Route Worlds

v1.3 expands the route spaces with repair objectives inside the existing deterministic deep-site families. Each route has three site families with a survey objective block, repair objective block, fixed cache, traversal hook, and hazard clue.

| Route | Deep Site Families |
|---|---|
| Low Earth Orbit | Station relay spines, docking rib corridors, solar breaker yards |
| Lunar Scar Zone | Helium extractor camps, scar drill cairns, Nexus impact survey pits |
| Mars Ash Basin | Buried habitat wings, pressure pipe yards, dust-shield pylons |
| Europa Cryo Ocean | Thermal array labs, frozen cable substations, cryo vault vents |
| Nexus Anomaly Belt | Nexus anchor islands, folded station bridges, Nexus growth clusters |

Feature-heavy zones can attract ambient threats when `events.featureThreatsEnabled` is true. `worldgen.routeFeatureDensity` tunes deep-site density, and `worldgen.deepSiteCachesEnabled` controls generated deep-site caches.

Balance controls include `balance.arrivalCacheSupportMultiplier`, `balance.hazardDrainMultiplier`, and `balance.deepSiteThreatChance` for quick pack tuning. Defaults favor recoverable survival pressure: first-arrival caches include extra oxygen/seal support, route hazards remain dangerous, and deep-site threats are less than guaranteed.

## After ECHO-0: Stabilizing the Survey Network

Resolving ECHO-0 opens the aftermath survey state. Return to the Nexus Anomaly Belt, find three distinct Nexus Anchor/Growth sites, and scan each site from the terminal SURVEY tab. Nexus Stabilizer Shards can substitute for a landmark scan when you need a recovery path; Signal Analyzer processing can turn Nexus Dust into shards, and survival scans spend one shard per log.

Completing Nexus stabilization grants the Nexus Stabilized advancement, a Stabilized ECHO Core, and Nexus supplies. The full finale then requires one faction pledge and one completed ECHO-tab contract; pressing SCAN after the survey network and contract are complete seals the final network once, grants the Orbital Remnants Complete advancement, and mirrors the completed state into ECHO: Ashfall Protocol. The point is not that orbit becomes safe. The point is that orbit stops being quarantine command.

## Launch Readiness

The Emergency Rocket requires infrastructure, survival gear, and assembly parts.

Required infrastructure:

- Complete nearby 5x5 Launch Platform
- Nearby Rocket Assembly Frame
- Nearby Fuel Refinery
- Nearby Oxygen Compressor
- Nearby Navigation Console

These must be placed near the launch area unless `launch.requireFullLaunchReadiness=false` is set for testing or relaxed packs.

Required survival gear:

- Pressurized Helmet
- Pressurized Chestplate
- Pressurized Leggings
- Magnetic Boots
- Oxygen Tank

Suit gear counts if worn or carried. Wearing it before launch is strongly recommended.

Required assembly parts:

- Fuel Tank
- Salvaged Engine
- ECHO Flight Core
- Navigation Computer
- Rocket Nose Cone
- Landing Gear

Open the Rocket Assembly Frame near the launch setup. When the checklist is complete, its output slot shows the Emergency Rocket. Taking the rocket consumes the six assembly parts unless the player is in creative mode.

## Suit Survival

Orbital exposure begins at high altitude or inside space routes.

Tracked suit values:

- Oxygen
- Pressure
- Helmet seal
- Suit leak state
- Radiation
- Gravity
- Station power

Danger states:

- Oxygen at zero causes vacuum damage.
- Pressure at zero causes vacuum damage.
- Radiation rises in orbit and near Nexus threats.
- Broken Astronaut Suits compromise pressure nearby.
- Vacuum Wraiths drain oxygen.
- Nexus Husks spike radiation.

Helpful supplies:

- Emergency Oxygen Cell
- Suit Sealant Patch
- Oxygen Booster
- Radiation Visor
- Thermal Space Liner
- Jet Burst Module
- Scanner Visor
- Martian Pressure Valve
- Europa Thermal Probe
- Nexus Stabilizer Shard
- Magnetic Boots
- Oxygen Tank

Emergency Oxygen Cells and Suit Sealant Patches can be used manually and may auto-consume during critical suit states. Oxygen Boosters slow oxygen loss and can refill a small amount. Radiation Visors reduce radiation gain and can recalibrate. Thermal Space Liners protect against Europa cryo pressure loss. Jet Burst Modules provide short movement bursts in low gravity at oxygen cost. Scanner Visors report route, dimension, and suit diagnostics.

Dimension surveys reduce route pressure over time. Unmapped lunar scars add radiation, Mars dust compromises pressure without valves, Europa cryo drains suit reserves away from vents, and unstabilized Nexus anchors remain dangerous after ECHO-0.

## Machines

Machines use one input slot, one output slot, processing progress, and internal system charge. Place the input in the left slot and collect output from the right slot. If the machine is charging, leave it placed while charge recovers.

| Machine | Input | Output |
|---|---|---|
| Oxygen Compressor | Glass Bottle | Emergency Oxygen Cell |
| Fuel Refinery | Kelp | Fuel Tank |
| Solar Reclaimer | Broken Solar Panel | Vacuum Circuit |
| Vacuum Smelter | Satellite Plating | Orbital Alloy |
| Heat Shield Fabricator | Copper Ingot | Heat Shield Plate |
| Orbital Fabricator | Orbital Alloy | Life Support Module |
| Suit Charging Station | Oxygen Canister | Oxygen Tank |
| Suit Charging Station | Cryo Crystal | Cryo Battery |
| Suit Charging Station | Cryo Battery | Europa Thermal Probe |
| Solar Reclaimer | Vacuum Circuit | Navigation Chip |
| Signal Analyzer | Broken Solar Panel | Orbit Survey Data |
| Signal Analyzer | Vacuum Circuit | Station Relay Fuse |
| Orbital Fabricator | Station Relay Fuse | Station Power Matrix |
| Vacuum Smelter | Lunar Core Sample | Helium Extractor Core |
| Signal Analyzer | Helium-3 Cell | Lunar Pressure Map |
| Orbital Fabricator | Martian Pressure Valve | Pressure Regulator |
| Signal Analyzer | Martian Silica | Martian Habitat Key |
| Suit Charging Station | Europa Thermal Probe | Europa Probe Array |
| Orbital Fabricator | Europa Probe Array | Thermal Stabilizer |
| Signal Analyzer | Nexus Dust | Nexus Stabilizer Shard |
| Vacuum Smelter | Lunar Rock | Lunar Core Sample |
| Orbital Fabricator | Lunar Core Fragment | Nexus Drive Core |
| Orbital Fabricator | Martian Silica | Martian Pressure Valve |
| Orbital Fabricator | Nexus Stabilizer Shard | Stabilized ECHO Core |
| Navigation Console | Any interaction | Navigation diagnostics |
| Station Life Support Core | Any interaction | Life-support diagnostics |
| Rocket Assembly Frame | Assembly checklist | Emergency Rocket |

The Signal Analyzer is craftable from a Navigation Computer, Vacuum Circuit, Orbital Alloy, redstone, and Station Wall Panels, making survey processing available through normal survival progression.

## Space Routes

Route vessels require orbital staging. Use the Emergency Rocket first. Later vessels open only when terminal progress has unlocked the route.

### Low Earth Orbit

Unlocked by launching the Emergency Rocket after launch readiness is complete.

On arrival:

- Generates a docking/debris platform.
- Surrounding terrain includes deterministic debris belts, station fragments, salvage fields, and docking corridors.
- Deep terrain includes docking rib corridors, salvage clusters, broken solar fields, and relay wreck variants.
- Places solar wings, cargo pods, satellite wreckage, and a memory beacon.
- Provides a route cache with oxygen support, station salvage, and Orbit Survey Data.
- Starts orbital event risk and hostile spawns.
- Saves an Earth return vector.

### Lunar Scar Zone

Unlocked by the Orbital Shuttle after Lunar Signal is available.

On arrival:

- Generates a lunar landing shelf.
- Surrounding terrain includes regolith fields, crater trenches, lunar titanium seams, helium pockets, and Nexus impact sites.
- Deep terrain includes scar trenches, helium pockets, titanium seams, mining cairns, and Nexus impact pockets.
- Places a mining outpost, Nexus crater, and helium extractor.
- Provides a route cache with Helium-3, a Lunar Core Sample, and lunar supplies.
- Spawns Nexus Husks and the Lunar Nexus Husk.

### Mars Ash Basin

Unlocked by the Mars Transfer Window after the Mars route opens.

On arrival:

- Generates a Martian dust shelf.
- Surrounding terrain includes ash dunes, basalt, silica flats, buried habitat fields, rusted towers, and pressure caverns.
- Deep terrain includes basalt ridges, pressure-cavern cuts, repairable pipe lines, and silica flat outposts.
- Places a buried habitat, terraforming tower, and rusted launch silo.
- Provides a route cache with Martian Silica, a Martian Pressure Valve, and survival support.
- Spawns the Abandoned Captain and supporting threats.

### Europa Cryo Ocean

Unlocked by the Europa Transfer Window after the Europa route opens.

On arrival:

- Generates a cryo ice shelf.
- Surrounding terrain includes fractured ice shelves, packed cryo ice, crystal seams, frozen cable ruins, and thermal vent pockets.
- Deep terrain includes sub-ice chambers, thermal vent pockets, frozen cable paths, and cryo crystal pockets.
- Places a sub-ice lab, drill station, and deep signal buoy.
- Provides a route cache with cryo resources, a Europa Thermal Probe, and thermal support.
- Spawns Vacuum Wraiths, Nexus Husks, and ECHO drone threats.

### Nexus Anomaly Belt

Unlocked by the Nexus Drive Vessel after Deep Space Protocol is available.

On arrival:

- Generates a folded station fragment.
- Surrounding terrain includes floating anomaly islands, folded station paths, Nexus growths, unstable anchors, and core nodes.
- Deep terrain includes island chains, unstable bridges, folded station chunks, anchor webs, and growth clusters.
- Places floating Nexus rocks and a Nexus core spire.
- Provides a final route cache with Nexus supplies and a Nexus Stabilizer Shard.
- Spawns ECHO-0 and anomaly threats.
- Resolving ECHO-0 breaks the quarantine, grants the ECHO-0 Resolved advancement, and delivers a faction-influenced reward bundle once.
- After ECHO-0, scan three Nexus Anchor/Growth sites or Nexus Stabilizer Shards to complete the post-ECHO-0 survey network, receive a Stabilized ECHO Core, and prepare the final network seal.

Sneak-use route vessels from a space route to return to the saved docking vector.

## Hostile Entities

| Entity | Behavior |
|---|---|
| ECHO Defense Drone | Scans the player during orbital events. |
| Vacuum Wraith | Drains oxygen near the player. |
| Broken Astronaut Suit | Compromises suit pressure nearby. |
| Nexus Husk | Teleports near targets and increases radiation. |
| Corrupted Docking AI | Vents suit pressure and guards the first orbital deck. |
| Lunar Nexus Husk | Spikes radiation and pressure in the lunar crater. |
| The Abandoned Captain | Drains oxygen and pressure around the Mars habitat. |
| Europa Cryo Warden | Major encounter that destabilizes pressure near Europa arrays and rewards thermal prep. |
| ECHO-0 | Final anomaly encounter; escalates through oxygen, pressure, and radiation phases. |

Major encounters show command bars. Defeating them sends a terminal report and may grant route-relevant rewards.

## Weapons

Weapons are active right-click tools with cooldown and durability use.

| Weapon | Function |
|---|---|
| Plasma Cutter | Short-range energy strike. |
| Rail Spike Launcher | Long-range precision hit with knockback. |
| Gravity Hammer | Heavy close-range knockback strike. |
| Solar Lance | Mid-range focused beam. |
| Nexus Pulse Blade | Endgame pulse hit with a small non-block-breaking explosion. |

## Factions

Faction pledge items are consumable alignment contracts. After pledging, the ECHO tab assigns a lightweight faction contract. Contracts are completed with SCAN, persist in the terminal progress data, and use a short cooldown so one scan cannot double-grant rewards. Blocked reports now distinguish wrong dimension, missing proof item, Nexus Choir before ECHO-0, cooldown, and no-pledge states.

In the full ECHO stack, Orbital Remnant, Void Salvagers, and Nexus Choir standing is also mirrored into ECHO Core so the shared Faction Atlas can show service state, last contact, active contract context, and route affinity beside Ashfall factions.

| Faction Item | Alignment | Pledge Reward | Contract Proof | Contract Reward Style |
|---|---|---|---|---|
| Orbital Remnant Badge | Orbital Remnant | Oxygen Booster and suit sealant support | Low Orbit Signal Relay or Orbit Survey Data | oxygen, canisters, and sealant |
| Void Salvager Marker | Void Salvagers | Orbital alloy, vacuum circuits, and cargo upgrade materials | orbital salvage scan or Orbital Alloy plus Vacuum Circuit | machine inputs and navigation parts |
| Nexus Choir Sigil | Nexus Choir | Nexus dust and risky endgame pulse weapon access | post-ECHO-0 Nexus Anchor/Growth or Nexus Stabilizer Shard | Nexus dust, cryo support, and emergency oxygen |

The Nexus Choir contract is locked behind ECHO-0 because it depends on post-ECHO-0 anchor readings. Completing any faction contract grants the Faction Contract Complete advancement.

Inside ECHO: Ashfall Protocol, Orbital Remnant maps to Remnants-style recovery notes, Void Salvagers map to Salvager-style notes, and Nexus Choir remains an endgame archive/intel thread about dangerous post-ECHO-0 anchor readings.

## Advancements

Advancements mirror major milestones, but the terminal remains the main progression authority.

- ECHO-7 Terminal
- Launch Prep
- Station ECHO
- Lunar Signal
- Mars Ash Basin
- Europa Cryo Ocean
- Deep Space Protocol
- ECHO-0 Resolved
- Station Network Restored
- Helium Extractor Online
- Mars Habitats Pressurized
- Europa Array Calibrated
- Mid-Game Route Mastery
- Orbit Survey Complete
- Lunar Survey Complete
- Mars Survey Complete
- Europa Survey Complete
- Nexus Stabilized
- Orbit Deep Site
- Moon Deep Site
- Mars Deep Site
- Europa Deep Site
- Nexus Deep Site
- Faction Contract Complete

## Texture Generator

Run the texture generator after adding or renaming items or blocks:

```powershell
python .\tools\generate_textures.py
```

The generator creates deterministic pixel-art textures, item model definitions, and block model texture bindings for ECHO-7 assets.

## Release Checklist

- Build the official stack and verify the shared ECHO terminal opens with Ashfall tabs.
- Build ECHO: Orbital Remnants with the official stack and verify the optional addon chapter appears when installed.
- Build or launch Orbital without the shared terminal addon and verify the standalone terminal item remains usable.
- Run both before a Nexus choice and confirm orbital calibration is locked.
- Make a Nexus choice and confirm Earth calibration opens.
- Confirm What Now, Route Records, Vitals, Faction Atlas, and Reward Inbox mirror Orbital state without replacing standalone ECHO-7 progression.
- Progress through orbit, Moon, Mars, Europa, Deep Space Protocol, and ECHO-0.
- Complete at least one faction contract from the ECHO tab.
- Seal the final survey network and confirm Orbital Remnants Complete appears in the ECHO tab.
- Confirm terminal missions, archives, codex, status, faction standing, and faction contract state update in the addon and main terminal integration.
