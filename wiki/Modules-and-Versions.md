# Modules and Versions

## Naming

Ashfall is the modpack. ECHO is the ecosystem. `ECHO: Ashfall Protocol` is the main campaign addon and root `echoashfallprotocol` artifact.

## Full Gradle Stack

| Module | Version | Role |
|---|---:|---|
| `echoashfallprotocol` | `1.3.0` | Main Ashfall campaign and entry progression |
| `echocore` | `1.1.0` | Shared service contracts and persistence surfaces |
| `echonetcore` | `0.1.0` | Shared packet bridge, sync helpers, and network diagnostics |
| `echomissioncore` | `0.1.0` | Shared mission service, objectives, and reward contracts |
| `echodatacore` | `1.0.0` | Shared persistent player, world, and team data |
| `echoworldcore` | `0.1.0` | Shared world regions, hazards, markers, and discovery events |
| `echoterminal` | `1.1.0` | Shared terminal shell and route UI |
| `signalos` | `0.1.0` | Reusable mission/archive/reward/diagnostics framework |
| `signalosexample` | `0.1.0` | Example-only SignalOS addon |
| `echorendercore` | `0.5.0` | Shared visual-state and renderer profile support |
| `echoorbitalremnants` | `1.5.0` | Post-Nexus orbital continuation |
| `echonexusprotocol` | `1.0.0` | Nexus corruption and escalation chapter |
| `echoagriculturereclamation` | `0.1.1` | Ecology and food-route recovery chapter |
| `echostationfall` | `1.1.0` | Station horror/progression chapter |
| `echoblackboxprotocol` | `1.0.0` | Late-game blackbox finale chapter |
| `echoindustrialnexus` | `0.1.0` | Industrial automation/recovery chapter |
| `echologisticsnetwork` | `0.1.0` | Logistics, storage, delivery, and loadouts |
| `echoconvoyprotocol` | `0.1.0` | Vehicles, fuel, cargo, and convoy routes |
| `echoholomap` | `0.1.0` | Terminal-integrated world telemetry and marker map |
| `echoindex` | `0.1.0` | Shared item, recipe, usage, and archive index |
| `echoarmory` | `0.1.0` | Weapons, armor, modules, and loadout hooks |
| `echolens` | `0.1.0` | Smart scanner HUD and addon-context inspection layer |

The simplified public stack may group this as Core, Terminal, Ashfall Protocol, Orbital, Agriculture, Stationfall, Nexus, Industrial, and Blackbox. The Gradle `all` stack above is the build-truth artifact list.

## Workspace Module Sets

The build can target a **beta-only** addon set or **all** addons by `echoAddonSet`.

- `beta`: root + Core + NetCore + Terminal + MissionCore + DataCore + SignalOS + SignalOS Example + Orbital Remnants + Nexus Protocol + Agriculture Reclamation + WorldCore
- `all`: beta + RenderCore + Stationfall + Blackbox Protocol + Industrial Nexus + Logistics Network + Convoy Protocol + HoloMap + Index + Armory + Lens

See root `settings.gradle` for authoritative inclusion logic.
