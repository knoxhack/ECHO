# ECHO Recovery Changelog

## 1.3.0 — Make It Real

### Added
- **Sound events are now played**
  - `grave_create` plays when a grave is spawned on death
  - `grave_open` plays when a player right-clicks a grave
  - `grave_recover` plays when all items are recovered from a grave
- **HoloMap integration** (active when HoloMap is loaded)
  - Automatically creates a personal waypoint at every grave location
  - Waypoint title: "Grave of <player>"
- **Terminal integration** (active when Terminal is loaded)
  - Registers three archive entries: Grave Mechanics, Recovery Tools, Protection & Decay
- **Recover All button** in GraveScreen
  - Sends `RecoverAllPacket` to server; server validates access and recovers all items
- **Decay timer display** in GraveScreen
  - Shows "Expires in Xm" or "Expired" based on config
- **Grave block tooltip** — "Contains a player's lost items"

### Changed
- `RecoveryIntegrationDispatcher` now supports `onGraveCreated(ServerPlayer, BlockPos)` hook
- Version bumped to `1.3.0`

## 1.2.0 — Polish & UX

### Added
- **Grave UI / Container Screen** (`GraveMenu` + `GraveScreen`)
  - 54-slot grave inventory with full player inventory integration
  - Dark themed panel UI consistent with other ECHO modules
  - Right-click any grave to open its contents instead of instant recovery
- **Recipes**
  - `grave_key`: iron nuggets + name tag
  - `recovery_compass`: iron nuggets + compass
- **Advancements**
  - `echorecovery:root` — Obtained when acquiring a Grave Key
  - `echorecovery:recover_grave` — Use a grave block
  - `echorecovery:recovery_expert` — Recovery Expert goal (parented under recover_grave)
- **Loot Tables**
  - `blocks/grave` — Drops the grave block item when mined
- **Sound Events**
  - `grave_open`, `grave_close`, `grave_recover`, `grave_create`
- **Networking**
  - `RecoverAllPacket` serverbound payload for remote grave recovery

### Changed
- `GraveBlockEntity` now implements `Container`, enabling standard inventory slot sync
- `GraveBlock.useItemOn()` now opens the grave menu instead of instantly recovering all items
- Version bumped to `1.2.0`

## 1.0.0 — Initial Release

### Added
- Grave block creation on player death
- Safe placement logic with configurable radius
- Grave ownership, protection, and timed public access
- `/graves` command tree (list, locate, recover, delete, history, admin)
- Grave Key and Recovery Compass items
- Config spec with NeoForge config
- Datapack support for grave types, presets, and rules
- Integration stubs for Terminal, ThemeCore, HoloMap, MissionCore, TutorialCore, SoundCore
