# ECHO: RenderCore

`echorendercore` is a lightweight shared rendering and animation module for ECHO addons. It provides data-driven visual state, named-part animation, per-part glow and overlay layers, particle anchors, validation/lint reports, and client debug tools without depending on GeckoLib or any Ashfall gameplay module.

## Module Setup

RenderCore depends only on `echocore`. Addons that require it can use `implementation project(":echorendercore")` and a required TOML dependency.

Optional consumers should use:

```groovy
compileOnly project(":echorendercore")
localRuntime project(":echorendercore")
```

Then add an optional TOML dependency and register client integrations only behind `ModList.get().isLoaded("echorendercore")` plus reflection. Convoy and Industrial Nexus both use this pattern.

## Public API

Common API lives under `com.knoxhack.echorendercore.api` and is server-safe:

- `VisualState`, `IVisualStateProvider`, `IAdvancedVisualEntity`, `IAdvancedVisualBlockEntity`
- `VisualContext`, `VisualVariant`, `VisualProgressProvider`

Animation API lives under `com.knoxhack.echorendercore.animation`:

- `AnimationClip`, `AnimationTrack`, `AnimationKeyframe`, `AnimationTimeline`
- `AnimationController`, `AnimationPlayer`, `AnimationBlendMode`, `Easing`
- `PartTransform`, `ModelPose`

Profile API lives under `com.knoxhack.echorendercore.profile` and includes visual layers, materials, anchors, particle options, block part selectors, builder/data-gen helpers, cache metrics, performance diagnostics, stable validation issue codes, and namespace-filterable report types.

## Entity Integration

1. Expose or derive a profile id such as `yourmod:your_entity`.
2. Map gameplay state to `VisualState`.
3. Build a `VisualContext` in the client renderer.
4. Submit the model through `VisualProfileRenderer.submitEntityModel`.
5. Apply named-part animation with `AnimationController` or `AnimationPlayer` plus `ModelPoseApplier`.
6. Spawn configured emitters with `RenderCoreParticleSpawner.spawnForEntity`.

The Wasteland Rover in `echoconvoyprotocol` is the entity example. Convoy keeps its fallback renderer and registers the RenderCore renderer only when this module is loaded.

## Block Entity Integration

For hard dependencies, implement `IAdvancedVisualBlockEntity`. For optional dependencies, keep the block entity clean and add a reflection-gated client renderer or adapter.

Industrial Nexus demonstrates the optional pattern: `IndustrialMachineBlockEntity` exposes ordinary machine status/progress accessors, `IndustrialRenderCoreVisuals` maps those to RenderCore state names without importing RenderCore, and the client-only renderer consumes RenderCore only when the mod is present.

V4 adds opt-in baked block model helpers for renderers that want lightweight layer masks over vanilla-style models. V5 extends those selectors with block-state gates and tint-index rules:

- `BakedBlockPartResolver.collect(blockState)` collects `BlockStateModelPart` values from `BlockStateModel.collectParts`.
- `BakedBlockPartResolver.resolve(blockState, profile)` maps `visual_profile.block_parts` aliases to collected baked parts and applies `block_state`/`tint_indices` rules.
- `AdvancedBlockEntityVisualRenderer.submitBlockModelLayers(...)` submits matching layer masks for block entity renderers.
- `RenderCoreBlockPartProvider` is available for custom renderers that already know their own stable block part aliases.

RenderCore never invents semantic block part names from baked models. Addons must define aliases with selector rules.

## Visual Profiles

Profiles load from every namespace at:

```text
assets/[modid]/rendercore/visual_profiles/[name].json
```

V1 fields still work. V2 adds `schema_version`, `transition_seconds`, `materials`, and `layers`. V3 makes layer part masks active. V4 adds optional `block_parts` selectors for baked block models. V5 adds `block_state`/`blockState` selector gates and tint-index matching:

```json
{
  "schema_version": 5,
  "base_texture": "yourmod:textures/entity/machine.png",
  "animation_profile": "yourmod:machine",
  "particle_profile": "yourmod:machine",
  "default_state": "ONLINE",
  "transition_seconds": 0.2,
  "materials": {
    "cyan_emissive": {
      "color": "#FF66E8FF",
      "alpha": 0.9,
      "emissive": true,
      "blend_mode": "additive"
    }
  },
  "layers": [
    {
      "id": "online_glow",
      "kind": "glow",
      "texture": "yourmod:textures/entity/machine_glow.png",
      "material": "cyan_emissive",
      "states": ["ONLINE", "ACTIVE", "WORKING"],
      "parts": ["core", "screen"]
    }
  ],
  "state_animations": {
    "ACTIVE": "work_loop"
  },
  "anchors": {
    "exhaust_left": { "part": "exhaust_left", "offset": [-0.4, 0.6, 0.8] }
  },
  "block_parts": {
    "core": {
      "indices": [0],
      "directions": ["north", "south", "up"],
      "ambient_occlusion": true
    },
    "active_core": {
      "directions": ["north", "south", "up"],
      "block_state": { "active": "true" }
    },
    "screen": {
      "directions": ["north"],
      "tint_indices": [0]
    }
  }
}
```

Missing optional profile references become validation warnings. Invalid JSON skips only the bad file. `parts`, `part_filter`, and `partFilter` all map to the same named-part mask. Entity models that want per-part layers implement the client-only `RenderCorePartProvider`; block renderers use V4 `block_parts`. Missing masked parts are skipped safely and can be linted with `RenderCoreProfileValidator.validateLayerParts` or `validateBlockPartSelectors`.

`block_state` selector values may be a string or a string list. A selector matches only when the current `BlockState` exposes the property and its serialized value is allowed. `tint_indices` uses collected baked quad material tint metadata when Minecraft exposes it; missing tint indices become validation warnings and the masked alias is skipped safely.

## Animation Profiles

Animation profiles load from:

```text
assets/[modid]/rendercore/animations/[name].json
```

Legacy `from`/`to` tracks still work. V2 tracks can use keyframes:

```json
{
  "animations": {
    "scanner_rotate": {
      "loop": true,
      "length": 4.0,
      "transition_seconds": 0.15,
      "tracks": [
        {
          "part": "scanner",
          "channel": "rotation_y",
          "keyframes": [
            { "time": 0.0, "value": 0.0 },
            { "time": 4.0, "value": 360.0, "easing": "linear" }
          ]
        }
      ]
    }
  }
}
```

Channels: position, rotation, scale, visibility, and alpha. Rotations are degrees in JSON and are converted to model radians during application.

## Particle Emitters

Particle profiles load from:

```text
assets/[modid]/rendercore/particles/[name].json
```

```json
{
  "emitters": {
    "damaged_smoke": {
      "anchor": "exhaust_left",
      "particle": "minecraft:smoke",
      "states": ["DAMAGED", "FAILED"],
      "requires_damaged": true,
      "rate": 0.05,
      "burst_count": 1,
      "offset": [0.0, 0.1, 0.0],
      "velocity": [0.0, 0.02, 0.0],
      "spread": [0.1, 0.02, 0.1]
    },
    "dust": {
      "anchor": "wheel_back_left",
      "particle": "minecraft:dust",
      "state": "ACTIVE",
      "requires_moving": true,
      "options": {
        "color": [0.47, 0.36, 0.24],
        "scale": 0.9,
        "lifetime": 28
      }
    }
  }
}
```

Simple particles and `minecraft:dust` color/scale render by default. V4 built-ins also cover `dust_transition`, `color`/`entity_effect`, `item`, `block`, and `trail` option payloads when Minecraft exposes stable constructors:

```json
{
  "options": {
    "type": "dust_transition",
    "from_color": "#FF66E8FF",
    "to_color": "#FFFF7A28",
    "scale": 0.9
  }
}
```

Additional fields parsed by V4 include `from_color`, `to_color`, `item`, `block_state`, `target`, and `duration`. Optional addons can register client-only typed resolvers through `RenderCoreParticleOptionResolvers.register`. Unsupported custom options and currently non-applied lifetime values appear as `unsupported_particle_option` validation warnings.

## Data Generation

Addons can generate profile JSON from Java using the server-safe builders:

```java
VisualProfileBuilder.create(id("industrial_machine"))
   .schemaVersion(5)
   .baseTexture(id("textures/block/machine_casing.png"))
   .blockPart("screen", new BlockPartSelectorProfile("screen", List.of(), Set.of(Direction.NORTH), 0, true, List.of()));
```

Use `RenderCoreProfileDataProvider` from a client-data run and call `visual(...)`, `animation(...)`, and `particle(...)` inside `registerProfiles()`. Builder output round-trips through the same runtime parsers used by resource reload.

## Debug Tools

Client commands:

```text
/rendercore reload
/rendercore validate all
/rendercore validate yourmod
/rendercore debug state ACTIVE 30
/rendercore debug hud true
/rendercore debug anchors true
/rendercore debug missingparts true
/rendercore debug blockparts
```

The debug HUD shows loaded profile counts, validation counts, target information, and debug toggles. Missing part warnings and anchor diagnostics are opt-in.
When optional consumers call the debug target registry, the HUD also shows looked-at profile id, state, variant, active layer count, anchor count, and warning count. Anchor debug draws world-space bounds and anchor boxes for recently rendered RenderCore targets.
`/rendercore debug blockparts` inspects the looked-at RenderCore-supported block entity, posts a short chat summary, and logs the full block part export: profile id, block state, collected part count, aliases, matched indices, selector rules, material flags, AO rules, directions, tint indices, and warning count. V5 does not write debug files.

## Validation And Lint

Validation issues include stable machine-readable codes:

```text
missing_base_texture
missing_profile_reference
missing_animation_clip
missing_anchor
unsupported_particle_option
masked_part_missing
block_part_selector_empty
block_part_index_out_of_range
block_state_property_missing
block_state_property_value_missing
block_part_tint_index_missing
profile_perf_high_layer_count
profile_perf_high_emitter_rate
profile_perf_high_animation_track_count
```

`ProfileValidationReport.forNamespace("yourmod")` returns a namespace-filtered report for commands, CI-style checks, or data generators. `RenderCoreProfileValidator.validateLayerParts(profile, knownParts)` validates entity/custom model masks. `RenderCoreProfileValidator.validateBlockPartSelectors(profile, collectedPartCount, blockState, tintIndices)` validates baked block selectors with V5 state/tint context. `RenderCoreProfileValidator.analyzePerformance(...)` returns a `ProfilePerformanceReport` with layer count, masked layer count, clip/track count, emitter count, max burst estimate, and performance warning count. `RenderCoreProfileValidator.diagnostics(...)` is the preferred CI/data-gen entry point; it bundles validation, performance, and `ProfileCacheMetrics` with profile counts, discovered/loaded/failed JSON counts, warning/error counts, namespaces, and schema-version range. `/rendercore validate` is backed by the same diagnostics model.

Editor-friendly schema resources are shipped at:

```text
assets/echorendercore/rendercore/schemas/visual_profile.schema.json
assets/echorendercore/rendercore/schemas/animation_profile.schema.json
assets/echorendercore/rendercore/schemas/particle_profile.schema.json
```

## Testing

Recommended checks after Java 25 is on `PATH`/`JAVA_HOME`:

```text
.\gradlew.bat --no-configuration-cache :echorendercore:build
.\gradlew.bat --no-configuration-cache :echorendercore:runGameTestServer
.\gradlew.bat --no-configuration-cache :echoconvoyprotocol:build
.\gradlew.bat --no-configuration-cache :echoindustrialnexus:build
.\gradlew.bat --no-configuration-cache :echoindustrialnexus:runGameTestServer
.\gradlew.bat --no-configuration-cache validateEchoResources
```

## Known Limitations

- Per-part material masks are active for entity models that implement the client-only named-part provider. Baked block selectors are opt-in and selector-based because vanilla baked models do not expose stable semantic bones.
- V5 block model helpers resubmit selected baked model parts for layer effects, but they do not replace a full custom block model renderer for complex moving geometry.
- Particle lifetime is parsed and linted but only native particle options supported by Minecraft or a custom resolver can use it directly.

## V6 Roadmap

- Optional profile preview tooling for generated packs and CI artifacts.
- More renderer-side material controls when Minecraft exposes stable hooks for shader/lightmap state.
- Runtime profile hot-swap helpers for creators iterating on large addon packs.
- Broader schema examples for complex vehicle and multi-block machine profiles.
