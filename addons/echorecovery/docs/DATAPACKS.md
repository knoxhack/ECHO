# ECHO Recovery Datapacks

## Grave Type Override

Datapacks can define custom grave types via JSON in:
`data/<namespace>/recovery_grave_type/`

Example:
```json
{
  "id": "my_pack:ancient_tomb",
  "display_name": "Ancient Tomb",
  "texture": "my_pack:block/ancient_tomb",
  "sounds": {
    "open": "my_pack:block.tomb_open",
    "recover": "my_pack:block.tomb_recover"
  },
  "properties": {
    "explosion_proof": true,
    "fireproof": true
  }
}
```

## Recovery Rules

Custom recovery rules can be defined in:
`data/<namespace>/recovery_rule/`

Example boss arena rule:
```json
{
  "id": "my_pack:boss_arena_redirect",
  "dimension": "minecraft:the_end",
  "region": {
    "min": [0, 0, 0],
    "max": [100, 100, 100]
  },
  "action": "redirect_to_nearest_safe",
  "fallback_pos": [50, 64, 50]
}
```
