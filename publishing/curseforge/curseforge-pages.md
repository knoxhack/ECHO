# ECHO CurseForge Publish Copy

Use these as paste-ready CurseForge page descriptions for the four ECHO projects.

## ECHO: Core

Banner: `publishing/curseforge/banners/echo-core-banner.png`

Short description:

> Shared API, service registry, and integration foundation for ECHO mods and addons.

Full description:

ECHO: Core is the shared foundation for the modular ECHO mod stack. It provides the API layer, service registry, addon chapter registration, Nexus path hooks, terminal placement contracts, terminal reward contracts, and intel mirroring services used by ECHO gameplay mods.

This is a required library mod for ECHO projects. It does not add a standalone survival campaign by itself; it exists so ECHO: Ashfall Protocol, ECHO: Terminal, ECHO: Orbital Remnants, and future ECHO addons can speak the same progression and integration language.

Features:

- Shared ECHO API and service registry.
- Addon chapter registration for modular ECHO content.
- Nexus path service hooks for post-decision content.
- Terminal placement and reward service contracts.
- Intel mirroring support between gameplay mods and terminal surfaces.
- Lightweight foundation built for pack authors and future addon chapters.

Requirements:

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+

Recommended with:

- ECHO: Terminal
- ECHO: Ashfall Protocol
- ECHO: Orbital Remnants

## ECHO: Terminal

Banner: `publishing/curseforge/banners/echo-terminal-banner.png`

Short description:

> A shared in-world ECHO command terminal for missions, archives, addon pages, and route actions.

Full description:

ECHO: Terminal adds the shared ECHO Terminal block, UI shell, mission browser, archive surface, action routing, and addon integration hooks for the ECHO mod stack.

With ECHO gameplay mods installed, the terminal becomes the player's command surface: missions, records, route status, survival guidance, addon tabs, notifications, and actionable buttons can all appear in one consistent in-game interface. It is built as a modular shell, so ECHO: Ashfall Protocol, ECHO: Orbital Remnants, and future chapters can register their own pages without replacing the terminal.

Features:

- ECHO Terminal block and screen.
- Shared tab, page, badge, archive, and notification APIs.
- Mission browser and mission presentation system.
- Networked terminal actions for registered addon commands.
- Built-in visual shell for command, field, status, archive, and addon pages.
- Integration point for Ashfall, Orbital Remnants, and future ECHO chapters.

Requirements:

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+
- ECHO: Core 1.0.0 or newer

Recommended with:

- ECHO: Ashfall Protocol
- ECHO: Orbital Remnants

## ECHO: Ashfall Protocol

Banner: `publishing/curseforge/banners/echo-ashfall-protocol-banner.png`

Short description:

> Post-Gridfall survival overhaul with hazards, machines, factions, scanner-led POIs, ECHO-7 missions, and a Nexus finale.

Full description:

ECHO: Ashfall Protocol is a post-Gridfall survival overhaul for Minecraft 26.1.2 on NeoForge. You wake beside a ruined drop pod with ECHO-7, a damaged emergency AI, and rebuild from ash, scrap, toxic air, dirty water, radiation, failed machines, faction pressure, and buried Nexus infrastructure.

The world is not just hostile. It reacts. Toxic zones drain filters only where the air is dangerous, radiation can build into mutation pressure, dirty water is an emergency fallback, machines need power and maintenance, factions remember what you do, and the endgame resolves through a full Restore, Destroy, or Control Nexus choice.

Core features:

- Crash-site survival with ECHO-7 guidance and a first-night stabilization route.
- Hydration, dirty water, clean water, toxic air, radiation, cold pressure, filters, gas masks, RadAway, scrubbers, and field medicine.
- Scrap economy with debris salvage, hand recycling, thermal burning, water purification, ore grinding, isotope refining, battery storage, and power distribution.
- Scanner-led exploration through wastelands, toxic swamps, ruined cities, radiation zones, cryogenic ruins, crash scars, Nexus scars, faction hubs, POIs, and underground guardian arenas.
- Factions including Remnants, Salvagers, and Mutant-aligned threats with reputation, patrols, raids, quests, trade pressure, and intel.
- ECHO companion and drone progression for scouting, combat, scavenging, patrols, and terminal-linked reports.
- Research, schematics, JEI machine categories, recovered biome goods, rare resource routes, and late-game restoration infrastructure.
- Nine buried biome guardian nodes leading to the Nexus Core.
- Restore, Destroy, or Control finale with aftermath missions and optional orbital expansion unlock.

Requirements:

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+
- ECHO: Core 1.0.0 or newer

Recommended:

- ECHO: Terminal for the full shared ECHO interface.
- JEI for custom machine and process recipe visibility.
- ECHO: Orbital Remnants for the optional post-Nexus expansion.

## ECHO: Orbital Remnants

Banner: `publishing/curseforge/banners/echo-orbital-remnants-banner.png`

Short description:

> Post-Nexus orbital survival addon with launch prep, pressure suits, route worlds, surveys, factions, bosses, and ECHO-0.

Full description:

Earth made its choice below. Orbit still calls it quarantine.

ECHO: Orbital Remnants is a post-Nexus addon chapter for the modular ECHO stack. After any ECHO: Ashfall Protocol Nexus choice, ECHO-7 follows the pod's broken fall path back toward Station ECHO and the old ECHO-0 quarantine protocol.

Build launch infrastructure, assemble an Emergency Rocket, survive pressure and oxygen hazards, repair station routes, survey orbital worlds, pledge to orbital factions, and push through Low Earth Orbit, the Lunar Scar Zone, Mars Ash Basin, Europa Cryo Ocean, the Nexus Anomaly Belt, and the final ECHO-0 resolution.

Core features:

- Terminal-led progression with Next Step guidance, SCAN actions, last reports, launch readiness, route locks, suit telemetry, and faction contract status.
- Ground recovery sites including launch pads, satellite debris, comms arrays, cryo bunkers, and fallen pod evidence.
- Launch chain with Launch Platform, Rocket Assembly Frame, Fuel Refinery, Oxygen Compressor, pressure suit, oxygen support, and rocket assembly parts.
- Suit survival with oxygen, pressure, helmet seal, leaks, radiation, gravity, station power, emergency oxygen, suit sealant, and suit modules.
- Space routes through Low Earth Orbit, Moon, Mars, Europa, and the Nexus Anomaly Belt.
- Orbital machines including Solar Reclaimer, Vacuum Smelter, Heat Shield Fabricator, Orbital Fabricator, Suit Charging Station, Signal Analyzer, and more.
- Mid-game repair chains for station relays, lunar helium extractors, Mars pressure consoles, and Europa thermal arrays.
- Living Route Worlds surveys, deep sites, route caches, landmark scans, and post-ECHO Nexus stabilization.
- Orbital Remnant, Void Salvager, and Nexus Choir faction pledges and contracts.
- Hostile remnants including ECHO drones, Vacuum Wraiths, Broken Astronaut Suits, Nexus Husks, route bosses, and ECHO-0.

Requirements:

- Minecraft 26.1.2
- NeoForge 26.1.2.29-beta or newer
- Java 25+
- ECHO: Core 1.0.0 or newer

Recommended:

- ECHO: Ashfall Protocol for Nexus-choice gating and story continuity.
- ECHO: Terminal for shared Orbital Command, Route Survey, and ECHO-0 Records pages.

Compatibility notes:

- With ECHO: Ashfall Protocol installed, orbital calibration is locked until the player makes any Nexus choice: Restore, Destroy, or Control.
- With ECHO: Terminal installed, Orbital records and actions appear inside the shared terminal.
- Without ECHO: Terminal, Orbital Remnants keeps its standalone ECHO-7 Terminal item flow.
