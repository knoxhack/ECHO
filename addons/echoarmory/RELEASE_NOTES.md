# ECHO: Armory 1.2.0 Release Notes

ECHO: Armory 1.2.0 turns the first playable modular gear loop into a route-kit readiness system. Loadouts, boss previews, Core route records, Terminal actions, Logistics dispatch, and MissionCore side ops now agree on whether a kit is ready, staged, missing parts, or faction locked.

## Player Path

Players can craft early Armory gear, install protection modules, and open the Armory Terminal to check route-kit readiness before deploying. Toxic Breach now focuses on toxic protection, Fracture Guardian on fracture/Veil mitigation, and Orbital Assault on thermal survival plus mobility support.

## Datapack Compatibility

Loadout JSON may now define `requiredProtections`:

```json
{
  "requiredProtections": {
    "toxic": 55,
    "fracture": 40
  }
}
```

Existing datapacks that only use `minProtection` still work; Armory treats that value as a fracture requirement when `requiredProtections` is absent.

## Release Safety

- Existing ItemStack components and saves remain compatible.
- Optional Terminal, Logistics Network, and MissionCore integrations stay guarded.
- Failed readiness, install, recharge, and Logistics actions do not consume gear, modules, ammo, or fuel.
- Faction-locked gear reports a lock instead of being deleted or bypassed through readiness checks.

## Known Limits

- Readiness reports use existing item/module state and do not add new gear art, entities, or projectiles.
- Logistics availability depends on an eligible loaded Logistics endpoint near the player.
- Full all-stack verification can still be blocked by unrelated dirty modules in the workspace.
