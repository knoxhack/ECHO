# MissionCore Addon Registration

MissionCore content belongs in addons, while `echocore` owns the service contract. Register missions with `EchoCoreServices.registerMissionContent(source, registrar)` from common setup. The registrar is buffered by Core, so it is safe if MissionCore loads before or after the addon.

Use Java registration for missions that need live predicates, migration, or side effects. Use JSON under `data/<namespace>/missioncore` for static side ops and contracts.

Report runtime progress from server-side gameplay hooks only:

- `DISCOVER_STRUCTURE` for POI/structure discovery.
- `ENTER_REGION` for biome, dimension, and route region entry.
- `SCAN_BLOCK` and `SCAN_ENTITY` for scanner tools.
- `OBTAIN_ITEM`, `CRAFT_ITEM`, and `DELIVER_ITEM` for inventory objectives.
- `KILL_ENTITY` and `PLACE_BLOCK` for combat/building hooks.
- `REPAIR_MACHINE`, `BUILD_MULTIBLOCK`, `DRIVE_VEHICLE`, `ESTABLISH_ROUTE`, `COMPLETE_ORBITAL_SCAN`, `UNLOCK_RESEARCH`, `SURVIVE_TIME`, and `SURVIVE_DAYS` for addon-specific systems.

Never call client-only classes from a MissionCore registrar. Keep rendering and screens in Terminal or the owning addon client package.

## Java Registration Example

```java
EchoCoreServices.registerMissionContent("exampleaddon", registry -> {
    Identifier chapter = Identifier.fromNamespaceAndPath("exampleaddon", "field_ops");
    registry.registerChapter("exampleaddon", new MissionChapterDefinition(
            chapter, "Field Ops", "Example addon objectives.", 20, 0x55FFDD));

    registry.registerMission("exampleaddon", MissionDefinition.builder(
                    Identifier.fromNamespaceAndPath("exampleaddon", "scan_relay"), chapter)
            .phase("field_ops", "Field Ops", 0, 1)
            .text("Scan Relay", "Scan one relay block.", "Relay indexed.")
            .objective(ObjectiveDefinition.simple(
                    Identifier.fromNamespaceAndPath("exampleaddon", "scan_relay/objective"),
                    MissionObjectiveType.SCAN_BLOCK,
                    "Scan a relay",
                    "",
                    ItemStack.EMPTY,
                    1))
            .reward(RewardDefinition.item(
                    Identifier.fromNamespaceAndPath("exampleaddon", "scan_relay/reward"),
                    MissionRewardClaimMode.CLAIMABLE,
                    new ItemStack(Items.EMERALD)))
            .build());
});
```

## Java Custom Actions

MissionCore keeps Java-only hooks for addon-specific Terminal actions. Use
`MissionDefinition.Builder.actionProvider` to expose action buttons and
`actionHandler` to process action ids that are not MissionCore built-ins:

```java
registry.registerMission("exampleaddon", MissionDefinition.builder(
                Identifier.fromNamespaceAndPath("exampleaddon", "route_choice"), chapter)
        .text("Route Choice", "Choose a route path.", "Route acknowledged.")
        .actionProvider((player, mission, status, completeNow) -> List.of(
                MissionActionView.enabled("route_north", "North"),
                MissionActionView.enabled("route_south", "South")))
        .actionHandler((player, mission, actionId) -> switch (actionId) {
            case "route_north" -> LegacyRoutes.chooseNorth(player);
            case "route_south" -> LegacyRoutes.chooseSouth(player);
            default -> false;
        })
        .build());
```

Built-in actions still behave the same: `start` tracks a mission, `complete` turns
in complete objectives, and `claim` claims pending rewards. JSON missions cannot
declare arbitrary handlers; use a Java registrar when an action needs code.

Objective progress should be reported from the server-side gameplay action that proves it happened:

```java
EchoCoreServices.recordMissionObjective(
        serverPlayer,
        MissionObjectiveType.SCAN_BLOCK,
        Identifier.fromNamespaceAndPath("exampleaddon", "relay"),
        1,
        Map.of("source", "relay_scanner"));
```

## Direct Runtime Hooks

MissionCore 0.3.0 adds hook coverage validation for migrated addons. Adapters remain
the import/mirror layer, but server-side gameplay code should record objective
progress directly when a route, scan, machine output, decode, research unlock, boss
kill, or other milestone is actually completed.

Mission ids remain the existing `TerminalMissionDefinition.id()` values. Objective
targets should use stable addon namespace IDs:

```java
Identifier mission = Identifier.fromNamespaceAndPath("exampleaddon", "scan_relay");
Identifier target = MissionHookTargets.objectiveTarget("exampleaddon", mission, "scan");

EchoCoreServices.registerMissionHookCoverage("exampleaddon", mission, target);

EchoCoreServices.recordMissionObjective(
        serverPlayer,
        MissionObjectiveType.SCAN_BLOCK,
        target,
        1,
        MissionHookTargets.context("exampleaddon", mission, "action", "scanner"));
```

The target format is `<addon>:mission/<legacy_mission>/<objective_key>`. Context
maps should include `source`, `legacy_mission`, and one gameplay-specific field such
as `route`, `machine`, `region`, or `action`. `/echomission validate` reports each
known migrated source as `direct-hooks`, `adapter-state`, or `mixed` and warns when a
migrated mission still relies only on adapter-state fallback.

## JSON Rules

MissionCore loads static content from:

- `data/<namespace>/missioncore/chapters/*.json`
- `data/<namespace>/missioncore/missions/**/*.json`

Validation is intentionally strict for content safety:

- Mission ids, objective ids, and reward ids must be unique.
- Mission chapters must exist before missions register.
- Prerequisites must point at registered missions.
- Objective types must match `MissionObjectiveType.id()`.
- Reward `claimMode` must be `immediate` or `claimable`.
- Reward item ids must resolve to real items.

Invalid records are skipped and logged; the game remains usable.

Minimal JSON mission:

```json
{
  "chapter": "exampleaddon:field_ops",
  "title": "Scan Relay",
  "briefing": "Scan one relay block.",
  "objectives": [
    {
      "id": "exampleaddon:scan_relay/objective",
      "type": "scan_block",
      "label": "Scan a relay",
      "target": "exampleaddon:relay",
      "required": 1
    }
  ],
  "rewards": [
    {
      "id": "exampleaddon:scan_relay/reward",
      "item": "minecraft:emerald",
      "count": 1,
      "claimMode": "claimable"
    }
  ]
}
```

## Rewards and Repeatables

Immediate rewards are granted once when a mission completes. Claimable rewards become pending and can be claimed once through Terminal or `/echomission claim`; inventory insertion falls back to dropping the stack near the player.

Repeatable missions must be started again after completion. Starting a repeatable mission clears that mission's prior objective progress and claimed reward state for the next cycle.

## Terminal Migration

If an addon has an old `TerminalMissionProvider`, keep its actions, archives, and dashboards, but do not register the legacy mission provider while `echomissioncore` is loaded and the same missions are registered with MissionCore. This prevents duplicate mission display while preserving legacy UI affordances.

Migration checklist:

- Preserve every `TerminalMissionDefinition.id()` exactly as the MissionCore mission id.
- Register the existing chapter and mission order through `EchoCoreServices.registerMissionContent`.
- Use legacy `snapshot`, completion flags, and claimed flags as status/completion rules.
- Mirror MissionCore completion and claim callbacks back into legacy stores.
- Route custom action ids through `actionHandler` and delegate to the existing provider.
- Skip only the legacy mission provider when MissionCore coverage is complete; keep dashboards, archives, reports, and client screens registered.
- Run `/echomission validate` and confirm source counts include the addon once and no duplicate legacy provider warning appears.

Known 0.3.0 adapter and direct-hook targets are Reclamation, Industrial, Convoy,
Orbital, Nexus, Blackbox, and Stationfall. SignalOS is intentionally separate
because its app model is not a direct `TerminalMissionProvider` migration target.
