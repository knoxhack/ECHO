# ECHO Ecosystem Overview

Last updated: 2026-05-11

This document maps the full ECHO ecosystem as it exists in the current workspace. It covers the core service layer, gameplay/content addons, terminal and intel surfaces, release modules, newer recently-created addons, the Command Center operations app, and the managed ARCANA companion project used by the local modpack pipeline.

The primary source of truth for module inclusion is `settings.gradle`. The operational source of truth is the Command Center project catalog and modpack pipeline in `addons/echomodpackcommandcenter`.

## High-Level Shape

ECHO is a first-party NeoForge mod ecosystem built as a multi-module Gradle workspace. The system is organized around a main survival campaign, a growing set of shared service modules, and content chapters that plug into common terminal, map, index, scanner, mission, data, world, rendering, and networking infrastructure.

The ecosystem has three practical layers:

1. Core service layer: shared runtime contracts and infrastructure used by many addons.
2. Player-facing systems: survival, terminal, map, index, scanner, mission, combat, logistics, and chapter gameplay.
3. Operations layer: Command Center manages scans, readiness, jar builds, jar promotion, exports, and modpack updates.

ARCANA is a separate NeoForge mod project under `C:\Github\ARCANA`, but Command Center treats it as a managed companion target so the local modpack can update both ECHO and ARCANA jars from one button.

## Build Sets

The Gradle workspace supports two main addon sets through the `echoAddonSet` property.

### Beta Set

Use this when iterating on core and active development modules:

```powershell
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=beta
```

Beta modules:

| Module | Role |
| --- | --- |
| `echonetcore` | Shared networking, packet, sync, action, and rate-limit layer. |
| `echoterminal` | Terminal UI and mission/intel surface. |
| `echomissioncore` | Shared mission, objective, reward, and feed backend. |
| `echodatacore` | Shared persistent data and progression state layer. |
| `echosignalos` | Terminal framework and UI toolkit. |
| `signalosexample` | SignalOS integration example. |
| `echoorbitalremnants` | Orbital route content and telemetry. |
| `echonexusprotocol` | Nexus unlocks, anomalies, and endgame bridge support. |
| `echoagriculturereclamation` | Food, reclamation, and recovery systems. |
| `echoworldcore` | Shared world, region, hazard, marker, and discovery services. |

### All Set

Use this for full release and modpack deployment work:

```powershell
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=all
```

The `all` set includes the beta set plus the release modules:

| Module | Role |
| --- | --- |
| `echostationfall` | Station route, crew logs, AI override chain, and hostile station content. |
| `echoblackboxprotocol` | Memory fragments, archive gates, late-route endings, and Blackbox support. |
| `echorendercore` | Shared rendering, animation, glow, overlay, particle, and debug profiles. |
| `echoindustrialnexus` | Machine routes, recipe coverage, and industrial support. |
| `echologisticsnetwork` | Delivery loops, route infrastructure, and connected storage support. |
| `echoconvoyprotocol` | Mobile operations and late-pack traversal systems. |
| `echoholomap` | Terminal map telemetry and world route visualization. |
| `echoindex` | Item, recipe, usage, archive, bookmark, and Terminal index browsing. |
| `echoarmory` | Weapons, armor, augments, energy cores, faction gear, and loadouts. |
| `echolens` | Scanner HUD for blocks, entities, fluids, machines, and progression cues. |

## Complete Module Inventory

| Project | Mod Id | Version | Lane | Path | Status | Summary |
| --- | --- | --- | --- | --- | --- | --- |
| ECHO Full Stack | Multiple | Multiple | Full Stack | `C:\Github\Echo` | Release Candidate | Full workspace release target for all ECHO jars. |
| ECHO: Core | `echocore` | `1.1.0` | Core Module | `core\echocore` | Active | Shared services, data contracts, runtime hooks, and cross-addon infrastructure. |
| ECHO: NetCore | `echonetcore` | `1.1.0` | Core Module | `addons\echonetcore` | Active | Shared packet, sync, server action, rate limiting, and debug network layer. |
| ECHO: DataCore | `echodatacore` | `1.0.0` | Core Module | `addons\echodatacore` | Active | Shared persistent data and progression layer. |
| ECHO: MissionCore | `echomissioncore` | `0.1.0` | Core Module | `addons\echomissioncore` | Active | Shared mission, objective, reward, and Terminal feed backend. |
| ECHO: WorldCore | `echoworldcore` | `0.1.0` | Core Module | `addons\echoworldcore` | Active | Shared world vocabulary and runtime services for regions, hazards, markers, discoveries, and overlays. |
| ECHO: RenderCore | `echorendercore` | `0.5.0` | Core Module | `addons\echorendercore` | Active | Shared rendering, animation, glow, overlay, particle profile, validation, and debug tools. |
| ECHO: Ashfall Protocol | `echoashfallprotocol` | `1.3.0` | Root Module | `.` | Release Candidate | Root survival campaign module with resources, gameplay data, POIs, factions, and the first ECHO route. |
| ECHO: Terminal | `echoterminal` | `1.1.0` | Beta/Dev Module | `addons\echoterminal` | Active | Terminal UI, mission surfaces, reward inbox, route records, and addon tab integration. |
| SignalOS | `signalos` | `0.1.0` | Tooling/Example | `addons\echosignalos` | Active | Terminal framework and shared UI primitives. |
| SignalOS Example Addon | `signalosexample` | `0.1.0` | Tooling/Example | `addons\signalosexample` | Example | Example addon for SignalOS integration patterns and test wiring. |
| ECHO: Orbital Remnants | `echoorbitalremnants` | `1.5.0` | Beta/Dev Module | `addons\echoorbitalremnants` | Ready | Orbital route content, launch chain, telemetry, caches, and suit support. |
| ECHO: Nexus Protocol | `echonexusprotocol` | `1.0.0` | Beta/Dev Module | `addons\echonexusprotocol` | Ready | Nexus unlocks, anomaly storms, route gates, and Blackbox bridge support. |
| ECHO: Agriculture Reclamation | `echoagriculturereclamation` | `0.1.1` | Beta/Dev Module | `addons\echoagriculturereclamation` | Active | Agriculture reclamation content for survival loops and recovery infrastructure. |
| ECHO: Stationfall | `echostationfall` | `1.1.0` | Release Module | `addons\echostationfall` | Needs Pass | Station pressure route, crew logs, AI override chain, hostile station entities, and terminal support. |
| ECHO: Blackbox Protocol | `echoblackboxprotocol` | `1.0.0` | Release Module | `addons\echoblackboxprotocol` | Needs Pass | Memory fragments, dungeon gates, archive terminal hooks, and late-route endings. |
| ECHO: Industrial Nexus | `echoindustrialnexus` | `0.1.0` | Release Module | `addons\echoindustrialnexus` | Needs Pass | Machine routes, compat hooks, recipe coverage, and station/blackbox industrial support. |
| ECHO: Logistics Network | `echologisticsnetwork` | `0.1.0` | Release Module | `addons\echologisticsnetwork` | Active | Logistics routes, delivery loops, and connected storage support. |
| ECHO: Convoy Protocol | `echoconvoyprotocol` | `0.1.0` | Release Module | `addons\echoconvoyprotocol` | Active | Convoy route content, mobile operations, and late-pack traversal. |
| ECHO: HoloMap | `echoholomap` | `0.1.0` | Beta/Dev Module | `addons\echoholomap` | Active | Terminal command-map tab for crash sites, routes, hazards, missions, bases, overlays, anomalies, and scan markers. |
| ECHO: Index | `echoindex` | `0.1.0` | Beta/Dev Module | `addons\echoindex` | Active | Shared item, recipe, usage, archive, bookmark, and Terminal index browser. |
| ECHO: Armory | `echoarmory` | `0.1.0` | Release Module | `addons\echoarmory` | Active | Modular weapons, armor, augments, energy cores, mission loadouts, faction gear, and combat readiness. |
| ECHO: Lens | `echolens` | `0.1.0` | Release Module | `addons\echolens` | Active | Scanner HUD for blocks, entities, fluids, machines, progression, and addon integrations. |
| ARCANA: Veilbound Studies | `arcanaveil` | `0.1.0` | Managed Companion | `C:\Github\ARCANA` | Active | Separate magic research mod managed by Command Center for local modpack deployment. |
| ECHO Command Center | N/A | N/A | Operations App | `addons\echomodpackcommandcenter` | Active | Local ops console for scans, readiness, features, jars, releases, exports, and modpack updates. |

## Core Service Layer

### ECHO: Core

ECHO Core is the base shared-services module. It owns common contracts, shared runtime behavior, cross-addon helpers, and foundational wiring expected by the rest of the ECHO stack.

It should be treated as the root dependency layer for most ECHO modules. Changes here have a broad blast radius because they can affect every content addon, terminal integration, and shared service.

Key responsibilities:

- Common ECHO runtime contracts.
- Shared hooks and helper APIs.
- Base infrastructure used by addon chapters.
- Cross-addon compatibility foundations.

### ECHO: NetCore

NetCore centralizes networking. It owns packet and sync helpers, server actions, packet kind classification, rate limiting, debug pathways, and safer client/server communication behavior.

This module is especially important now that newer addons are sending richer state to UI surfaces. Recent DataCore work depends on the updated `EchoNetSend` shape that requires an explicit packet kind.

Key responsibilities:

- Packet send helpers.
- Sync payload delivery.
- Server action routing.
- Rate limiting and debug network support.
- Shared packet kind metadata.

### ECHO: DataCore

DataCore is the persistent data and progression layer. It stores and synchronizes structured state that can be shared across missions, research, terminal views, unlocks, discoveries, and addon progression.

It is a central bridge between gameplay actions and UI/intel surfaces. Because it touches persistent state, it should be treated carefully in migrations and release checks.

Key responsibilities:

- Persistent player/world data.
- Progression and unlock state.
- Server-side data synchronization.
- Data services consumed by chapter and intel modules.

Current operational note:

- The latest full-stack build found and fixed a NetCore send signature mismatch in DataCore.
- DataCore now builds in the full ECHO stack, but its deployed jar can remain stale if Minecraft or CurseForge keeps the target jar locked.

### ECHO: MissionCore

MissionCore provides shared mission services. It exists so individual modules can register objectives, rewards, mission feed entries, route state, and terminal-visible tasks without each addon rebuilding the same mission system.

Key responsibilities:

- Mission and objective contracts.
- Reward definitions.
- Mission feed backend.
- Terminal mission integration.
- Shared route/chapter task infrastructure.

### ECHO: WorldCore

WorldCore is the shared world vocabulary and runtime service layer. It gives other modules common ways to describe and publish regions, hazards, markers, discoveries, routes, and world telemetry.

It is the natural backend partner for HoloMap, Lens, mission routing, and world-facing chapter systems.

Key responsibilities:

- Region and route definitions.
- Hazard metadata.
- World markers and discoveries.
- Runtime feeds for maps and scanners.
- Shared world state vocabulary.

### ECHO: RenderCore

RenderCore owns shared visual systems. It gives the ecosystem reusable rendering, animation, glow, overlay, particle profile, validation, and debug tools.

Its goal is to keep visual language consistent while preventing each addon from inventing its own rendering glue.

Key responsibilities:

- Shared rendering helpers.
- Animation and glow profiles.
- Overlay and particle profile support.
- Visual debug and validation tools.
- Server-safe rendering boundary checks.

## Command, Intel, And Tooling Layer

### ECHO: Terminal

Terminal is the main diegetic command surface for ECHO. It hosts mission surfaces, reward inboxes, route records, addon tabs, and the player-facing operational interface.

The Terminal is where many otherwise separate systems become visible as one coherent player workflow.

Key responsibilities:

- Terminal UI and navigation.
- Mission and route views.
- Reward inbox.
- Addon tab integration.
- Player-facing command center fantasy.

### SignalOS

SignalOS is the terminal framework and UI toolkit. It provides shared UI primitives for ECHO-style command surfaces and keeps Terminal-like interfaces consistent.

SignalOS is a tooling module, not a story chapter.

Key responsibilities:

- Terminal UI primitives.
- Shared command-surface behavior.
- Framework-level UI patterns for addons.

### SignalOS Example Addon

SignalOS Example is a small integration sample. It exists to exercise SignalOS wiring, validate integration patterns, and give future modules a working reference.

Key responsibilities:

- Example SignalOS integration.
- Test wiring reference.
- Developer-facing validation target.

### ECHO: HoloMap

HoloMap is the map telemetry surface for ECHO. It is a Terminal command-map tab that can show crash sites, routes, hazards, missions, bases, orbital overlays, anomalies, and drone scan markers.

HoloMap depends conceptually on WorldCore, MissionCore, and scan/intel-producing modules. It turns system state into route-level navigation.

Key responsibilities:

- Terminal map tab.
- Crash site and route display.
- Hazard and anomaly overlays.
- Mission and base markers.
- Drone scan marker visualization.

### ECHO: Index

Index is the searchable knowledge surface for items, recipes, usages, archive entries, bookmarks, and Terminal browsing.

It is the ecosystem's reference layer. As more modules add gear, machines, artifacts, and lore entries, Index gives those objects a shared discovery and lookup surface.

Key responsibilities:

- Item and recipe lookup.
- Usage browsing.
- Archive entries.
- Bookmarks.
- Terminal index UI.

### ECHO: Lens

Lens is the scanner HUD. It reads the world through an ECHO lens: blocks, entities, fluids, machines, progression state, and addon-specific integrations.

Lens should become the player's fast, in-world intelligence layer, complementing the deeper Terminal, HoloMap, and Index views.

Key responsibilities:

- Scanner HUD.
- Block/entity/fluid inspection.
- Machine and progression cues.
- Addon integration hooks.
- Player-facing scan readability.

## Main Campaign And Chapter Modules

### ECHO: Ashfall Protocol

Ashfall Protocol is the root survival campaign module. It anchors the pack's core identity: environmental collapse, survival pressure, route progression, POIs, factions, hazards, and the initial ECHO story loop.

It is also the root Gradle project jar in the workspace.

Key responsibilities:

- Main survival campaign.
- Early and mid-route progression.
- Toxic air, radiation, hydration, mutations, and survival hazards.
- POIs, factions, data resources, and story setup.
- Baseline content that other modules extend.

### ECHO: Orbital Remnants

Orbital Remnants extends the ecosystem upward into orbital route content. It includes launch-chain content, telemetry, caches, suit support, and orbital salvage/intel flow.

Key responsibilities:

- Orbital route content.
- Launch and telemetry chain.
- Cache and suit support.
- Space-adjacent survival/intel loop.

### ECHO: Nexus Protocol

Nexus Protocol handles Nexus unlocks, anomaly storms, route gates, and bridge support into Blackbox and late-game content.

It is an endgame bridge module. It should be read as the point where the world stops being only ruined infrastructure and begins exposing deeper ECHO protocol behavior.

Key responsibilities:

- Nexus unlocks.
- Anomaly storm support.
- Route gates.
- Bridge into Blackbox and late-game systems.

### ECHO: Agriculture Reclamation

Agriculture Reclamation adds food, reclamation, and recovery systems. It supports the survival loop by giving the player infrastructure for restoration rather than only scavenging.

Key responsibilities:

- Agriculture recovery content.
- Food and survival support.
- Reclamation infrastructure.
- Route support for long-term base viability.

### ECHO: Stationfall

Stationfall is a release chapter centered on station pressure, crew logs, AI override chains, hostile station entities, and terminal support.

Its status is "Needs Pass", which means it is represented in the workspace and expected jar set, but still needs QA and release-readiness attention.

Key responsibilities:

- Station route content.
- Crew log narrative.
- AI override chain.
- Hostile station entities.
- Terminal integration.

### ECHO: Blackbox Protocol

Blackbox Protocol is a late-route release module for memory fragments, dungeon gates, archive terminal hooks, and endings.

It is tied to the deeper mystery and archival side of ECHO.

Key responsibilities:

- Memory fragments.
- Dungeon gates.
- Archive terminal hooks.
- Late-route endings.
- Nexus bridge support.

## Industrial, Logistics, Convoy, And Combat Modules

### ECHO: Industrial Nexus

Industrial Nexus provides machine routes, recipe coverage, compatibility hooks, and industrial support for station and blackbox content.

It gives the ecosystem a stronger machine and production layer.

Key responsibilities:

- Machine routes.
- Recipe coverage.
- Compatibility hooks.
- Industrial infrastructure for other chapters.

### ECHO: Logistics Network

Logistics Network handles route infrastructure, delivery loops, and connected storage support.

It is the connective tissue for moving resources and mission materials through the pack.

Key responsibilities:

- Logistics route content.
- Delivery loops.
- Connected storage support.
- Infrastructure for larger-scale operations.

### ECHO: Convoy Protocol

Convoy Protocol adds mobile operations and late-pack traversal systems.

It sits naturally beside Logistics Network and Armory: once the player has infrastructure and gear, Convoy turns movement and field operations into a managed system.

Key responsibilities:

- Convoy route content.
- Mobile operations.
- Late-pack traversal.
- Support systems for field operations.

### ECHO: Armory

Armory provides weapons, armor, augments, energy cores, mission loadouts, faction gear, and Terminal-managed combat readiness.

It is the player readiness and gear-prep layer for hostile routes.

Key responsibilities:

- Modular weapons.
- Armor and augments.
- Energy cores.
- Faction gear.
- Mission loadouts and combat readiness.

## Managed Companion Project

### ARCANA: Veilbound Studies

ARCANA is not an ECHO addon, but it is managed alongside ECHO by Command Center. It lives at `C:\Github\ARCANA` and builds the `arcanaveil-0.1.0.jar` companion mod.

ARCANA's feature catalog covers hidden resonances, research theory, Veil pressure, rituals, convergence, fracture risk, late-game Veil choices, Field Journal UI, research JSON/layout support, assets/resources, creative test coverage, and validation workflow.

Operationally, Command Center's one-click modpack flow treats ARCANA as a deduped first-party target beside ECHO Full Stack.

Key responsibilities:

- Separate magic research mod.
- Veil resonance and research loop.
- Field Journal UI.
- Ritual and fracture systems.
- Managed jar deployment into the same local modpack target.

## Operations App

### ECHO Command Center

Command Center is the local web app under `addons\echomodpackcommandcenter`. It is not a Minecraft jar. It is the ops console for making the ecosystem manageable.

Current responsibilities:

- Real project catalog for ECHO, every ECHO module, and ARCANA.
- Feature/lore implementation catalog.
- Readiness checklist with done, missing, blocked, and warning items.
- QA quick scans and deeper scan evidence.
- Release deck for allowlisted Gradle actions.
- Jar inventory, build, promote, checksum verification, and quarantine.
- One-click modpack rebuild and update pipeline.
- Export bundles containing project state, scans, readiness, jar manifests, features, and modpack run history.

Important local URLs:

| Surface | URL |
| --- | --- |
| Command Center UI | `http://127.0.0.1:5177/` |
| Command Center API | `http://127.0.0.1:4177/` |

## Feature And Lore Intelligence

Command Center tracks feature implementation separately from jar readiness. This matters because a project can build cleanly while still having planned, partial, or QA-needed feature work.

### ECHO Full Stack Feature Snapshot

Current ECHO full-stack feature catalog:

| Status | Count |
| --- | ---: |
| Implemented | 2 |
| Partial | 14 |
| Planned | 2 |
| Deferred | 0 |
| Blocked | 0 |

Primary feature categories:

- Release operations.
- Core gameplay loop.
- Progression.
- Survival hazards.
- Automation and machines.
- World and POIs.
- Narrative and factions.
- Terminal and intel surfaces.
- Nexus/endgame flow.
- Release chapter coverage.
- Core services.
- Terminal intel.

This means ECHO has broad implementation coverage, but many systems are intentionally marked partial because the catalog is evidence-based. A feature should only be marked implemented when docs or concrete project evidence support that claim.

### ARCANA Feature Snapshot

Current ARCANA feature catalog:

| Status | Count |
| --- | ---: |
| Implemented | 4 |
| Partial | 5 |
| Planned | 2 |
| Deferred | 0 |
| Blocked | 0 |

ARCANA's feature intelligence is separate from ECHO readiness. It is included because Command Center manages ARCANA in the same local modpack update flow.

## Modpack Deployment Snapshot

The current managed modpack target is:

```text
C:\CurseForge\Instances\Ashfall Protocol\mods
```

Current expected managed jar count:

| Target Group | Expected Jars |
| --- | ---: |
| ECHO Full Stack | 22 |
| ARCANA | 1 |
| Total | 23 |

The latest observed Command Center state:

| State | Count |
| --- | ---: |
| Source jars built | 23 / 23 |
| Current target jars | 22 / 23 |
| Stale managed target jars | 1 |
| Duplicate managed target jars | 0 |
| Missing source jars | 0 |

The remaining stale jar is `echodatacore-1.0.0.jar`. It can stay stale when Minecraft or CurseForge is running because Windows locks jars loaded by the game process. Command Center quarantines stale managed jars instead of deleting them, but it cannot move a locked file.

If promotion reports `EBUSY` or `resource busy or locked`, close Minecraft and the CurseForge profile, then run Command Center's modpack update button again.

## Managed Jar List

The full modpack pipeline expects these first-party jars:

| Jar | Source Project |
| --- | --- |
| `echocore-1.1.0.jar` | ECHO: Core |
| `echonetcore-1.1.0.jar` | ECHO: NetCore |
| `echodatacore-1.0.0.jar` | ECHO: DataCore |
| `echomissioncore-0.1.0.jar` | ECHO: MissionCore |
| `echoterminal-1.1.0.jar` | ECHO: Terminal |
| `echoashfallprotocol-1.3.0.jar` | ECHO: Ashfall Protocol |
| `signalos-0.1.0.jar` | SignalOS |
| `signalosexample-0.1.0.jar` | SignalOS Example |
| `echoorbitalremnants-1.5.0.jar` | ECHO: Orbital Remnants |
| `echonexusprotocol-1.0.0.jar` | ECHO: Nexus Protocol |
| `echoagriculturereclamation-0.1.1.jar` | ECHO: Agriculture Reclamation |
| `echoworldcore-0.1.0.jar` | ECHO: WorldCore |
| `echostationfall-1.1.0.jar` | ECHO: Stationfall |
| `echoblackboxprotocol-1.0.0.jar` | ECHO: Blackbox Protocol |
| `echoindustrialnexus-0.1.0.jar` | ECHO: Industrial Nexus |
| `echologisticsnetwork-0.1.0.jar` | ECHO: Logistics Network |
| `echorendercore-0.5.0.jar` | ECHO: RenderCore |
| `echoconvoyprotocol-0.1.0.jar` | ECHO: Convoy Protocol |
| `echoholomap-0.1.0.jar` | ECHO: HoloMap |
| `echoindex-0.1.0.jar` | ECHO: Index |
| `echoarmory-0.1.0.jar` | ECHO: Armory |
| `echolens-0.1.0.jar` | ECHO: Lens |
| `arcanaveil-0.1.0.jar` | ARCANA: Veilbound Studies |

## Recommended Workflows

### Full Local Modpack Update

Use Command Center's Modpack view for the normal local workflow:

1. Close Minecraft and the CurseForge instance if any managed jar is locked.
2. Open Command Center at `http://127.0.0.1:5177/`.
3. Confirm Settings points to `C:\CurseForge\Instances\Ashfall Protocol\mods`.
4. Run the one-click rebuild/update button.
5. Confirm the pipeline builds ECHO and ARCANA, promotes jars, verifies checksums, runs quick scans, and records history.

### Full ECHO Build From Terminal

```powershell
cd C:\Github\Echo
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=all
```

### Beta Development Build

```powershell
cd C:\Github\Echo
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=beta
```

### ARCANA Build

```powershell
cd C:\Github\ARCANA
.\gradlew.bat build --warning-mode all
```

## Readiness Meaning

Command Center readiness is not the same thing as "the mod has features". Readiness answers whether the selected project is deployable to the local target right now.

For ECHO Full Stack, 100 percent readiness means:

- Latest quick scan has no findings.
- Latest deep scan has passed or is not required for the current goal state.
- All expected ECHO jars are built.
- Settings has a configured and existing modpack `mods` folder.
- Every expected jar is current in the target folder.
- No stale or duplicate managed ECHO jars remain.

Full build, GameTests, and verify-release are higher-confidence release gates. They are intentionally kept in the Release Deck instead of being required for the readiness percentage.

## Current Attention Points

1. Keep the Command Center project catalog synced with `settings.gradle` when new addons are added.
2. Keep feature records evidence-based. Do not mark features implemented unless docs or concrete project code support it.
3. Close Minecraft before promoting jars into the CurseForge `mods` folder.
4. Treat Core, NetCore, DataCore, MissionCore, WorldCore, and RenderCore changes as high-blast-radius work.
5. Use the `all` build set before release or modpack deployment.
6. Use the `beta` build set for faster iteration across active service and development modules.
7. Keep ARCANA managed as a companion target, not an ECHO addon, unless the project structure changes.

## Module Documentation Map

These are the local docs that currently back the ecosystem overview. Some newer modules are represented by build metadata, source code, and Command Center seed data but do not yet have a dedicated README.

| Area | Existing Docs |
| --- | --- |
| Whole ECHO pack | `C:\Github\Echo\README.md`, `C:\Github\Echo\MODPACK_OVERVIEW.md`, `C:\Github\Echo\LORE_BIBLE.md`, `C:\Github\Echo\wiki\Lore-and-World-Canon.md`, `C:\Github\Echo\wiki\Build-and-Release.md` |
| Release process | `C:\Github\Echo\docs\release_process.md`, `C:\Github\Echo\docs\FULL_GRADLE_STACK.md`, `C:\Github\Echo\docs\releases\ashfall_1.3.0_smoke_test.md` |
| Ownership and handoff | `C:\Github\Echo\docs\OWNERSHIP_ROADMAP.md`, `C:\Github\Echo\docs\chapter_handoff_ids.md`, `C:\Github\Echo\docs\missioncore_addon_registration.md` |
| ECHO: NetCore | `C:\Github\Echo\addons\echonetcore\README.md` |
| ECHO: DataCore | `C:\Github\Echo\addons\echodatacore\README.md` |
| ECHO: MissionCore | `C:\Github\Echo\addons\echomissioncore\README.md` |
| ECHO: WorldCore | `C:\Github\Echo\addons\echoworldcore\README.md`, `C:\Github\Echo\addons\echoworldcore\SMOKE_TEST.md` |
| ECHO: RenderCore | `C:\Github\Echo\addons\echorendercore\README.md` |
| ECHO: Terminal | `C:\Github\Echo\addons\echoterminal\README.md` |
| SignalOS | `C:\Github\Echo\addons\echosignalos\README.md`, `C:\Github\Echo\addons\echosignalos\RELEASE_NOTES.md` |
| SignalOS Example | `C:\Github\Echo\addons\signalosexample\README.md` |
| ECHO: Orbital Remnants | `C:\Github\Echo\addons\echoorbitalremnants\README.md`, `C:\Github\Echo\addons\echoorbitalremnants\PLAN.md`, `C:\Github\Echo\addons\echoorbitalremnants\BETA_TEST_PLAN.md` |
| ECHO: Nexus Protocol | `C:\Github\Echo\addons\echonexusprotocol\docs\nexus_public_release_smoke_pass.md` |
| ECHO: Agriculture Reclamation | `C:\Github\Echo\addons\echoagriculturereclamation\README.md` |
| ECHO: Stationfall | Source tree and Command Center catalog; no dedicated README found yet. |
| ECHO: Blackbox Protocol | Source tree and Command Center catalog; no dedicated README found yet. |
| ECHO: Industrial Nexus | `C:\Github\Echo\addons\echoindustrialnexus\README.md` |
| ECHO: Logistics Network | `C:\Github\Echo\addons\echologisticsnetwork\SMOKE_TEST.md` |
| ECHO: Convoy Protocol | `C:\Github\Echo\addons\echoconvoyprotocol\README.md` |
| ECHO: HoloMap | `C:\Github\Echo\addons\echoholomap\README.md` |
| ECHO: Index | Source tree and Command Center catalog; no dedicated README found yet. |
| ECHO: Armory | `C:\Github\Echo\addons\echoarmory\RELEASE_NOTES.md`, `C:\Github\Echo\addons\echoarmory\SMOKE_TEST.md` |
| ECHO: Lens | Source tree and Command Center catalog; no dedicated README found yet. |
| Command Center | `C:\Github\Echo\addons\echomodpackcommandcenter\README.md` |
| ARCANA | `C:\Github\ARCANA\README.md`, `C:\Github\ARCANA\docs\ARCANA_CREATIVE_TEST_CHECKLIST.md`, `C:\Github\ARCANA\docs\ARCANA_ASSET_AUDIT.md`, `C:\Github\ARCANA\docs\ARCANA_VISUAL_STYLE.md` |

## Source Pointers

| Area | Path |
| --- | --- |
| Lore bible | `C:\Github\Echo\LORE_BIBLE.md` |
| Modpack overview | `C:\Github\Echo\MODPACK_OVERVIEW.md` |
| World canon wiki | `C:\Github\Echo\wiki\Lore-and-World-Canon.md` |
| Gradle module source of truth | `C:\Github\Echo\settings.gradle` |
| Release process | `C:\Github\Echo\docs\release_process.md` |
| Full Gradle stack notes | `C:\Github\Echo\docs\FULL_GRADLE_STACK.md` |
| MissionCore registration | `C:\Github\Echo\docs\missioncore_addon_registration.md` |
| Ownership roadmap | `C:\Github\Echo\docs\OWNERSHIP_ROADMAP.md` |
| Command Center app | `C:\Github\Echo\addons\echomodpackcommandcenter` |
| ARCANA project | `C:\Github\ARCANA` |
