# ECHO Chapter Handoff IDs

Canonical IDs are stable contract strings used by ECHO Core services, Terminal navigation, mission providers, recipe providers, and addon handoffs. Release code should add new IDs only with a migration note.

## Full-Stack Release Chapters

| Chapter | Canonical ID | Mod ID | Notes |
| --- | --- | --- | --- |
| Core services | `core` | `echocore` | Service contracts, addon registry, route/progress/reward APIs. |
| Terminal shell | `terminal` | `echoterminal` | UI/navigation shell, mission/action registries, archive surfaces, and recipe index registries. |
| Ashfall | `ashfall` | `echoashfallprotocol` | Main survival route and release starting chapter. |
| Orbital Remnants | `orbital_remnants` | `echoorbitalremnants` | Post-Nexus orbital follow-up chapter after Ashfall recovery. |
| Agriculture Reclamation | `agriculture_reclamation` | `echoagriculturereclamation` | Optional field recovery chapter for seed recovery, greenhouse safety, hydroponics, and chunk-local restoration. |
| Stationfall | `stationfall` | `echostationfall` | Station ECHO horror chapter after Orbital route state exposes the station. |
| Nexus Protocol | `nexus` | `echonexusprotocol` | Nexus corruption and memory escalation chapter. |
| Industrial Nexus | `industrial_nexus` | `echoindustrialnexus` | Included infrastructure and automation support chapter. |
| Blackbox Protocol | `blackbox_protocol` | `echoblackboxprotocol` | Late-game memory finale and final directive chapter. |

## Release Handoff IDs

| Flow | ID | Owner | Purpose |
| --- | --- | --- | --- |
| Ashfall initial pod ready | `ashfall:drop_pod_ready` | Ashfall | Fresh-world recovery starting state. |
| Ashfall terminal repair | `ashfall:repair_terminal` | Ashfall | Terminal reward/cache contract test mission ID. |
| Ashfall to Orbital route | `beta.route=ashfall_to_orbital` | Core profile ledger | Legacy stable ledger flag for Ashfall-to-Orbital handoff compatibility. |
| Orbital launch site scan | `orbital:scan_launch_site` | Orbital | Player-facing first Orbital calibration objective. |
| Orbital launch chain route | `echoorbitalremnants:orbital_launch_chain` | Orbital | Echo Core route record for launch readiness. |
| Orbital route worlds | `echoorbitalremnants:orbital_route_worlds` | Orbital | Echo Core route record for station/world survey progression. |
| Orbital quarantine endpoint | `echoorbitalremnants:orbital_echo_zero` | Orbital | Final Orbital endpoint record. |
| Agriculture Reclamation terminal | `echoagriculturereclamation:agriculture_reclamation` | Agriculture Reclamation | FIELD > Reclamation terminal tab and mission provider chapter. |
| Agriculture recovery cache | `agriculture_seed_cache` | Agriculture Reclamation | Echo Core recovery hook that grants emergency seed capsules. |
| Agriculture seed recovery milestone | `echoagriculturereclamation:recover_seed` | Agriculture Reclamation | Core milestone recorded when a recovered seed is opened or identified. |
| Agriculture soil analysis milestone | `echoagriculturereclamation:analyze_soil` | Agriculture Reclamation | Core milestone recorded by scanner or purifier soil analysis. |
| Agriculture first growth milestone | `echoagriculturereclamation:first_growth` | Agriculture Reclamation | Core milestone recorded after the first crop harvest. |
| Agriculture gene stabilization milestone | `echoagriculturereclamation:gene_stabilization` | Agriculture Reclamation | Core milestone recorded after a seed route is stabilized. |
| Agriculture greenhouse milestone | `echoagriculturereclamation:greenhouse_online` | Agriculture Reclamation | Core milestone recorded when greenhouse safety reaches the configured threshold. |
| Agriculture chunk restoration milestone | `echoagriculturereclamation:restore_chunk` | Agriculture Reclamation | Core milestone recorded when local restoration pressure reaches chunk restoration. |
| Stationfall Blackbox recovered | `stationfall:blackbox_recovered` | Stationfall | Opens Nexus Protocol handoff context. |
| Industrial processing recipes | `echoindustrialnexus:industrial_processing` | Industrial Nexus | Data recipe type surfaced by Industrial machine processing and Terminal Recipe Index entries. |

## Selectable Addon Sets

| Addon set | Included modules | Purpose |
| --- | --- | --- |
| `-PechoAddonSet=beta` | Core, Terminal, Ashfall, Orbital Remnants, Agriculture Reclamation | Smaller compatibility build. |
| `-PechoAddonSet=all` | Core, Terminal, Ashfall, Orbital Remnants, Agriculture Reclamation, Stationfall, Nexus Protocol, Industrial Nexus, Blackbox Protocol | Current full-stack release verification target. |

## Rules

- Full-stack release tasks should verify every included ECHO module and keep exactly one current jar for each module in release modpack folders.
- Smaller beta addon-set builds may include only Core, Terminal, Ashfall, and Orbital Remnants, but docs and publish copy should not describe Stationfall, Nexus Protocol, Industrial Nexus, or Blackbox as reserved or excluded.
- Addons must register Terminal navigation profiles explicitly; group-name fallbacks are not a chapter ownership contract.
- Recipe-aware addons should register `TerminalRecipeProvider` instances explicitly; duplicate provider/category/recipe IDs are compatibility errors or warnings, not ownership negotiation.
- Existing save keys may remain for compatibility, but current UI should guide Ashfall completion toward the full post-Nexus addon chain.
