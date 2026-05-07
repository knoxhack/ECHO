# ECHO Chapter Handoff IDs

Canonical IDs are stable contract strings used by ECHO Core services, Terminal navigation, mission providers, and addon handoffs. Public beta code should add new IDs only with a migration note.

## Public Beta Chapters

| Chapter | Canonical ID | Mod ID | Notes |
| --- | --- | --- | --- |
| Core services | `core` | `echocore` | Service contracts, addon registry, route/progress/reward APIs. |
| Terminal shell | `terminal` | `echoterminal` | UI/navigation shell and mission/action registries. |
| Ashfall | `ashfall` | `echoashfallprotocol` | Main survival route and public beta starting chapter. |
| Orbital Remnants | `orbital_remnants` | `echoorbitalremnants` | Public beta follow-up chapter after Ashfall recovery. |

## Beta Handoff IDs

| Flow | ID | Owner | Purpose |
| --- | --- | --- | --- |
| Ashfall initial pod ready | `ashfall:drop_pod_ready` | Ashfall | Fresh-world recovery starting state. |
| Ashfall terminal repair | `ashfall:repair_terminal` | Ashfall | Terminal reward/cache contract test mission ID. |
| Ashfall to Orbital route | `beta.route=ashfall_to_orbital` | Core profile ledger | Stable ledger flag for beta route handoff documentation. |
| Orbital launch site scan | `orbital:scan_launch_site` | Orbital | Player-facing first Orbital calibration objective. |
| Orbital launch chain route | `echoorbitalremnants:orbital_launch_chain` | Orbital | Echo Core route record for launch readiness. |
| Orbital route worlds | `echoorbitalremnants:orbital_route_worlds` | Orbital | Echo Core route record for station/world survey progression. |
| Orbital quarantine endpoint | `echoorbitalremnants:orbital_echo_zero` | Orbital | Final public beta Orbital endpoint record. |

## Reserved Future IDs

These IDs are reserved for save compatibility and future planning, but they are not enabled in the default public beta addon set.

| Future Chapter | Reserved ID | Mod ID | Beta Status |
| --- | --- | --- | --- |
| Stationfall | `stationfall` | `echostationfall` | Development only; planned bridge after Orbital. |
| Blackbox Protocol | `blackbox_protocol` | `echoblackboxprotocol` | Development only; final-chapter scale after Stationfall/Nexus contracts stabilize. |
| Nexus Protocol | `nexus` | `echonexusprotocol` | Excluded from public beta; legacy Ashfall save data remains readable. |
| Industrial Nexus | `industrial_nexus` | `echoindustrialnexus` | Optional/side addon until product scope is decided. |

## Rules

- Default beta release tasks must include only Core, Terminal, Ashfall, and Orbital Remnants.
- Future chapter jars must be treated as forbidden in beta modpack folders.
- Addons must register Terminal navigation profiles explicitly; group-name fallbacks are not a chapter ownership contract.
- Existing save keys may remain for compatibility, but beta UI should guide Ashfall completion toward Orbital rather than Nexus.
