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
| root | `echoashfallprotocol` | ECHO: Ashfall Protocol | `1.3.0` | Main Ashfall campaign addon. |
| `core/echocore` | `echocore` | ECHO: Core | `1.1.0` | Shared services, diagnostics, route records, rewards, factions, hazards, and mirrors. |
| `addons/echonetcore` | `echonetcore` | ECHO: NetCore | `0.1.0` | Shared packet registration, sync, action validation, and debug network contracts. |
| `addons/echomissioncore` | `echomissioncore` | ECHO: MissionCore | `0.1.0` | Shared mission, objective, progression, reward, and Terminal feed engine. |
| `addons/echodatacore` | `echodatacore` | ECHO: DataCore | `1.0.0` | Shared persistent player, world, and team progression data. |
| `addons/echoworldcore` | `echoworldcore` | ECHO: WorldCore | `0.1.0` | Shared world regions, markers, hazards, discoveries, and world event contracts. |
| `addons/echoterminal` | `echoterminal` | ECHO: Terminal | `1.1.0` | Player-facing terminal shell, missions, archives, rewards, route records, and recipe index UI. |
| `addons/echosignalos` | `signalos` | SignalOS | `0.1.0` | Reusable chapter, mission, archive, reward, diagnostics, JSON, and KubeJS-friendly framework. |
| `addons/signalosexample` | `signalosexample` | SignalOS Example Addon | `0.1.0` | Example-only SignalOS integration addon. |
| `addons/echorendercore` | `echorendercore` | ECHO: RenderCore | `0.5.0` | Shared visual-state, animation-profile, particle-profile, and renderer helper layer for advanced assets. |
| `addons/echoorbitalremnants` | `echoorbitalremnants` | ECHO: Orbital Remnants | `1.5.0` | Post-Nexus orbital continuation. |
| `addons/echonexusprotocol` | `echonexusprotocol` | ECHO: Nexus Protocol | `1.0.0` | Nexus corruption and memory escalation chapter. |
| `addons/echoagriculturereclamation` | `echoagriculturereclamation` | ECHO: Agriculture Reclamation | `0.1.1` | Ecology, agriculture, hydroponics, and chunk restoration. |
| `addons/echostationfall` | `echostationfall` | ECHO: Stationfall | `1.1.0` | Station ECHO horror chapter. |
| `addons/echoblackboxprotocol` | `echoblackboxprotocol` | ECHO: Blackbox Protocol | `1.0.0` | Late-game memory finale. |
| `addons/echoindustrialnexus` | `echoindustrialnexus` | ECHO: Industrial Nexus | `0.1.0` | Machines, Thermal Flux, ducts, filters, salvage, and factory recovery. |
| `addons/echologisticsnetwork` | `echologisticsnetwork` | ECHO: Logistics Network | `0.1.0` | Storage, loadouts, remote requests, courier delivery, and depots. |
| `addons/echoconvoyprotocol` | `echoconvoyprotocol` | ECHO: Convoy Protocol | `0.1.0` | Vehicles, fuel, cargo, contracts, checkpoints, and travel routes. |
| `addons/echoholomap` | `echoholomap` | ECHO: HoloMap | `0.1.0` | Terminal-integrated command map, telemetry layers, and marker registry. |
| `addons/echoindex` | `echoindex` | ECHO: Index | `0.1.0` | Shared item, recipe, usage, and archive index. |
| `addons/echoarmory` | `echoarmory` | ECHO: Armory | `0.1.0` | Weapons, armor, modules, energy recharge, faction locks, and loadout hooks. |
| `addons/echolens` | `echolens` | ECHO: Lens | `0.1.0` | Smart scanner HUD for blocks, entities, fluids, machines, and addon context. |

## Addon Sets

- `-PechoAddonSet=beta`: NetCore, Terminal, MissionCore, DataCore, SignalOS, SignalOS Example, Orbital Remnants, Nexus Protocol, Agriculture Reclamation, and WorldCore, plus root and Core.
- `-PechoAddonSet=all`: every beta module plus RenderCore, Stationfall, Blackbox Protocol, Industrial Nexus, Logistics Network, Convoy Protocol, HoloMap, Index, Armory, and Lens.

## Local Tooling

- `addons/echomodpackcommandcenter` is a local dashboard/tooling folder, not an active mod artifact unless `settings.gradle` explicitly includes it.
- `tools/echo-release-terminal` is a local release workflow/dashboard, not a gameplay mod.
- ARCANA references in local command-center seed data are out of scope for the ECHO/Ashfall stack and must not be expanded into active modules.
