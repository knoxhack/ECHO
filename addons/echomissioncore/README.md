# ECHO: MissionCore

MissionCore is the shared backend for ECHO missions, objectives, rewards, and Terminal mission feeds.

## Addon Registration

Addons should depend on `echocore` and register content through Core:

```java
EchoCoreServices.registerMissionContent("myaddon", registry -> {
    Identifier chapterId = Identifier.fromNamespaceAndPath("myaddon", "field_ops");
    registry.registerChapter("myaddon", new MissionChapterDefinition(
            chapterId, "Field Ops", "Addon objectives.", 50, 0x55FFDD));

    registry.registerMission("myaddon", MissionDefinition.builder(
                    Identifier.fromNamespaceAndPath("myaddon", "first_signal"), chapterId)
            .phase("field_ops", "Field Ops", 0, 1)
            .text("First Signal", "Scan the first signal.", "Signal archived.")
            .objective(ObjectiveDefinition.simple(
                    Identifier.fromNamespaceAndPath("myaddon", "first_signal/scan"),
                    MissionObjectiveType.SCAN_BLOCK,
                    "Scan signal block",
                    "",
                    ItemStack.EMPTY,
                    1))
            .reward(RewardDefinition.item(
                    Identifier.fromNamespaceAndPath("myaddon", "first_signal/reward"),
                    MissionRewardClaimMode.CLAIMABLE,
                    new ItemStack(Items.EMERALD)))
            .build());
});
```

Gameplay code should report progress through `EchoCoreServices.recordMissionObjective(...)`. MissionCore safely no-ops when it is not loaded.

## JSON Content

Datapacks can register chapters under:

- `data/<namespace>/missioncore/chapters/*.json`
- `data/<namespace>/missioncore/missions/**/*.json`

Mission rewards support `immediate` and `claimable` modes. Objective `type` accepts the shared MissionCore objective ids such as `obtain_item`, `place_block`, `kill_entity`, `establish_route`, and `unlock_research`.

## Validation and Debug

MissionCore is server-authoritative. Operators can inspect and test content with:

- `/echomission list`
- `/echomission inspect <mission>`
- `/echomission start <mission>`
- `/echomission progress <mission> <objective> <amount>`
- `/echomission record <objective_type> <target> <amount>`
- `/echomission complete <mission>`
- `/echomission claim <mission>`
- `/echomission validate`
- `/echomission reload`

JSON missions with duplicate ids, missing chapters, broken prerequisites, unknown objective types, unknown reward modes, or invalid reward items are skipped with warnings instead of crashing world load.

## Migration Notes

When ECHO Terminal is installed, MissionCore registers one shared mission feed. Legacy addon Terminal mission providers should skip their direct mission provider registration when `echomissioncore` is loaded, while keeping non-mission dashboards, archives, and actions registered. Existing addon save data remains authoritative until its adapter mirrors completion and reward claims into MissionCore.
