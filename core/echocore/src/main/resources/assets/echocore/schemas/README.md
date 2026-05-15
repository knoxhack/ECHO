# ECHO Datapack Schemas

This directory contains JSON Schema (draft 2020-12) definitions for every data-driven content type in the ECHO mod stack.

## How to use these schemas

1. Copy the schema file into your datapack workspace.
2. Reference it in your JSON editor or IDE (VS Code, IntelliJ, Neovim, etc.).
3. Validate your datapack files before distributing them.

## Schema index

| Schema | Content type | Loader directory |
|---|---|---|
| `theme.schema.json` | ECHO Theme (colors, textures, sound, blocks) | `data/<namespace>/themes/` or `data/<namespace>/echothemecore/themes/` |
| `render_preset.schema.json` | Theme render preset | `data/<namespace>/echothemecore/render_presets/` |
| `tutorial_card.schema.json` | Tutorial card | `data/<namespace>/tutorial_cards/` |
| `tutorial_hint.schema.json` | Tutorial hint | `data/<namespace>/tutorial_hints/` |
| `tutorial_flow.schema.json` | Tutorial flow (step sequence) | `data/<namespace>/tutorial_flows/` |
| `tutorial_tooltip.schema.json` | Tutorial item tooltip | `data/<namespace>/tutorial_tooltips/` |
| `soundcore_music_profile.schema.json` | SoundCore music profile | `data/<namespace>/echosoundcore/audio_profiles/music/` |
| `soundcore_ambience_profile.schema.json` | SoundCore ambience profile | `data/<namespace>/echosoundcore/audio_profiles/ambience/` |
| `world_hazard.schema.json` | World hazard definition | `data/<namespace>/echoworldcore/world_hazards/` |
| `world_region.schema.json` | World region definition | `data/<namespace>/echoworldcore/world_regions/` |
| `mission_chapter.schema.json` | Mission chapter | `data/<namespace>/missioncore/chapters/` |
| `mission_definition.schema.json` | Mission definition | `data/<namespace>/missioncore/missions/` |
| `index_category.schema.json` | Index category | `data/<namespace>/echo_index/categories/` |
| `index_entry.schema.json` | Index entry | `data/<namespace>/echo_index/entries/` |
| `weather_profile.schema.json` | Weather profile | `data/<namespace>/weather_profiles/` |
| `terminal_mission.schema.json` | Terminal vanilla journey mission | `data/<namespace>/echoterminal/vanilla_journey/missions.json` |
| `terminal_reward_tier.schema.json` | Terminal vanilla journey reward tier | `data/<namespace>/echoterminal/vanilla_journey/reward_tiers.json` |
| `armory_gear.schema.json` | Armory gear definition | `data/<namespace>/echoarmory/gear/` |
| `armory_module.schema.json` | Armory module definition | `data/<namespace>/echoarmory/modules/` |
| `armory_synergy.schema.json` | Armory synergy definition | `data/<namespace>/echoarmory/synergies/` |
| `armory_loadout.schema.json` | Armory loadout definition | `data/<namespace>/echoarmory/loadouts/` |
| `logistics_category.schema.json` | Logistics supply category | `data/<namespace>/echologisticsnetwork/categories/` |
| `logistics_loadout.schema.json` | Logistics loadout preset | `data/<namespace>/echologisticsnetwork/loadouts/` |
| `logistics_offer.schema.json` | Logistics faction depot offer | `data/<namespace>/echologisticsnetwork/faction_offers/` |
| `multiblock_definition.schema.json` | Multiblock structure blueprint | `data/<namespace>/echo_multiblocks/` |
| `convoy_route.schema.json` | Convoy route definition | `data/<namespace>/convoy_routes/` or `data/<namespace>/echoconvoyprotocol/convoy_routes/` |

## RenderCore schemas (existing)

RenderCore already publishes its own schemas under `assets/echorendercore/rendercore/schemas/`:

- `visual_profile.schema.json`
- `animation_profile.schema.json`
- `particle_profile.schema.json`
- `profile_preview.schema.json`
- `creator_pack.schema.json`

## Compatibility notes

- All schemas use `additionalProperties: true` where the Java loader is lenient (future fields may be added without breaking validation).
- Identifiers follow Minecraft's `namespace:path` format. Empty strings are treated as `null` / optional in many loaders.
- Colors may be specified as hex strings (`"#FF00E5FF"`) or integer values (`0xFF00E5FF`) depending on the loader.
