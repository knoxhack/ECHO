# ECHO Logistics Network Smoke Test

## Local Build Precondition

This workspace targets Java 25. In the current Windows environment, set the local JDK before running Gradle:

```powershell
$env:JAVA_HOME='C:\Users\knox\.jdks\temurin-25'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

## Survival Discovery Loop

1. Craft a Supply Tag from paper and redstone. This unlocks the `ECHO: Logistics Network` advancement root.
2. Craft a Supply Crate, then optionally craft Smart Storage Labels for adjacent vanilla inventories.
3. Use the Supply Tag to cycle a category, then apply it to the crate or label. Stock tagged supplies such as potions, bread, paper, arrows, iron ingots, firework rockets, echo shards, and emeralds.
4. Craft a Logistics Chip, then build a Drone Delivery Dock and Loadout Locker. The network-online advancement should unlock when either infrastructure block enters the player's inventory.
5. Craft a Route Manifest and Loadout Card. Use the manifest on the dock, crate, and locker to link the same network. Use the loadout card on the locker to select a shipped preset.
6. Craft a Remote Request Tablet or Logistics Terminal. Bind the tablet to the locker or open the terminal page; the dispatch-ready advancement should unlock.
7. Request the selected loadout from the locker, requester, restock station, tablet, or `SYSTEM > Logistics` terminal action.
8. Confirm the courier reserves items before launch, carries one sealed payload, delivers to the target inventory, and never duplicates or silently voids supplies.
9. Save and reload the world after setting categories/routes/loadouts and while a courier is in flight. Confirm block state, inventories, and courier payload state persist.
10. Break a stocked Supply Crate, Drone Delivery Dock, or Loadout Locker. Confirm its stored contents drop as recoverable item entities instead of vanishing or duplicating.
11. Use the Faction Trade Depot with the default salvage-water exchange and claim Remote Reward Relay rewards twice; the second claim should be idempotent.
12. Repeat with ECHO Terminal and Industrial Nexus absent where possible. Logistics should still load with ECHO Core, own blocks, labels, crates, lockers, docks, and datapack definitions.

## Verification Commands

Run these from the workspace root, `C:\Github\Echo`.

Run after implementation changes:

```powershell
.\gradlew.bat :echologisticsnetwork:compileJava --no-configuration-cache
.\gradlew.bat :echologisticsnetwork:build --no-configuration-cache
```

Run for release hardening:

```powershell
.\gradlew.bat validateEchoResources -PechoAddonSet=all --no-configuration-cache
.\gradlew.bat validateEchoGameplayData -PechoAddonSet=all --no-configuration-cache
.\gradlew.bat :echologisticsnetwork:runGameTestServer --no-configuration-cache
.\gradlew.bat buildEchoWorkspace -PechoAddonSet=all --no-configuration-cache
```

Verified results on 2026-05-10 for `0.1.0` release readiness:

- `:echologisticsnetwork:compileJava`: build successful.
- `:echologisticsnetwork:build`: build successful.
- `validateEchoResources`: resource validation passed.
- `validateEchoGameplayData`: gameplay data validation passed with 252 registered item/block ids and 118 structure palette block ids checked.
- `:echologisticsnetwork:runGameTestServer`: 25 game tests complete; all 25 required tests passed, including persistence, break-drop safety, reservation, recovery, relay idempotency, Terminal actions, and optional integration coverage.
- `buildEchoWorkspace -PechoAddonSet=all`: build successful for the full included ECHO workspace.

## Implementation Notes

- Route Manifests assign a block to the interacting player's short network id; sneaking with a manifest resets a block to `global`.
- Courier drones use deterministic dock-to-target waypoint movement. They do not run full pathfinding.
- Industrial Nexus reach is guarded and heuristic-based for now: Logistics recognizes nearby Industrial Nexus duct blocks by registry namespace/path rather than depending on an internal duct API.
- The first release keeps block menus intentionally simple because the ECHO Terminal `SYSTEM > Logistics` page is the primary overview workflow.

## Production Readiness Notes

For `0.1.0`, the core loop is production-ready when ECHO Core is present:

1. Label a supply crate or adjacent inventory.
2. Link blocks to a route with a Route Manifest.
3. Select a loadout with a Loadout Card.
4. Request the loadout from a locker, requester, restock station, tablet, or Terminal action.
5. Confirm the courier reserves, carries, delivers, cancels, or recovers the sealed payload without duplicating or silently voiding items.

Known limitations that should stay visible for follow-up polish:

- Block-local screens are functional but intentionally compact; the ECHO Terminal page remains the best overview surface.
- Courier drones fly deterministic waypoint routes and do not avoid every possible obstacle.
- Industrial Nexus reach uses guarded duct discovery by registry id because no stable public duct graph API is available here.
- Faction depot reputation is local Logistics state; deeper faction economy integration depends on future public ECHO faction APIs.
- Visual assets use simple block/item models rather than final custom art.
- Loadout balance is conservative and datapack-driven; pack authors should tune shipped defaults against the final late-game economy.
