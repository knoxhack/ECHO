# ECHO PlayerCore

Player utility, homes, random teleport, back, spawn, cooldown, and travel QoL systems for the ECHO/Ashfall ecosystem.

## Purpose
ECHO PlayerCore is a first-party ECHO addon that owns player utility commands and quality-of-life features. It is intentionally separate from ECHO Ashfall Protocol (survival campaign/content) and ECHO WorldCore (world regions/hazards/marker services).

## Commands
- `/sethome [name]` - Save your current location as a home.
- `/home [name]` - Teleport to a saved home.
- `/delhome [name]` - Delete a saved home.
- `/homes` - List your saved homes.
- `/back` - Return to your last teleport/death location.
- `/rtp` - Random teleport to a safe surface location.
- `/spawn` - Teleport to world spawn.
- `/echo sethome [name]`, `/echo home [name]`, `/echo delhome [name]`, `/echo homes`, `/echo back`, `/echo rtp`, `/echo spawn` - ECHO namespace aliases.

## Config
All config lives in `echoplayercore.toml` (COMMON side).

### Categories
- `general` - Enable/disable module and aliases.
- `homes` - Max homes, cross-dimension rules, naming rules.
- `random_teleport` - Radius, cooldown, safety checks, allowed dimensions.
- `back` - Cooldown, store-back rules, death recovery.
- `spawn` - Spawn command settings, cross-dimension rules.
- `permissions` - Op bypass, permission levels for admin commands.
- `performance` - RTP scan limits and optional RuntimeGuard integration.
- `messages` - Prefix and message style settings.

## Data Storage
Player travel data (homes, back, death, cooldowns) is stored via Minecraft `SavedData` per overworld by default. If ECHO DataCore is present, future integration may migrate to DataCore keys.

## Optional Integrations
- **DataCore** - Future persistent data bridge if safe.
- **WorldCore** - Safe-location search and hazard avoidance if available.
- **HoloMap** - Future home/warp/death markers provider.
- **Terminal** - Future Player/Travel tab DTOs.
- **RuntimeGuard** - RTP search budgeting if available.
- **ClaimCore** - Future claim-aware RTP landing.

## Future Roadmap
- TPA system (`/tpa`, `/tpaccept`, `/tpdeny`)
- Warps (`/warp`, `/warps`, `/setwarp`, `/delwarp`)
- Terminal Travel tab integration
- HoloMap home/warp markers
- Outpost fast travel network
- ClaimCore support for RTP

## License
All Rights Reserved
