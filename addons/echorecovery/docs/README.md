# ECHO Recovery

ECHO Recovery is a standalone-first death recovery module for the ECHO ecosystem.

## Overview

- **Public name**: ECHO Recovery
- **In-game name**: Graves / Recovery
- **Ashfall name**: Field Recovery Cache
- **Mod ID**: `echorecovery`
- **Current version**: 1.3.0

## Standalone Behavior

Works with only **ECHO Core + NetCore** installed. When a player dies:

- A grave block is created at the death location
- All inventory items, armor, and offhand items are stored
- A Grave Key is optionally given to the player
- A Recovery Compass can be crafted to locate graves
- Right-click a grave to open its inventory UI and retrieve items

Players can use `/graves` to list, locate, and recover their graves.

## What's New in 1.3.0

- **Sound events are now live**: grave creation, opening, and recovery all play registered sounds.
- **HoloMap integration**: When HoloMap is loaded, every grave automatically creates a personal waypoint.
- **Terminal integration**: When Terminal is loaded, Recovery archive entries appear in the terminal.
- **Recover All button**: Click the button in the grave UI to recover all items at once.
- **Decay timer**: Grave UI shows how long until the grave expires.
- **Grave tooltip**: Block item tooltip shows "Contains a player's lost items".

## What's New in 1.2.0

- **Grave UI / Container Screen**: Right-click any grave to open a 54-slot inventory screen. Items can be moved individually or in bulk.
- **Recipes**: Grave Keys and Recovery Compasses now have crafting recipes.
- **Advancements**: New advancement tree — `root`, `recover_grave`, and `recovery_expert`.
- **Loot Tables**: Graves now have proper loot table support.
- **Sound Events**: Registered placeholder sound events for grave interactions (`grave_open`, `grave_close`, `grave_recover`, `grave_create`).
- **Packet Networking**: Serverbound `RecoverAllPacket` for "recover all" convenience button.

## Integration Behavior

When other ECHO modules are present, ECHO Recovery optionally integrates:

| Module | Integration |
|--------|-----------|
| Terminal | Registers Graves / Recovery / Field Recovery pages |
| ThemeCore | Uses theme tokens for UI and grave visuals |
| HoloMap | Adds grave markers and death markers |
| MissionCore | Exposes recovery objectives |
| TutorialCore | Registers tutorial cards for new players |
| SoundCore | Plays grave/recovery sounds |
| WorldCore | Uses hazard-aware safe placement |
| WeatherCore | Storms may interfere with markers |
| Lens | Scan graves for condition and contamination |
| Armory | Preserves weapon modules and loadouts |
| Logistics | Enables remote grave delivery |
| Convoy | Enables convoy recovery missions |
| PowerGrid | Powered recovery beacons |
| RelicTech | Soul-bound relic handling |
| Nexus | Optional corruption effects |
| Blackbox | Death evidence records |
| Ashfall | Activates Field Recovery naming and hazards |

## Config

See [CONFIG.md](CONFIG.md) for all configuration options.

## Commands

- `/graves` - List active graves
- `/graves locate <id>` - Show grave coordinates
- `/graves recover <id>` - Remote recover (if enabled)
- `/graves delete <id>` - Delete a grave
- `/graves history` - Show death history
- `/graves debug` - Debug info (admin)
- `/graves reload` - Reload config (admin)
- `/graves admin list <player>` - List player graves (admin)
- `/graves admin restore <player> <id>` - Admin restore (admin)
- `/graves admin delete <player> <id>` - Admin delete (admin)
