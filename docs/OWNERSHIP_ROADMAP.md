# ECHO Ownership Roadmap

The long-term goal is for Ashfall to feel like a first-party survival sci-fi RPG built from ECHO systems, not a normal modpack assembled from identity-defining external mods.

## Owned Now

| System | Current Owner |
|---|---|
| Main campaign, crash start, early survival, guardians, Prime Relays, Nexus finale | `echoashfallprotocol` |
| Shared services, profiles, diagnostics, hazards, route records, rewards, factions, Nexus mirrors | `echocore` |
| Player-facing terminal UI, mission graph, archives, rewards, route records, vitals, recipe index | `echoterminal` |
| Reusable chapters, missions, archives, rewards, diagnostics, JSON loading, optional KubeJS bridge | `signalos` |
| Orbital progression and surveys | `echoorbitalremnants` |
| Nexus corruption chapter | `echonexusprotocol` |
| Agriculture, hydroponics, and ecology restoration | `echoagriculturereclamation` |
| Station horror chapter | `echostationfall` |
| Blackbox memory finale | `echoblackboxprotocol` |
| Machines and automation | `echoindustrialnexus` |
| Logistics, storage, delivery, and loadouts | `echologisticsnetwork` |
| Vehicles, cargo, and ruined-Earth travel | `echoconvoyprotocol` |
| Combat gear, modules, armory stations, and loadout hooks | `echoarmory` |

## Partial Ownership Gaps

| Gap | Current Coverage | Default Decision |
|---|---|---|
| Full recipe viewer | ECHO Terminal recipe index plus optional JEI | Improve Terminal first; create `echoindex` only if it becomes a standalone JEI/EMI replacement. |
| Inventory QoL | Logistics covers storage/loadouts, not broad player inventory helpers | Future `echoinventory` if sorting, locking, quick-stack, restock, and hotbar refill become first-party scope. |
| Survey/map/waypoints | Orbital surveys, Convoy routes, Core route records, Terminal route UI | Future `echosurvey` if map, fog-of-war, scanner overlays, and waypoint ownership needs a dedicated module. |
| In-world HUD | Terminal vitals and mission HUD notices | Future `echohud` if compass, pinned objectives, hazard meters, route markers, and transmissions move outside Terminal. |
| Unified config | Module-local NeoForge configs | Future `echoconfig` if config APIs/UI need one first-party owner. |
| Developer tools | Core diagnostics, SignalOS diagnostics, validators, and local dashboards | Future `echodevtools` if release validation and progression analysis outgrow scripts/local tooling. |
| World/structures | Root campaign and chapter modules | Future `echoworld` only if worldgen centralization reduces real duplication. |
| Creatures/encounters | Root campaign and chapter modules | Future `echocreatures` only if shared mobs and scan/codex behavior cross chapter boundaries. |

## Temporary External Support

- JEI stays optional until the ECHO Terminal recipe index or future `echoindex` can cover recipe and usage lookup.
- KubeJS stays a soft SignalOS bridge for pack scripting and rapid content iteration.
- Performance mods and libraries are allowed when they are not identity-defining gameplay, UI, progression, content, or QoL systems.

## Module Creation Rule

Improve the existing owner first. Create a new first-party ECHO module only when no current owner can keep the system clean, optional, and maintainable under `-PechoAddonSet=all`.
