# ECHO Logistics Network Smoke Test

1. Start with ECHO Core and Logistics enabled, then place a Logistics Terminal, Drone Delivery Dock, Loadout Locker, Supply Crate, and Smart Storage Label within the same base area.
2. Use Supply Tags on the crate or label to assign categories, then stock tagged items such as potions, bread, paper, arrows, iron ingots, firework rockets, echo shards, and emeralds.
3. Open the Logistics Terminal or `SYSTEM > Logistics` when ECHO Terminal is present. Confirm stock rows, low-stock rows, loadout readiness, dock state, depot offers, and relay rewards render without errors.
4. Select/request each shipped loadout preset. Confirm items are reserved before the courier launches, the courier delivers to the locker/requester/crate, and failed routes recover payloads into the source dock or drop a recoverable crate payload.
5. Use the Faction Trade Depot with the default salvage-water exchange and claim Remote Reward Relay rewards twice; the second claim should be idempotent.
6. Repeat with ECHO Terminal and Industrial Nexus absent where possible. Logistics should still load with ECHO Core, own blocks, labels, crates, lockers, docks, and datapack definitions.

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
