# Modules and Versions

## Public Stack

| Module | Version | Role |
|---|---:|---|
| `echocore` | `1.1.0` | Shared service contracts and persistence surfaces |
| `echoterminal` | `1.1.0` | Shared terminal shell and route UI |
| `echoashfallprotocol` | `1.3.0` | Main campaign and entry progression |
| `echoorbitalremnants` | `1.5.0` | Post-Nexus orbital continuation |
| `echoagriculturereclamation` | `0.1.0` | Ecology and food-route recovery chapter |
| `echostationfall` | `1.1.0` | Station horror/progression chapter |
| `echonexusprotocol` | `1.0.0` | Nexus corruption and escalation chapter |
| `echoindustrialnexus` | `0.1.0` | Industrial automation/recovery chapter |
| `echoblackboxprotocol` | `1.0.0` | Late-game blackbox finale chapter |

## Workspace Module Sets

The build can target a **beta-only** addon set or **all** addons by `echoAddonSet`.

- `beta`: core + beta addon list
- `all`: beta + release addon list

See root `settings.gradle` for authoritative inclusion logic.
