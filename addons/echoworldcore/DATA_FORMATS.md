# WorldCore Data Formats

WorldCore loads datapack definitions from:

- `data/<namespace>/echoworldcore/world_hazards/**/*.json`
- `data/<namespace>/echoworldcore/world_regions/**/*.json`

The `id` field is optional. If omitted, the file path becomes the id. Use an
explicit `id` when describing a region owned by another ECHO chapter.

## Hazard Definition

```json
{
  "id": "echoworldcore:hazard/radiation",
  "displayName": "Radiation",
  "summary": "Irradiated terrain and unstable fallout pockets.",
  "defaultSeverity": 70,
  "ticking": false
}
```

Validation:

- `defaultSeverity` must be `0..100`.
- `displayName` and `summary` should be non-empty.
- Duplicate ids replace earlier datapack entries and log a warning.

## Region Definition

```json
{
  "id": "echoashfallprotocol:crash_zone_wasteland",
  "type": "crash_zone",
  "displayName": "Crash Zone Wasteland",
  "summary": "Impact-scattered wreckage fields and Ashfall crash debris.",
  "biomeIds": ["echoashfallprotocol:crash_zone_wasteland"],
  "biomeTags": ["echoashfallprotocol:common_wasteland_biomes"],
  "structureIds": ["echoashfallprotocol:drop_pod"],
  "hazardIds": ["echoworldcore:hazard/salvage_debris"],
  "discoveryId": "echoashfallprotocol:crash_zone_wasteland",
  "radius": 96,
  "renderProfileId": "echoworldcore:region/crash_zone_wasteland",
  "audioProfileId": "echoworldcore:ambience/crash_zone_wasteland",
  "sortOrder": 10
}
```

Supported `type` values:

- `crash_zone`
- `ruined_city`
- `toxic_swamp`
- `radiation_zone`
- `cryogenic_ruins`
- `nexus_scar`
- `orbital_debris_field`
- `convoy_route`
- `secure_outpost`
- `anomaly_zone`

Validation:

- `radius` must be at least `16`.
- `hazardIds` must reference loaded hazard definitions.
- `discoveryId` should be unique per region unless multiple regions intentionally
  share the same player-facing discovery.
- Render and audio profile ids are optional references; WorldCore never loads
  client RenderCore or AudioCore classes from common code.

## RenderCore Profiles

WorldCore ships client resource profiles under:

- `assets/echoworldcore/rendercore/visual_profiles/region/*.json`
- `assets/echoworldcore/rendercore/visual_profiles/hazard/*.json`

The region profile ids match built-in `renderProfileId` values such as
`echoworldcore:region/crash_zone_wasteland`. Hazard profiles use ids such as
`echoworldcore:hazard/radiation` for consumers that want a visual overlay for
the active hazard snapshot.

## AudioCore Profiles

WorldCore v1 has no hard dependency on AudioCore. It ships forward-compatible
ambience descriptors under:

- `assets/echoworldcore/audiocore/ambience_profiles/ambience/*.json`

Their ids match built-in `audioProfileId` values such as
`echoworldcore:ambience/crash_zone_wasteland`. The referenced sound events are
declared in `assets/echoworldcore/sounds.json` with empty sound lists so future
audio packs can supply actual loops without changing WorldCore code.
