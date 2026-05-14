from __future__ import annotations

import hashlib
import math
import textwrap
from dataclasses import dataclass
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageFilter


ROOT = Path(__file__).resolve().parents[1]
START = "<!-- CURSEFORGE_README_START -->"
END = "<!-- CURSEFORGE_README_END -->"


@dataclass(frozen=True)
class AddonPage:
    folder: str
    mod_id: str
    name: str
    version: str
    tagline: str
    short: str
    overview: tuple[str, ...]
    features: tuple[str, ...]
    how_it_plays: tuple[str, ...]
    required: tuple[str, ...]
    recommended: tuple[str, ...]
    compatibility: tuple[str, ...]
    feature_labels: tuple[str, ...]
    theme: tuple[str, str, str]
    visual_terms: tuple[str, ...]


ADDONS: tuple[AddonPage, ...] = (
    AddonPage(
        folder="echoagriculturereclamation",
        mod_id="echoagriculturereclamation",
        name="ECHO: Agriculture Reclamation",
        version="1.0.0",
        tagline="Recover life from ash, stabilize ruined crops, and turn hostile soil into local restoration zones.",
        short="Field agriculture recovery with seed vaults, hydroponics, greenhouse zones, Pollinator Drones, and local soil restoration.",
        overview=(
            "ECHO: Agriculture Reclamation is the field recovery chapter for players who want the ruined world to answer back with life instead of free vanilla abundance. It adds profiled seed recovery, contaminated and stabilized genetics, soil purification, hydroponic growth, greenhouse scoring, and Pollinator Drone service as a practical survival loop.",
            "The addon is built for the ECHO stack but stays focused on local recovery. It does not rewrite entire biomes or hand the player a clean world. Instead, progress is earned block by block and chunk by chunk through seed capsules, purification enzymes, Bio-Gel, greenhouse control, and crop utility chains.",
            "With ECHO Terminal installed, Agriculture Reclamation publishes FIELD > Reclamation records, diagnostics, milestones, and support-cache hooks so the ecology route feels like a first-class chapter beside Ashfall, Orbital, Nexus, and Industrial systems.",
        ),
        features=(
            "Recovered Seed Capsules that identify into profiled contaminated seeds.",
            "Hydroponic Trays, Seed Vault Terminals, Soil Purifiers, Bio-Reactors, Gene Stabilizers, and Compost Recyclers.",
            "Greenhouse Controllers that score local protection, filter support, tray coverage, and sealed glass envelopes.",
            "Pollinator Drone Docks that deploy bound drones for crop and tray service.",
            "Useful crops including Ash Wheat, Hardroot, Glow Beans, Radleaf, Cryo Moss, Clean Corn, Medicinal Aloe, Filter Reed, Nexus Orchid, and Signal Fungus.",
            "Soft compatibility with Ashfall soils, Nexus recovery routes, Orbital resources, and Industrial support loops.",
        ),
        how_it_plays=(
            "Recover or craft a Seed Capsule, identify a seed profile, plant it in compatible soil or a Hydroponic Tray, then harvest crop matter for Bio-Gel and nutrient loops.",
            "Stabilize genetics with Gene Samples or Bio-Gel, build a safer greenhouse, deploy Pollinator Drones, and raise local restoration pressure without bypassing the wider survival game.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal for missions, records, diagnostics, and support caches", "ECHO: Ashfall Protocol for ruined soils and survival context", "ECHO: Industrial Nexus for infrastructure support"),
        compatibility=("Optional integration is registry-id based so the addon can load safely without sibling chapters.", "Restoration is intentionally local and does not replace normal biome generation."),
        feature_labels=("Seed Recovery", "Hydroponics", "Greenhouse Zones", "Pollinator Drones"),
        theme=("#173820", "#69d36f", "#f4d35e"),
        visual_terms=("seed", "hydroponic", "greenhouse", "pollinator", "bio", "soil"),
    ),
    AddonPage(
        folder="echoarmory",
        mod_id="echoarmory",
        name="ECHO: Armory",
        version="1.0.0",
        tagline="Build modular weapons, survival armor, recharge stations, and faction-aware loadout tools for the ECHO endgame.",
        short="Weapons, armor, modules, recharge systems, workstations, and loadout hooks for ECHO combat progression.",
        overview=(
            "ECHO: Armory adds a dedicated combat equipment layer for the ECHO ecosystem. It centers on workstations, alloy components, energy cores, modular weapons, survival armor, and upgrade tools that let a player turn recovered technology into field-ready gear.",
            "The addon is designed to sit across the release chain: Ashfall expeditions need stronger survival equipment, Logistics can organize loadouts, Industrial can feed component production, Orbital and Stationfall benefit from specialized protection, and Nexus or Blackbox routes can gate stranger technology.",
            "Armory is not just a list of stronger swords. Its identity is modular preparation: build the workstation network, craft the core gear, slot useful modules, keep energy systems charged, and bring the right kit for the route ahead.",
        ),
        features=(
            "Armory Bench, Weapon Forge, Armor Forge, Module Upgrade Table, Sigil Engraver, Loadout Terminal, and charging support.",
            "Weapons such as Alloy Sword, Frost Blade, Veil Sabre, Harmonic Staff, Arcane Dagger, Energy Rifle, Veil Bow, Convergence Gun, Resonance Hammer, Sigil Chakram, and Construct Gauntlet.",
            "Armor and survival modules including thermal protection, gas mask modules, radiation shielding, mobility servos, drone docks, and orbital boots.",
            "Energy Core Charging Station and ammo or crystal support for advanced gear.",
            "Soft integrations with Terminal, Logistics Network, Industrial Nexus, Orbital Remnants, Stationfall, Nexus Protocol, and Blackbox Protocol.",
        ),
        how_it_plays=(
            "Recover rare components, craft the Armory workstation chain, assemble weapons and armor, then improve them through elemental cores, sigils, and route-specific survival modules.",
            "Use loadout and logistics hooks to make equipment prep part of the wider ECHO mission flow instead of a separate gear grind.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Logistics Network for loadout handling", "ECHO: Industrial Nexus for component manufacturing context", "ECHO: Terminal for shared chapter visibility"),
        compatibility=("Sibling chapter integrations are optional and guarded.", "Armory is intended to complement route difficulty rather than replace chapter-specific rewards."),
        feature_labels=("Weapon Forges", "Armor Modules", "Energy Recharge", "Loadout Hooks"),
        theme=("#2d1d22", "#ef4f64", "#7dd6ff"),
        visual_terms=("armory", "sword", "rifle", "armor", "core", "module"),
    ),
    AddonPage(
        folder="echoblackboxprotocol",
        mod_id="echoblackboxprotocol",
        name="ECHO: Blackbox Protocol",
        version="1.0.0",
        tagline="Decode forbidden memory, survive archive dungeons, assemble the Core Access Key, and choose the final directive.",
        short="Late-game memory finale with typed Blackbox fragments, archive dungeons, boss proofs, the Truth Engine, and final directives.",
        overview=(
            "ECHO: Blackbox Protocol is the late-game memory chapter of the ECHO saga. It turns recovered evidence into a full route of typed fragments, decoded records, memory instability, hostile recordings, archive dungeons, boss proof, and final directive choices.",
            "Players decode Personal, ECHO, Security, Command, Core, and Deleted fragments, then push deeper through the Blackbox Vault, Command Bunker, Memory Labyrinth, Core Access Temple, and Nexus Core Chamber. Each step asks whether the player is recovering truth or giving the old systems another way in.",
            "The chapter culminates in the Truth Engine and the Restore, Control, Destroy, or Merge directive set. It is meant for packs that want a final ECHO ending with evidence, consequence, and a clean handoff from Stationfall and Nexus Protocol.",
        ),
        features=(
            "Typed Blackbox fragments and matching Memory Records.",
            "Blackbox Decoder, Memory Projector, Archive Terminal, Core Key Assembler, Truth Engine, Memory Stabilizer, and Protocol Extractor.",
            "Archive dungeon gates using Vault, Bunker, Labyrinth, Temple, and Core Chamber Monoliths.",
            "Boss and threat chain including Archive Husk, Security ECHO, Memory Parasite, Blackbox Sentinel, False ECHO, Command Remnant, and Nexus Guardian.",
            "Core Access Key assembly from logs, boss proofs, identity fragments, command keys, and protocol schematics.",
            "Final directives for Restore, Control, Destroy, and the secret Merge route.",
        ),
        how_it_plays=(
            "Decode fragments to reveal records, manage memory stability and false signal pressure, clear archive dungeon proof chains, then assemble the Nexus Core Access Key.",
            "Use the Truth Engine only when you are ready to commit a final directive for the save's ECHO memory state.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal for records, chapter pages, and actions", "ECHO: Stationfall for the blackbox handoff", "ECHO: Nexus Protocol for Core context"),
        compatibility=("Orbital Remnants, Stationfall, and Nexus Protocol hooks are optional where supported.", "The chapter is intended as late-game content and assumes the player has reached advanced ECHO infrastructure."),
        feature_labels=("Memory Fragments", "Archive Dungeons", "Boss Proofs", "Truth Engine"),
        theme=("#121017", "#a666ff", "#5df2ff"),
        visual_terms=("blackbox", "memory", "truth", "monolith", "key", "core"),
    ),
    AddonPage(
        folder="echoblockworks",
        mod_id="echoblockworks",
        name="ECHO Blockworks",
        version="1.0.0",
        tagline="A first-party structural block library for ECHO bases, ruins, stations, vaults, domes, and set pieces.",
        short="Decorative and structural block families for ECHO builds, ruins, command rooms, orbital interiors, and late-game facilities.",
        overview=(
            "ECHO Blockworks is the construction and set-dressing library for the ECHO and Ashfall ecosystem. It provides themed block families, structural variants, detail blocks, and conversion tools so bases and generated ruins can share a consistent visual language.",
            "Families cover reinforced metal, rusted metal, ashstone, charred concrete, terminal panels, ECHO circuits, orbital hulls, Nexus crystals, Blackbox vault materials, and reclamation glass. Structural families include slabs, stairs, and walls for production-ready building variety.",
            "The addon is valuable for players who like building functional outposts and for pack authors who want ECHO structures to feel connected across Ashfall, Orbital, Stationfall, Nexus, Industrial, Blackbox, and Agriculture content.",
        ),
        features=(
            "Large themed block families with full blocks plus structural variants where appropriate.",
            "ECHO Blockworks Table and Pattern Cutter conversion workflows.",
            "Detail blocks such as strip lights, warning beacons, server cabinets, cables, pipes, vents, debris, hologram projectors, and signal dishes.",
            "Family metadata for future unlock tier filtering and integration surfaces.",
            "Optional visibility in ECHO Index, Lens, and MultiblockCore style workflows.",
        ),
        how_it_plays=(
            "Craft or recover a family block, place the Blockworks Table, then convert blocks across variants and shapes to build clean command rooms, ruined streets, factory shells, orbital interiors, and sealed greenhouse structures.",
            "Use detail blocks to make functional bases look like they belong in the ECHO world instead of ordinary survival rooms with a new coat of paint.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Index for block family browsing", "ECHO: Lens for inspection support", "ECHO: MultiblockCore for future casing workflows"),
        compatibility=("Blockworks is safe to use as a visual library even when other chapter mods are missing.", "Themed blocks are first-party ECHO assets and are intended for both survival builds and generated structures."),
        feature_labels=("Block Families", "Structural Variants", "Detail Props", "Build Tools"),
        theme=("#26221e", "#e59a4b", "#7dd6ff"),
        visual_terms=("ashstone", "terminal", "circuit", "orbital", "blackbox", "glass"),
    ),
    AddonPage(
        folder="echoconvoyprotocol",
        mod_id="echoconvoyprotocol",
        name="ECHO: Convoy Protocol",
        version="1.0.0",
        tagline="Bring vehicles, fuel, cargo, checkpoint contracts, and roadside survival to the ruined Earth route.",
        short="Ruined-Earth convoy travel with vehicles, fuel, cargo systems, route contracts, checkpoints, and roadside threats.",
        overview=(
            "ECHO: Convoy Protocol turns long-distance ruined-world travel into a real gameplay layer. It adds vehicle kits, fuel support, cargo equipment, route beacons, roadside contracts, checkpoint logic, and travel hazards for players who want expeditions to feel planned, supplied, and risky.",
            "The addon is built around field vehicles such as the Scrap Bike, Wasteland Rover, Cargo Crawler, and Armored Relay Truck. Each one supports the fantasy of moving through a dangerous ECHO landscape with cargo, upgrades, repairs, and route infrastructure.",
            "Convoy Protocol ties naturally into Logistics, HoloMap, MultiblockCore, RenderCore, and Terminal surfaces, but it keeps the core loop grounded: build a vehicle, fuel it, maintain it, scan the route, carry supplies, and make the next checkpoint alive.",
        ),
        features=(
            "Vehicle Workbench, Fuel Still, Battery Charging Pad, Vehicle Dock, Vehicle Upgrade Bay, Convoy Beacon, Cargo Anchor, and Field Repair Station.",
            "Four vehicle families: Scrap Bike, Wasteland Rover, Cargo Crawler, and Armored Relay Truck.",
            "Vehicle kits, tires, frames, engine cores, batteries, fuel canisters, cargo nets, route beacons, and repair kits.",
            "Upgrade kits for speed, storage, armor, scanners, torque, relay arrays, and route endurance.",
            "Route contracts, checkpoint gates, roadside signal markers, and Terminal route status.",
        ),
        how_it_plays=(
            "Craft a vehicle kit, assemble and maintain the vehicle, fuel it through the Convoy support blocks, then use route tools to connect supply movement with actual wasteland traversal.",
            "Larger vehicles reward planning: they carry more, survive more, and turn remote expeditions into logistics runs instead of backpack sprints.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer", "ECHO: MultiblockCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for route reports", "ECHO: Logistics Network for supply movement", "ECHO: RenderCore for enhanced vehicle visuals"),
        compatibility=("Terminal, Logistics, and RenderCore integrations are optional.", "Vehicles are tuned for ruined-world traversal and may feel strongest in packs with large ECHO regions."),
        feature_labels=("Vehicles", "Fuel Chain", "Cargo Routes", "Checkpoints"),
        theme=("#2c2417", "#ffb13b", "#ff5548"),
        visual_terms=("vehicle", "fuel", "convoy", "beacon", "cargo", "tire"),
    ),
    AddonPage(
        folder="echodatacore",
        mod_id="echodatacore",
        name="ECHO: DataCore",
        version="1.0.0",
        tagline="Persistent player, world, and team data services for modular ECHO progression.",
        short="Shared persistent data service for player, world, team, progression, sync, and addon-owned state.",
        overview=(
            "ECHO: DataCore is the concrete persistence layer behind the lightweight data contracts exposed by ECHO: Core. It gives addons a safe way to register data keys, store player/world/team values, sync public state, and keep optional modules from hard-crashing when a backend is absent.",
            "The design keeps ownership clear. Addons define stable namespaced keys for their own state, Core exposes no-op-safe service access, and DataCore provides the real storage and sync behavior when installed.",
            "For players, DataCore is a library mod. For pack authors and addon developers, it is the part that makes cross-addon progression more reliable without forcing every gameplay chapter to reinvent persistence.",
        ),
        features=(
            "Player, world, and team data scopes for addon-owned keys.",
            "Registration helpers through ECHO Core services.",
            "Safe no-op fallback behavior when the backend is absent.",
            "NetCore-backed sync support for public progression and diagnostics.",
            "Stable key naming guidance for long-term save compatibility.",
        ),
        how_it_plays=(
            "Install it alongside ECHO gameplay chapters that need shared persistent state. Most players will never interact with DataCore directly; it quietly keeps route flags, unlocks, team data, and world records stable.",
            "Addon authors can depend on Core contracts and let DataCore supply the storage implementation.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for surfacing synced records and diagnostics",),
        compatibility=("Data keys should remain namespaced to the owning addon.", "If missing, Core no-op services avoid hard failure for optional integrations."),
        feature_labels=("Data Keys", "Player State", "World State", "Sync Bridge"),
        theme=("#182637", "#4ca3ff", "#73f4d0"),
        visual_terms=("data", "sync", "terminal", "key", "record"),
    ),
    AddonPage(
        folder="echoholomap",
        mod_id="echoholomap",
        name="ECHO: HoloMap",
        version="1.0.0",
        tagline="Discover terrain, plot markers, scan hazards, and read the ECHO world through a command-map interface.",
        short="Terminal-integrated command map for regions, routes, hazards, missions, scans, markers, waypoints, and minimap terrain.",
        overview=(
            "ECHO: HoloMap adds a tactical map layer for the ECHO stack. It samples discovered terrain around players, stores compact per-player map tiles, renders world markers, and provides map controls that make routes, hazards, missions, bases, anomalies, and scan data easier to understand.",
            "The addon is not a magical full-world reveal. Terrain is discovered from loaded chunks near the player, then rendered through a lightweight command-map view and minimap HUD. That makes exploration feel earned while still giving players a readable operational picture.",
            "HoloMap is especially useful in a modular ECHO pack because WorldCore markers, Convoy routes, Orbital scans, Ashfall hazards, Nexus anomalies, and Terminal objectives can all share one map vocabulary.",
        ),
        features=(
            "Surface-aware terrain tile discovery with height shading, water, shore, snow, ice, lava, and biome fallback handling.",
            "Terminal HoloMap tab with panning, zooming, recentering, search, marker filtering, and waypoint actions.",
            "Local, personal, and shared waypoints with dimension and coordinate data.",
            "Minimap HUD toggle with configurable zoom and screen position.",
            "Integration surfaces for WorldCore markers, Terminal pages, RenderCore visuals, routes, hazards, and addon scan markers.",
        ),
        how_it_plays=(
            "Explore the world to discover map tiles, open the ECHO Terminal HoloMap page, filter markers and hazards, set waypoints, and use the minimap for field navigation.",
            "The map becomes more valuable as the pack gains route systems, faction outposts, anomaly zones, and deep expedition targets.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for the main map surface", "ECHO: WorldCore for rich region and marker data", "ECHO: RenderCore for optional visual polish"),
        compatibility=("HoloMap terrain data is owned by HoloMap and does not force-load chunks.", "Core markers are drawn over discovered terrain when available."),
        feature_labels=("Terrain Tiles", "Waypoints", "Hazard Layers", "Minimap HUD"),
        theme=("#142a31", "#36e3c2", "#9cff6d"),
        visual_terms=("map", "marker", "terrain", "waypoint", "holo"),
    ),
    AddonPage(
        folder="echoindex",
        mod_id="echoindex",
        name="ECHO: Index",
        version="1.0.0",
        tagline="A shared item, recipe, usage, and archive reference layer for ECHO survival planning.",
        short="Searchable ECHO index for items, recipes, uses, sources, archive context, and Terminal reference surfaces.",
        overview=(
            "ECHO: Index is the reference layer for players who need to understand a large modular ECHO pack. It collects item entries, recipe and usage views, source notes, tracking actions, and addon-provided context into a searchable interface.",
            "The addon complements ECHO Terminal's Recipe Index and Lens inspection actions. Instead of forcing players to memorize which chapter owns every material, machine, cache, or process, Index gives the ecosystem a common lookup surface.",
            "For pack authors, ECHO: Index is a clean place to publish explainers for special recipes, locked outputs, machine categories, and non-vanilla acquisition routes without burying everything in tooltips.",
        ),
        features=(
            "Searchable item and usage reference for ECHO content.",
            "Recipes, uses, tracking, source records, and archive-style notes.",
            "Provider-backed data so each addon can publish its own process context.",
            "Optional Terminal and RenderCore integrations.",
            "Useful companion to Lens actions for recipes, uses, and tracking.",
        ),
        how_it_plays=(
            "Open the index surface, search an item or machine, inspect recipes and uses, then follow source notes to learn which route, process, or chapter unlocks the next step.",
            "When paired with Lens, looking at a target can jump directly into recipe, usage, or tracking context.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for integrated recipe browsing", "ECHO: Lens for inspection actions", "JEI alongside large modpacks"),
        compatibility=("Index providers are additive and should tolerate missing sibling addons.", "The addon is informational and does not replace recipe authority from owning mods."),
        feature_labels=("Item Search", "Recipe Uses", "Source Notes", "Tracking"),
        theme=("#172333", "#59d7ff", "#ffd166"),
        visual_terms=("index", "recipe", "item", "book", "terminal"),
    ),
    AddonPage(
        folder="echoindustrialnexus",
        mod_id="echoindustrialnexus",
        name="ECHO: Industrial Nexus",
        version="1.0.0",
        tagline="Rebuild factory power with Thermal Flux, machines, fluids, scrubbers, POIs, and the Furnace Warden.",
        short="Industrial automation chapter with Thermal Flux, machines, ducts, fluids, heat, scrubber safe zones, POIs, and boss progression.",
        overview=(
            "ECHO: Industrial Nexus turns Ashfall survival into infrastructure recovery. It adds Thermal Flux power, recipe-driven machines, sided automation, item ducts, Flux ducts, NeoForge fluid tanks and pipes, machine heat, Industrial Scrubbers, procedural industrial POIs, and Furnace Warden progression.",
            "The addon is built to support the rest of the ECHO stack. It can manufacture survival filters, pressure parts, launch components, station repairs, blackbox machinery pieces, Core key support, and late-game factory materials while still owning its own machine and safety loop.",
            "With ECHO Terminal installed, Industrial Nexus registers missions, records, support caches, factory scans, POI hints, actions, and a Recipe Index provider that reads industrial processing JSON into player-facing process notes.",
        ),
        features=(
            "Thermal Flux generators, capacitor banks, ducts, storage, controller scans, and generation stats.",
            "Recipe-driven machines with inputs, catalysts, outputs, byproducts, fluids, duration, heat, and TF cost or generation.",
            "NeoForge fluid tanks, cells, bucket workflows, tiered fluid pipes, filtering, transfer rates, loss, and hazardous leaks.",
            "Industrial Scrubber modes for air, radiation, blight, station support, and cooling safe zones.",
            "Procedural Abandoned Thermal Plants, Rusted Factory Complexes, Geothermal Drill Sites, Reactor Cooling Stations, and Nexus Heat Exchanger Ruins.",
            "Furnace Warden activation, phased fight state, participant reward credit, and once-only Terminal reward eligibility.",
        ),
        how_it_plays=(
            "Start with power and grinding, build machine chains, automate items and fluids, manage heat, place scrubbers for safer work zones, then push into industrial POIs for schematics and Warden progression.",
            "The more of the ECHO stack you run, the more Industrial Nexus becomes the practical backbone for filters, launch parts, station parts, and late-game infrastructure.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: MultiblockCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for missions, support caches, and Recipe Index visibility", "ECHO: RenderCore for visual polish", "ECHO: Ashfall Protocol for survival context"),
        compatibility=("Sibling chapter support is optional and reflection or registry guarded.", "Machine recipe authority stays with Industrial Nexus data."),
        feature_labels=("Thermal Flux", "Machines", "Fluid Pipes", "Scrubbers"),
        theme=("#2b2117", "#ff9b3d", "#5df2ff"),
        visual_terms=("flux", "machine", "duct", "pipe", "scrubber", "warden"),
    ),
    AddonPage(
        folder="echolens",
        mod_id="echolens",
        name="ECHO: Lens",
        version="1.0.0",
        tagline="A smart scanner HUD for blocks, entities, fluids, machines, progression, and addon diagnostics.",
        short="Modern inspection HUD with compact scans, expanded local details, server-verified Deep Scan, privacy rules, and provider APIs.",
        overview=(
            "ECHO: Lens gives players a modern inspection overlay for the ECHO stack. Look at a block, fluid, or entity to see compact public information, hold Shift for expanded local details, and hold the Deep Scan key for categorized server-verified facts.",
            "The addon is a framework as much as a UI. Other mods can register Lens providers that contribute structured rows, categories, tones, and optional actions without hardwiring their data into Lens itself.",
            "Lens is intentionally privacy-conscious. Local scans stay client-side, server Deep Scan requests are bounded and public-first, and inventory-like information follows configurable access policy rather than exposing private contents by default.",
        ),
        features=(
            "Compact, expanded, and Deep Scan HUD modes.",
            "Server-assisted public scan rows through NetCore.",
            "Configurable position, scale, opacity, theme, category visibility, row limits, and reduced motion.",
            "Provider registry for addon-contributed block, entity, fluid, machine, progression, and diagnostic sections.",
            "Optional ECHO Index recipe, use, and tracking actions from inspection targets.",
        ),
        how_it_plays=(
            "Install Lens, look at the world, and let the HUD tell you what matters. Hold Shift for more detail or Deep Scan for server-verified public facts when the target supports it.",
            "In a large ECHO pack, Lens becomes the fast answer for what a machine is doing, whether a target is public, and which addon owns the interaction.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Index for recipe/use/track actions", "ECHO: Terminal for shared settings context", "ECHO: RenderCore for optional client visual support"),
        compatibility=("Deep Scan respects server config, distance, rate limits, and protected-target redaction.", "Lens registers no player-facing blocks or items; it is a HUD and provider framework."),
        feature_labels=("Scanner HUD", "Deep Scan", "Provider API", "Privacy Rules"),
        theme=("#171d2d", "#71b7ff", "#ff6bc6"),
        visual_terms=("lens", "scan", "hud", "overlay", "target"),
    ),
    AddonPage(
        folder="echologisticsnetwork",
        mod_id="echologisticsnetwork",
        name="ECHO: Logistics Network",
        version="1.0.0",
        tagline="Organize supply crates, loadouts, courier drones, depots, and remote requests across ECHO bases.",
        short="Supply crates, labels, loadouts, drone delivery docks, route requesters, faction depots, and operations dashboards.",
        overview=(
            "ECHO: Logistics Network gives the ECHO stack a practical supply layer. It adds tagged storage, loadout cards, route manifests, remote request tablets, drone delivery docks, faction trade depots, restock stations, and networked logistics terminals.",
            "The addon is built for repeated field operations. Instead of digging through scattered chests before every expedition, players can mark supply categories, bind logistics endpoints, request loadouts, dispatch courier support, and keep faction or chapter supplies in known places.",
            "Logistics Network pairs naturally with Convoy Protocol, Armory, Industrial Nexus, Terminal, and late-route chapters because every one of those systems benefits from cleaner supply movement and better preparation.",
        ),
        features=(
            "Logistics Terminal, Supply Crate, Smart Storage Label, Drone Delivery Dock, Route Requester, Loadout Locker, Faction Trade Depot, Remote Reward Relay, and Auto-Restock Station.",
            "Supply Tags, Logistics Chips, Courier Drone Modules, Route Manifests, Loadout Cards, and Remote Request Tablets.",
            "Courier Drone entity support for network delivery fantasy.",
            "Network binding, endpoint scanning, supply categories, loadout cycling, and remote requests.",
            "Optional Terminal and Industrial Nexus integration with hooks for Orbital, Stationfall, Nexus, and Blackbox support.",
        ),
        how_it_plays=(
            "Create a logistics network, label storage by role, bind endpoints with manifests or request tablets, then use terminals and drones to keep expedition supplies moving.",
            "As your base grows, Logistics turns repeated prep into an operations loop: scan, request, restock, dispatch, and go.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal for operations visibility", "ECHO: Industrial Nexus for production support", "ECHO: Convoy Protocol for route hauling", "ECHO: Armory for loadouts"),
        compatibility=("Optional sibling chapter integrations are guarded.", "The system is designed for survival organization, not global free item teleportation."),
        feature_labels=("Supply Crates", "Loadouts", "Courier Drones", "Remote Requests"),
        theme=("#183025", "#54e38d", "#5ddcff"),
        visual_terms=("logistics", "crate", "drone", "loadout", "route", "terminal"),
    ),
    AddonPage(
        folder="echomissioncore",
        mod_id="echomissioncore",
        name="ECHO: MissionCore",
        version="1.0.0",
        tagline="Shared mission, objective, reward, action, and Terminal feed backend for ECHO addons.",
        short="Mission backend for chapter definitions, objectives, progression, rewards, actions, and Terminal mission feeds.",
        overview=(
            "ECHO: MissionCore is the shared backend for modular ECHO missions. It lets addons define chapters, phases, objectives, rewards, completion rules, repeat policies, and mission actions while keeping the public contracts available through ECHO Core.",
            "The goal is migration without chaos. Existing addon mission sets can be mirrored into MissionCore while owning addons keep their public mission IDs and save compatibility. Terminal-facing providers can draw from a common service instead of each chapter inventing its own state pipeline.",
            "Players experience MissionCore indirectly through better mission consistency. Pack authors and addon developers get cleaner registration, validation commands, objective reporting, reward claim handling, and future-ready mission graph surfaces.",
        ),
        features=(
            "Mission chapters, phases, definitions, objective types, rewards, completion rules, and repeat policies.",
            "Core service hooks for no-op-safe mission registration and progress reporting.",
            "Terminal feed support for shared mission presentation.",
            "Server-authoritative custom addon actions.",
            "Validation, inspect, start, complete, claim, and progress commands for development and operations.",
        ),
        how_it_plays=(
            "Install it as a library beside ECHO chapters that use shared mission state. Players will see the effect through cleaner mission feeds and reward behavior rather than a standalone block.",
            "Addon authors register mission content during setup and report objective progress through Core services.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal for mission browsing and action surfaces",),
        compatibility=("Owning addons should keep mission IDs stable for save compatibility.", "MissionCore no-ops safely through Core contracts when absent from optional callers."),
        feature_labels=("Chapters", "Objectives", "Rewards", "Actions"),
        theme=("#211b34", "#a78bfa", "#65e4ff"),
        visual_terms=("mission", "objective", "reward", "terminal", "chapter"),
    ),
    AddonPage(
        folder="echomultiblockcore",
        mod_id="echomultiblockcore",
        name="ECHO: MultiblockCore",
        version="1.0.0",
        tagline="Data-driven multiblocks, validation, robotics, workcells, and scanner contracts for the ECHO factory stack.",
        short="Shared multiblock validation and runtime framework with controllers, frames, robotic arms, task queues, and JSON definitions.",
        overview=(
            "ECHO: MultiblockCore provides a shared framework for larger ECHO machines and structures. It adds data-driven multiblock definitions, controller validation, runtime state, diagnostics, task queues, robotics parts, workcell concepts, and integration hooks for scanner, map, and terminal surfaces.",
            "The addon is built to keep complex structures from becoming one-off code in every chapter. A multiblock can be described in JSON, validated in world, reported through diagnostics, cached safely, and rebuilt across runtime versions with clearer ownership.",
            "For players, MultiblockCore is the foundation for bigger factory and infrastructure builds. For developers, it is the common language for signal towers, assembly lines, robot arms, crates, power buses, data buses, and future ECHO workcells.",
        ),
        features=(
            "ECHO Multiblock Controller, Signal Tower Core, Reinforced Frames, Signal Conduits, Power Bus, Data Bus, Input/Output Crates, Robotic Arms, and Machine Frames.",
            "Blueprint items and JSON definitions under data namespaces.",
            "Validation caching with matched positions, issue summaries, rotation/mirror information, and unloaded-area warnings.",
            "Robotic tool heads for gripping, welding, scanning, assembling, injecting, and cutting.",
            "Runtime schemas, task transactions, diagnostics, and optional Terminal/HoloMap/Lens hooks.",
        ),
        how_it_plays=(
            "Build a defined structure, place or use a controller, inspect the blueprint, resolve validation issues, then let the runtime handle workcell-style tasks and diagnostics.",
            "Large machines become understandable because the controller can explain what is missing and which parts are participating.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for diagnostics", "ECHO: HoloMap for marker context", "ECHO: Lens for inspection"),
        compatibility=("Definitions are namespaced JSON and can be supplied by other addons.", "Task transactions are designed to avoid item loss when outputs are blocked."),
        feature_labels=("JSON Structures", "Controllers", "Robotics", "Diagnostics"),
        theme=("#20282d", "#f1c857", "#8fd3ff"),
        visual_terms=("multiblock", "controller", "robotic", "blueprint", "signal", "frame"),
    ),
    AddonPage(
        folder="echonetcore",
        mod_id="echonetcore",
        name="ECHO: NetCore",
        version="1.0.0",
        tagline="Shared networking, sync, server actions, rate limits, and diagnostics for ECHO addons.",
        short="Packet bridge, sync helpers, optional-channel safe sends, server action validation, rate limits, and packet diagnostics.",
        overview=(
            "ECHO: NetCore is the networking foundation for the ECHO ecosystem. It centralizes packet registration patterns, clientbound sync helpers, serverbound action validation, rate limit policies, optional send handling, debug hooks, and client action helpers.",
            "The addon exists so gameplay chapters can focus on authority and state instead of duplicating fragile network glue. Serverbound packets represent intent only; handlers validate permissions, distance, ownership, inventory, menu state, and world state on the server.",
            "For players, NetCore is a library dependency. For developers, it is the standard way to keep optional addon channels, packet diagnostics, and action rate limits consistent across Terminal buttons, machine screens, scanner requests, and mission actions.",
        ),
        features=(
            "Optional packet registrar helpers for clientbound sync, serverbound actions, and debug packets.",
            "Rate-limited server action policies.",
            "Safe send helpers that catch missing-channel failures and emit debug events.",
            "Client action helpers for UI and terminal buttons.",
            "Shared network bridge for data, mission progress, visual state, machines, debug data, factions, and discovery toasts.",
        ),
        how_it_plays=(
            "Install it as a required library for ECHO modules that need shared packets or sync. It has no standalone survival loop, but it makes interactive ECHO screens and services behave safely.",
            "Addon developers should keep direct NeoForge packet registration inside NetCore patterns and validate all serverbound actions authoritatively.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal, HoloMap, Lens, DataCore, WorldCore, and any interactive ECHO chapter",),
        compatibility=("Debug packets are disabled by default and should require operator permissions when enabled.", "Optional sends should never crash missing consumers."),
        feature_labels=("Packets", "Sync", "Rate Limits", "Diagnostics"),
        theme=("#141b2d", "#4cd4ff", "#ff5fd2"),
        visual_terms=("net", "sync", "packet", "terminal", "network"),
    ),
    AddonPage(
        folder="echonexusprotocol",
        mod_id="echonexusprotocol",
        name="ECHO: Nexus Protocol",
        version="1.0.0",
        tagline="Enter Chapter IV: Nexus corruption, field stabilization, memory decoding, Core access, and final path commitment.",
        short="Nexus corruption chapter with Nexus Charge, field planning, corrupted biomes, machines, memory decoding, Guardian fights, and endings.",
        overview=(
            "ECHO: Nexus Protocol is Chapter IV of the ECHO collapse saga. It opens deeper Core corruption and memory systems after Stationfall or development unlocks, then pushes players through dirty charge, stabilization, signal towers, deleted history, monolith memory, reality forging, Core access, and final path decisions.",
            "The addon introduces Nexus Charge, corrupted materials, field stabilizers, memory decoders, a Reality Forge, corruption reactors, protocol seals, new biomes and structures, Nexus gear, and a smarter field-map risk layer. It is where the old ECHO infrastructure stops being background lore and starts rewriting the route.",
            "Nexus Protocol can commit Restore, Control, Destroy, or Merge. It respects Ashfall as the first irreversible Nexus choice while exposing deeper Core-state interpretations for the later stack.",
        ),
        features=(
            "Nexus Charge systems, scanners, stabilizers, charge tanks, infusers, corruption filters, memory decoders, and Reality Forge progression.",
            "Corrupted blocks, materials, fluids, armor, weapons, anchors, charges, membranes, glass, ferrite, and reality dust.",
            "Fractured Wasteland, Nexus Scar, Static Basin, Blackbox Forest, Core Exclusion Zone, and Nexus dimension content.",
            "Structures such as field stations, signal relay towers, data vaults, containment labs, Blackbox monoliths, and Core chambers.",
            "Threats including Nexus Husk, Data Wraith, Static Crawler, Core Soldier, Archive Seeker, Corruption Warden, and Nexus Guardian.",
            "Restore, Control, Destroy, and Merge path commitment support.",
        ),
        how_it_plays=(
            "Stabilize your camp, gather charge and corrupted materials, use machines to decode memory and forge reality-grade components, then unlock deeper Core access through monoliths and Guardian proof.",
            "The route expects planning: risk, charge, stabilizers, gear, and field-map decisions all matter before the final commitment.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Terminal for chapter missions and records", "ECHO: Orbital Remnants for previous route context", "JEI for recipe visibility"),
        compatibility=("Orbital and Terminal integrations are optional where configured.", "Nexus choices are intended to be consequential in the ECHO story chain."),
        feature_labels=("Nexus Charge", "Memory Decode", "Reality Forge", "Endings"),
        theme=("#201532", "#ad78ff", "#f8f7ff"),
        visual_terms=("nexus", "reality", "memory", "corruption", "charge", "crystal"),
    ),
    AddonPage(
        folder="echoorbitalremnants",
        mod_id="echoorbitalremnants",
        name="ECHO: Orbital Remnants",
        version="1.0.0",
        tagline="Leave ruined Earth, rebuild launch infrastructure, survive pressure routes, and confront ECHO-0.",
        short="Post-Nexus orbital survival chapter with launch prep, suits, route worlds, surveys, factions, bosses, and ECHO-0.",
        overview=(
            "ECHO: Orbital Remnants is the post-Nexus route chapter for the modular ECHO stack. After Earth makes its Nexus choice, ECHO-7 follows the pod's broken fall path back toward orbit, Station ECHO debris, route worlds, faction outposts, and the old ECHO-0 quarantine protocol.",
            "The addon adds ground recovery sites, launch infrastructure, pressure suit survival, oxygen systems, orbital machines, route vessels, station repairs, route surveys, deep sites, faction charters, and final network seal objectives. It is a route survival adventure rather than a generic planet pack.",
            "Orbital Remnants can run with its standalone ECHO-7 Terminal flow, but ECHO Terminal gives it a richer shared surface for Orbital Command, Survey, records, route status, suit telemetry, support caches, and faction reports.",
        ),
        features=(
            "ECHO-7 Terminal progression with Next Step, SCAN, SURVEY, route locks, launch readiness, and faction status.",
            "Emergency Rocket, Orbital Shuttle, transfer windows, Nexus Drive Vessel, launch platform, assembly frame, fuel refinery, oxygen compressor, and rocket parts.",
            "Suit survival with oxygen, pressure, helmet seal, leaks, radiation, gravity, station power, sealant, and suit modules.",
            "Routes through Low Earth Orbit, Station Network, Moon, Mars, Europa, Saturn, Titan, Deep Space Protocol, and the Nexus Anomaly Belt.",
            "Orbital factions, route NPCs, Tier I charters, support/barter services, route bosses, hostile anomalies, and ECHO-0.",
        ),
        how_it_plays=(
            "Calibrate Earth contact, scavenge launch sites, build suit and rocket infrastructure, launch, repair station route systems, unlock outer routes, complete surveys, finish faction charters, and seal the final network.",
            "The Terminal's route reports are the intended guide whenever a launch, route, suit, station, survey, or faction requirement is missing.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Ashfall Protocol for Nexus-choice gating", "ECHO: Terminal for shared command pages", "ECHO: Stationfall for the next chapter handoff"),
        compatibility=("Without ECHO Terminal, the standalone ECHO-7 Terminal item remains the command surface.", "With Ashfall installed, orbital calibration waits for any Nexus choice unless configured for standalone play."),
        feature_labels=("Launch Chain", "Suit Survival", "Route Worlds", "ECHO-0"),
        theme=("#101b2b", "#62b6ff", "#d6f0ff"),
        visual_terms=("rocket", "orbital", "suit", "oxygen", "saturn", "terminal"),
    ),
    AddonPage(
        folder="echorendercore",
        mod_id="echorendercore",
        name="ECHO: RenderCore",
        version="1.0.0",
        tagline="Shared visual state, animation, glow, overlay, particle, and profile tooling for advanced ECHO assets.",
        short="Lightweight rendering and animation framework with visual profiles, named parts, effects, previews, and validation reports.",
        overview=(
            "ECHO: RenderCore is the shared client visual framework for advanced ECHO assets. It provides visual state contracts, animation timelines, named-part transforms, glow and overlay layers, particle anchors, visual profiles, composition, previews, validation, and debug tooling without making gameplay modules depend on a heavy external animation library.",
            "RenderCore helps the ECHO stack present machines, vehicles, entities, terminals, and effects consistently. Addons can expose gameplay state on the common API, map it to profiles, and let client renderers compose animations, materials, lights, overlays, and particles from data.",
            "Players experience RenderCore as polish: more readable machines, cleaner state transitions, better animated objects, and richer effects in chapters that opt into it.",
        ),
        features=(
            "VisualState, VisualContext, VisualVariant, and advanced visual provider contracts.",
            "Animation clips, tracks, keyframes, timelines, controllers, blending, easing, model poses, and named-part transforms.",
            "Profile-driven layers, materials, anchors, particle options, composition, previews, screenshots, schemas, and validation reports.",
            "Glow and overlay render layers with effect pipeline support.",
            "Debug HUD, client commands, hot-swap and profile preview tooling for artists and developers.",
        ),
        how_it_plays=(
            "Install RenderCore with addons that use it. It has no standalone survival loop, but it upgrades how participating ECHO content looks and communicates state.",
            "Addon developers can keep server-safe state in common code and register client integrations only when RenderCore is present.",
        ),
        required=("ECHO: Core 1.0.0 or newer",),
        recommended=("ECHO: Convoy Protocol, Industrial Nexus, HoloMap, Index, and other visual-heavy ECHO modules",),
        compatibility=("Optional consumers should guard client integrations behind mod-loaded checks.", "Profiles are data-driven and designed for validation instead of silent failure."),
        feature_labels=("Visual State", "Animation", "Effects", "Previews"),
        theme=("#1b1830", "#ff65c9", "#69e6ff"),
        visual_terms=("render", "profile", "glow", "effect", "preview", "animation"),
    ),
    AddonPage(
        folder="echosignalos",
        mod_id="signalos",
        name="SignalOS",
        version="1.0.0",
        tagline="A full-screen computer OS for terminals, workstations, data drives, racks, notes, archives, and ECHO links.",
        short="Standalone Echo-compatible computer tech addon with desktop shell, apps, data drives, server racks, missions, archives, and diagnostics.",
        overview=(
            "SignalOS is an Echo-compatible computer tech addon for NeoForge. It adds terminal and workstation blocks, server racks, network relays, portable data drives, and a full-screen desktop shell with apps, notifications, settings, notes, files, logs, data vaults, ECHO links, missions, archives, rewards, and diagnostics.",
            "It is not a replacement for ECHO: Terminal. SignalOS owns the computer OS fantasy while still bridging into ECHO Core state and exposing compatible mission, archive, reward, and diagnostic surfaces.",
            "The 0.2 line focuses on real persistence and interaction: editable operator notes, drive records, rack bays, network discovery, player preferences, archive read state, mission claim state, pending reward counts, Java registration APIs, datapack JSON content, and a soft KubeJS bridge.",
        ),
        features=(
            "SignalOS Terminal, Workstation, Server Rack, Network Relay, and Data Drive.",
            "Desktop shell with app launcher, status bar, active app view, notifications, settings, and visual tokens.",
            "Built-in apps including Home, Files, Notes, Logs, Network Monitor, Settings, Data Vault, Echo Link, Missions, Archives, Rewards, and Diagnostics.",
            "Persistent operator notes, player preferences, archive read state, mission claimed state, and reward counts.",
            "Server-rack screen with drive bays, templates, rename, clear, copy, remove, and apply-template actions.",
            "Java registration APIs, datapack JSON loading, and KubeJS-friendly bridge access.",
        ),
        how_it_plays=(
            "Place a terminal or workstation, open the desktop shell, use built-in apps to manage notes and records, then expand into server racks, relays, drives, and networked data surfaces.",
            "Pack makers can register custom apps, chapters, missions, archives, diagnostics, and drive records using Java or datapack content.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("KubeJS for packs that want script-driven SignalOS content", "ECHO: Terminal when you want both command-terminal and computer-OS fantasies"),
        compatibility=("SignalOS uses one active app at a time rather than draggable windows.", "Notes and drive records are bounded for persistence safety."),
        feature_labels=("Desktop Shell", "Data Drives", "Server Racks", "Apps API"),
        theme=("#122a2f", "#42e8ff", "#8dff70"),
        visual_terms=("terminal", "workstation", "server", "drive", "network", "signalos"),
    ),
    AddonPage(
        folder="echostationfall",
        mod_id="echostationfall",
        name="ECHO: Stationfall",
        version="1.0.0",
        tagline="Board Station ECHO, restore failing sections, survive pressure and panic, and recover the blackbox.",
        short="Station ECHO horror chapter with boarding, section power, crew logs, oxygen, pressure, Signal Panic, AI override, and Station Mother.",
        overview=(
            "ECHO: Stationfall follows the orbital signal into Station ECHO. It is a contained horror-survival chapter about boarding a damaged station, restoring section power, reading crew logs, managing oxygen and pressure, surviving Signal Panic, overriding corrupted systems, and confronting Station Mother.",
            "The station is built around nine sections: Docking Ring, Crew Quarters, Hydroponics Bay, Medical Wing, Engineering Deck, Data Core, Observation Deck, Containment Wing, and Command Module. Each section can carry power, doors, records, pressure hazards, and route consequences.",
            "Recovering the Stationfall Blackbox records the handoff that opens deeper Nexus Protocol and Blackbox Protocol routes, making Stationfall the bridge between Orbital Remnants and the memory endgame.",
        ),
        features=(
            "Station Access Card, Pressure Seal Kit, Emergency Oxygen Pack, Station Battery, Hull Cutter, Crew Log Tablet, AI Override Chip/Core, Signal Panic Dampener, and Stationfall Blackbox.",
            "Station Power Nodes, Pressure Doors, Damaged Airlocks, Hull Breaches, Crew Log Terminals, Data Core Terminals, Command Consoles, Observation Glass, Containment Pods, and corrupted hydroponics.",
            "Threats including Hollow Crewman, EVA Stalker, Medical Husk, Hydroponic Growth, Maintenance Drone, Screaming Signal, Station Mimic, Suit Without a Body, and Station Mother.",
            "Section power/log recovery, oxygen, pressure, panic telemetry, AI override, boss state, return points, and blackbox recovery.",
            "Terminal integration for Stationfall state and handoff visibility.",
        ),
        how_it_plays=(
            "Board Station ECHO from the orbital route, restore power section by section, gather crew logs, manage oxygen and pressure tools, obtain the AI override, and start the Station Mother finale from the Command Console.",
            "Recover the blackbox to close Stationfall and unlock the next memory and Nexus escalation.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: Orbital Remnants 1.0.0 or newer"),
        recommended=("ECHO: Terminal for chapter state and records", "ECHO: Blackbox Protocol for the later memory finale", "ECHO: Nexus Protocol for the next route"),
        compatibility=("Stationfall is designed as a post-Orbital chapter.", "Blackbox recovery is the intended route handoff into later chapters."),
        feature_labels=("Station Boarding", "Section Power", "Signal Panic", "Blackbox Handoff"),
        theme=("#24171b", "#ff5964", "#83e9ff"),
        visual_terms=("station", "oxygen", "airlock", "crew", "blackbox", "mother"),
    ),
    AddonPage(
        folder="echoterminal",
        mod_id="echoterminal",
        name="ECHO: Terminal",
        version="1.0.0",
        tagline="The shared in-world command surface for missions, records, recipes, diagnostics, rewards, and addon chapters.",
        short="Shared ECHO terminal block, UI shell, mission browser, archive surface, recipe index, action routing, and addon integration hub.",
        overview=(
            "ECHO: Terminal is the player-facing command surface for the modular ECHO stack. It adds the ECHO Terminal block and screen, then gives installed chapters a common place to publish missions, records, diagnostics, route status, recipe references, rewards, actions, vitals, faction reports, and chapter pages.",
            "The terminal is organized around Command, Progress, Intel, and System sections. It can show a Command Deck, What Now guidance, Survival Route, Mission Graph, Mission Browser, Recipe Index, Field Archive, Route Records, Faction Atlas, Vitals, Reward Inbox, settings, and addon hubs.",
            "For players, Terminal answers the practical question: what should I stabilize, craft, scan, repair, or explore next? For addon authors, it is the shared presentation and action contract that keeps the ECHO stack from becoming separate disconnected screens.",
        ),
        features=(
            "ECHO Terminal block, menu, client screen, and configurable key binding.",
            "Command, Progress, Intel, and System shell with navigation profiles.",
            "Mission browser with filters, search, detail panes, keyboard navigation, reward claims, and server-authoritative actions.",
            "Recipe Index with ECHO item search, recipes/uses modes, provider categories, item details, machine/input/catalyst/output slots, process time, and locked hints.",
            "Field Archive, Route Records, Faction Atlas, Vitals, Reward Inbox, diagnostics, addon chapters, and transient mission HUD notices.",
            "Theme engine with Echo Console and Nexus Modpack presentation styles.",
        ),
        how_it_plays=(
            "Craft or place the ECHO Terminal, open it from the block or keybind, then use Command Deck and What Now for immediate direction. Progress and Intel pages handle route detail, records, recipes, factions, and chapter context.",
            "As more ECHO addons are installed, the terminal expands with their pages and providers without replacing the shared shell.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("All gameplay ECHO chapters", "ECHO: Index and JEI for recipe-heavy packs"),
        compatibility=("If an addon is missing, Terminal only shows registered providers that exist.", "Mission and recipe authority stays with owning addons."),
        feature_labels=("Command Deck", "Missions", "Recipe Index", "Addon Pages"),
        theme=("#10242d", "#4de3ff", "#ffd166"),
        visual_terms=("terminal", "mission", "recipe", "archive", "reward", "faction"),
    ),
    AddonPage(
        folder="echoworldcore",
        mod_id="echoworldcore",
        name="ECHO: WorldCore",
        version="1.0.0",
        tagline="Shared world regions, hazards, markers, discoveries, and events for ECHO exploration systems.",
        short="Foundation module for region definitions, world markers, hazard snapshots, structure discovery, and world event contracts.",
        overview=(
            "ECHO: WorldCore is the shared world vocabulary for the ECHO ecosystem. It does not own Ashfall worldgen, Convoy routes, Orbital debris, or Nexus structures. Instead, it provides common services and definitions so those systems can describe regions, markers, hazards, discoveries, and events consistently.",
            "WorldCore gives addons a safer way to publish region definitions, active region lookup, hazard definitions, current hazard snapshots, persistent world markers, per-player discovery, runtime events, validation commands, and optional Terminal or HoloMap feeds.",
            "For players, it is a library that makes exploration features line up. For pack authors, it is the reason a crash site, convoy route, orbital scan, anomaly zone, faction outpost, and hazard field can all participate in the same map and diagnostic language.",
        ),
        features=(
            "Region definitions and active region lookup.",
            "Hazard definitions and current hazard snapshots.",
            "Persistent markers for structures, crash sites, routes, debris, outposts, anomalies, and addon points of interest.",
            "Per-player discovery through ECHO Core discovery data and WorldCore saved data.",
            "Runtime bus events for region enter, discover, scan, marker reveal, and hazard changes.",
            "Optional Terminal status, HoloMap feed support, RenderCore profiles, and future AudioCore ambience profile resources.",
        ),
        how_it_plays=(
            "Install it with ECHO chapters that publish shared world telemetry. The result is cleaner maps, safer diagnostics, and route systems that can describe locations in a common way.",
            "Developers should call ECHO Core service accessors so optional integrations fall back to no-op world services when WorldCore is absent.",
        ),
        required=("ECHO: Core 1.0.0 or newer", "ECHO: NetCore 1.0.0 or newer"),
        recommended=("ECHO: Terminal for status surfaces", "ECHO: HoloMap for world marker display", "ECHO: Ashfall Protocol, Orbital Remnants, Convoy Protocol, and Nexus Protocol for rich world telemetry"),
        compatibility=("WorldCore is a foundation module and should not own sibling chapter worldgen.", "Missing implementation services fall back to no-op Core services."),
        feature_labels=("Regions", "Hazards", "Markers", "Discovery"),
        theme=("#16291f", "#6fe27e", "#5cbcff"),
        visual_terms=("world", "region", "hazard", "marker", "discovery", "map"),
    ),
    AddonPage(
        folder="signalosexample",
        mod_id="signalosexample",
        name="SignalOS Example Addon",
        version="1.0.0",
        tagline="A compact reference addon showing Java, datapack JSON, diagnostics, rewards, archives, and KubeJS-friendly SignalOS content.",
        short="Example SignalOS integration module with Java registration, JSON content, diagnostics, archives, missions, and script-friendly patterns.",
        overview=(
            "SignalOS Example Addon is a small reference module for pack authors and developers. It demonstrates how to register SignalOS chapters, missions, archives, diagnostics, rewards, and content from both Java and datapack JSON.",
            "The addon intentionally keeps its content simple so the integration shape is easy to copy. It includes Java and JSON chapters, quick missions, archive records, diagnostics providers, and KubeJS-friendly bridge usage examples.",
            "Most players do not need this in a final survival pack unless the pack wants a visible example chapter. Its real value is as documentation you can run.",
        ),
        features=(
            "Java chapter and mission registration examples.",
            "Datapack JSON chapter, mission, and archive content under the example namespace.",
            "Diagnostics provider example.",
            "KubeJS-friendly script shape through the SignalOS bridge.",
            "Fast completion hooks suitable for testing a normal world.",
        ),
        how_it_plays=(
            "Install it with SignalOS, open the SignalOS surfaces, and inspect the example Java and JSON content. Use the source files as a template for your own addon or pack scripts.",
            "Because it is a demonstration module, its content is intentionally small and direct.",
        ),
        required=("SignalOS 1.0.0 or newer",),
        recommended=("KubeJS if you want to test the script-friendly bridge",),
        compatibility=("This is a sample addon, not a required gameplay chapter.", "Keep it out of public packs unless you want example content visible to players."),
        feature_labels=("Java API", "JSON Content", "Diagnostics", "KubeJS Bridge"),
        theme=("#202038", "#b191ff", "#72f5d6"),
        visual_terms=("example", "signalos", "mission", "archive", "diagnostic"),
    ),
)


def hex_to_rgb(value: str) -> tuple[int, int, int]:
    value = value.lstrip("#")
    return int(value[0:2], 16), int(value[2:4], 16), int(value[4:6], 16)


def mix(a: tuple[int, int, int], b: tuple[int, int, int], t: float) -> tuple[int, int, int]:
    return tuple(int(a[i] * (1.0 - t) + b[i] * t) for i in range(3))


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    candidates = [
        r"C:\Windows\Fonts\segoeuib.ttf" if bold else r"C:\Windows\Fonts\segoeui.ttf",
        r"C:\Windows\Fonts\arialbd.ttf" if bold else r"C:\Windows\Fonts\arial.ttf",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ]
    for path in candidates:
        if Path(path).exists():
            return ImageFont.truetype(path, size=size)
    return ImageFont.load_default()


FONT_TITLE = font(82, True)
FONT_SUBTITLE = font(34)
FONT_H2 = font(38, True)
FONT_H3 = font(30, True)
FONT_BODY = font(25)
FONT_SMALL = font(20)
FONT_TINY = font(16)


def rounded_rect(draw: ImageDraw.ImageDraw, box: tuple[int, int, int, int], radius: int, fill, outline=None, width: int = 1) -> None:
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)


def draw_gradient(size: tuple[int, int], base: tuple[int, int, int], accent: tuple[int, int, int], warm: tuple[int, int, int]) -> Image.Image:
    width, height = size
    img = Image.new("RGB", size, base)
    draw = ImageDraw.Draw(img, "RGBA")
    for y in range(height):
        t = y / max(1, height - 1)
        color = mix(base, mix(base, warm, 0.28), t)
        shade = 0.82 + 0.18 * (1.0 - t)
        color = tuple(max(0, min(255, int(c * shade))) for c in color)
        draw.line((0, y, width, y), fill=color)
    glow = Image.new("RGBA", size, (0, 0, 0, 0))
    gdraw = ImageDraw.Draw(glow, "RGBA")
    gdraw.ellipse((int(width * 0.48), -int(height * 0.7), int(width * 1.08), int(height * 0.92)), fill=(*accent, 88))
    gdraw.ellipse((-int(width * 0.18), int(height * 0.38), int(width * 0.52), int(height * 1.24)), fill=(*warm, 54))
    glow = glow.filter(ImageFilter.GaussianBlur(radius=max(28, min(width, height) // 14)))
    img = img.convert("RGBA")
    img.alpha_composite(glow)
    return img


def cover_image(source: Image.Image, size: tuple[int, int]) -> Image.Image:
    width, height = size
    img = source.convert("RGBA")
    scale = max(width / img.width, height / img.height)
    new_size = (max(width, int(img.width * scale + 0.5)), max(height, int(img.height * scale + 0.5)))
    img = img.resize(new_size, Image.Resampling.LANCZOS)
    left = (img.width - width) // 2
    top = (img.height - height) // 2
    return img.crop((left, top, left + width, top + height))


def source_art(addon: AddonPage) -> Image.Image | None:
    path = ROOT / "addons" / addon.folder / "docs" / "curseforge" / f"{addon.mod_id}-source.png"
    if not path.exists():
        return None
    try:
        return Image.open(path).convert("RGBA")
    except Exception:
        return None


def left_readability_gradient(size: tuple[int, int], strength: int = 232) -> Image.Image:
    width, height = size
    overlay = Image.new("RGBA", size, (0, 0, 0, 0))
    pix = overlay.load()
    for x in range(width):
        t = x / max(1, width - 1)
        alpha = int(max(0, strength * (1.0 - t) ** 1.7))
        for y in range(height):
            pix[x, y] = (0, 0, 0, alpha)
    return overlay


def save_opaque_png(img: Image.Image, output: Path) -> None:
    flat = Image.new("RGBA", img.size, (0, 0, 0, 255))
    flat.alpha_composite(img.convert("RGBA"))
    output.parent.mkdir(parents=True, exist_ok=True)
    flat.convert("RGB").save(output, "PNG", optimize=True)


def stable_noise_overlay(img: Image.Image, key: str, opacity: int = 22) -> None:
    width, height = img.size
    seed = hashlib.sha256(key.encode("utf-8")).digest()
    draw = ImageDraw.Draw(img, "RGBA")
    count = max(240, min(900, (width * height) // 1400))
    state = int.from_bytes(seed[:8], "big")
    for i in range(count):
        state = (1103515245 * state + 12345 + i) & 0x7FFFFFFF
        x = state % width
        state = (1103515245 * state + 12345 + i * 3) & 0x7FFFFFFF
        y = state % height
        state = (1103515245 * state + 12345 + i * 7) & 0x7FFFFFFF
        value = 205 + (state % 50)
        alpha = opacity if i % 5 == 0 else max(3, opacity // 3)
        draw.point((x, y), fill=(value, value, value, alpha))


def draw_circuit_lines(draw: ImageDraw.ImageDraw, size: tuple[int, int], accent: tuple[int, int, int], key: str, alpha: int = 96) -> None:
    width, height = size
    digest = hashlib.sha256(key.encode("utf-8")).digest()
    for i in range(22):
        y = 24 + (digest[i % len(digest)] * 7 + i * 41) % (height - 48)
        x0 = (digest[(i + 9) % len(digest)] * 11 + i * 67) % width
        length = 110 + digest[(i + 13) % len(digest)] % 260
        x1 = min(width + 60, x0 + length)
        color = (*accent, alpha if i % 3 else alpha + 36)
        draw.line((x0, y, x1, y), fill=color, width=2)
        if i % 2 == 0:
            drop = 20 + digest[(i + 5) % len(digest)] % 80
            draw.line((x1, y, x1, min(height, y + drop)), fill=color, width=2)
            draw.ellipse((x1 - 4, min(height, y + drop) - 4, x1 + 4, min(height, y + drop) + 4), fill=color)


def draw_text_fit(
    draw: ImageDraw.ImageDraw,
    text: str,
    box: tuple[int, int, int, int],
    base_font: ImageFont.ImageFont,
    fill,
    line_spacing: int = 6,
    min_size: int = 16,
    bold: bool = False,
) -> None:
    x0, y0, x1, y1 = box
    max_width = x1 - x0
    max_height = y1 - y0
    base_size = getattr(base_font, "size", 24)
    for size in range(base_size, min_size - 1, -1):
        f = font(size, bold=bold)
        words = text.split()
        lines: list[str] = []
        current = ""
        for word in words:
            candidate = word if not current else current + " " + word
            if draw.textbbox((0, 0), candidate, font=f)[2] <= max_width:
                current = candidate
            else:
                if current:
                    lines.append(current)
                current = word
        if current:
            lines.append(current)
        line_height = draw.textbbox((0, 0), "Ag", font=f)[3] + line_spacing
        total = line_height * len(lines) - line_spacing
        widest = max((draw.textbbox((0, 0), line, font=f)[2] for line in lines), default=0)
        if total <= max_height and widest <= max_width:
            y = y0 + (max_height - total) // 2
            for line in lines:
                draw.text((x0, y), line, font=f, fill=fill)
                y += line_height
            return
    f = font(min_size, bold=bold)
    words = text.split()
    lines = []
    current = ""
    for word in words:
        candidate = word if not current else current + " " + word
        if draw.textbbox((0, 0), candidate, font=f)[2] <= max_width:
            current = candidate
        else:
            if current:
                lines.append(current)
            current = word
    if current:
        lines.append(current)
    line_height = draw.textbbox((0, 0), "Ag", font=f)[3] + line_spacing
    max_lines = max(1, max_height // max(1, line_height))
    clipped = lines[:max_lines]
    if len(lines) > max_lines and clipped:
        clipped[-1] = clipped[-1].rstrip(".") + "..."
    y = y0
    for line in clipped:
        draw.text((x0, y), line, font=f, fill=fill)
        y += line_height


def collect_textures(addon: AddonPage, limit: int = 8) -> list[Path]:
    addon_root = ROOT / "addons" / addon.folder
    textures_root = addon_root / "src" / "main" / "resources" / "assets" / addon.mod_id / "textures"
    if not textures_root.exists():
        candidates = list((addon_root / "src" / "main" / "resources" / "assets").glob("*/textures"))
        textures_root = candidates[0] if candidates else textures_root
    if not textures_root.exists():
        return []
    all_pngs = [p for p in textures_root.rglob("*.png") if p.is_file()]
    selected: list[Path] = []
    for term in addon.visual_terms:
        term_matches = [p for p in all_pngs if term.lower() in p.stem.lower()]
        for path in term_matches:
            if path not in selected and "age_" not in path.stem:
                selected.append(path)
                if len(selected) >= limit:
                    return selected
    for path in all_pngs:
        if path not in selected and "age_" not in path.stem and "gui" not in path.parts:
            selected.append(path)
            if len(selected) >= limit:
                break
    return selected[:limit]


def load_icon(path: Path, size: int) -> Image.Image | None:
    try:
        img = Image.open(path).convert("RGBA")
    except Exception:
        return None
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
    scale = min(size / img.width, size / img.height)
    if scale > 1:
        new_size = (max(1, int(img.width * scale)), max(1, int(img.height * scale)))
        img = img.resize(new_size, Image.Resampling.NEAREST)
    else:
        img.thumbnail((size, size), Image.Resampling.NEAREST)
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    canvas.alpha_composite(img, ((size - img.width) // 2, (size - img.height) // 2))
    return canvas


def draw_generated_icon(draw: ImageDraw.ImageDraw, center: tuple[int, int], radius: int, accent: tuple[int, int, int], warm: tuple[int, int, int], index: int) -> None:
    cx, cy = center
    if index % 4 == 0:
        draw.rounded_rectangle((cx - radius, cy - radius, cx + radius, cy + radius), radius=12, outline=(*accent, 210), width=5)
        draw.line((cx - radius + 18, cy, cx + radius - 18, cy), fill=(*warm, 210), width=4)
        draw.line((cx, cy - radius + 18, cx, cy + radius - 18), fill=(*warm, 210), width=4)
    elif index % 4 == 1:
        draw.polygon((cx, cy - radius, cx + radius, cy, cx, cy + radius, cx - radius, cy), outline=(*accent, 220), fill=(*accent, 38))
        draw.ellipse((cx - 10, cy - 10, cx + 10, cy + 10), fill=(*warm, 220))
    elif index % 4 == 2:
        for step in range(4):
            rr = radius - step * 14
            if rr <= 4:
                continue
            draw.arc((cx - rr, cy - rr, cx + rr, cy + rr), 20 + step * 18, 260 - step * 12, fill=(*accent, 180), width=4)
    else:
        draw.line((cx - radius, cy + radius, cx, cy - radius, cx + radius, cy + radius), fill=(*accent, 220), width=5)
        draw.line((cx - radius // 2, cy + radius // 3, cx + radius // 2, cy + radius // 3), fill=(*warm, 220), width=5)


def make_banner(addon: AddonPage, output: Path) -> None:
    base = hex_to_rgb(addon.theme[0])
    accent = hex_to_rgb(addon.theme[1])
    warm = hex_to_rgb(addon.theme[2])
    source = source_art(addon)
    if source:
        img = cover_image(source, (1600, 420))
        img.alpha_composite(left_readability_gradient(img.size, 244))
        img.alpha_composite(Image.new("RGBA", img.size, (*base, 54)))
    else:
        img = draw_gradient((1600, 420), base, accent, warm).convert("RGBA")
        stable_noise_overlay(img, addon.mod_id, 15)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_circuit_lines(draw, img.size, accent, addon.mod_id, 70)

    img.alpha_composite(Image.new("RGBA", img.size, (0, 0, 0, 38)))
    draw.rectangle((0, 0, 1600, 10), fill=(*accent, 220))
    draw.rectangle((0, 410, 1600, 420), fill=(*warm, 180))
    draw.line((86, 333, 865, 333), fill=(*accent, 210), width=3)

    draw_text_fit(draw, addon.name.upper(), (86, 52, 980, 160), FONT_TITLE, (246, 252, 255, 255), line_spacing=2, min_size=48, bold=True)
    draw_text_fit(draw, addon.tagline, (90, 176, 980, 258), FONT_SUBTITLE, (226, 239, 242, 245), line_spacing=4)
    draw.text((92, 292), f"{addon.mod_id}  //  v{addon.version}", font=FONT_SMALL, fill=(*warm, 235))

    chips = ["Minecraft 26.1.2", "NeoForge", "ECHO Stack"]
    x = 92
    for chip in chips:
        bbox = draw.textbbox((0, 0), chip, font=FONT_SMALL)
        w = bbox[2] + 34
        rounded_rect(draw, (x, 346, x + w, 382), 18, fill=(4, 10, 14, 168), outline=(*accent, 130), width=1)
        draw.text((x + 17, 352), chip, font=FONT_SMALL, fill=(234, 244, 246, 235))
        x += w + 12

    if not source:
        # Fallback art rail built from real addon textures where available.
        textures = collect_textures(addon, 6)
        panel = Image.new("RGBA", (470, 310), (0, 0, 0, 0))
        pdraw = ImageDraw.Draw(panel, "RGBA")
        rounded_rect(pdraw, (0, 0, 470, 310), 28, fill=(2, 8, 12, 122), outline=(*accent, 140), width=2)
        for i in range(8):
            offset = i * 58
            pdraw.line((30 + offset, 260, 110 + offset, 66), fill=(*accent, 36), width=4)
        icon_positions = [(58, 54), (190, 50), (322, 52), (120, 178), (252, 178), (374, 178)]
        for i, pos in enumerate(icon_positions):
            x0, y0 = pos
            outline_base = warm if i % 2 else accent
            rounded_rect(pdraw, (x0, y0, x0 + 86, y0 + 86), 18, fill=(255, 255, 255, 22), outline=(*outline_base, 150), width=2)
            icon = load_icon(textures[i], 66) if i < len(textures) else None
            if icon:
                panel.alpha_composite(icon, (x0 + 10, y0 + 10))
            else:
                draw_generated_icon(pdraw, (x0 + 43, y0 + 43), 30, accent, warm, i)
        panel = panel.filter(ImageFilter.UnsharpMask(radius=1, percent=120))
        img.alpha_composite(panel, (1040, 58))

    save_opaque_png(img, output)


def make_features(addon: AddonPage, output: Path) -> None:
    base = hex_to_rgb(addon.theme[0])
    accent = hex_to_rgb(addon.theme[1])
    warm = hex_to_rgb(addon.theme[2])
    source = source_art(addon)
    if source:
        img = cover_image(source, (1200, 675)).filter(ImageFilter.GaussianBlur(radius=1.1))
        img.alpha_composite(Image.new("RGBA", img.size, (0, 0, 0, 96)))
        img.alpha_composite(Image.new("RGBA", img.size, (*base, 34)))
    else:
        img = draw_gradient((1200, 675), base, accent, warm).convert("RGBA")
        stable_noise_overlay(img, addon.mod_id + "-features", 16)
    draw = ImageDraw.Draw(img, "RGBA")
    draw_circuit_lines(draw, img.size, warm, addon.mod_id + "-features", 48)
    img.alpha_composite(Image.new("RGBA", img.size, (0, 0, 0, 62)))
    draw.text((58, 42), "MAIN FEATURES", font=FONT_H2, fill=(*warm, 245))
    draw.text((58, 88), addon.name.upper(), font=FONT_H3, fill=(246, 252, 255, 245))
    draw.line((58, 132, 1140, 132), fill=(*accent, 180), width=2)

    textures = collect_textures(addon, 8)
    cards = [
        (58, 170, 568, 352),
        (632, 170, 1142, 352),
        (58, 404, 568, 586),
        (632, 404, 1142, 586),
    ]
    for i, box in enumerate(cards):
        x0, y0, x1, y1 = box
        fill = (4, 10, 14, 172)
        outline = (*(accent if i % 2 == 0 else warm), 150)
        rounded_rect(draw, box, 24, fill=fill, outline=outline, width=2)
        icon_box = (x0 + 24, y0 + 30, x0 + 134, y0 + 140)
        rounded_rect(draw, icon_box, 20, fill=(255, 255, 255, 24), outline=outline, width=2)
        icon = load_icon(textures[i], 86) if i < len(textures) else None
        if icon:
            img.alpha_composite(icon, (x0 + 36, y0 + 42))
        else:
            draw_generated_icon(draw, (x0 + 79, y0 + 85), 42, accent, warm, i)

        label = addon.feature_labels[i] if i < len(addon.feature_labels) else f"Feature {i + 1}"
        detail = addon.features[i] if i < len(addon.features) else addon.short
        draw.text((x0 + 160, y0 + 34), label.upper(), font=FONT_H3, fill=(248, 252, 255, 245))
        draw_text_fit(draw, detail, (x0 + 160, y0 + 82, x1 - 28, y1 - 24), FONT_BODY, (221, 237, 240, 238), line_spacing=5)

    draw.text((58, 620), f"{addon.mod_id} // v{addon.version}", font=FONT_SMALL, fill=(232, 244, 246, 190))
    save_opaque_png(img, output)


def bullet_list(items: tuple[str, ...]) -> str:
    return "\n".join(f"- {item}" for item in items)


def paragraphs(items: tuple[str, ...]) -> str:
    return "\n\n".join(items)


def make_generated_section(addon: AddonPage) -> str:
    image_dir = "docs/curseforge"
    banner = f"{image_dir}/{addon.mod_id}-banner.png"
    features = f"{image_dir}/{addon.mod_id}-features.png"
    required = ("Minecraft 26.1.2", "NeoForge 26.1.2.29-beta or newer", "Java 25+", *addon.required)
    section = f"""\
{START}
# {addon.name}

![{addon.name} banner]({banner})

**{addon.tagline}**

![{addon.name} feature overview]({features})

## CurseForge Summary

{addon.short}

## Overview

{paragraphs(addon.overview)}

## Main Features

{bullet_list(addon.features)}

## How It Plays

{bullet_list(addon.how_it_plays)}

## Requirements

{bullet_list(required)}

## Recommended Pairings

{bullet_list(addon.recommended)}

## Compatibility Notes

{bullet_list(addon.compatibility)}

## CurseForge Asset Files

- Banner: `{banner}`
- Feature image: `{features}`

{END}
"""
    return section


def merge_readme(addon: AddonPage, generated: str) -> None:
    readme = ROOT / "addons" / addon.folder / "README.md"
    previous = readme.read_text(encoding="utf-8") if readme.exists() else ""
    if START in previous and END in previous:
        before, rest = previous.split(START, 1)
        _, after = rest.split(END, 1)
        content = before.rstrip() + ("\n\n" if before.strip() else "") + generated + after.lstrip("\n")
    elif previous.strip():
        content = generated + "\n---\n\n## Existing Developer Notes\n\n" + previous.strip() + "\n"
    else:
        content = generated
    readme.write_text(content, encoding="utf-8", newline="\n")


def main() -> None:
    for addon in ADDONS:
        asset_dir = ROOT / "addons" / addon.folder / "docs" / "curseforge"
        make_banner(addon, asset_dir / f"{addon.mod_id}-banner.png")
        make_features(addon, asset_dir / f"{addon.mod_id}-features.png")
        merge_readme(addon, make_generated_section(addon))
        print(f"generated {addon.folder}")


if __name__ == "__main__":
    main()
