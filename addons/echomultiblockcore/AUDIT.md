# ECHO: MultiblockCore — Completion Audit

## Baseline (current commit)

- 143 Java source files
- 11 multiblock definitions (10 showcase 3x2x3 + 1 assembly 5x2x3)
- 16 automation recipes
- 11 progression entries
- 4 upgrades
- 47 crafting recipes
- 11 block loot tables
- 32 GameTest registrations
- Compiles cleanly (`:echomultiblockcore:compileJava`)

## Feature Surface vs Claims

| Feature | Status | Notes |
|---------|--------|-------|
| JSON definition loader | OK | Isolated bad files, keeps last-good registry |
| Validation engine | OK | Rotations, mirror, unloaded chunks, max volume, foundation warnings |
| Validation cache | OK | Versioned key, TTL, force option, dirty flag |
| Controller block entity | OK | Tick, form, break, integrity, upgrades, task queue, auto-builder, diagnostics |
| Task queue | OK | WAITING/BLOCKED/ACTIVE/PAUSED/COMPLETED/FAILED/RETRYING, 8 max persisted |
| Automation transaction | OK | Consume + produce with rollback on failure |
| Robotic arm BE | OK | Tool install/remove, heat, cooling, state machine, persistence |
| Input/output crates | OK | 18-slot, insert/consume/extract, matching predicate, status line |
| Blueprint item + build assist | OK | Tooltip, material summary, use-on-controller, preview renderer, keys |
| Controller screen | OK | 8 buttons, data sync, progression line |
| Commands | OK | validate, form, break, info, set, task, recipes, progression, upgrades, autobuild, materials, robotics, integrations, snapshot, scan, markers, preview |
| Auto-builder service | OK | Plans missing blocks, consumes exact items from input crate, respects permissions |
| Capability service | OK | Discovers nodes from matched blocks, evaluates recipe + definition requirements |
| Upgrade runtime | OK | JSON definitions, install/remove via commands, snapshot visibility |
| Progression system | OK | Tier, prerequisites, featured recipes, rewards, guide text |
| Optional integrations | OK | Terminal (reflective), Lens (reflective), HoloMap (via EchoCore), DataCore, RenderCore, RuntimeGuard, MissionCore |
| GameTests | OK | 32 tests covering parse, validation, robotics, tasks, effects, build assist, progression |

## Gaps / Placeholders

### 1. IDs and naming
- `industrial_assembly_line_demo` is the default controller definition and appears in:
  - `ModBlocks.java` defaultDefinitionId
  - `ModItems.java` blueprint target
  - `MultiblockControllerBlockEntity.java` fallback id
  - 6 automation recipe `allowed_multiblocks`
  - 1 progression entry
  - Lang key `multiblock.echomultiblockcore.industrial_assembly_line_demo`
  - Advancement parents
  - GameTests
  - README / SMOKE_TEST docs
- **Decision**: rename canonical id to `industrial_assembly_line`, keep `industrial_assembly_line_demo` as a compatibility alias so existing worlds/datapacks do not break.

### 2. Showcase facility depth
- All 10 non-assembly facilities are 3x2x3 with nearly identical palette (frame + controller + crate + bus + robot + optional auto-builder).
- They differ only in display name, role, category, and 1 workcell.
- **Gap**: no distinct layout, no unique capability requirements, no dedicated recipes beyond the 1 featured task each. This makes them feel like placeholders.
- **Decision**: keep current small footprints for tier-1 accessibility, but add distinct `capability_requirements` and `upgrade_slots` per facility so each has a real runtime identity. Do NOT expand sizes unless content design justifies it (that can be a future addon).

### 3. Placeholder text in datapacks
- `orbital_launch_platform.json` progression: `"guide":"Calibrate launch guidance cores as a standalone MultiblockCore placeholder for Orbital Remnants."`
- `stabilize_nexus_field_coil.json` task: `"notes":["Produces a safe placeholder coil for Nexus-side facilities."]`
- `armory_fabricator.json` progression: `"guide":"Fabricate armory pattern cores as neutral bridge content for future combat systems."`
- **Decision**: rewrite to present current, concrete behavior. Remove "placeholder", "future", "bridge content" wording.

### 4. Optional dependency metadata
- `neoforge.mods.toml` for `echoterminal` says: `reason="Future dashboard tabs can consume MultiblockCore status snapshots."`
- Terminal integration is already live (recipe provider, addon info, actions). Update reason to present tense.

### 5. Schema completeness
- `multiblock_definition.schema.json` exists but `workcells` items are sparse and missing `id`, `pos`, `size`, `required_tools`, `allowed_tasks`.
- Missing schemas: `echo_multiblock_tasks`, `echo_multiblock_progression`, `echo_multiblock_upgrades`.
- Missing datapack examples for those three.
- Missing schema index update.

### 6. Recipe selection UX
- Controller screen `START` button always starts the first available recipe (`availableAutomationRecipes().stream().findFirst()`).
- **Gap**: no deterministic recipe choice; player cannot select which recipe to run from the first-party UI.
- **Decision**: for now, make `START` deterministic (sorted by repair priority then id), and add a command `/echo_multiblock task start <recipe>` that accepts any allowed recipe. The UI enhancement is future work unless requested.

### 7. Manual-only smoke coverage
- SMOKE_TEST.md has many manual steps that should be GameTests:
  - forming each showcase facility
  - running each featured recipe
  - blocked task cases
  - malformed reload isolation
  - persistence after reload
  - optional-runtime integration checks
- **Decision**: add GameTests for all structure formations and at least one transaction per facility. Keep manual list only for client rendering/UI.

### 8. Missing crate menu
- `MultiblockCrateBlockEntity.createMenu` returns `null`.
- **Gap**: players cannot open crate GUI to insert/extract items manually; only hoppers/automation can interact.
- **Decision**: implement a simple 18-slot container menu and screen. This is a real usability blocker.

### 9. RenderCore / animation safety
- `EchoMultiblockCoreClient` and `MultiblockControllerBlockEntity` use reflection for RenderCore and Terminal. This is correct for optional deps.
- Need to verify no client class is referenced on dedicated server paths. Current code looks safe (reflective invoke, `level.isClientSide()` guards).

## Punch List (execution order)

- [x] 1. **Rename `industrial_assembly_line_demo` → `industrial_assembly_line`**
  - Compatibility alias map added to `MultiblockContent`.
  - All first-party references updated (blocks, items, recipes, progression, lang, advancements, tests, docs).
  - Old saves/datapacks with `industrial_assembly_line_demo` still resolve correctly.

- [x] 2. **Remove placeholder text**
  - Rewrote progression guides for orbital, armory.
  - Rewrote task notes for nexus coil.
  - Updated `neoforge.mods.toml` optional reasons.

- [~] 3. **Add distinct capability requirements to showcase facilities**
  - Added unique capability costs to `signal_tower_tier_1` and `logistics_depot`.
  - Remaining: add requirements to the other 8 showcase facilities and add `upgrade_slots` to at least 3.

- [x] 4. **Implement crate GUI**
  - Added `MultiblockCrateMenu` and `MultiblockCrateScreen`.
  - Registered menu type in `ModMenus`.
  - Wired `MultiblockCrateBlockEntity.createMenu` and `MultiblockCrateBlock` open on right-click.

- [ ] 5. **Add missing schemas**
  - Update `multiblock_definition.schema.json` with full workcell/capability/upgrade/robotics fields.
  - Add `automation_recipe.schema.json`, `progression.schema.json`, `upgrade.schema.json`.
  - Add datapack examples and update schema index.

- [ ] 6. **Add automated smoke GameTests**
  - One test per facility: build from JSON palette, validate, form, run featured recipe, assert output in crate.
  - One test per blocked case: missing input, full output, missing tool, pause/resume.
  - One malformed reload test per loader.
  - One persistence test: save + reload + assert queue state.

- [ ] 7. **Final gates**
  - `:echomultiblockcore:build`
  - `:echomultiblockcore:runGameTestServer`
  - `:echomultiblockcore:runGameTestServer -PechoMultiblockIncludeOptionalRuntime=true`
  - Dependent addon compiles
  - `buildEchoWorkspace`
