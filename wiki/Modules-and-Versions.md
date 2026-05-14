# Modules and Versions

## Naming

Ashfall is the modpack. ECHO is the ecosystem. `ECHO: Ashfall Protocol` is the main campaign addon and root `echoashfallprotocol` artifact.

Build-truth validation token: `1.1.0`.

## Full Gradle Stack

| Module | Version | Role |
|---|---:|---|
| `echoashfallprotocol` | `1.0.0` | Main Ashfall campaign and entry progression |
| `echocore` | `1.0.0` | Shared service contracts and persistence surfaces |
| `echonetcore` | `1.0.0` | Shared packet bridge, sync helpers, and network diagnostics |
| `echoruntimeguard` | `1.0.0` | Runtime budgets, lag diagnostics, and smart tick hints |
| `echothemecore` | `0.2.0` | Shared visual/theme/UI skin service |
| `echoplayercore` | `0.1.0` | Player utility commands, homes, back, spawn, random teleport, and travel QoL |
| `echomissioncore` | `1.0.0` | Shared mission service, objectives, and reward contracts |
| `echodatacore` | `1.0.0` | Shared persistent player, world, and team data |
| `echoworldcore` | `1.0.0` | Shared world regions, hazards, markers, and discovery events |
| `echoterminal` | `1.0.0` | Shared terminal shell and route UI |
| `signalos` | `1.0.0` | Reusable mission/archive/reward/diagnostics framework |
| `signalosexample` | `1.0.0` | Example-only SignalOS addon |
| `echorendercore` | `1.0.0` | Shared visual-state, preview, composition, and renderer profile support |
| `echoorbitalremnants` | `1.0.0` | Post-Nexus orbital continuation |
| `echonexusprotocol` | `1.0.0` | Nexus corruption and escalation chapter |
| `echoagriculturereclamation` | `1.0.0` | Ecology, pollinator drone, and food-route recovery chapter |
| `echostationfall` | `1.0.0` | Station horror/progression chapter |
| `echoblackboxprotocol` | `1.0.0` | Late-game blackbox finale chapter |
| `echoindustrialnexus` | `1.0.0` | Industrial automation/recovery chapter |
| `echologisticsnetwork` | `1.0.0` | Logistics, storage, delivery, loadouts, and addon external endpoints |
| `echoconvoyprotocol` | `1.0.0` | Vehicles, multiblock depots, fuel, cargo, HoloMap routes, and convoy operations |
| `echoholomap` | `1.0.0` | Terminal-integrated world telemetry and marker map |
| `echoindex` | `1.0.0` | Shared item, recipe, usage, and archive index |
| `echoarmory` | `1.0.0` | Weapons, armor, modules, and loadout hooks |
| `echolens` | `1.0.0` | Smart scanner HUD with server-assisted Deep Scan and addon-context inspection layer |
| `echomultiblockcore` | `1.0.0` | Shared multiblock validation, runtime, robotics, and workcell framework |
| `echoblockworks` | `1.0.0` | Decorative, structural, themed block family catalog, palette kits, and rare showcase site palettes |

The simplified public stack may group this as Core, Terminal, Ashfall Protocol, Orbital, Agriculture, Stationfall, Nexus, Industrial, and Blackbox. The Gradle `all` stack above is the build-truth artifact list.

## Workspace Module Sets

The build can target a **beta-only** addon set or **all** addons by `echoAddonSet`.

- `beta`: root + Core + NetCore + RuntimeGuard + ThemeCore + PlayerCore + Terminal + MissionCore + DataCore + SignalOS + SignalOS Example + RenderCore + Orbital Remnants + Nexus Protocol + Agriculture Reclamation + WorldCore + MultiblockCore + Blockworks
- `all`: beta + Stationfall + Blackbox Protocol + Industrial Nexus + Logistics Network + Convoy Protocol + HoloMap + Index + Armory + Lens

See root `settings.gradle` for authoritative inclusion logic.

## Newly Active Service Addons

- `echopowergrid` `0.1.0` - EP generator/storage/cable/consumer MVP and diagnostics.
- `echosoundcore` `0.1.0` - Shared audio framework; many advanced tracks remain partial until real OGG files are produced.
- `echotutorialcore` `0.1.0` - Tutorial/hint service with onboarding APIs and partial UI integrations.
- `echorelictech` `0.2.0-beta` - Relic recovery/restoration MVP with partial advanced integrations.
- `echoweathercore` `0.1.0` - Weather event service with forecast/shelter systems and integration hooks.
