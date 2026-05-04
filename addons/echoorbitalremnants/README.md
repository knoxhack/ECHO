# ECHO: Orbital Remnants

Earth made its choice below. Orbit still calls it quarantine.

ECHO: Orbital Remnants is a post-Nexus addon chapter for ECHO: Ashfall Protocol built on the bundled 26.1.2 NeoForge setup. It adds a terminal-led progression arc from Earth recovery sites to explorable Low Earth Orbit, Station ECHO debris, the Lunar Scar Zone, Mars Ash Basin, Europa Cryo Ocean, the Nexus Anomaly Belt, and the ECHO-0 quarantine protocol.

## Chapter Flow

After any ECHO: Ashfall Protocol Nexus choice, ECHO-7 controls orbital progression through the ECHO-7 Terminal and contributes status back into the main ECHO terminal. Restore, Destroy, or Control all tell orbit the same thing: Earth is no longer only a containment field.

- Craft the ECHO-7 Terminal.
- Sneak-use it on Earth to calibrate orbital contact.
- Recover starter salvage from launch pads, satellite debris, comms arrays, cryo bunkers, and fallen pod evidence.
- Build suit gear, oxygen support, launch infrastructure, and rocket parts.
- Assemble the Emergency Rocket and reach orbit.
- Restore Station ECHO systems, repair route networks, unlock later route vessels, survive hostile anomalies, and resolve ECHO-0.
- Continue into the Living Route Worlds loop: explore deep route sites, map landmarks, stabilize hazards, finish post-ECHO Nexus anchors, complete one faction contract, and seal the final survey network.
- Use the terminal's Next Step, SCAN, SURVEY, and faction contract reports whenever progression is blocked; ECHO guidance names missing hooks directly.

## Modular ECHO Integration

ECHO: Orbital Remnants is an optional post-Nexus chapter in the modular ECHO stack. It runs with `echocore`, can surface its route state in `echoterminal`, and uses shared core services to react to Ashfall's Nexus choice when Ashfall is installed.

- Before an ECHO: Ashfall Protocol Nexus choice, Earth orbital calibration is locked by the quarantine handoff.
- After any Nexus choice, the normal ECHO-7 launch-site scan path opens.
- The main ECHO terminal shows Orbital Command, Survey, and ECHO mission records with route flags, suit telemetry, station power, faction standings, and optional utility support caches.
- Without the shared ECHO Terminal, Orbital Remnants keeps its standalone terminal item flow as the fallback command surface.

## Core Systems

- **Terminal progression:** Next Step guidance, scan requirement, last report, launch readiness, route locks, ECHO memory, faction standing, and active faction contract.
- **Shared Terminal polish:** optional Terminal-installed mission records and once-only support caches that mirror Orbital progress without replacing standalone ECHO-7 progression.
- **Launch chain:** Launch Platform, Rocket Assembly Frame, Fuel Refinery, Oxygen Compressor, pressure suit, oxygen tank, and six rocket assembly parts.
- **Suit survival:** oxygen, pressure, helmet seal, suit leak state, radiation, gravity, station power, emergency oxygen cells, suit sealant, and suit modules.
- **Orbital machines:** one-input/one-output machines with processing progress and internal charge.
- **Space routes:** Emergency Rocket, Orbital Shuttle, Mars Transfer Window, Europa Transfer Window, and Nexus Drive Vessel.
- **Route terrain:** deterministic orbital debris corridors, lunar scar trenches, Martian ash/cavern fields, Europa ice pockets, and Nexus anomaly chains.
- **Survey objectives:** terminal-tracked unique landmark scans for Orbit, Moon, Mars, Europa, and post-ECHO Nexus stabilization.
- **Mid-game route objectives:** three-site repair chains for the Station Network, Lunar Helium Extractors, Mars Pressure Consoles, and Europa Thermal Arrays.
- **Deep sites:** three repeatable site families per route with fixed caches, objective blocks, traversal hooks, and local hazard pressure.
- **Factions:** Orbital Remnant, Void Salvagers, and Nexus Choir alignment rewards plus terminal-driven contracts.
- **Threats:** ECHO drones, Vacuum Wraiths, Broken Astronaut Suits, Nexus Husks, route bosses, and ECHO-0.

## Progression

1. Ground Recovery
2. Launch Prep
3. Low Earth Orbit
4. Station Signal Recovery
5. Station Network Repairs
6. Lunar Helium Extractors
7. Mars Pressure Consoles
8. Europa Thermal Arrays
9. Deep Space Protocol
10. Nexus Anomaly Belt and ECHO-0
11. Living Route Worlds Survey Network
12. First faction contract completion
13. Final network seal

## Build

Requirements:

- Java 25
- NeoForge userdev through the bundled Gradle setup

Build the addon from the ECHO workspace root:

```powershell
.\gradlew.bat :echoorbitalremnants:build
```

Run a development client with Ashfall loaded from the root project:

```powershell
.\gradlew.bat :echoorbitalremnants:runOrbitalClient
```

Run automated checks:

```powershell
.\gradlew.bat :echoorbitalremnants:build :echoorbitalremnants:runGameTestServer
```

The built jar is produced in `addons/echoorbitalremnants/build/libs/`.

## Release Checklist

1. Run `.\gradlew.bat :echoorbitalremnants:build`.
2. Run `.\gradlew.bat :echoorbitalremnants:runGameTestServer`.
3. Launch a client and craft the ECHO-7 Terminal, Signal Analyzer, launch chain, and route vessels.
4. Confirm first-play flow: Earth calibration, launch, Orbit, Moon, Mars, Europa, ECHO-0, SURVEY stabilization, one faction contract, and the final network seal.
5. With ECHO: Terminal installed, confirm Orbital Command, Survey, and ECHO mission tabs render and that optional support caches claim once.
6. Complete at least one faction pledge and terminal contract.
7. Run the automated build and GameTest server before packaging.
8. Expected jar: `addons/echoorbitalremnants/build/libs/echoorbitalremnants-1.4.0.jar`.

## Documentation

Use `guide.md` for the full progression, machine, route, survival, faction, and texture-generation reference.
