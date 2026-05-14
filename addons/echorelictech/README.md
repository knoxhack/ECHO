# ECHO: RelicTech

**Display name:** ECHO: RelicTech  
**Mod ID:** `echorelictech`

ECHO: RelicTech is Ashfall's legendary loot chase — but not fantasy magic loot.

Players recover damaged old-world prototypes from vaults, labs, blacksites, guardian sites, Nexus scars, orbital wreckage, cryogenic ruins, Stationfall sections, and Blackbox memory chambers. They must identify, repair, stabilize, overclock, contain, or risk using them.

## Design Rule

**Every relic must be powerful enough to be exciting, but dangerous enough to respect.**

These are broken pre-Gridfall devices, failed ECHO experiments, forbidden Nexus prototypes, lost military hardware, damaged AI cores, and experimental survival tools.

## Gameplay Loop

1. **Recover** an `Unidentified Relic` from a vault or ruin.
2. **Analyze** it in a `Relic Analyzer` to discover what it is.
3. **Repair / Stabilize** it at a `Prototype Workbench` using materials.
4. **Use** it when you need it most — and accept the risk.
5. **Contain** dangerous relics in a `Containment Locker` if they become too unstable.

## Relic Conditions

- `UNKNOWN` — Cannot be used.
- `DAMAGED` — Can activate, but higher failure chance.
- `STABILIZED` — Normal operation.
- `OVERCLOCKED` — Stronger effects, but riskier.
- `CONTAINED` — Safer, but weaker / longer cooldown.
- `CORRUPTED` — Powerful and unreliable.

## Relic Tiers

- `FIELD` — Common survivor tech.
- `PROTOTYPE` — Pre-Gridfall experimental devices.
- `FORBIDDEN` — Decommissioned or outlawed tech.
- `NEXUS` — Nexus-touched or post-Gridfall anomaly tech.

## Relic Categories

MOBILITY, SURVIVAL, COMBAT, UTILITY, AI, WORLD, POWER, SCANNER, ARMOR, WEAPON

## MVP Relics

| Relic | Category | Tier | Behavior |
|-------|----------|------|----------|
| Phase Anchor | Mobility | Prototype | Bind and recall to safe anchor point |
| Null Battery | Power | Prototype | Stores Null Charge for relics |
| Guardian Lens | Scanner | Prototype | Detects nearby relic machines/traces |
| Echo Mirror | Combat | Forbidden | Creates defensive echo projection |
| Matter Stitcher | Survival | Forbidden | Repairs armor or heals player |

## Instability System

Using dangerous relics increases **Player Relic Instability**. At higher levels, relics become more likely to malfunction, attract hostile signals, or cause reality bleeds.

Levels:
- 0 STABLE
- 1 ECHO STATIC
- 2 SYSTEM DRIFT
- 3 NEXUS ATTENTION
- 4 REALITY BLEED
- 5 CRITICAL INSTABILITY

Instability decays over time if configured.

## Failure System

When a relic malfunctions, it consults a data-driven **Failure Table**. Failures range from minor fizzles to major backfires, each with appropriate effects.

## Machines

- **Relic Analyzer** — Identifies unknown relics.
- **Prototype Workbench** — Repairs, stabilizes, overclocks, contains, or purges relics.
- **Containment Locker** — Stores dangerous relics safely.
- **Null Battery Dock** — Charges Null Batteries.

## Containment

High-tier or corrupted relics carried without containment may trigger passive warnings. Stored relics in a Containment Locker do not contribute to passive warnings.

## Data-Driven Relics

Relic definitions and failure tables are loaded from JSON and support addon namespaces:
- `data/*/relics/*.json`
- `data/*/relic_failures/*.json`

## Integrations

Optional no-op safe integrations are provided for:
- ECHO Terminal
- ECHO Lens
- ECHO HoloMap
- ECHO PowerGrid
- ECHO WorldCore
- ECHO SoundCore

## Commands

- `/echorelictech give_relic <player> <relicId>`
- `/echorelictech instability get <player>`
- `/echorelictech instability set <player> <amount>`
- `/echorelictech instability add <player> <amount>`
- `/echorelictech identify_held`
- `/echorelictech corrupt_held`
- `/echorelictech stabilize_held`
- `/echorelictech contain_held`
- `/echorelictech overclock_held`
- `/echorelictech trigger_failure <severity>`
- `/echorelictech bind_phase_anchor`
- `/echorelictech locate_vault`
- `/echorelictech debug`
- `/echorelictech reload`

## Config

See `RelicTechConfig.java` for server-side balance options including:
- `enableRelicInstability`
- `phaseAnchorCooldownTicks`
- `nullBatteryMaxCharge`
- Failure chance multipliers per condition
- Instability thresholds and decay settings

## API

`RelicTechApi` provides public methods for:
- Querying relic state
- Modifying relic condition
- Managing player instability
- Consuming Null Charge
- Triggering failures
- Reporting relic use for faction integration

## Known Limitations / Future Passes

- Full Terminal UI pages are scaffolded, not yet rendered.
- Lens deep-scan rows are data-ready but UI integration is scaffold.
- HoloMap vault markers are scaffolded.
- Worldgen structures are placeholders.
- Faction reputation reactions are event-scaffolded.
- Research integration is data-ready but not gated.
- Nexus ending-path multipliers are scaffolded.
- Custom entity decoys (Echo Mirror) use effect-based MVP.

## License

All Rights Reserved
