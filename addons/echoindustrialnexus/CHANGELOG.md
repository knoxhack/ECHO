# Changelog

## 0.1.0 - Priority Fix RC

- Fixed NeoForge fluid capability transaction semantics for Industrial machines and fluid pipes with snapshot rollback behavior.
- Added data-driven tank fluid recipes for Water Purifier and Fluid Refiner and decoded fluid fields in `IndustrialProcessingRecipe` JSON.
- Upgraded Smart Ducts with a one-slot filter, whitelist/blacklist wrench toggle, Nexus-safe override behavior, persistence, and loop/backtrack guarding.
- Cached Factory Controller scan state and stopped passive Terminal mission snapshots from rescanning the factory every mission read.
- Improved machine GUI readouts with named fluids, warning causes, side-config text, and early-chain route hints.
- Added ECHO Terminal Recipe Index coverage for `echoindustrialnexus:industrial_processing` JSON, including item/tag ingredients, catalysts, byproducts, fluids, Thermal Flux, heat, and duration notes.
- Added POI terrain/biome/loaded-footprint safety checks and Warden phase telegraphs, Furnace Drone minions, and marked cooling objectives.
- Added focused regression coverage for fluid transaction rollback, mixed-fluid rejection, pipe filtering, and Smart Duct filtering.

## 0.1.0 - Production Completion

- Promoted Industrial Nexus into the default included addon set.
- Added registered Industrial fluids and NeoForge fluid capabilities for machine tanks.
- Added active Rusted, Reinforced, Pressurized, Shielded, and Static Pipe block entities with fluid transfer, filtering, tier capacities, transfer rates, loss, and hazardous leak behavior.
- Kept bucket/cell workflows as fallback automation for fluid recipes.
- Hardened ECHO Terminal compatibility so common and client bridges load reflectively only when `echoterminal` is present.
- Tuned Thermal Flux, capacitor, duct, scrubber, and POI spacing defaults for production gameplay.
- Added optional soft compat hooks for Ashfall air/radiation cleanup, Nexus thermal pressure, and Stationfall oxygen support while preserving safe fallback behavior.
- Updated Terminal mission metadata from Functional V1 to Production Complete.
- Documented release jar path, dependencies, validation commands, and manual smoke checklist.

## 0.1.0 - Gameplay Polish Baseline

- Added custom Industrial machine menus/screens.
- Added progression storage, mission snapshots, reward idempotency, procedural POIs, Furnace Warden progression, and focused GameTests.
- Added generated vanilla-style block/item textures and JSON resources for the Functional V1 content set.
