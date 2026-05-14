# ECHO: TutorialCore

**Make Ashfall deep, but not confusing.**

ECHO: TutorialCore is a first-party ECHO addon that provides shared onboarding, guided tutorials, contextual hints, first-time popups, mistake detection, Terminal tutorial cards, beginner guide mode, and codex-style help for the entire ECHO ecosystem.

TutorialCore does not make Ashfall simpler. It makes Ashfall **readable**.

---

## What Problem It Solves

Ashfall is a complex ruined-Earth expedition survival RPG. New players face deep systems, hidden dependencies, and lethal hazards without clear feedback. TutorialCore exists to:

- Surface critical information exactly when it is needed.
- Explain mistakes without punishing the player.
- Provide a searchable, persistent guide system through the Terminal.
- Respect expert players by being ignorable and non-spammy.

---

## Guide Modes

Per-player guide modes control how much help the system gives:

- **OFF**: No automatic popups or hints. Manual Guide cards still exist.
- **MINIMAL**: Only major first-time tips and critical danger warnings.
- **NORMAL**: Default balanced guidance.
- **ASSISTED**: More active guidance, missing item hints, stuck detection, and next-step suggestions.

Default: **NORMAL**

Guide modes are persisted per player and can be changed via command or (if present) Terminal UI.

---

## Tutorial Cards

Data-driven guide cards organized by category:

- Start Here, Survival, Terminal, Scanner, HoloMap, Lens, Power, Machines, Water, Hazards, Factions, Research, Drones, Combat, Nexus, Route Chapters, Troubleshooting, Advanced, Addons.

Cards support:
- Title, summary, body paragraphs, steps, common mistakes
- Related cards, items, blocks, missions
- Unlock triggers, visibility states
- Addon ownership for extensibility

Cards are readable in the Terminal Guide page when Terminal is present. Without Terminal, they are accessible via commands or chat fallback.

---

## Contextual Hints

Hints trigger based on player state and are evaluated periodically (not every tick). They respect:
- Guide mode restrictions
- Cooldowns and dismissals
- Caps per minute / per session
- Danger overrides (critical warnings bypass normal cooldowns)

Hint types: INFO, WARNING, DANGER, BLOCKED, MISSING_ITEM, PROGRESSION, SYSTEM_HELP, MISSION_HELP, RECIPE_HELP, HAZARD_HELP, COMBAT_HELP, MACHINE_HELP, POWER_HELP, ROUTE_HELP.

---

## Mistake Detection

TutorialMistakeDetector tracks common errors and produces useful hints after repeated occurrences:

- No power connected to machine
- Missing filter in scrubber / gas mask
- Dirty water overuse
- Hazard unprepared entry
- Recipe locked attempt
- Unclaimed reward
- No active mission
- Scanner / HoloMap ignored
- Repeated failure pattern

All detection is non-spammy, server-safe, and configurable.

---

## Requirement Hints

When a player lacks items, research, missions, or faction standing for an action, TutorialCore can explain what is missing. The resolver supports:
- Item, tag, block, research, mission, and faction requirements
- Safe optional lookups (no hard crash if other modules are absent)
- Guide card links for deeper reading

---

## Recipe Lock Explanation Scaffold

TutorialCore provides API hooks and a data-driven lock explanation model for "why can't I craft this?" systems. Full integration with Index/Recipe systems can be wired later without breaking vanilla crafting.

---

## First-Hour Onboarding Flow

Optional first-hour flow tracks:
- Terminal opened
- Water found
- Resources gathered
- First power loop
- Clean water produced
- Signal lead followed
- Hazard preparation

Flows unlock cards and provide a gentle, non-intrusive introduction.

---

## Terminal Integration

When ECHO Terminal is present, TutorialCore:
- Registers a Guide page/hub scaffold
- Provides card lists by category
- Supplies "What Now" recommendations
- Shows unread/new card markers (if Terminal API supports it)

If Terminal is absent, all functionality falls back to commands and chat.

---

## Lens Integration

When ECHO Lens is present, TutorialCore can supply assist rows:
- Machine offline: suggest power connection
- Missing filter: suggest cartridge insertion
- Unknown block: suggest deep scan

A provider scaffold is included for future wiring.

---

## HoloMap Integration

When ECHO HoloMap is present, TutorialCore can supply:
- Route prep warnings (radiation, toxic air, difficulty)
- Signal lead context (survivor cache, guardian site)
- Preparation checklists before dangerous routes

A provider scaffold is included for future wiring.

---

## PowerGrid Integration

When ECHO PowerGrid is present, TutorialCore:
- Receives power events (no power, breaker trip, brownout, overload)
- Shows PowerGrid-specific tutorial hints
- Unlocks Power Basics cards

If direct integration is not available, PowerGrid can call:
- `TutorialCoreApi.reportNoPower(player, blockPos)`
- `TutorialCoreApi.reportBreakerTripped(player, blockPos)`
- `TutorialCoreApi.reportBrownout(player, blockPos)`
- `TutorialCoreApi.reportOverload(player, blockPos)`

---

## MissionCore Integration

When ECHO MissionCore is present, TutorialCore can register tutorial missions/flows internally and provide a MissionCore bridge scaffold.

---

## WorldCore Integration

When ECHO WorldCore is present, TutorialCore reads hazard/region context and triggers first-time hazard hints with accurate region names.

---

## SoundCore Integration

When ECHO SoundCore is present, TutorialCore:
- Plays subtle tutorial notification sounds
- Plays warning stingers for danger hints
- Plays guide card unlock sounds

If SoundCore is absent, all sound calls are no-op.

---

## Data-Driven JSON Formats

All cards, hints, and flows are loaded from JSON under:
- `data/<namespace>/tutorial_cards/*.json`
- `data/<namespace>/tutorial_hints/*.json`
- `data/<namespace>/tutorial_flows/*.json`

Addons can add their own content by placing JSON in their own namespace.

Invalid JSON is logged and skipped. The game does not crash.

---

## Public API

```java
TutorialCoreApi.registerCard(...)
TutorialCoreApi.registerHint(...)
TutorialCoreApi.registerFlow(...)
TutorialCoreApi.unlockCard(ServerPlayer player, Identifier cardId)
TutorialCoreApi.showCard(ServerPlayer player, Identifier cardId)
TutorialCoreApi.showHint(ServerPlayer player, Identifier hintId)
TutorialCoreApi.showHint(ServerPlayer player, TutorialHint hint)
TutorialCoreApi.markProgress(ServerPlayer player, Identifier progressId)
TutorialCoreApi.hasProgress(ServerPlayer player, Identifier progressId)
TutorialCoreApi.setGuideMode(ServerPlayer player, TutorialGuideMode mode)
TutorialCoreApi.getGuideMode(ServerPlayer player)
TutorialCoreApi.reportMistake(ServerPlayer player, Identifier mistakeId)
TutorialCoreApi.reportMissingRequirement(ServerPlayer player, Identifier requirementId)
TutorialCoreApi.getRecommendedNextSteps(ServerPlayer player)
```

Convenience calls:
```java
TutorialCoreApi.reportNoPower(player, pos)
TutorialCoreApi.reportMissingFilter(player)
TutorialCoreApi.reportRecipeLocked(player)
TutorialCoreApi.reportHazardUnprepared(player)
TutorialCoreApi.reportRewardAvailable(player)
TutorialCoreApi.reportSignalDetected(player)
TutorialCoreApi.reportGuardianLocated(player)
TutorialCoreApi.reportFactionContact(player)
```

All methods are null-safe, side-safe, and optional-integration-safe.

---

## Commands

```
/echotutorialcore guide_mode <off|minimal|normal|assisted>
/echotutorialcore progress [player]
/echotutorialcore reset <player>
/echotutorialcore unlock_card <player> <cardId>
/echotutorialcore show_card <player> <cardId>
/echotutorialcore hint <player> <hintId>
/echotutorialcore list_cards
/echotutorialcore list_hints
/echotutorialcore debug
/echotutorialcore simulate_stuck <player>
/echotutorialcore reload
```

Normal players can set their own guide mode and view progress. Admin permission is required for reset, unlock, show to others, simulate_stuck, and debug.

---

## Config Options

**Server (`echotutorialcore-common.toml`)**
- `allowAssistedGuideMode`
- `forceGuideMode`
- `enableMistakeDetection`
- `enableStuckDetection`
- `enableRecipeLockExplanations`
- `enableHazardWarnings`
- `enableFirstHourFlow`
- `enableTooltipHelp`
- `maxHintsPerMinute`
- `maxPopupsPerSession`
- `stuckDetectionMinutes`
- `repeatedDeathThreshold`
- `noCleanWaterWarningDay`

**Client (`echotutorialcore-client.toml`)**
- `showTutorialPopups`
- `showContextualHints`
- `showDangerWarnings`
- `showTooltipHelp`
- `showTerminalGuideCards`
- `showToastHints`
- `playTutorialSounds`
- `guideModeDefault`
- `hintScale`
- `hintDurationTicks`

---

## Known Limitations

- Full Terminal UI integration is scaffolded; wiring depends on Terminal addon hub API evolution.
- Lens and HoloMap assist rows are scaffolded for future addon-specific integration.
- Deep inventory/equipment condition evaluation for hints is partially scaffolded and will expand with WorldCore and MissionCore APIs.
- Custom tutorial card screens are not implemented in MVP; chat and toast fallback is used.

---

## Future Roadmap

- Full Terminal Guide page with search/filter
- Deep condition evaluation engine (inventory, region, mission state)
- Expanded tooltip help system
- MissionCore tutorial mission registration
- WorldCore hazard discovery events
- PowerGrid direct event subscription
- SoundCore event bridge
- Stuck detection heuristics with automated What Now updates
- Addon registration API for third-party tutorial content

---

## License

All Rights Reserved.

Built as part of the ECHO / Ashfall ecosystem by KnoxHack.
