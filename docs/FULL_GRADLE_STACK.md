# ECHO Full Gradle Stack

`settings.gradle` and each module's `gradle.properties` are the source of truth for active artifacts and versions. Ashfall is the modpack, ECHO is the ecosystem, and `ECHO: Ashfall Protocol` is the main campaign addon published as `echoashfallprotocol`.

## Runtime Stack

- Minecraft: `26.1.2`
- NeoForge: `26.1.2.29-beta`
- Java: `25+`
- Gradle Wrapper: `9.2.1`
- NeoForge ModDev: `2.0.141`
- Optional JEI: `29.5.0.26`
- Default build mode: `echoAddonSet=all`

## Active Modules

| Project | Mod ID | Display Name | Version | Role |
|---|---|---|---:|---|
| root | `echoashfallprotocol` | ECHO: Ashfall Protocol | `1.1.0` | Main Ashfall campaign addon. |
| `core/echocore` | `echocore` | ECHO: Core | `1.1.0` | Shared services, diagnostics, route records, rewards, factions, hazards, and mirrors. |
| `addons/echonetcore` | `echonetcore` | ECHO: NetCore | `1.1.0` | Shared packet registration, sync, action validation, and debug network contracts. |
| `addons/echoruntimeguard` | `echoruntimeguard` | ECHO RuntimeGuard | `1.1.0` | Shared TPS/FPS pressure monitoring, runtime budgets, smart tick hints, and performance diagnostics. |
| `addons/echothemecore` | `echothemecore` | ECHO ThemeCore | `0.2.0` | Shared visual/theme/UI skin service for ECHO modules and vanilla surfaces. |
| `addons/echoplayercore` | `echoplayercore` | ECHO: PlayerCore | `0.1.0` | Player utility commands, homes, back, spawn, random teleport, and travel QoL. |
| `addons/echomissioncore` | `echomissioncore` | ECHO: MissionCore | `1.1.0` | Shared mission, objective, progression, reward, and Terminal feed engine. |
| `addons/echodatacore` | `echodatacore` | ECHO: DataCore | `1.1.0` | Shared persistent player, world, and team progression data. |
| `addons/echoworldcore` | `echoworldcore` | ECHO: WorldCore | `1.1.0` | Shared world regions, markers, hazards, discoveries, and world event contracts. |
| `addons/echoterminal` | `echoterminal` | ECHO: Terminal | `1.1.0` | Player-facing terminal shell, missions, archives, rewards, route records, and recipe index UI. |
| `addons/echosignalos` | `signalos` | SignalOS | `1.1.0` | Reusable chapter, mission, archive, reward, diagnostics, JSON, and KubeJS-friendly framework. |
| `addons/signalosexample` | `signalosexample` | SignalOS Example Addon | `1.1.0` | Example-only SignalOS integration addon. |
| `addons/echorendercore` | `echorendercore` | ECHO: RenderCore | `1.1.0` | Shared visual-state, animation-profile, particle-profile, preview, composition, and renderer helper layer for advanced assets. |
| `addons/echoorbitalremnants` | `echoorbitalremnants` | ECHO: Orbital Remnants | `1.1.0` | Post-Nexus orbital continuation. |
| `addons/echonexusprotocol` | `echonexusprotocol` | ECHO: Nexus Protocol | `1.1.0` | Nexus corruption and memory escalation chapter with smarter field-map risk planning. |
| `addons/echoagriculturereclamation` | `echoagriculturereclamation` | ECHO: Agriculture Reclamation | `1.1.0` | Ecology, agriculture, hydroponics, greenhouse zones, pollinator drones, and chunk restoration. |
| `addons/echostationfall` | `echostationfall` | ECHO: Stationfall | `1.1.0` | Station ECHO horror chapter. |
| `addons/echoblackboxprotocol` | `echoblackboxprotocol` | ECHO: Blackbox Protocol | `1.1.0` | Late-game memory finale. |
| `addons/echoindustrialnexus` | `echoindustrialnexus` | ECHO: Industrial Nexus | `1.1.0` | Machines, Thermal Flux, ducts, filters, salvage, MultiblockCore factory ops, and factory command. |
| `addons/echologisticsnetwork` | `echologisticsnetwork` | ECHO: Logistics Network | `1.1.0` | Storage, loadouts, external endpoints, remote requests, courier delivery, depots, and operations dashboards. |
| `addons/echoconvoyprotocol` | `echoconvoyprotocol` | ECHO: Convoy Protocol | `1.1.0` | Vehicles, multiblock depots, fuel, cargo, deterministic Field Ops, HoloMap routes, recovery signals, and travel operations. |
| `addons/echoholomap` | `echoholomap` | ECHO: HoloMap | `1.1.0` | Terminal-integrated command map, telemetry layers, and marker registry. |
| `addons/echoindex` | `echoindex` | ECHO: Index | `1.1.0` | Shared item, recipe, usage, and archive index. |
| `addons/echoarmory` | `echoarmory` | ECHO: Armory | `1.1.0` | Weapons, armor, modules, energy recharge, faction locks, and loadout hooks. |
| `addons/echolens` | `echolens` | ECHO: Lens | `1.1.0` | Smart scanner HUD with local inspection, server-assisted Deep Scan, and addon context. |
| `addons/echomultiblockcore` | `echomultiblockcore` | ECHO: MultiblockCore | `1.1.0` | Shared multiblock definitions, validation, runtime, robotics, and workcell contracts. |
| `addons/echoblockworks` | `echoblockworks` | ECHO Blockworks | `1.1.0` | Decorative, structural, themed block families, palette kits, and rare showcase ruin palettes for ECHO builds. |

## Addon Sets

- `-PechoAddonSet=beta`: NetCore, RuntimeGuard, ThemeCore, PlayerCore, Terminal, MissionCore, DataCore, SignalOS, SignalOS Example, RenderCore, Orbital Remnants, Nexus Protocol, Agriculture Reclamation, WorldCore, MultiblockCore, and Blockworks, plus root and Core.
- `-PechoAddonSet=all`: every beta module plus Stationfall, Blackbox Protocol, Industrial Nexus, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

## Local Tooling

- `addons/echomodpackcommandcenter` is a local dashboard/tooling folder, not an active mod artifact unless `settings.gradle` explicitly includes it.
- `tools/echo-release-terminal` is a local release workflow/dashboard, not a gameplay mod.
- ARCANA references in local command-center seed data are out of scope for the ECHO/Ashfall stack and must not be expanded into active modules.

## Newly Active Service Addons

The current `settings.gradle` all-stack also includes `echopowergrid` `0.1.0`, `echosoundcore` `0.1.0`, `echotutorialcore` `0.1.0`, `echorelictech` `0.2.0-beta`, and `echoweathercore` `0.1.0`. `addons/echomodpackcommandcenter` and `tools/echo-release-terminal` remain local tooling surfaces, not an active mod artifact. Ashfall is the modpack; ECHO is the ecosystem; ECHO: Ashfall Protocol remains `echoashfallprotocol`.
