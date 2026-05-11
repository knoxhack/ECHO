# ECHO: Armory 0.1.0 Release Notes

ECHO: Armory 0.1.0 is the first playable release of the modular combat addon. It adds survival-obtainable workstations, modular weapons and armor, ItemStack component state, energy recharge, faction-gated gear, Terminal loadout actions, Logistics dispatch hooks, and Core diagnostics.

## Player Path

Players can craft Armory Alloy Plate, build the Armory Bench and Module Upgrade Table, craft early gear, install compatible modules, spend and recharge energy, and use Terminal readiness guidance without any optional sibling mod. Terminal and Logistics integrations activate only when those sibling addons are present.

## Release Safety

- Module install failures do not consume modules.
- Ranged weapons do not spend ammo or energy without a valid target.
- Successful ranged shots spend ammo crystals before stored energy and do not spend both.
- Recharge consumes `veil_crystal` or `resonance_shard`.
- Faction-locked gear is not deleted; survival behavior stays locked with visible feedback.
- Optional Terminal/Logistics/faction paths are guarded.

## Known Limits

- v1 visuals are simple generated JSON models using vanilla textures.
- Weapon projectiles are server-side targeted energy strikes rather than custom projectile entities.
- Drone Dock behavior is conservative repair/shield support; no uncontrolled custom drone entities are spawned.
- Some mid/late gear is intentionally depot-only and depends on faction/Logistics content for normal survival acquisition.
- GameTests and release manifest tasks should be run with `--no-configuration-cache` until the workspace run/release tasks are made configuration-cache compatible.
